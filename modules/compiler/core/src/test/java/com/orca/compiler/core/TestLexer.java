package com.orca.compiler.core;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import org.junit.Test;

import com.orca.compiler.core.diagnostics.DiagnosticCode;
import com.orca.compiler.core.lexer.Lexer;
import com.orca.compiler.core.lexer.Token;
import com.orca.compiler.core.lexer.TokenKind;
import com.orca.compiler.core.syntax.SyntaxFacts;
import com.orca.compiler.core.text.StringSource;

/**
 * Tests pour le Lexer (analyse lexicale). Utilise la nouvelle architecture avec
 * CompilationContext pour l'isolation.
 */
public class TestLexer {

    private Lexer lexerFor(String input) {
        return new Lexer(new StringSource(input));
    }

    private Token next(Lexer lexer) {
        return lexer.getNextSymbol();
    }

    private void assertHasDiagnostic(Lexer lexer, DiagnosticCode expectedCode) {
        var diagnostics = lexer.diagnostics();
        assertTrue("Expected diagnostic code: " + expectedCode, diagnostics.hasDiagnostic(expectedCode));
    }

    // EOF & Whitespace
    @Test
    public void testEmptyInput() {
        assertEquals(TokenKind.EOF, next(lexerFor("")).kind());
    }

    @Test
    public void testOnlyWhitespaceAndComment() {
        assertEquals(TokenKind.EOF, next(lexerFor("  \t\n# comment\n")).kind());
    }

    // Identifiers & Type Identifiers
    @Test
    public void testIdentifiers() {
        String[] ids = {"abc", "abc123", "_abc_", "_12s", "myVar"};
        for (String id : ids) {
            Token s = next(lexerFor(id));
            assertEquals("Failed for: " + id, TokenKind.Identifier, s.kind());
            assertEquals(id, s.lexeme());
        }
    }

    @Test
    public void testTypeIdentifiers() {
        String[] types = {"INT", "FLOAT", "STRING", "BOOL", "MyType"};
        for (String t : types) {
            assertEquals("Failed for: " + t, TokenKind.Identifier, next(lexerFor(t)).kind());
        }
    }
    // Keywords

    @Test
    public void testKeywords() {
        var keywords = SyntaxFacts.getWellKnownKeywords();
        for (String keyword : keywords.keySet()) {
            assertEquals("Failed for: " + keyword, keywords.get(keyword), next(lexerFor(keyword)).kind());
        }
    }

    @Test
    public void testKeywordNotMixedWithIdentifier() {
        // "finale" should be an Identifier, not the keyword "final"
        assertEquals(TokenKind.Identifier, next(lexerFor("finale")).kind());
    }

    // Literal
    @Test
    public void testIntegerLiterals() {
        Token first = next(lexerFor("42"));
        assertEquals(TokenKind.IntegerLiteral, first.kind());
        assertEquals(42, first.value());

        Token second = next(lexerFor("00342"));
        assertEquals(TokenKind.IntegerLiteral, second.kind());
        assertEquals(342, second.value());
    }

    @Test
    public void testFloatLiterals() {
        assertEquals(TokenKind.FloatLiteral, next(lexerFor("3.14")).kind());

        Token dotFloat = next(lexerFor(".234"));
        assertEquals(TokenKind.FloatLiteral, dotFloat.kind());
        assertEquals(0.234, (double) dotFloat.value(), 0.00001);

        Token zeroFloat = next(lexerFor("0.234"));
        assertEquals(TokenKind.FloatLiteral, zeroFloat.kind());
        assertEquals(0.234, (double) zeroFloat.value(), 0.00001);

        Token leadingZeroFloat = next(lexerFor("0000.234"));
        assertEquals(TokenKind.FloatLiteral, leadingZeroFloat.kind());
        assertEquals(0.234, (double) leadingZeroFloat.value(), 0.00001);

        Token trailingZeroAfterDotFloat = next(lexerFor("234.000"));
        assertEquals(TokenKind.FloatLiteral, trailingZeroAfterDotFloat.kind());
        assertEquals(234.0, (double) trailingZeroAfterDotFloat.value(), 0.00001);
    }

    @Test
    public void testStringLiteral() {
        assertStringLiteral("\"hello\"", "\"hello\"", "hello");

        // Test escape sequences
        assertStringLiteral("\"\"", "\"\"", "");
        assertStringLiteral("\"a\\nb\"", "\"a\\nb\"", "a\nb");
        assertStringLiteral("\"a\\\\nb\"", "\"a\\\\nb\"", "a\\nb");
        assertStringLiteral("\"a\tb\"", "\"a\tb\"", "a\tb");
        assertStringLiteral("\"say \\\"hi\\\"\"", "\"say \\\"hi\\\"\"", "say \"hi\"");
        assertStringLiteral("\"a\\\\b\"", "\"a\\\\b\"", "a\\b");
    }

    @Test
    public void testBoolLiterals() {
        Token trueSym = next(lexerFor("true"));
        assertEquals(TokenKind.BoolLiteral, trueSym.kind());
        assertEquals(true, trueSym.value());

        Token falseSym = next(lexerFor("false"));
        assertEquals(TokenKind.BoolLiteral, falseSym.kind());
        assertEquals(false, falseSym.value());
    }

    private void assertStringLiteral(String input, String expectedLexeme, String expectedValue) {
        Token s = next(lexerFor(input));
        assertEquals(TokenKind.StringLiteral, s.kind());
        assertEquals(expectedLexeme, s.lexeme());
        assertEquals(expectedValue, s.value());
    }

    @Test
    public void testUnterminatedString() {
        Lexer lexer = lexerFor("\"unterminated");
        next(lexer);

        assertHasDiagnostic(lexer, DiagnosticCode.LEX_UNTERMINATED_STRING);
    }

    @Test
    public void testInvalidEscapeSequence() {
        Lexer lexer = lexerFor("\"invalid escape: \\q\"");
        next(lexer);

        assertHasDiagnostic(lexer, DiagnosticCode.LEX_INVALID_ESCAPE_SEQUENCE);
    }

    @Test
    public void testUnknownCharacterThrows() {
        Lexer lexer = lexerFor("@");
        next(lexer);

        assertHasDiagnostic(lexer, DiagnosticCode.LEX_UNEXPECTED_CHARACTER);
    }

    //Operators & Punctuation
    @Test
    public void testOperators() {
        Object[][] cases = {
            {"=", TokenKind.Equals},
            {"==", TokenKind.DoubleEquals},
            {"!=", TokenKind.BangEqual},
            {"+", TokenKind.Plus},
            {"-", TokenKind.Minus},
            {"*", TokenKind.Star},
            {"/", TokenKind.Slash},
            {"%", TokenKind.Percent},
            {">", TokenKind.GreaterThan},
            {">=", TokenKind.GreaterThanEq},
            {"<", TokenKind.LessThan},
            {"<=", TokenKind.LessThanEq},
            {"&&", TokenKind.DoubleAmpersand},
            {"||", TokenKind.DoublePipe},
            {"(", TokenKind.LParen},
            {")", TokenKind.RParen},
            {"{", TokenKind.LBrace},
            {"}", TokenKind.RBrace},
            {"[", TokenKind.LBracket},
            {"]", TokenKind.RBracket},
            {".", TokenKind.Dot},
            {";", TokenKind.Semicolon},
            {",", TokenKind.Comma},
            {"!", TokenKind.Bang}
        };
        for (Object[] c : cases) {
            assertEquals("Failed for: " + c[0], c[1], next(lexerFor((String) c[0])).kind());
        }
    }

    @Test
    public void testSingleAmpersandThrows() {
        Lexer lexer = lexerFor("& ");
        next(lexer);

        assertHasDiagnostic(lexer, DiagnosticCode.LEX_UNEXPECTED_CHARACTER);
    }

    @Test
    public void testSinglePipeThrows() {
        Lexer lexer = lexerFor("| ");
        next(lexer);

        assertHasDiagnostic(lexer, DiagnosticCode.LEX_UNEXPECTED_CHARACTER);
    }

    // Integration / token sequences
    @Test
    public void testVariableDeclaration() {
        // "INT x = 12;" and "INT x=12;" should produce the same tokens
        TokenKind[] expected = {
            TokenKind.Identifier, TokenKind.Identifier,
            TokenKind.Equals, TokenKind.IntegerLiteral, TokenKind.Semicolon, TokenKind.EOF
        };
        for (String input : new String[]{"INT x = 12;", "INT x=12;"}) {
            Lexer lexer = lexerFor(input);
            for (TokenKind kind : expected) {
                assertEquals("Failed for input: " + input, kind, next(lexer).kind());
            }
        }
    }

    @Test
    public void testFunctionDefinition() {
        Lexer lexer = lexerFor("def foo(INT x) {");
        TokenKind[] expected = {
            TokenKind.DefKeyword, TokenKind.Identifier, TokenKind.LParen,
            TokenKind.Identifier, TokenKind.Identifier, TokenKind.RParen,
            TokenKind.LBrace, TokenKind.EOF
        };
        for (TokenKind kind : expected) {
            assertEquals(kind, next(lexer).kind());
        }
    }

    @Test
    public void testWhileLoop() {
        Lexer lexer = lexerFor("while x > 0 { x = x - 1; }");
        TokenKind[] expected = {
            TokenKind.WhileKeyword, TokenKind.Identifier, TokenKind.GreaterThan,
            TokenKind.IntegerLiteral, TokenKind.LBrace, TokenKind.Identifier,
            TokenKind.Equals, TokenKind.Identifier, TokenKind.Minus,
            TokenKind.IntegerLiteral, TokenKind.Semicolon, TokenKind.RBrace, TokenKind.EOF
        };
        for (TokenKind kind : expected) {
            assertEquals(kind, next(lexer).kind());
        }
    }

    @Test
    public void testCommentInCode() {
        Lexer lexer = lexerFor("x = 1; # set x\ny = 2;");
        TokenKind[] expected = {
            TokenKind.Identifier, TokenKind.Equals, TokenKind.IntegerLiteral, TokenKind.Semicolon,
            TokenKind.Identifier, TokenKind.Equals, TokenKind.IntegerLiteral, TokenKind.Semicolon,
            TokenKind.EOF
        };
        for (TokenKind kind : expected) {
            assertEquals(kind, next(lexer).kind());
        }
    }

    @Test
    public void testSymbolHelpers() {
        Token id = next(lexerFor("abc"));
        Token eof = next(lexerFor(""));
        assertTrue(id.is(TokenKind.Identifier));
        assertFalse(id.isEOF());
        assertTrue(eof.isEOF());
        assertNotNull(id.toString());
    }
}
