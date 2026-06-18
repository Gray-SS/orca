package com.orca.compiler.core.typesystem;

import java.util.Objects;

import com.orca.compiler.core.Debug;

public final class PrimitiveType extends BaseType {
    private static final PrimitiveType[] VALUES = {
        Byte,
        Short,
        Int,
        Long,
        Float,
        Double,
        Char,
        String,
        Bool,
    };

    /**
     * Returns an array containing all the primitive types.
     * @return An array containing all the primitive types.
     * @implNote The returned array is a copy of the internal array, so modifying it will not affect the internal state of the class.
     * @implNote If another primitive type is introduced, it should be added to the VALUES array as well.
     */
    public static PrimitiveType[] values() {
        return VALUES.clone();
    }

    private final String name;
    private final PrimitiveTypeKind kind;
    private final String descriptor;

    PrimitiveType(PrimitiveTypeKind kind, String name, String descriptor) {
        Debug.requireNotNull(kind, "Primitive type kind cannot be null.");
        Debug.assertTrue(kind != PrimitiveTypeKind.None, "Primitive type kind cannot be None.");
        Debug.requireNotNullOrEmpty(name, "Primitive type name cannot be null or empty.");
        Debug.requireNotNullOrEmpty(descriptor, "Primitive type descriptor cannot be null or empty");

        this.kind = kind;
        this.name = name;
        this.descriptor = descriptor;
    }

    @Override
    public TypeKind kind() {
        return TypeKind.Primitive;
    }

    @Override
    public PrimitiveTypeKind getPrimitiveKind() {
        return kind;
    }

    public String descriptor() {
        return descriptor;
    }

    @Override
    public String displayName() {
        return name;
    }

    @Override
    public boolean equals(Object other) {
        return other instanceof PrimitiveType otherPrimitive && this.kind == otherPrimitive.kind;
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(kind);
    }
}
