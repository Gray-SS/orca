package com.orca.compiler.core.diagnostics;

/**
 * Required diagnostic categories for the project specification.
 *
 * These categories must appear in the displayed error message for the
 * associated kinds of failures.
 */
public enum DiagnosticCategory {
    MISC("Miscellaneous"),
    INPUT("InputError"),
    LEXER("LexerError"),
    PARSER("ParserError"),
    EXECUTION("ExecutionError"),
    SEMANTIC_OVERLOAD("OverloadResolutionError"),
    SEMANTIC_TYPE("TypeError"),
    SEMANTIC_COLLECTION("CollectionError"),
    SEMANTIC_OPERATOR("OperatorError"),
    SEMANTIC_ARGUMENT("ArgumentError"),
    SEMANTIC_CONDITION("MissingConditionError"),
    SEMANTIC_RETURN("ReturnError"),
    SEMANTIC_SCOPE("ScopeError"),
    SEMANTIC_CONSTANT("ConstantError"),
    SEMANTIC_TOP_LEVEL_ORDER("TopLevelOrderError"),
    UNCATEGORIZED("Uncategorized");

    private final String text;

    DiagnosticCategory(String text) {
        this.text = text;
    }

    public String text() {
        return text;
    }
}
