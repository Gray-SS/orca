package com.orca.compiler.core.symbols;

import com.orca.compiler.core.typesystem.LangType;

public abstract class ConstructorSymbol extends CallableSymbol {
    /**
     * The default name for constructor symbols.
     */
    public static final String DEFAULT_NAME = "<init>";

    public ConstructorSymbol(TypeSymbol owner) {
        super(DEFAULT_NAME, owner, SymbolModifiers.INSTANCE_ONLY);
    }

    @Override
    protected final LangType getReturnTypeInternal() {
        return LangType.Void;
    }

    /**
     * Gets the symbol of the type that this constructor constructs.
     * @return The symbol of the type that this constructor constructs.
     */
    @Override
    public TypeSymbol owner() {
        return (TypeSymbol)super.owner();
    }

    @Override
    public final SymbolKind kind() {
        return SymbolKind.CONSTRUCTOR;
    }
}
