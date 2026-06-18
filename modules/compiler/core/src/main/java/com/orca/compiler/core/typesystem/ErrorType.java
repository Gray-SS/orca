package com.orca.compiler.core.typesystem;

public final class ErrorType extends BaseType {

    public static final ErrorType INSTANCE = new ErrorType();

    @Override
    public TypeKind kind() {
        return TypeKind.ERROR;
    }

    @Override
    public java.lang.String displayName() {
        return "<error>";
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof ErrorType;
    }

    @Override
    public int hashCode() {
        return System.identityHashCode(this);
    }
}
