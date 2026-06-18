package com.orca.compiler.core.typesystem;

public final class VoidType extends BaseType {
    public static final VoidType INSTANCE = new VoidType();

    private VoidType() {}

    @Override
    public TypeKind kind() {
        return TypeKind.VOID;
    }

    @Override
    public String displayName() {
        return "void";
    }

    @Override
    public boolean equals(Object other) {
        return other instanceof VoidType;
    }
}
