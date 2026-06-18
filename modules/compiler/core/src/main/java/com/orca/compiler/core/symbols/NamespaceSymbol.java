package com.orca.compiler.core.symbols;

import java.util.List;

import com.orca.compiler.core.symbols.synthesized.SynthesizedTypeSymbol;
import com.orca.compiler.core.typesystem.LangType;
import com.orca.compiler.core.typesystem.NominalType;

public interface NamespaceSymbol extends NamespaceOrTypeSymbol {

    @Override
    default SymbolKind kind() {
        return SymbolKind.NAMESPACE;
    }

    default SynthesizedTypeSymbol createSynthesizedClassSymbol() {
        var fullName = getFullName();
        var fullClassName = fullName + ".Package";
        return new SynthesizedTypeSymbol(this, "Package", new NominalType(fullClassName, LangType.Unknown), 0);
    }

    @Override
    /**
     * Gets the namespace that contains this namespace, or null if this is a
     * top-level namespace.
     *
     * @return The namespace that contains this namespace, or null if this is a
     * top-level namespace.
     */
    NamespaceSymbol owner();

    @Override
    default int getModifiers() {
        return NamespaceOrTypeSymbol.super.getModifiers();
    }

    default List<Symbol> getMembersWithChildren() {
        var members = new java.util.ArrayList<Symbol>();
        getMembers().forEach(members::add);
        getMembers().stream()
                .filter(m -> m.isNamespace())
                .map(m -> (NamespaceSymbol) m)
                .forEach(ns -> members.addAll(ns.getMembersWithChildren()));
        return members;
    }

    /**
     * Gets the namespace with the specified name that is a member of this
     * namespace, or null if no such namespace exists.
     *
     * @param name The name of the namespace to get.
     * @return The namespace with the specified name, or null if no such
     * namespace exists.
     */
    default NamespaceSymbol getNamespace(String name) {
        return getMembers().stream()
                .filter(m -> m.isNamespace() && m.name().equals(name))
                .map(m -> (NamespaceSymbol) m)
                .findFirst()
                .orElse(null);
    }
}
