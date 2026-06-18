package com.orca.compiler.core.typesystem;

public enum TypeKind {
    // === Compound Types ===
    ARRAY("array"),
    COLLECTION("collection"),
    FUNCTION("function type"),
    // === Special ===
    ERROR("error type"),
    OPAQUE("opaque"),
    Primitive("primitive type"),
    NOMINAL("nominal type"),
    ANY("any"),
    UNKNOWN("unknown type"),
    VOID("void"),;

    String displayName;

    TypeKind(String displayName) {
        this.displayName = displayName;
    }

    public String displayName() {
        return displayName;
    }
}
