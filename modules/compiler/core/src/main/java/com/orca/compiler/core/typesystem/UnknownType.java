package com.orca.compiler.core.typesystem;

public final class UnknownType extends BaseType {
    public static final UnknownType INSTANCE = new UnknownType();

    private UnknownType() {}

    @Override
    public String displayName() {
        return "<unknown>";
    }

    @Override
    public TypeKind kind() {
        return TypeKind.UNKNOWN;
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof UnknownType;
    }
}
