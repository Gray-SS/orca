package com.orca.compiler.core.typesystem;

public abstract sealed class BaseType implements LangType permits
        AnyType,
        UnknownType,
        ArrayType,
        PrimitiveType,
        CallableType,
        VoidType,
        NominalType,
        CollectionType,
        OpaqueType,
        ErrorType {

    /**
     * Returns a human-readable name for this type, used in error messages and
     * debugging. This should ideally be concise and easy to understand, but it
     * doesn't have to be unique (for example, two different class types could
     * have the same display name if they have the same name but different type
     * parameters).
     *
     * @return A human-readable name for this type.
     */
    @Override
    public abstract String displayName();

    @Override
    public String toString() {
        return displayName();
    }
}
