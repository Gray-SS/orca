package com.orca.compiler.core.symbols;

import java.util.List;

import com.orca.compiler.core.boundtree.constants.BoundConstant;
import com.orca.compiler.core.typesystem.LangType;

public non-sealed interface ParameterSymbol extends ValueSymbol {

    /**
     * Gets the index of this parameter within its declaring callable.
     *
     * @return
     */
    int index();

    /**
     * Indicates whether this parameter is a varargs parameter.
     *
     * @return true if this parameter is a varargs parameter, false otherwise.
     */
    boolean isVarargs();

    ParameterSymbol withIndex(int newIndex);

    @Override
    default SymbolKind kind() {
        return SymbolKind.PARAMETER;
    }

    @Override
    default boolean isCompileTimeConstant() {
        // Parameters are never compile-time constants
        return false;
    }

    @Override
    default BoundConstant getConstant() {
        // Parameters are never compile-time constants, so this method should always return null
        return null;
    }

    @Override
    default NamespaceOrTypeSymbol owner() {
        // Parameters do not have an owner namespace or type
        return null;
    }

    public static List<LangType> typesOf(List<? extends ParameterSymbol> parameters) {
        var result = new java.util.ArrayList<LangType>();

        for (var param : parameters) {
            result.add(param.type());
        }

        return result;
    }

    @Override
    default int getModifiers() {
        return SymbolModifiers.NOT_APPLICABLE;
    }
}
