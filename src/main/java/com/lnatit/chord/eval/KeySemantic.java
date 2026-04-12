package com.lnatit.chord.eval;

import com.lnatit.chord.eval.intent.Intent;
import com.lnatit.chord.eval.mutex.StateSet;

import java.util.List;

public interface KeySemantic
{
    KeySemantic DEFAULT = new KeySemantic()
    {
        @Override
        public StateSet states() {
            return StateSet.EMPTY;
        }

        @Override
        public boolean intercept() {
            return false;
        }

        public RedirectMode redirectMode() {
            return RedirectMode.NONE;
        }

        @Override
        public Modality modality() {
            return Modality.PRESS;
        }
    };

    StateSet states();

    boolean intercept();

    RedirectMode redirectMode();

    Modality modality();

    record Simple(StateSet states,
                  boolean intercept,
                  RedirectMode redirectMode,
                  Modality modality) implements KeySemantic
    {}

    record Advanced(StateSet states,
                    boolean intercept,
                    RedirectMode redirectMode,
                    Resource resource,
                    boolean readOnly,
                    List<Intent> intents,
                    Modality modality) implements KeySemantic
    {
        public static Advanced of(StateSet states, boolean intercept, RedirectMode redirectMode, Resource resource, boolean readOnly, List<Intent> intents, Modality modality) {
            return new Advanced(states, intercept, redirectMode, resource, readOnly, intents, modality);
        }




    }
}
