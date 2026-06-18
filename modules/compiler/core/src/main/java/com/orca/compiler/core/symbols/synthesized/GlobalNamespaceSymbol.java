package com.orca.compiler.core.symbols.synthesized;

import java.util.List;

import com.orca.compiler.core.Compilation;
import com.orca.compiler.core.symbols.NamespaceSymbol;
import com.orca.compiler.core.symbols.Symbol;
import com.orca.compiler.core.symbols.SymbolKind;

public final class GlobalNamespaceSymbol implements SynthesizedSymbol, NamespaceSymbol {

    private final Compilation compilation;
    private final List<Symbol> members = new java.util.ArrayList<>();

    public GlobalNamespaceSymbol(Compilation compilation) {
        this.compilation = compilation;
    }

    public Compilation getCompilation() {
        return compilation;
    }

    /**
     * Global namespace has no owner, as it is the root of the namespace
     * hierarchy.
     *
     * @implNote This method will always return null
     */
    @Override
    public NamespaceSymbol owner() {
        return null;
    }

    @Override
    public String name() {
        return "<global_namespace>";
    }

    @Override
    public String getFullName() {
        return "";
    }

    @Override
    public SymbolKind kind() {
        return SymbolKind.GLOBAL_NAMESPACE;
    }

    @Override
    public int getModifiers() {
        return SynthesizedSymbol.super.getModifiers() | NamespaceSymbol.super.getModifiers();
    }

    @Override
    public void addMember(Symbol member) {
        members.add(member);
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

    @Override
    public List<Symbol> getMembers(String name) {
        List<Symbol> result = new java.util.ArrayList<>();

        for (Symbol member : members) {
            if (member.name().equals(name)) {
                result.add(member);
            }
        }

        return result;
    }

    @Override
    public List<Symbol> getMembers() {
        return new java.util.ArrayList<>(members);
    }

    @Override
    public boolean hasMember(String name) {
        return getMember(name) != null;
    }
}
