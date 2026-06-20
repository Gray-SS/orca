package com.orca.compiler.core.symbols.sources;

import java.util.List;

import com.orca.compiler.core.symbols.ExtensionSymbol;
import com.orca.compiler.core.symbols.Symbol;
import com.orca.compiler.core.symbols.SymbolScope;
import com.orca.compiler.core.symbols.TypeSymbol;
import com.orca.compiler.core.symbols.synthesized.GlobalNamespaceSymbol;
import com.orca.compiler.core.syntax.SyntaxNode;

public final class SourceExtensionSymbol implements SourceSymbol, ExtensionSymbol {

    private final SymbolScope members = new SymbolScope();
    private final TypeSymbol extendedTypeSymbol;
    private final SyntaxNode declaringSyntax;
    private final GlobalNamespaceSymbol globalNamespace;

    public SourceExtensionSymbol(GlobalNamespaceSymbol globalNamespace, TypeSymbol extendedType, SyntaxNode declaringSyntax) {
        this.globalNamespace = globalNamespace;
        this.extendedTypeSymbol = extendedType;
        this.declaringSyntax = declaringSyntax;
    }

    @Override
    public int getModifiers() {
        return SourceSymbol.super.getModifiers()
                | ExtensionSymbol.super.getModifiers();
    }

    @Override
    public TypeSymbol getExtendedTypeSymbol() {
        return extendedTypeSymbol;
    }

    @Override
    public GlobalNamespaceSymbol owner() {
        return globalNamespace;
    }

    @Override
    public void addMember(Symbol member) {
        members.add(member);
    }

    @Override
    public Symbol getMember(String name) {
        return members.get(name);
    }

    @Override
    public List<Symbol> getMembers(String name) {
        return members.getAll(name);
    }

    @Override
    public List<Symbol> getMembers() {
        return members.getAll();
    }

    @Override
    public boolean hasMember(String name) {
        return members.has(name);
    }

    @Override
    public SyntaxNode declaringSyntax() {
        return declaringSyntax;
    }
}
