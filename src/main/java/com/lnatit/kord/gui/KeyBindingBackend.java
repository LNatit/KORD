package com.lnatit.kord.gui;

import com.lnatit.kord.result.risk.Severity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Pure data model for KeyBindingScreen. It can be created independently from the Screen lifecycle.
 */
public final class KeyBindingBackend
{






























    private static final KeyBindingBackend GLOBAL = mock();

    private final List<BindingEntry> bindings;
    private final List<PairEntry> pairs;
    private final Map<String, Severity> maxSeverityByBinding;

    public static KeyBindingBackend global() {
        return GLOBAL;
    }

    public KeyBindingBackend(List<BindingEntry> bindings, List<PairEntry> pairs) {
        this.bindings = List.copyOf(bindings);
        this.pairs = List.copyOf(pairs);
        this.maxSeverityByBinding = buildMaxSeverityByBinding(this.bindings, this.pairs);
    }

    public List<BindingEntry> bindings() {
        return bindings;
    }

    public List<PairEntry> pairs() {
        return pairs;
    }

    public Severity bindingSeverity(String bindingId) {
        return maxSeverityByBinding.getOrDefault(bindingId, Severity.SAFE);
    }

    public List<PairEntry> visiblePairs(String bindingId, Severity threshold) {
        List<PairEntry> visible = new ArrayList<>();
        for (PairEntry entry : pairs) {
            boolean related = entry.a().equals(bindingId) || entry.b().equals(bindingId);
            if (related && entry.severity().ordinal() >= threshold.ordinal()) {
                visible.add(entry);
            }
        }
        return visible;
    }

    public static KeyBindingBackend mock() {
        List<BindingEntry> bindings = List.of(
                new BindingEntry("key.jump", "Jump"),
                new BindingEntry("key.forward", "Move Forward"),
                new BindingEntry("key.inventory", "Open Inventory"),
                new BindingEntry("key.example.dash", "Dash"),
                new BindingEntry("key.example.map", "Open Map"),
                new BindingEntry("key.example.skill", "Skill")
        );

        List<PairEntry> pairs = List.of(
                new PairEntry("pair-1", "key.jump", "key.example.dash", Severity.WARNING, "Intercept input in IN_GAME"),
                new PairEntry("pair-2", "key.inventory", "key.example.map", Severity.SEVERE, "Context clash + focus collision"),
                new PairEntry("pair-3", "key.forward", "key.example.skill", Severity.INFO, "Timing mismatch only"),
                new PairEntry("pair-4", "key.jump", "key.example.skill", Severity.WARNING, "Resource read/write overlap")
        );

        return new KeyBindingBackend(bindings, pairs);
    }

    private static Map<String, Severity> buildMaxSeverityByBinding(List<BindingEntry> bindings, List<PairEntry> pairs) {
        Map<String, Severity> map = new HashMap<>();
        for (BindingEntry entry : bindings) {
            map.put(entry.id(), Severity.SAFE);
        }

        for (PairEntry pair : pairs) {
            map.put(pair.a(), maxSeverity(map.get(pair.a()), pair.severity()));
            map.put(pair.b(), maxSeverity(map.get(pair.b()), pair.severity()));
        }
        return map;
    }

    private static Severity maxSeverity(Severity left, Severity right) {
        if (left == null) return right;
        if (right == null) return left;
        return left.ordinal() >= right.ordinal() ? left : right;
    }

    public record BindingEntry(String id, String label) {
    }

    public record PairEntry(String id, String a, String b, Severity severity, String summary) {
    }
}

