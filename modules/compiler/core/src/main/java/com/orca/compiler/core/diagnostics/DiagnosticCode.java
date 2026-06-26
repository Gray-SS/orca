package com.orca.compiler.core.diagnostics;

public enum DiagnosticCode {
    // ─── Cross-cutting pipeline ──────────────────────────────────────────────
    // These errors don't belong to a single compilation phase.

    UNCATEGORIZED(DiagnosticCategory.UNCATEGORIZED, "Uncategorized error"),
    MISSING_STD_PATH(DiagnosticCategory.MISC, "Missing standard library path"),
    MISSING_PACKAGE_DIRECTIVE(DiagnosticCategory.MISC, "Missing package directive"),
    UNSUPPORTED_SYNTAX_TREE_ROOT(DiagnosticCategory.MISC, "Unsupported syntax tree root node"),
    UNSUPPORTED_FEATURE(DiagnosticCategory.MISC, "Unsupported feature"),
    UNEXPECTED_ERROR(DiagnosticCategory.MISC, "Unexpected error"),
    // ─── Input  ──────────────────────────────────────────────────────────────

    INPUT_SOURCE_MISSING(DiagnosticCategory.INPUT, "Input source missing"),
    INPUT_FILE_NOT_FOUND(DiagnosticCategory.INPUT, "Input file not found"),
    INPUT_IO_ERROR(DiagnosticCategory.INPUT, "Input I/O error"),
    // ─── Lexing  ────────────────────────────────────────────────────────────

    LEX_UNEXPECTED_CHARACTER(DiagnosticCategory.LEXER, "Unexpected character"),
    LEX_UNTERMINATED_STRING(DiagnosticCategory.LEXER, "Unterminated string literal"),
    LEX_INVALID_ESCAPE_SEQUENCE(DiagnosticCategory.LEXER, "Invalid escape sequence"),
    LEX_MALFORMED_NUMBER_LITERAL(DiagnosticCategory.LEXER, "Malformed number literal"),
    // ─── Parsing  ────────────────────────────────────────────────────────────

    PARSER_FORBIDDEN_EXPRESSION_STATEMENT(DiagnosticCategory.PARSER, "Forbidden expression statement"),
    PARSER_UNEXPECTED_TOKEN(DiagnosticCategory.PARSER, "Unexpected token"),
    PARSER_EXPECTED_IDENTIFIER_AFTER_DOUBLE_COLON(DiagnosticCategory.PARSER, "Expected identifier after '::'"),
    PARSER_MISSING_SEMICOLON(DiagnosticCategory.PARSER, "Missing semicolon"),
    // ─── Execution  ──────────────────────────────────────────────────────────

    EXEC_ERROR(DiagnosticCategory.EXECUTION, "Execution error"),
    EXEC_INTERRUPTED(DiagnosticCategory.EXECUTION, "Execution interrupted"),
    // ────── Semantic - Overload resolution  ──────────────────────────────────

    SEM_NO_MATCHING_OVERLOAD(DiagnosticCategory.SEMANTIC_OVERLOAD, "No matching overload"),
    SEM_AMBIGUOUS_OVERLOAD(DiagnosticCategory.SEMANTIC_OVERLOAD, "Ambiguous overload"),
    // ────── Semantic - Scope  ────────────────────────────────────────────────

    SEM_AMBIGUOUS_IDENTIFIER(DiagnosticCategory.SEMANTIC_SCOPE, "Ambiguous identifier"),
    SEM_UNDECLARED_IDENTIFIER(DiagnosticCategory.SEMANTIC_SCOPE, "Undeclared identifier"),
    SEM_UNDECLARED_PACKAGE(DiagnosticCategory.SEMANTIC_SCOPE, "Undeclared package"),
    SEM_UNDECLARED_TYPE(DiagnosticCategory.SEMANTIC_SCOPE, "Undeclared type"),
    SEM_SYMBOL_REDECLARED(DiagnosticCategory.SEMANTIC_SCOPE, "Symbol redeclared"),
    SEM_SYMBOL_NOT_A_FUNCTION(DiagnosticCategory.SEMANTIC_SCOPE, "Symbol is not a function"),
    SEM_SYMBOL_NOT_A_VARIABLE(DiagnosticCategory.SEMANTIC_SCOPE, "Symbol is not a variable"),
    SEM_SYMBOL_NOT_A_VALUE(DiagnosticCategory.SEMANTIC_SCOPE, "Symbol is not a value"),
    SEM_SYMBOL_NOT_A_TYPE(DiagnosticCategory.SEMANTIC_SCOPE, "Symbol is not a type"),
    SEM_INVALID_OWNER_SYMBOL(DiagnosticCategory.SEMANTIC_SCOPE, "Invalid owner symbol"),
    SEM_UNINITIALIZED_VARIABLE(DiagnosticCategory.SEMANTIC_SCOPE, "Variable is not initialized"),
    SEM_FORBIDDEN_NESTED_DECLARATION(DiagnosticCategory.SEMANTIC_SCOPE, "Nested declaration is forbidden"),
    SEM_MISSING_MAIN_FUNCTION(DiagnosticCategory.SEMANTIC_SCOPE, "Main function is missing"),
    SEM_AMBIGUOUS_MAIN_FUNCTION(DiagnosticCategory.SEMANTIC_SCOPE, "Multiple main functions found"),
    // ─── Semantic — Types ────────────────────────────────────────────────────

    SEM_TYPE_MISMATCH(DiagnosticCategory.SEMANTIC_TYPE, "Type mismatch"),
    SEM_CANNOT_INFER_VARIABLE_TYPE(DiagnosticCategory.SEMANTIC_TYPE, "Cannot infer variable type"),
    SEM_MEMBER_ACCESS_ON_NON_NAMED_TYPE(DiagnosticCategory.SEMANTIC_TYPE, "Field access on non-collection"),
    SEM_MEMBER_NOT_FOUND(DiagnosticCategory.SEMANTIC_TYPE, "Member not found"),
    SEM_AMBIGUOUS_MEMBER_ACCESS(DiagnosticCategory.SEMANTIC_TYPE, "Ambiguous member access"),
    SEM_IMPLICIT_CONVERSION_UNSUPPORTED(DiagnosticCategory.SEMANTIC_TYPE, "Implicit conversion unsupported"),
    SEM_VOID_FIELD_TYPE(DiagnosticCategory.SEMANTIC_TYPE, "Void field type"),
    SEM_VOID_VARIABLE_TYPE(DiagnosticCategory.SEMANTIC_TYPE, "Void variable type"),
    SEM_INSTANCE_MEMBER_ACCESS_ON_NON_NAMED_TYPE(DiagnosticCategory.SEMANTIC_TYPE, "Instance member accessed on non-named type"),
    SEM_INSTANCE_MEMBER_ACCESS_STATICALLY(DiagnosticCategory.SEMANTIC_TYPE, "Instance member accessed statically"),
    SEM_STATIC_MEMBER_ACCESS_ON_INSTANCE(DiagnosticCategory.SEMANTIC_TYPE, "Static member accessed on instance"),
    SEM_TYPE_NOT_CONSTRUCTIBLE(DiagnosticCategory.SEMANTIC_TYPE, "Type cannot be constructed"),
    SEM_VALUE_NOT_CALLABLE(DiagnosticCategory.SEMANTIC_TYPE, "Value is not callable"),
    // ─── Semantic — Arguments ────────────────────────────────────────────────

    SEM_ARGUMENT_COUNT_MISMATCH(DiagnosticCategory.SEMANTIC_ARGUMENT, "Argument count mismatch"),
    SEM_ARGUMENT_TYPE_MISMATCH(DiagnosticCategory.SEMANTIC_ARGUMENT, "Argument type mismatch"),
    // ─── Semantic — Top-level ──────────────────────────────────────────

    SEM_INVALID_DECLARATION_ORDER(DiagnosticCategory.SEMANTIC_TOP_LEVEL_ORDER, "Invalid declaration order"),
    SEM_TOP_LEVEL_EXECUTABLE_STATEMENTS_WITH_EXPLICIT_MAIN(DiagnosticCategory.SEMANTIC_TOP_LEVEL_ORDER, "Top-level executable statements are not allowed when an explicit 'main' function is declared"),
    SEM_NO_TOP_LEVEL_EXECUTABLE_STATEMENTS_FOR_IMPLICIT_MAIN(DiagnosticCategory.SEMANTIC_TOP_LEVEL_ORDER, "No top-level executable statements found for implicit 'main' function"),
    SEM_TOP_LEVEL_STATEMENTS_NOT_ALLOWED(DiagnosticCategory.SEMANTIC_TOP_LEVEL_ORDER, "Top-level statements are not allowed"),
    // ─── Semantic — Constants ────────────────────────────────────────────────

    SEM_CONSTANT_MUTABLE(DiagnosticCategory.SEMANTIC_CONSTANT, "Constant cannot be mutable"),
    SEM_IMMUTABLE_VAR_MISSING_INITIALIZER(DiagnosticCategory.SEMANTIC_CONSTANT, "Constant missing initializer"),
    SEM_IMMUTABLE_ASSIGNMENT(DiagnosticCategory.SEMANTIC_CONSTANT, "Constant assignment"),
    SEM_CONSTANT_MISSING_BASE_TYPE(DiagnosticCategory.SEMANTIC_CONSTANT, "Constant missing base type"),
    SEM_CONSTANT_NON_COMPILE_TIME_FOLDABLE_INITIALIZER(DiagnosticCategory.SEMANTIC_CONSTANT, "Constant non-compile-time foldable initializer"),
    SEM_LOCAL_CONSTANT_DECLARATION(DiagnosticCategory.SEMANTIC_CONSTANT, "Local constant declaration"),
    // ─── Semantic — Collections ──────────────────────────────────────────────

    SEM_LOWERCASE_COLLECTION_NAME(DiagnosticCategory.SEMANTIC_COLLECTION, "Lowercase collection name"),
    SEM_RESERVED_TYPE_NAME(DiagnosticCategory.SEMANTIC_COLLECTION, "Reserved type name"),
    SEM_RECURSIVE_COLLECTION_DECLARATION(DiagnosticCategory.SEMANTIC_COLLECTION, "Recursive collection declaration"),
    SEM_IMPL_ON_NON_COLLECTION_TYPE(DiagnosticCategory.SEMANTIC_COLLECTION, "Impl on non-collection type"),
    SEM_INVALID_IMPL_METHOD(DiagnosticCategory.SEMANTIC_COLLECTION, "Invalid impl method"),
    SEM_INVALID_RECEIVER_PARAMETER(DiagnosticCategory.SEMANTIC_COLLECTION, "Invalid receiver parameter"),
    SEM_METHOD_REDECLARED(DiagnosticCategory.SEMANTIC_COLLECTION, "Method redeclared"),
    SEM_FIELD_REDECLARED(DiagnosticCategory.SEMANTIC_COLLECTION, "Field redeclared"),
    // ─── Semantic — Operators ────────────────────────────────────────────────

    SEM_INVALID_ASSIGNMENT_TARGET(DiagnosticCategory.SEMANTIC_OPERATOR, "Invalid assignment target"),
    SEM_STRING_INDEX_ASSIGNMENT(DiagnosticCategory.SEMANTIC_OPERATOR, "String index assignment"),
    SEM_UNSUPPORTED_INDEX_OPERATOR(DiagnosticCategory.SEMANTIC_OPERATOR, "Unsupported index operator"),
    SEM_UNSUPPORTED_ASSIGNMENT_OPERATOR(DiagnosticCategory.SEMANTIC_OPERATOR, "Unsupported assignment operator"),
    SEM_AMBIGUOUS_ASSIGNMENT_OPERATOR(DiagnosticCategory.SEMANTIC_OPERATOR, "Ambiguous assignment operator"),
    SEM_UNSUPPORTED_UNARY_OPERATOR(DiagnosticCategory.SEMANTIC_OPERATOR, "Unsupported unary operator"),
    SEM_AMBIGUOUS_UNARY_OPERATOR(DiagnosticCategory.SEMANTIC_OPERATOR, "Ambiguous unary operator"),
    SEM_UNSUPPORTED_BINARY_OPERATOR(DiagnosticCategory.SEMANTIC_OPERATOR, "Unsupported binary operator"),
    SEM_AMBIGUOUS_BINARY_OPERATOR(DiagnosticCategory.SEMANTIC_OPERATOR, "Ambiguous binary operator"),
    // ─── Semantic — Return / Control Flow ────────────────────────────────────

    SEM_FOR_LOOP_STEP_MUST_BE_ASSIGNMENT(DiagnosticCategory.SEMANTIC_RETURN, "For loop step expression must be an assignment"),
    SEM_RETURN_VALUE_IN_VOID_FUNCTION(DiagnosticCategory.SEMANTIC_RETURN, "Return value in void function"),
    SEM_MISSING_RETURN_VALUE(DiagnosticCategory.SEMANTIC_RETURN, "Missing return value"),
    SEM_RETURN_TYPE_MISMATCH(DiagnosticCategory.SEMANTIC_RETURN, "Return type mismatch"),
    SEM_INCOMPLETE_RETURN_PATHS(DiagnosticCategory.SEMANTIC_RETURN, "Incomplete return paths"),
    // ─── Semantic — Conditions ───────────────────────────────────────────────

    SEM_MISSING_CONDITION(DiagnosticCategory.SEMANTIC_CONDITION, "Missing condition"),;

    private final DiagnosticCategory keyword;

    // Default message for this diagnostic code.
    private final String message;

    // Default severity for all codes is ERROR, but this can be extended in the future if needed.
    private final DiagnosticSeverity severity = DiagnosticSeverity.ERROR;

    DiagnosticCode(DiagnosticCategory keyword, String message) {
        this.keyword = keyword;
        this.message = message;
    }

    /**
     * Default keyword to display for this diagnostic code, if any. Callers may
     * still override at the call site when necessary.
     */
    public DiagnosticCategory keyword() {
        return keyword;
    }

    /**
     * Message to display for this diagnostic code.
     *
     * @return The default message associated with this code.
     */
    public String message() {
        return message;
    }

    /**
     * Default severity for this diagnostic code.
     *
     * @return The default severity associated with this code.
     */
    public DiagnosticSeverity severity() {
        return severity;
    }

    public String keywordText() {
        return keyword == null ? null : keyword.text();
    }
}
