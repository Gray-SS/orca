package com.orca.compiler.core.lexer;

import com.orca.compiler.core.text.IHaveSpan;
import com.orca.compiler.core.text.SourceLocation;
import com.orca.compiler.core.text.SourceSpan;

/**
 * Represents a symbol (token) produced by the lexer. Each symbol has a kind
 * (type), a lexeme (the actual text from the input), and a position (line and
 * column) in the input.
 */
public final class Token implements IHaveSpan {

    private final TokenKind kind;
    private final String lexeme;
    private final SourceSpan span;

    // Optional field for storing literal values (e.g., integer value for IntegerLiteral)
    private final Object value;

    public Token(TokenKind kind, String lexeme, SourceSpan span, Object value) {
        this.kind = kind;
        this.lexeme = lexeme;
        this.span = span;
        this.value = value;
    }

    /**
     * Checks if this symbol is of the expected kind.
     *
     * @param expectedKind The kind to check against.
     * @return true if this symbol's kind matches the expected kind, false
     * otherwise.
     */
    public boolean is(TokenKind expectedKind) {
        return this.kind == expectedKind;
    }

    /**
     * Convenience method to check if this symbol is the end-of-file (EOF)
     * symbol.
     *
     * @return true if this symbol is EOF, false otherwise.
     */
    public boolean isEOF() {
        return is(TokenKind.EOF);
    }

    public TokenKind kind() {
        return kind;
    }

    public String lexeme() {
        return lexeme;
    }

    public SourceSpan span() {
        return span;
    }

    public SourceLocation loc() {
        return span.loc();
    }

    public SourceLocation endLoc() {
        return span.endLoc();
    }

    public Object value() {
        return value;
    }

    @Override
    public String toString() {
        String displayLexeme = lexeme.isEmpty() ? "None" : "\"" + lexeme + "\"";
        String displayValue = (value != null) ? ", value=" + value : "";
        return "<" + kind + ", " + displayLexeme + displayValue + "> at " + span;
    }
}
