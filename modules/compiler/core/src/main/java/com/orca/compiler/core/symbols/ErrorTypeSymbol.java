package com.orca.compiler.core.symbols;

import java.util.List;

import com.orca.compiler.core.text.Location;
import com.orca.compiler.core.typesystem.ErrorType;
import com.orca.compiler.core.typesystem.LangType;

public final class ErrorTypeSymbol implements TypeSymbol {

    private final String name;
    private final ErrorType errorType;

    public ErrorTypeSymbol(String name, ErrorType errorType) {
        this.name = name;
        this.errorType = errorType;
    }

    @Override
    public LangType type() {
        return errorType;
    }

    @Override
    public String name() {
        return name;
    }

    @Override
    public NamespaceOrTypeSymbol owner() {
        return null;
    }

    @Override
    public Location location() {
        return null;
    }

    @Override
    public void addMember(Symbol member) {
        // No members can be added to an error type symbol.
        throw new UnsupportedOperationException("Cannot add members to an error type symbol.");
    }

    @Override
    public Symbol getMember(String name) {
        // No members can be retrieved from an error type symbol.
        return null;
    }

    @Override
    public List<Symbol> getMembers(String name) {
        // No members can be retrieved from an error type symbol.
        return List.of();
    }

    @Override
    public List<Symbol> getMembers() {
        // No members can be retrieved from an error type symbol.
        return List.of();
    }

    @Override
    public boolean hasMember(String name) {
        // No members can be retrieved from an error type symbol.
        return false;
    }

    @Override
    public void addExtension(ExtensionSymbol extension) {
        // No extensions can be added to an error type symbol.
        throw new UnsupportedOperationException("Cannot add extensions to an error type symbol.");
    }

    @Override
    public List<ExtensionSymbol> getExtensions() {
        // No extensions can be retrieved from an error type symbol.
        return List.of();
    }
}
