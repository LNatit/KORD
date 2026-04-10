package com.lnatit.chord.core;

import java.util.List;

public interface KeySemantic {
    KeySemantic DEFAULT = new KeySemantic() {
        @Override
        public StateSet states() {
            return StateSet.EMPTY;
        }

        @Override
        public Modality modality() {
            return Modality.PRESS;
        }

        @Override
        public boolean intercept() {
            return false;
        }

        public RedirectMode redirectMode() {
            return RedirectMode.NONE;
        }
    };

    StateSet states();

    Modality modality();

    boolean intercept();

    RedirectMode redirectMode();

    record Simple(StateSet states, Modality modality, boolean intercept,
                  RedirectMode redirectMode) implements KeySemantic {
    }

    record Advanced(StateSet states, List<String> intents, Modality modality, boolean intercept,
                    RedirectMode redirectMode, Resource resource, boolean readOnly) implements KeySemantic {
    }
}
