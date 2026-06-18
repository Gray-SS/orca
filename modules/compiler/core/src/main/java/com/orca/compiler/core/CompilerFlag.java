package com.orca.compiler.core;

public enum CompilerFlag {
    PRINT_TOKENS(1 << 0),
    PRINT_AST(1 << 1),
    STOP_AFTER_LEXER(1 << 2),
    DEBUG(1 << 4),
    STOP_AFTER_PARSER(1 << 3),
    PRINT_LOWERED_BOUND_TREE(1 << 5),
    EXECUTE_COMPILED_PROGRAM(1 << 6),
    SILENT_MODE(1 << 7),
    ALLOW_IMPLICIT_MAIN(1 << 8),
    PRINT_SYMBOL_HIERARCHY(1 << 9),
    LIBRARY_MODE(1 << 11);

    public final int mask;

    CompilerFlag(int mask) {
        this.mask = mask;
    }
}
