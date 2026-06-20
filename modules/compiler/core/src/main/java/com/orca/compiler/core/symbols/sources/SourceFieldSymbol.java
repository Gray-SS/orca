package com.orca.compiler.core.symbols.sources;

import com.orca.compiler.core.symbols.FieldSymbol;
import com.orca.compiler.core.symbols.Lazy;
import com.orca.compiler.core.symbols.TypeSymbol;
import com.orca.compiler.core.syntax.SyntaxNode;
import com.orca.compiler.core.typesystem.LangType;

public final class SourceFieldSymbol implements SourceSymbol, FieldSymbol {

    private final int index;
    private final String name;
    private final SyntaxNode declaringSyntax;
    private final Lazy<LangType> lazyType;

    // The type that declares a field should always come from the source.
    private final SourceNominalTypeSymbol owner;

    public SourceFieldSymbol(String name, SyntaxNode declaringSyntax, SourceNominalTypeSymbol owner, int index, Lazy<LangType> type) {
        this.name = name;
        this.declaringSyntax = declaringSyntax;
        this.index = index;
        this.lazyType = type;
        this.owner = owner;
    }

    public int index() {
        return index;
    }

    @Override
    public TypeSymbol owner() {
        return owner;
    }

    @Override
    public String name() {
        return name;
    }

    @Override
    public LangType type() {
        return lazyType.resolve();
    }

    @Override
    public SyntaxNode declaringSyntax() {
        return declaringSyntax;
    }

    @Override
    public boolean isStatic() {
        return false;
    }

    @Override
    public int getModifiers() {
        return SourceSymbol.super.getModifiers()
                | FieldSymbol.super.getModifiers();
    }
}
