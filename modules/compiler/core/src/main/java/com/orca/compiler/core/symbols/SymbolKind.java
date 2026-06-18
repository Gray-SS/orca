package com.orca.compiler.core.symbols;

public enum SymbolKind {
    MISSING("<MISSING>"),
    VARIABLE("variable"),
    PARAMETER("parameter"),
    TYPE("type"),
    NAMESPACE("namespace"),
    GLOBAL_NAMESPACE("global namespace"),
    FIELD("field"),
    METHOD("method"),
    CONSTRUCTOR("constructor"),
    METHOD_GROUP("method group");

    private final String displayName;

    SymbolKind(String displayName) {
        this.displayName = displayName;
    }

    /**
     * Gets the display name of this symbol kind, which is used in error
     * messages and other user-facing contexts.
     *
     * @return the display name of this symbol kind
     */
    public String getDisplayName() {
        return displayName;
    }
}
