package com.orca.compiler.core.symbols.constructed;

import java.util.List;

import com.orca.compiler.core.symbols.ExtensionSymbol;
import com.orca.compiler.core.symbols.NamespaceOrTypeSymbol;
import com.orca.compiler.core.symbols.Symbol;
import com.orca.compiler.core.symbols.TypeSymbol;
import com.orca.compiler.core.symbols.synthesized.SynthesizedSymbol;
import com.orca.compiler.core.typesystem.LangType;

public abstract class ConstructedTypeSymbol implements SynthesizedSymbol, TypeSymbol {

    private final LangType constructedType;
    private final List<ExtensionSymbol> extensions = new java.util.ArrayList<>();

    public ConstructedTypeSymbol(LangType type) {
        this.constructedType = type;
    }

    @Override
    public String name() {
        return constructedType.displayName();
    }

    @Override
    public void addExtension(ExtensionSymbol extension) {
        extensions.add(extension);
    }

    @Override
    public List<ExtensionSymbol> getExtensions() {
        return extensions;
    }

    @Override
    public LangType type() {
        return constructedType;
    }

    @Override
    public NamespaceOrTypeSymbol owner() {
        // Constructed types do not have an owner, they are not declared in any namespace or type, they are synthesized by the compiler based on the types used in the source code.
        return null;
    }

    @Override
    public final void addMember(Symbol member) {
        throw new UnsupportedOperationException("Cannot add members to a constructed type symbol");
    }

    @Override
    public final Symbol getMember(String name) {
        return null;
    }

    @Override
    public final List<Symbol> getMembers() {
        return List.of();
    }

    @Override
    public final List<Symbol> getMembers(String name) {
        return List.of();
    }

    @Override
    public final boolean hasMember(String name) {
        return false;
    }

    @Override
    public final int getModifiers() {
        return SynthesizedSymbol.super.getModifiers()
                | TypeSymbol.super.getModifiers();
    }
}
