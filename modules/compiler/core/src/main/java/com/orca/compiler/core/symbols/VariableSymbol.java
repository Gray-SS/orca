package com.orca.compiler.core.symbols;

public non-sealed interface VariableSymbol extends ValueSymbol {

    @Override
    default SymbolKind kind() {
        return SymbolKind.VARIABLE;
    }
}
