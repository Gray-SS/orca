package com.orca.compiler.core.typesystem;

public final class OpaqueType extends BaseType {

    public static final OpaqueType INSTANCE = new OpaqueType();

    private OpaqueType() {
    }

    @Override
    public TypeKind kind() {
        return TypeKind.OPAQUE;
    }

    @Override
    public String displayName() {
        return "opaque";
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof OpaqueType;
    }

    @Override
    public int hashCode() {
        return System.identityHashCode(this);
    }
}
