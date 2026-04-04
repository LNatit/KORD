package com.lnatit.suck.core;

public enum HijackMode
{
    PASSIVE,
    CONSUME,
    REDIRECT_MOUSE,
    REDIRECT_KEY;

    public boolean isRedirect() {
        return this == REDIRECT_MOUSE || this == REDIRECT_KEY;
    }
}
