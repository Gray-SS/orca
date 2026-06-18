package com.orca.compiler.core.typesystem;

import java.util.Objects;

public final class ArrayType extends BaseType {
    @Deprecated
    public final LangType elementType;

    public ArrayType(LangType elementType) {
        this.elementType = elementType;
    }

    public LangType getElementType() {
        return elementType;
    }

    @Override
    public TypeKind kind() {
        return TypeKind.ARRAY;
    }

    @Override
    public String displayName() {
        return elementType.displayName() + "[]";
    }

    @Override
    public boolean equals(Object other) {
        return other instanceof ArrayType o && this.elementType.equals(o.elementType);
    }

    @Override
    public int hashCode() {
        return Objects.hash(elementType, 1);
    }
}
