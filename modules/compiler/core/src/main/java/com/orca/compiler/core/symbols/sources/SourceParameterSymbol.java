package com.orca.compiler.core.symbols.sources;

import com.orca.compiler.core.symbols.ParameterSymbol;
import com.orca.compiler.core.syntax.SyntaxNode;
import com.orca.compiler.core.typesystem.LangType;

public final class SourceParameterSymbol implements SourceSymbol, ParameterSymbol {

    private final String name;
    private final SyntaxNode declaringSyntax;

    private final int index;
    private final LangType type;

    public SourceParameterSymbol(String name, SyntaxNode declaringSyntax, int index, LangType type) {
        this.name = name;
        this.declaringSyntax = declaringSyntax;
        this.index = index;
        this.type = type;
    }

    @Override
    public String name() {
        return name;
    }

    @Override
    public boolean isVarargs() {
        // Not supported for source parameters.
        return false;
    }

    @Override
    public int getModifiers() {
        return SourceSymbol.super.getModifiers()
                | ParameterSymbol.super.getModifiers();
    }

    @Override
    public ParameterSymbol withIndex(int newIndex) {
        return new SourceParameterSymbol(name, declaringSyntax, newIndex, type);
    }

    @Override
    public SyntaxNode declaringSyntax() {
        return declaringSyntax;
    }

    public int index() {
        return index;
    }

    @Override
    public LangType type() {
        return type;
    }

    @Override
    public String toString() {
        return String.format("SourceParameterSymbol(name=%s, type=%s, index=%d)", name, type, index);
    }
}
