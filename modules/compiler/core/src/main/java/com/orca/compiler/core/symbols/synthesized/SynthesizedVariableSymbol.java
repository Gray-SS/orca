package com.orca.compiler.core.symbols.synthesized;

import com.orca.compiler.core.boundtree.constants.BoundConstant;
import com.orca.compiler.core.symbols.NamespaceOrTypeSymbol;
import com.orca.compiler.core.symbols.SymbolKind;
import com.orca.compiler.core.symbols.VariableSymbol;
import com.orca.compiler.core.typesystem.LangType;

public final class SynthesizedVariableSymbol implements VariableSymbol, SynthesizedSymbol {

    private final String name;
    private final LangType type;

    public SynthesizedVariableSymbol(String name, LangType type) {
        this.name = name;
        this.type = type;
    }

    @Override
    public String name() {
        return name;
    }

    @Override
    public LangType type() {
        return type;
    }

    @Override
    public SymbolKind kind() {
        return SymbolKind.VARIABLE;
    }

    @Override
    public NamespaceOrTypeSymbol owner() {
        return null;
    }

    @Override
    public int getModifiers() {
        return VariableSymbol.super.getModifiers()
                | SynthesizedSymbol.super.getModifiers();
    }

    @Override
    public boolean isCompileTimeConstant() {
        return false;
    }

    @Override
    public BoundConstant getConstant() {
        throw new UnsupportedOperationException("Synthesized temporary variables are not compile-time constants");
    }
}
