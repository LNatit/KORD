package com.lnatit.kord.data.context;

import com.lnatit.kord.data.Requirement;
import com.lnatit.kord.semantic.ConflictType;
import com.lnatit.kord.semantic.KeyContext;
import net.neoforged.neoforge.client.settings.IKeyConflictContext;

public record ContextDefinition(Requirement requirement, String id, String lookup, ConflictType type) {
    public boolean isInvalid() {
        return !requirement.isValid() || id.isBlank() || lookup.isBlank();
    }

    public KeyContext toKeyContext() {
        return new KeyContext(id, resolveLookup(lookup), type);
    }

    /**
     * Resolves a static field reference to an {@link IKeyConflictContext} instance.
     * <p>
     * Supported fully-qualified lookup formats:
     * <ul>
     *   <li>{@code fully.qualified.ClassName#FIELD_NAME}</li>
     *   <li>{@code fully.qualified.ClassName.FIELD_NAME}</li>
     * </ul>
     * The referenced field must be static and its runtime value must implement {@link IKeyConflictContext}.
     */
    private static IKeyConflictContext resolveLookup(String lookup) {
        String className;
        String fieldName;

        int hash = lookup.lastIndexOf('#');
        if (hash > 0 && hash < lookup.length() - 1) {
            className = lookup.substring(0, hash);
            fieldName = lookup.substring(hash + 1);
        } else {
            int dot = lookup.lastIndexOf('.');
            if (dot <= 0 || dot >= lookup.length() - 1) {
                throw new IllegalArgumentException("Invalid lookup format: " + lookup);
            }
            className = lookup.substring(0, dot);
            fieldName = lookup.substring(dot + 1);
        }

        try {
            Class<?> owner = Class.forName(className);
            java.lang.reflect.Field field = owner.getDeclaredField(fieldName);
            field.setAccessible(true);
            Object value = field.get(null);
            if (!(value instanceof IKeyConflictContext context)) {
                throw new IllegalArgumentException("Lookup does not resolve to IKeyConflictContext: " + lookup);
            }
            return context;
        } catch (ReflectiveOperationException e) {
            throw new IllegalArgumentException("Failed to resolve context lookup: " + lookup, e);
        }
    }
}
