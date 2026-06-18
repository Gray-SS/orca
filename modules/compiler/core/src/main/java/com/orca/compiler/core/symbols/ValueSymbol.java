package com.orca.compiler.core.symbols;

import com.orca.compiler.core.boundtree.constants.BoundConstant;

/**
 * Represents a symbol that has a value, such as a variable, parameter, or
 * field. This interface is sealed and can only be implemented by
 * VariableSymbol, ParameterSymbol, and Field
 */
public sealed interface ValueSymbol extends TypedSymbol permits VariableSymbol, ParameterSymbol, FieldSymbol {

    @Override
    default String displayName() {
        var sb = new StringBuilder();
        if (isCompileTimeConstant()) {
            sb.append("constant ");
        } else if (isImmutable()) {
            sb.append("immutable ");
        }

        return sb.append(TypedSymbol.super.displayName()).toString();
    }

    @Override
    default int getModifiers() {
        int additionalModifiers = 0;
        if (getConstant() != null) {
            additionalModifiers |= SymbolModifiers.CONSTANT | SymbolModifiers.IMMUTABLE;
        }

        return TypedSymbol.super.getModifiers() | additionalModifiers;
    }

    /**
     * Determines whether this value is a compile-time constant.
     *
     * @return true if this value is a compile-time constant, false otherwise.
     */
    default boolean isCompileTimeConstant() {
        return hasModifier(SymbolModifiers.CONSTANT);
    }

    /**
     * Determines whether this value is immutable, meaning that it cannot be
     * assigned to after it has been initialized. This includes both variables
     * declared with the 'immutable' modifier and compile-time constants.
     *
     * @return true if this value is immutable, false otherwise.
     */
    default boolean isImmutable() {
        return hasModifier(SymbolModifiers.IMMUTABLE);
    }

    /**
     * Determines whether this value is mutable, meaning that it can be assigned
     * to
     *
     * @return true if this value is mutable, false otherwise.
     */
    default boolean isMutable() {
        return !isImmutable();
    }

    /**
     * Gets the constant value of this variable if it is a compile-time
     * constant.
     *
     * @return The constant value of this variable if it is a compile-time
     * constant, or null if it is not a compile-time constant.
     */
    BoundConstant getConstant();
}
