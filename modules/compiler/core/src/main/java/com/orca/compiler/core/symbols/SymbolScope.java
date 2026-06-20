package com.orca.compiler.core.symbols;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class SymbolScope {

    private final Map<String, List<Symbol>> symbols = new HashMap<>();

    public boolean has(String name) {
        var found = symbols.get(name);
        return found != null && !found.isEmpty();
    }

    public void add(Symbol symbol) {
        var found = symbols.computeIfAbsent(symbol.name(), k -> new java.util.ArrayList<>());
        found.add(symbol);
    }

    public Symbol get(String name) {
        var found = symbols.get(name);
        if (found == null || found.isEmpty()) {
            return null;
        }

        return found.get(0);
    }

    public List<Symbol> getAll(String name) {
        return symbols.getOrDefault(name, List.of());
    }

    public List<Symbol> getAll() {
        return symbols.values().stream().flatMap(List::stream).toList();
    }
}
