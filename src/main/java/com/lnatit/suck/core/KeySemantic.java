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
        public boolean intercept() {
            return false;
        }

        public RedirectMode redirectMode() {return RedirectMode.NONE;}
    };

    KeyContext context();

    StateSet states();

    Modality modality();

    boolean intercept();

    RedirectMode redirectMode();

    record Simple(KeyContext context,
                  StateSet states,
                  Modality modality,
                  boolean intercept,
                  RedirectMode redirectMode) implements KeySemantic
    {}

    sealed interface Advanced extends KeySemantic permits InGame, InGui
    {
        List<String> intents();

        ActionCategory actionCategory();
    }

    record InGame(StateSet states,
                  List<String> intents,
                  Modality modality,
                  boolean intercept,
                  RedirectMode redirectMode,
                  ActionCategory.InGame actionCategory) implements Advanced
    {
        @Override
        public KeyContext context() {
            return KeyContext.IN_GAME;
        }
    }

    record InGui(StateSet states,
                 List<String> intents,
                 Modality modality,
                 boolean intercept,
                 RedirectMode redirectMode,
                 ActionCategory.InGui actionCategory) implements Advanced
    {
        @Override
        public KeyContext context() {
            return KeyContext.IN_GUI;
        }
    }
}
