package com.orca.compiler.core.symbols.synthesized;

import java.util.List;

import com.orca.compiler.core.Debug;
import com.orca.compiler.core.symbols.ExtensionSymbol;
import com.orca.compiler.core.symbols.NamespaceOrTypeSymbol;
import com.orca.compiler.core.symbols.NamespaceSymbol;
import com.orca.compiler.core.symbols.Symbol;
import com.orca.compiler.core.symbols.TypeSymbol;
import com.orca.compiler.core.typesystem.LangType;

public final class SynthesizedTypeSymbol implements SynthesizedSymbol, TypeSymbol {

    private final String name;
    private final LangType type;
    private final NamespaceSymbol owner;
    private final int modifiers;

    public SynthesizedTypeSymbol(NamespaceSymbol owner, String name, LangType type, int modifiers) {
        Debug.requireNotNull(name, "Name cannot be null.");
        Debug.requireNotNull(type, "Type cannot be null.");
        Debug.requireNotNull(owner, "Owner cannot be null.");

        this.name = name;
        this.type = type;
        this.modifiers = modifiers;
        this.owner = owner;
    }

    @Override
    public String name() {
        return name;
    }

    @Override
    public void addExtension(ExtensionSymbol extension) {
        throw new UnsupportedOperationException("Cannot add extensions to a synthesized type symbol.");
    }

    @Override
    public List<ExtensionSymbol> getExtensions() {
        return List.of();
    }

    @Override
    public int getModifiers() {
        return SynthesizedSymbol.super.getModifiers()
                | TypeSymbol.super.getModifiers()
                | modifiers;
    }

    @Override
    public NamespaceOrTypeSymbol owner() {
        return owner;
    }

    @Override
    public LangType type() {
        return type;
    }

    @Override
    public void addMember(Symbol member) {
        throw new UnsupportedOperationException("Cannot add members to a primitive type symbol.");
    }

    @Override
    public Symbol getMember(String name) {
        return null;
    }

    @Override
    public List<Symbol> getMembers(String name) {
        return List.of();
    }

    @Override
    public List<Symbol> getMembers() {
        return List.of();
    }

    @Override
    public boolean hasMember(String name) {
        return false;
    }
}
