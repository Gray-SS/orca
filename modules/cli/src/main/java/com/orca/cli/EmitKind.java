package com.orca.cli;

/**
 * Represents the kind of target the CLI should emit.
 */
public enum EmitKind {
    /**
     * Emit the tokens produced by the lexer.
     */
    TOKENS,
    /**
     * Emit the abstract syntax tree.
     */
    AST,
    /**
     * Emit the lowered bound tree.
     */
    IR,
    /**
     * Emit the symbol hierarchy.
     */
    SYMBOLS,
    /**
     * Emit bytecode.
     */
    BYTECODE,
}
