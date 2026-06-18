package com.orca.compiler.core.symbols;

import com.orca.compiler.core.boundtree.constants.BoundConstant;

public non-sealed interface FieldSymbol extends ValueSymbol {

    /**
     * Gets the index of this field within its declaring type.
     *
     * @return The index of this field within its declaring type.
     */
    int index();

    /**
     * Gets the type that declares this field.
     *
     * @return The type that declares this field.
     */
    @Override
    TypeSymbol owner();

    /**
     * Indicates whether this field is static.
     *
     * @return true if this field is static, false otherwise.
     */
    @Override
    public abstract boolean isStatic();

    @Override
    default SymbolKind kind() {
        return SymbolKind.FIELD;
    }

    @Override
    default int getModifiers() {
        return ValueSymbol.super.getModifiers() | (isStatic() ? SymbolModifiers.STATIC : SymbolModifiers.INSTANCE_ONLY);
    }

    @Override
    default boolean isCompileTimeConstant() {
        return false;
    }

    @Override
    default BoundConstant getConstant() {
        return null;
    }
}
