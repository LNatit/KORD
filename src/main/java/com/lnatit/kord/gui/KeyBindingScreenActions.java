package com.lnatit.kord.gui;

/**
 * External operation callbacks for KeyBindingScreen actions.
 */
public interface KeyBindingScreenActions {
    void onExclude(KeyBindingBackend.PairEntry pair);

    void onPromoteRule(KeyBindingBackend.PairEntry pair);

    static KeyBindingScreenActions noop() {
        return new KeyBindingScreenActions() {
            @Override
            public void onExclude(KeyBindingBackend.PairEntry pair) {
            }

            @Override
            public void onPromoteRule(KeyBindingBackend.PairEntry pair) {
            }
        };
    }
}

