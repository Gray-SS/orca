package com.orca.compiler.core.symbols.synthesized;

import java.util.List;

import com.orca.compiler.core.Compilation;
import com.orca.compiler.core.CompilerConstants;
import com.orca.compiler.core.symbols.ExtensionSymbol;
import com.orca.compiler.core.symbols.MethodSymbol;
import com.orca.compiler.core.symbols.NamespaceOrTypeSymbol;
import com.orca.compiler.core.symbols.Symbol;
import com.orca.compiler.core.symbols.SymbolScope;
import com.orca.compiler.core.symbols.TypeSymbol;
import com.orca.compiler.core.typesystem.LangType;
import com.orca.compiler.core.typesystem.NominalType;

public class SynthesizedEntryClassSymbol implements SynthesizedSymbol, TypeSymbol {

    private final SymbolScope members;
    private final Compilation compilation;
    private final MethodSymbol mainMethodSymbol;

    public SynthesizedEntryClassSymbol(Compilation compilation, MethodSymbol mainMethodSymbol) {
        this.compilation = compilation;
        this.members = new SymbolScope();
        this.mainMethodSymbol = mainMethodSymbol;
    }

    @Override
    public String name() {
        return CompilerConstants.DEFAULT_ENTRY_CLASS_NAME;
    }

    public MethodSymbol getMainMethodSymbol() {
        return mainMethodSymbol;
    }

    @Override
    public NamespaceOrTypeSymbol owner() {
        return compilation.getGlobalNamespace();
    }

    @Override
    public int getModifiers() {
        return SynthesizedSymbol.super.getModifiers()
                | TypeSymbol.super.getModifiers();
    }

    @Override
    public LangType type() {
        return new NominalType(CompilerConstants.DEFAULT_ENTRY_CLASS_NAME, LangType.Unknown);
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
    public void addExtension(ExtensionSymbol extension) {
        throw new UnsupportedOperationException("Cannot add extensions to the synthesized program symbol.");
    }

    @Override
    public List<ExtensionSymbol> getExtensions() {
        return List.of();
    }
}
