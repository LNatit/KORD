package com.lnatit.chord.eval;

import com.lnatit.chord.eval.context.IKeyContext;
import net.neoforged.neoforge.client.extensions.IKeyMappingExtension;
import net.neoforged.neoforge.client.settings.IKeyConflictContext;

import java.util.Map;
import java.util.Set;

public interface SemanticalKey extends IKeyMappingExtension
{
    void chord$addSemantic(IKeyContext context, KeySemantic semantic);

    KeySemantic chord$getSemantic(IKeyContext context);

    Set<Map.Entry<IKeyConflictContext, KeySemantic>> chord$getSemanticEntries();

    void chord$clearSemantics();

    default IKeyConflictContext chord$getSemanticalConflictCtx(IKeyContext context) {
        return context.transform(getKeyConflictContext());
    }
}
