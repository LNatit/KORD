package com.lnatit.chord.resource.semantic;

import com.lnatit.chord.eval.KeySemantic;
import com.lnatit.chord.eval.context.IKeyContext;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.ModList;
import org.apache.maven.artifact.versioning.ArtifactVersion;

import java.util.List;
import java.util.Optional;

public record KeyDefinitions(int version,
                             String modid,
                             Optional<String> mod_version_range,
                             List<KeyDefinition> keys)
{
    public boolean checkValid() {
        Optional<? extends ModContainer> container = ModList.get().getModContainerById(modid());
        if (container.isEmpty()) {
            return false;
        }

        ArtifactVersion mod_version = container.get().getModInfo().getVersion();
        if (mod_version_range().isPresent() && !Versioned.containsVersion(mod_version, mod_version_range().get())) {
            return false;
        }
        keys().removeIf(k -> !k.checkVersion(mod_version));
        return !keys().isEmpty();
    }

    public record KeyDefinition(String name,
                                Optional<String> mod_version_range,
                                List<SemanticEntry> semantics) implements Versioned
    {
        @Override
        public boolean checkVersion(ArtifactVersion mod_version) {
            if (mod_version_range().isPresent() && !Versioned.containsVersion(mod_version, mod_version_range().get())) {
                return false;
            }
            semantics().removeIf(s -> !s.checkVersion(mod_version));
            return !semantics().isEmpty();
        }
    }

    public record SemanticEntry(List<IKeyContext> contexts,
                                Optional<String> mod_version_range,
                                KeySemantic semantic) implements Versioned
    {
        @Override
        public boolean checkVersion(ArtifactVersion mod_version) {
            return mod_version_range().isEmpty() || Versioned.containsVersion(mod_version, mod_version_range().get());
        }
    }
}
