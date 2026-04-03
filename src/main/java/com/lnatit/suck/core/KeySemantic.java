package com.lnatit.suck.core;

import java.util.List;

public interface KeySemantic
{
    KeySemantic DEFAULT = new KeySemantic() {
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

//    record Wrapper(IKeyConflictContext conflictContext, Simple sematic) implements KeySemantic
//    {
//        @Override
//        public KeyContext context() {
//            return sematic.context();
//        }
//
//        @Override
//        public StateSet states() {
//            return sematic.states();
//        }
//
//        @Override
//        public Modality modality() {
//            return sematic.modality();
//        }
//
//        @Override
//        public HijackMode hijackMode() {
//            return sematic.hijackMode();
//        }
//    }

    record InGame(StateSet states,
                  List<String> intents,
                  Modality modality,
                  HijackMode hijackMode,
                  ActionRoot.InGame actionRoot) implements KeySemantic
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
                 ActionRoot.InGui actionRoot) implements KeySemantic
    {
        @Override
        public KeyContext context() {
            return KeyContext.IN_GUI;
        }
    }
}
