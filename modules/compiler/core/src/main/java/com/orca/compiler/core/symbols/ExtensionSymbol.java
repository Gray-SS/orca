package com.orca.compiler.core.symbols;

import java.util.List;

import com.orca.compiler.core.symbols.synthesized.GlobalNamespaceSymbol;
import com.orca.compiler.core.typesystem.LangType;

public interface ExtensionSymbol extends TypeSymbol {

    @Override
    GlobalNamespaceSymbol owner();

    @Override
    default String name() {
        return "Extension<" + getExtendedTypeSymbol().name() + ">";
    }

    @Override
    default int getModifiers() {
        return TypeSymbol.super.getModifiers();
    }

    @Override
    default boolean isExtensionType() {
        return true;
    }

    @Override
    default void addExtension(ExtensionSymbol extension) {
        throw new UnsupportedOperationException("Extensions cannot have extensions.");
    }

    @Override
    default List<ExtensionSymbol> getExtensions() {
        return List.of();
    }

    @Override
    default LangType type() {
        return getExtendedTypeSymbol().type();
    }

    /**
     * Gets the type symbol of the type being extended by this extension symbol.
     *
     * @return The type symbol of the type being extended by this extension
     * symbol.
     */
    TypeSymbol getExtendedTypeSymbol();
}
