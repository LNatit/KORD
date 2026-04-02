package com.lnatit.suck.core;

public interface ActionRoot
{
    enum InGame implements ActionRoot {
        GUI_OPEN,
        MOVEMENT,
        CORE_ACT,
        ABILITY,
        OVERLAY,
        INFO,
        SYSTEM;
    }

    enum InGui implements ActionRoot {

    }
}
