package com.orca.compiler.core.symbols;

import java.util.List;

public interface NamespaceOrTypeSymbol extends Symbol {

    @Override
    default int getModifiers() {
        return SymbolModifiers.NAMESPACE_OR_TYPE;
    }

    /**
     * Adds a member symbol to this namespace or type.
     *
     * @param symbol The symbol to add as a member of this namespace or type.
     */
    void addMember(Symbol member);

    /**
     * Retrieves a member symbol by name from this namespace or type.
     *
     * @param name The name of the member symbol to retrieve.
     * @return The member symbol with the specified name, or null if no such
     * member exists.
     */
    Symbol getMember(String name);

    /**
     * Retrieves a list of member symbols with the specified name from this
     * namespace or type.
     *
     * @param name The name of the member symbols to retrieve.
     * @return A list of member symbols with the specified name, or an empty
     * list if no such members exist.
     */
    List<Symbol> getMembers(String name);

    /**
     * Retrieves a list of callable member symbols (e.g., methods) with the
     * specified name from this namespace or type.
     *
     * @param name The name of the callable member symbols to retrieve.
     * @return A list of callable member symbols with the specified name, or an
     * empty list if no such members exist.
     */
    default List<CallableSymbol> getCallableMembers(String name) {
        return getMembers(name).stream()
                .filter(symbol -> symbol instanceof CallableSymbol)
                .map(symbol -> (CallableSymbol) symbol)
                .toList();
    }

    /**
     * Returns an unmodifiable list of the member symbols declared in this
     * namespace or type.
     *
     * @return An unmodifiable list of the member symbols declared in this
     * namespace or type.
     */
    List<Symbol> getMembers();

    /**
     * Returns an unmodifiable list of the member symbols declared in this
     * namespace or type that have been resolved (i.e., their types have been
     * determined).
     *
     * @return An unmodifiable list of the member symbols declared in this
     * namespace or type that have been resolved.
     */
    default List<Symbol> getResolvedMembers() {
        return getMembers();
    }

    /**
     * Checks if a member symbol with the specified name is declared in this
     * namespace or type.
     *
     * @param name The name of the member symbol to check for declaration.
     * @return True if a member symbol with the specified name is declared in
     * this namespace or type, false otherwise.
     */
    boolean hasMember(String name);
}
