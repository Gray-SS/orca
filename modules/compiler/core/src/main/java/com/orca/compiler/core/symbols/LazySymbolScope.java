package com.orca.compiler.core.symbols;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Supplier;
import java.util.stream.Stream;

public class LazySymbolScope {

    private final Map<String, List<SymbolKey>> byName = new HashMap<>();
    private final Map<SymbolKey, LazySymbol<? extends Symbol>> registry = new LinkedHashMap<>();

    public <T extends Symbol> void register(SymbolKey key, Supplier<T> resolver) {
        if (registry.containsKey(key)) {
            return;
        }
        registry.put(key, new LazySymbol<>(key, resolver));
        byName.computeIfAbsent(key.name(), (d) -> new ArrayList<>()).add(key);
    }

    @SuppressWarnings("unchecked")
    public <T extends Symbol> List<T> resolve(String name) {
        var keys = byName.getOrDefault(name, List.of());
        return keys.stream()
                .map(key -> (T) registry.get(key).resolve())
                .toList();
    }

    @SuppressWarnings("unchecked")
    public <T extends Symbol> Optional<T> resolveExact(SymbolKey key) {
        var lazy = registry.get(key);
        if (lazy == null) {
            return Optional.empty();
        }
        return Optional.of((T) lazy.resolve());
    }

    public Stream<Symbol> resolvedSymbols() {
        return registry.values().stream()
                .filter(LazySymbol::isResolved)
                .map(LazySymbol::getIfResolved);
    }

    public Set<String> knownNames() {
        return Collections.unmodifiableSet(byName.keySet());
    }

    public boolean hasName(String name) {
        return byName.containsKey(name);
    }

    public int countByName(String name) {
        return byName.getOrDefault(name, List.of()).size();
    }

    public void resolveAll() {
        registry.values().forEach(LazySymbol::resolve);
    }
}
