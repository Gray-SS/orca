package com.orca.compiler.core.symbols;

import java.util.function.Supplier;

public final class LazySymbol<T extends Symbol> {
    private final SymbolKey key;
    private final Supplier<T> resolver;
    private ResolutionState state = ResolutionState.UNRESOLVED;
    private T resolved = null;

    public LazySymbol(SymbolKey key, Supplier<T> resolver) {
        this.key = key;
        this.resolver = resolver;
    }

    /** Force la résolution. Lève une exception si cycle détecté. */
    public T resolve() {
        if (state == ResolutionState.RESOLVING)
            throw new IllegalStateException("Cycle detected while resolving symbol: " + key.name());

        if (state == ResolutionState.RESOLVED)
            return resolved;

        state = ResolutionState.RESOLVING;
        resolved = resolver.get();
        state = ResolutionState.RESOLVED;
        return resolved;
    }

    public T getIfResolved() {
        return state == ResolutionState.RESOLVED ? resolved : null;
    }

    public ResolutionState getState() {
        return state;
    }

    public SymbolKey getKey() {
        return key;
    }

    public boolean isResolved() {
        return state == ResolutionState.RESOLVED;
    }
}