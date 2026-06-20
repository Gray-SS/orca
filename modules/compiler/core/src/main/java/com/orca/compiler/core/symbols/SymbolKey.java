package com.orca.compiler.core.symbols;

import java.util.List;

public sealed interface SymbolKey {

    record Simple(String name) implements SymbolKey {

    }

    record Overloaded(String name, List<String> parameterTypes) implements SymbolKey {

        public static Overloaded of(String name, List<String> types) {
            return new Overloaded(name, types);
        }
    }

    static SymbolKey simple(String name) {
        return new Simple(name);
    }

    default String name() {
        return switch (this) {
            case Simple s ->
                s.name();
            case Overloaded o ->
                o.name();
        };
    }
}
