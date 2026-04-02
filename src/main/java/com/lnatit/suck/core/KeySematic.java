package com.lnatit.suck.core;

public interface KeySematic {
    KeyContext context();

    StateSet states();

    Modality modality();

    HijackMode hijackMode();

    record InGame(
            StateSet states,
            String intent,
            Modality modality,
            HijackMode hijackMode,
            ActionRoot.InGame actionRoot
    ) implements KeySematic {
        @Override
        public KeyContext context() {
            return KeyContext.IN_GAME;
        }
    }

    record InGui(
            StateSet states,
            String intent,
            Modality modality,
            HijackMode hijackMode,
            ActionRoot.InGui actionRoot
    ) implements KeySematic {
        @Override
        public KeyContext context() {
            return KeyContext.IN_GUI;
        }
    }
}
