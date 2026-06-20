package com.orca.compiler.core.symbols;

import java.util.function.Supplier;

public final class Lazy<T> {

    private final Supplier<T> resolver;
    private ResolutionState state = ResolutionState.UNRESOLVED;
    private T resolved = null;

    public Lazy(Supplier<T> resolver) {
        this.resolver = resolver;
    }

    public T resolve() {
        if (state == ResolutionState.RESOLVING) {
            throw new IllegalStateException("Cycle detected while resolving lazy value");
        }

        if (state == ResolutionState.RESOLVED) {
            return resolved;
        }

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

    public boolean isResolved() {
        return state == ResolutionState.RESOLVED;
    }
}
