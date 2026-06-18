package com.orca.compiler.core.symbols.sources;

import java.util.ArrayList;
import java.util.List;

import com.orca.compiler.core.symbols.CallableSymbol;
import com.orca.compiler.core.symbols.NamespaceSymbol;
import com.orca.compiler.core.symbols.Symbol;
import com.orca.compiler.core.symbols.SymbolKind;
import com.orca.compiler.core.syntax.SyntaxNode;

public final class SourceNamespaceSymbol implements SourceSymbol, NamespaceSymbol {
    private final String name;
    private final SyntaxNode declaringSyntax;
    private final NamespaceSymbol ownerNamespace;

    private final List<Symbol> members = new ArrayList<>();

    public SourceNamespaceSymbol(NamespaceSymbol ownerNamespace, String name, SyntaxNode declaringSyntax) {
        this.name = name;
        this.ownerNamespace = ownerNamespace;
        this.declaringSyntax = declaringSyntax;
    }

    @Override
    public String name() {
        return name;
    }

    @Override
    public int getModifiers() {
        return SourceSymbol.super.getModifiers()
            | NamespaceSymbol.super.getModifiers();
    }

    @Override
    public SymbolKind kind() {
        return SymbolKind.NAMESPACE;
    }

    @Override
    public NamespaceSymbol owner() {
        return ownerNamespace;
    }

    @Override
    public SyntaxNode declaringSyntax() {
        return declaringSyntax;
    }

    @Override
    public List<Symbol> getMembers() {
        return List.copyOf(members);
    }

    @Override
    public List<Symbol> getMembers(String name) {
        List<Symbol> result = new ArrayList<>();

        for (Symbol member : members) {
            if (member.name().equals(name)) {
                result.add(member);
            }
        }

        return result;
    }

    @Override
    public void addMember(Symbol symbol) {
        if (!(symbol instanceof CallableSymbol) && hasMember(symbol.name())) {
            throw new IllegalStateException("Name conflict: " + symbol.name() + " already exists in namespace " + getFullName());
        }

        members.add(symbol);
    }

    @Override
    public Symbol getMember(String name) {
        for (Symbol member : members) {
            if (member.name().equals(name)) {
                return member;
            }
        }

        return null;
    }

    public NamespaceSymbol getNamespace(String name) {
        for (Symbol member : members) {
            if (member.name().equals(name) && member instanceof NamespaceSymbol namespace) {
                return namespace;
            }
        }

        return null;
    }

    @Override
    public boolean hasMember(String name) {
        for (Symbol member : members) {
            if (member.name().equals(name)) {
                return true;
            }
        }

        return false;
    }
}
