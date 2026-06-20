package com.orca.compiler.core.lexer;

import java.io.IOException;
import java.io.Reader;

import com.orca.compiler.core.Debug;
import com.orca.compiler.core.diagnostics.DiagnosticCollector;
import com.orca.compiler.core.syntax.SyntaxFacts;
import com.orca.compiler.core.text.SourceSpan;
import com.orca.compiler.core.text.TextSource;

public class Lexer implements AutoCloseable {

    private static final int EOF = -1;

    // 0-based index of the current character in the source.
    // Starts at -1 (before the first character). It is not incremented when we read EOF.
    private int _currentOffset = -1;
    private int _tokenStartOffset = 0;

    private int _nextCharacter;
    private final TextSource _source;
    private final Reader _input;
    private final DiagnosticCollector _diagnostics;

    private boolean _hasStarted = false;

    public Lexer(TextSource source) {
        this(source, new DiagnosticCollector());
    }

    public Lexer(TextSource source, DiagnosticCollector diagnostics) {
        _source = source;
        _input = source.reader();
        _diagnostics = diagnostics;
    }

    public DiagnosticCollector diagnostics() {
        return _diagnostics;
    }

    @Override
    public void close() throws IOException {
        _input.close();
    }

    public TextSource source() {
        return _source;
    }

    /**
     * Reads the next symbol from the input and returns a Symbol representing
     * it. This method skips any whitespace characters and comments before
     * trying to read the next symbol. It handles identifiers, keywords, number
     * literals, string literals, and various operators and punctuation. If an
     * unrecognized character is encountered, it returns a Symbol of kind
     * Unknown with the character as its lexeme. If the end of file is reached,
     * it returns a Symbol of kind EOF with an empty lexeme. The line and column
     * numbers of the returned Symbol correspond to the position of the first
     * character of the symbol in the input. If an error occurs while reading
     * the next symbol (e.g., an unterminated string literal), a
     * CompilerException is thrown with a descriptive error message.
     *
     * @return a Symbol representing the next symbol read from the input.
     */
    public Token getNextSymbol() {
        if (!_hasStarted) {
            advance();
            _hasStarted = true;
        }

        skipWhitespaces();
        skipComments();

        if (isAtEndOfFile()) {
            return new Token(
                    TokenKind.EOF,
                    "",
                    _source.spanEnd(),
                    null);
        }

        // From here on, the next character is the first character of the next token.
        _tokenStartOffset = _currentOffset;

        if (isIdentifierStart()) {
            return readIdentifierOrKeyword();
        }

        if (isNumberLiteralStart()) {
            return readNumberLiteral();
        }

        switch (_nextCharacter) {
            case '!' -> {
                // Handle '!' and '!='
                advance();
                if (_nextCharacter == '=') {
                    advance();
                    return createSymbol(TokenKind.BangEqual, "!=");
                }
                return createSymbol(TokenKind.Bang, "!");
            }
            case '"' -> {
                return readStringLiteral();
            }
            case '.' -> {
                advance();
                return createSymbol(TokenKind.Dot, ".");
            }
            case '+' -> {
                // handle '+', '++' and '+='
                advance();
                switch (_nextCharacter) {
                    case '+' -> {
                        advance();
                        return createSymbol(TokenKind.PlusPlus, "++");
                    }
                    case '=' -> {
                        advance();
                        return createSymbol(TokenKind.PlusEquals, "+=");
                    }
                }
                return createSymbol(TokenKind.Plus, "+");
            }
            case '-' -> {
                // Handle '-', '--', '-=' and '->'
                advance();
                switch (_nextCharacter) {
                    case '>' -> {
                        advance();
                        return createSymbol(TokenKind.Arrow, "->");
                    }
                    case '=' -> {
                        advance();
                        return createSymbol(TokenKind.MinusEquals, "-=");
                    }
                    case '-' -> {
                        advance();
                        return createSymbol(TokenKind.MinusMinus, "--");
                    }
                    default -> {
                        return createSymbol(TokenKind.Minus, "-");
                    }
                }
            }
            case '*' -> {
                // handle '*' and '*='
                advance();
                if (_nextCharacter == '=') {
                    advance();
                    return createSymbol(TokenKind.StarEquals, "*=");
                }
                return createSymbol(TokenKind.Star, "*");
            }
            case '/' -> {
                // handle '/' and '/='
                advance();
                if (_nextCharacter == '=') {
                    advance();
                    return createSymbol(TokenKind.SlashEquals, "/=");
                }
                return createSymbol(TokenKind.Slash, "/");
            }
            case '%' -> {
                // handle '%' and '%='
                advance();
                if (_nextCharacter == '=') {
                    advance();
                    return createSymbol(TokenKind.PercentEquals, "%=");
                }
                return createSymbol(TokenKind.Percent, "%");
            }
            case '(' -> {
                advance();
                return createSymbol(TokenKind.LParen, "(");
            }
            case ')' -> {
                advance();
                return createSymbol(TokenKind.RParen, ")");
            }
            case '{' -> {
                advance();
                return createSymbol(TokenKind.LBrace, "{");
            }
            case '}' -> {
                advance();
                return createSymbol(TokenKind.RBrace, "}");
            }
            case '[' -> {
                advance();
                return createSymbol(TokenKind.LBracket, "[");
            }
            case ']' -> {
                advance();
                return createSymbol(TokenKind.RBracket, "]");
            }
            case ';' -> {
                advance();
                return createSymbol(TokenKind.Semicolon, ";");
            }
            case ',' -> {
                advance();
                return createSymbol(TokenKind.Comma, ",");
            }
            case '=' -> {
                // Handle '=' and '=='
                advance();
                switch (_nextCharacter) {
                    case '=' -> {
                        advance();
                        return createSymbol(TokenKind.DoubleEquals, "==");
                    }
                    default -> {
                        return createSymbol(TokenKind.Equals, "=");
                    }
                }
            }
            case '>' -> {
                // Handle '>' and '>='
                advance();
                if (_nextCharacter == '=') {
                    advance();
                    return createSymbol(TokenKind.GreaterThanEq, ">=");
                } else {
                    return createSymbol(TokenKind.GreaterThan, ">");
                }
            }
            case '<' -> {
                // Handle '<' and '<='
                advance();
                if (_nextCharacter == '=') {
                    advance();
                    return createSymbol(TokenKind.LessThanEq, "<=");
                } else {
                    return createSymbol(TokenKind.LessThan, "<");
                }
            }
            case '&' -> {
                // Handle '&&'
                advance();
                if (_nextCharacter == '&') {
                    advance();
                    return createSymbol(TokenKind.DoubleAmpersand, "&&");
                } else {
                    _diagnostics.reportUnexpectedCharacterAfter(currentTokenSpan(), '&', (char) _nextCharacter);
                    return badToken();
                }
            }
            case '|' -> {
                // Handle '||'
                advance();
                if (_nextCharacter == '|') {
                    advance();
                    return createSymbol(TokenKind.DoublePipe, "||");
                } else {
                    _diagnostics.reportUnexpectedCharacterAfter(currentTokenSpan(), '|', (char) _nextCharacter);
                    return badToken();
                }
            }
            case ':' -> {
                // Handle ':', '::' and ':='
                advance();
                switch (_nextCharacter) {
                    case ':' -> {
                        advance();
                        return createSymbol(TokenKind.DoubleColon, "::");
                    }
                    case '=' -> {
                        advance();
                        return createSymbol(TokenKind.ColonEquals, ":=");
                    }
                    default -> {
                        return createSymbol(TokenKind.Colon, ":");
                    }
                }
            }
        }

        _diagnostics.reportUnexpectedCharacter(currentTokenSpan(), (char) _nextCharacter);
        return badToken();
    }

    private Token badToken() {
        String lexeme = Character.toString((char) _nextCharacter);
        advance();
        return createSymbol(TokenKind.BadToken, lexeme);
    }

    /**
     * Creates a Symbol with the given kind and lexeme, using the current line
     * and column numbers as the position of the symbol in the input. The line
     * and column numbers of the returned Symbol correspond to the position of
     * the first character of the symbol in the input.
     *
     * @param kind the kind of the Symbol to create.
     * @param lexeme the lexeme of the Symbol to create.
     * @return a Symbol with the given kind and lexeme, using the current line
     * and column numbers as the position of the symbol in the input.
     */
    private Token createSymbol(TokenKind kind, String lexeme) {
        return createSymbol(kind, lexeme, null);
    }

    /**
     * Creates a Symbol with the given kind and lexeme, using the current line
     * and column numbers as the position of the symbol in the input. The line
     * and column numbers of the returned Symbol correspond to the position of
     * the first character of the symbol in the input.
     *
     * @param kind the kind of the Symbol to create.
     * @param lexeme the lexeme of the Symbol to create.
     * @return a Symbol with the given kind and lexeme, using the current line
     * and column numbers as the position of the symbol in the input.
     */
    private Token createSymbol(TokenKind kind, String lexeme, Object value) {
        var span = _source.spanAt(_tokenStartOffset, lexeme.length());
        return new Token(kind, lexeme, span, value);
    }

    private SourceSpan currentTokenSpan() {
        return _source.spanAt(_tokenStartOffset, _currentOffset - _tokenStartOffset + 1);
    }

    /**
     * Reads and discards all whitespace characters from the input until a
     * non-whitespace character is encountered.
     */
    private void skipWhitespaces() {
        while (Character.isWhitespace(_nextCharacter)) {
            advance();
        }
    }

    /**
     * Reads and discards comments from the input. A comment starts with a '#'
     * character and continues until the end of the line. After skipping a
     * comment, it also skips any whitespace characters that may follow it. This
     * method is called by getNextSymbol() before trying to read the next
     * symbol, ensuring that comments are ignored in the tokenization process.
     * If the end of file is reached while skipping a comment, it simply returns
     * without throwing an exception, as it will be handled by getNextSymbol()
     * when it checks for EOF.
     */
    private void skipComments() {
        while (_nextCharacter == '#') {
            while (!isAtEndOfFile() && _nextCharacter != '\n') {
                advance();
            }

            skipWhitespaces();
        }
    }

    /**
     * Reads a string literal from the input and returns a Symbol representing
     * it. A string literal starts and ends with a double quote ("). The content
     * of the string literal can contain escape sequences, which are handled
     * accordingly. If the end of file is reached before the closing double
     * quote is found, a bad token is returned and an error is reported to the
     * diagnostics.
     *
     * @return a Symbol representing the string literal read from the input.
     */
    private Token readStringLiteral() {
        StringBuilder rawLexeme = new StringBuilder();
        StringBuilder decodedValue = new StringBuilder();

        // Current char is the opening quote
        rawLexeme.append('"');
        advance();

        while (!isAtEndOfFile() && _nextCharacter != '\n' && _nextCharacter != '"') {
            if (_nextCharacter == '\\') {
                rawLexeme.append('\\');
                advance();
                if (isAtEndOfFile() || _nextCharacter == '\n') {
                    _diagnostics.reportUnterminatedStringLiteral(currentTokenSpan());
                    return badToken();
                }

                rawLexeme.append((char) _nextCharacter);
                switch (_nextCharacter) {
                    case 'n' ->
                        decodedValue.append('\n');
                    case '"' ->
                        decodedValue.append('"');
                    case '\\' ->
                        decodedValue.append('\\');
                    default -> {
                        _diagnostics.reportInvalidEscapeSequence(currentTokenSpan(), "\\" + (char) _nextCharacter);
                        return badToken();
                    }
                }
                advance();
            } else {
                rawLexeme.append((char) _nextCharacter);
                decodedValue.append((char) _nextCharacter);
                advance();
            }
        }

        if (isAtEndOfFile() || _nextCharacter == '\n') {
            _diagnostics.reportUnterminatedStringLiteral(currentTokenSpan());
            return badToken();
        }

        // Closing quote
        rawLexeme.append('"');
        advance();

        return createSymbol(TokenKind.StringLiteral, rawLexeme.toString(), decodedValue.toString());
    }

    /**
     * Reads a number literal from the input and returns a Symbol representing
     * it.
     *
     * @return a Symbol representing the number literal read from the input.
     */
    private Token readNumberLiteral() {
        StringBuilder lexeme = new StringBuilder();
        boolean hasDot = false;
        while (true) {
            if (Character.isDigit(_nextCharacter)) {
                lexeme.append((char) _nextCharacter);
                advance();
                continue;
            }

            // Only treat '.' as part of a float literal if it is followed by a digit.
            // This allows member access like: 10.timesTwo()
            if (_nextCharacter == '.' && Character.isDigit(peekChar(1))) {
                if (hasDot) {
                    _diagnostics.reportInvalidFloatLiteral(currentTokenSpan(), lexeme.toString());
                    return badToken();
                }
                hasDot = true;
                lexeme.append('.');
                advance();
                continue;
            }

            break;
        }

        String lexemeStr = lexeme.toString();
        TokenKind kind = hasDot ? TokenKind.FloatLiteral : TokenKind.IntegerLiteral;
        if (kind == TokenKind.IntegerLiteral) {
            return createSymbol(kind, lexemeStr, Integer.valueOf(lexemeStr));
        }

        return createSymbol(kind, lexemeStr, Double.valueOf(lexemeStr));
    }

    // private static String normalizeIntegerLexeme(String lexeme) {
    //     int firstNonZero = 0;
    //     while (firstNonZero < lexeme.length() && lexeme.charAt(firstNonZero) == '0') {
    //         firstNonZero++;
    //     }
    //     if (firstNonZero == lexeme.length()) {
    //         return "0";
    //     }
    //     return lexeme.substring(firstNonZero);
    // }
    // private static String normalizeFloatLexeme(String lexeme) {
    //     // ".234" should be recognized as a float "0.234".
    //     // "0000.234" should be recognized as a float "0.234".
    //     int firstNonZero = 0;
    //     while (firstNonZero < lexeme.length() && lexeme.charAt(firstNonZero) == '0') {
    //         firstNonZero++;
    //     }
    //     if (firstNonZero == lexeme.length()) {
    //         return "0.0";
    //     }
    //     lexeme = lexeme.substring(firstNonZero);
    //     if (!lexeme.isEmpty() && lexeme.charAt(0) == '.') {
    //         return "0" + lexeme;
    //     }
    //     return lexeme;
    // }
    /**
     * Reads an identifier from the input and returns a Symbol representing it.
     *
     * @return a Symbol representing the identifier read from the input.
     */
    private Token readIdentifierOrKeyword() {
        StringBuilder lexeme = new StringBuilder();
        while (isIdentifierPart()) {
            lexeme.append((char) _nextCharacter);
            advance();
        }

        String lexemeStr = lexeme.toString();
        if (lexemeStr.equals("true")) {
            return createSymbol(TokenKind.BoolLiteral, lexemeStr, true);
        } else if (lexemeStr.equals("false")) {
            return createSymbol(TokenKind.BoolLiteral, lexemeStr, false);
        }

        TokenKind keywordKind = SyntaxFacts.getKeywordKind(lexemeStr);
        if (keywordKind != null) {
            return createSymbol(keywordKind, lexemeStr);
        }

        return createSymbol(TokenKind.Identifier, lexemeStr);
    }

    /**
     * Advances the input by reading the next character and updating the line
     * and column numbers accordingly. If the next character is a newline, the
     * line number is incremented and the column number is reset to 0.
     * Otherwise, the column number is incremented. If an exception occurs while
     * reading the next character, it is treated as if the end of file has been
     * reached.
     */
    private void advance() {
        try {
            _nextCharacter = _input.read();
            if (_nextCharacter != EOF) {
                _currentOffset++;
            }
        } catch (IOException e) {
            Debug.warning("[Lexer] IOException while reading input: " + e.getMessage() + ". Treating it as end of file.");
            _nextCharacter = EOF;
        }
    }

    private int peekChar(int offset) {
        if (isAtEndOfFile()) {
            return EOF;
        }

        int index = _currentOffset + offset;
        String content = _source.content();
        if (index < 0 || index >= content.length()) {
            return EOF;
        }

        return content.charAt(index);
    }

    /**
     * Checks if the current character can start a number literal. A '.' only
     * starts a number if it is immediately followed by a digit.
     */
    private boolean isNumberLiteralStart() {
        return Character.isDigit(_nextCharacter)
                || (_nextCharacter == '.' && Character.isDigit(peekChar(1)));
    }

    /**
     * Checks if the next character can be the start of an identifier. An
     * identifier can starts with a non-capital letter or an underscore.
     *
     * @return true if the next character can be the start of an identifier,
     * false otherwise.
     */
    private boolean isIdentifierStart() {
        return Character.isLetter(_nextCharacter) || _nextCharacter == '_';
    }

    /**
     * Checks if the next character can be part of an identifier.
     *
     * @return true if the next character can be part of an identifier, false
     * otherwise.
     */
    private boolean isIdentifierPart() {
        return Character.isLetterOrDigit(_nextCharacter) || _nextCharacter == '_';
    }

    /**
     * Checks if the end of file has been reached.
     *
     * @return true if the end of file has been reached, false otherwise.
     */
    private boolean isAtEndOfFile() {
        return _nextCharacter == -1;
    }
}
