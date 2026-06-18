package com.orca.compiler.core.typesystem;

public final class AnyType extends BaseType {
    public static final AnyType INSTANCE = new AnyType();

    private AnyType() {
    }

    @Override
    public TypeKind kind() {
        return TypeKind.ANY;
    }

    @Override
    public String displayName() {
        return "any";
    }

    @Override
    public boolean equals(Object other) {
        return other instanceof AnyType;
    }
}
