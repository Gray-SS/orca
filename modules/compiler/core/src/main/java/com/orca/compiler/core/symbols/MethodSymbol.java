package com.orca.compiler.core.symbols;

public abstract class MethodSymbol extends CallableSymbol {
    public MethodSymbol(NamespaceOrTypeSymbol owner, String name, int modifiers) {
        super(name, owner, modifiers);
    }

    @Override
    public final SymbolKind kind() {
        return SymbolKind.METHOD;
    }

    /**
     * Determines if this method is the entry point of the program (i.e., the main function).
     * @return true if this method is the entry point, false otherwise.
     */
    public final boolean isEntryPoint() {
        return (getModifiers() & SymbolModifiers.ENTRY_POINT) != 0;
    }
}
