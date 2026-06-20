package com.orca.compiler.core.symbols.intrinsics;

public enum IntrinsicKind {
    NONE,
    METHOD_NOT("not"),
    METHOD_STR_0BYTE("str"),
    METHOD_STR_0SHORT("str"),
    METHOD_STR_0INT("str"),
    METHOD_STR_0LONG("str"),
    METHOD_STR_0FLOAT("str"),
    METHOD_STR_0DOUBLE("str"),
    METHOD_STR_0BOOL("str"),
    METHOD_STR_0CHAR("str"),
    METHOD_STR_0STRING("str"),
    METHOD_FLOOR("floor"),
    METHOD_CEIL("ceil"),
    METHOD_LENGTH_0STRING("length"),
    METHOD_LENGTH_0ARRAY("length"),;

    private final String name;

    IntrinsicKind() {
        this.name = null;
    }

    IntrinsicKind(String name) {
        this.name = name;
    }

    public String getDisplayName() {
        if (name == null) {
            throw new IllegalStateException("This intrinsic kind does not have a name.");
        }

        return name;
    }
}
