package com.orca.compiler.core.typesystem;

public enum ConversionKind {
    NONE,
    IDENTITY,
    IMPLICIT,
    EXPLICIT
    ;

    boolean none() {
        return this == NONE;
    }

    boolean identity() {
        return this == IDENTITY;
    }

    boolean implicit() {
        return this == IMPLICIT;
    }

    boolean explicit() {
        return this == EXPLICIT;
    }
}
