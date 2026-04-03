package com.lnatit.suck.core;

import java.util.List;

public interface KeySemantic
{
    KeySemantic DEFAULT = new KeySemantic()
    {
        @Override
        public KeyContext context() {
            return KeyContext.AS_IS;
        }

        @Override
        public StateSet states() {
            return StateSet.EMPTY;
        }

        @Override
        public Modality modality() {
            return Modality.PRESS;
        }

        @Override
        public HijackMode hijackMode() {
            return HijackMode.NONE;
        }
    };

    KeyContext context();

    StateSet states();

    Modality modality();

    HijackMode hijackMode();

    record Simple(KeyContext context,
                  StateSet states,
                  Modality modality,
                  HijackMode hijackMode) implements KeySemantic
    {}

    interface Advanced extends KeySemantic
    {
        List<String> intents();

        ActionRoot actionRoot();
    }

    record InGame(StateSet states,
                  List<String> intents,
                  Modality modality,
                  HijackMode hijackMode,
                  ActionRoot.InGame actionRoot) implements Advanced
    {
        @Override
        public KeyContext context() {
            return KeyContext.IN_GAME;
        }
    }

    record InGui(StateSet states,
                 List<String> intents,
                 Modality modality,
                 HijackMode hijackMode,
                 ActionRoot.InGui actionRoot) implements Advanced
    {
        @Override
        public KeyContext context() {
            return KeyContext.IN_GUI;
        }
    }
}
