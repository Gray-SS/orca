package com.orca.compiler.core.syntax;

import java.util.ArrayList;
import java.util.Optional;

import com.orca.compiler.core.diagnostics.DiagnosticCollector;
import com.orca.compiler.core.diagnostics.DiagnosticFactory;
import com.orca.compiler.core.lexer.Lexer;
import com.orca.compiler.core.lexer.Token;
import com.orca.compiler.core.lexer.TokenKind;
import com.orca.compiler.core.syntax.declarations.*;
import com.orca.compiler.core.syntax.expressions.*;
import com.orca.compiler.core.syntax.members.CollectionDeclarationSyntax;
import com.orca.compiler.core.syntax.members.ErrorMemberSyntax;
import com.orca.compiler.core.syntax.members.ImplBlockSyntax;
import com.orca.compiler.core.syntax.members.MemberSyntax;
import com.orca.compiler.core.syntax.members.MethodDeclarationSyntax;
import com.orca.compiler.core.syntax.members.VariableDeclarationSyntax;
import com.orca.compiler.core.syntax.nodes.*;
import com.orca.compiler.core.syntax.statements.*;
import com.orca.compiler.core.syntax.types.*;
import com.orca.compiler.core.text.SourceSpan;

public class Parser {

    private static final int BUFFER_COMPACT_THRESHOLD = 1024;

    private int position;
    private final Lexer lexer;
    private final ArrayList<Token> buffer = new ArrayList<>();
    private final DiagnosticCollector diagnostics;

    public Parser(Lexer lexer) {
        this(lexer, new DiagnosticCollector());
    }

    public Parser(Lexer lexer, DiagnosticCollector diagnostics) {
        this.lexer = lexer;
        this.diagnostics = diagnostics;
    }

    public DiagnosticCollector diagnostics() {
        return diagnostics;
    }

    public SyntaxNode getAST() {
        return parseCompilationUnit();
    }

    public CompilationUnit parseCompilationUnit() {
        Optional<PackageDirectiveSyntax> packageSyntax = parsePackageDeclOptional();

        var imports = new ArrayList<ImportSyntax>();
        var members = new ArrayList<MemberSyntax>();
        var topLevelStatements = new ArrayList<StatementSyntax>();

        while (!isAtEnd() && isImportDeclStart()) {
            var importSyntax = parseImportDecl();
            imports.add(importSyntax);

            if (importSyntax.hasError()) {
                // Try to recover from the error by skipping to the next import declaration or member start.
                skipToNextMemberOrImportStart();
            }
        }

        while (!isAtEnd()) {
            if (isMemberStart()) {
                MemberSyntax member = parseMember();
                members.add(member);
            } else if (isStatementStart()) {
                StatementSyntax statement = parseStatement();
                topLevelStatements.add(statement);
            } else {
                consumeUnexpectedToken(token(), "a member declaration or a statement");
                skipToNextMemberStart();
            }
        }

        return new CompilationUnit(packageSyntax, imports, topLevelStatements, members);
    }

    private Optional<PackageDirectiveSyntax> parsePackageDeclOptional() {
        if (!tokenIs(TokenKind.PackageKeyword)) {
            return Optional.empty();
        }

        matchToken(TokenKind.PackageKeyword);
        var identifier = parseIdentifier();
        matchToken(TokenKind.Semicolon);

        return Optional.of(new PackageDirectiveSyntax(identifier));
    }

    private boolean isImportDeclStart() {
        return tokenIs(TokenKind.ImportKeyword);
    }

    private ImportSyntax parseImportDecl() {
        matchToken(TokenKind.ImportKeyword);

        var identifier = parseIdentifier();

        matchToken(TokenKind.Semicolon);
        return new ImportSyntax(identifier);
    }

    private boolean isMemberStart() {
        return switch (tokenKind()) {
            case DefKeyword, CollKeyword, ImplKeyword, VarKeyword, LetKeyword, ConstKeyword ->
                true;
            default ->
                false;
        };
    }

    public MemberSyntax parseMember() {
        switch (tokenKind()) {
            case DefKeyword -> {
                return parseFunctionDecl();
            }
            case CollKeyword -> {
                return parseCollectionDecl();
            }
            case ImplKeyword -> {
                return parseImplBlock();
            }
            case VarKeyword, LetKeyword, ConstKeyword -> {
                return parseVariableDeclaration();
            }

            default -> {
                var errorToken = consumeUnexpectedToken(token(), "a member declaration");
                skipToNextMemberStart();
                return new ErrorMemberSyntax(errorToken);
            }
        }
    }

    public StatementSyntax parseStatement() {
        if (!isStatementStart()) {
            var errorToken = consumeUnexpectedToken(token(), "a statement");
            skipToNextStatementStart();
            return new ErrorStatementSyntax(errorToken);
        }

        switch (tokenKind()) {
            case LBrace -> {
                return parseBlockStmt();
            }
            case ReturnKeyword -> {
                return parseReturnStmt();
            }
            case IfKeyword -> {
                return parseIfStmt();
            }
            case WhileKeyword -> {
                return parseWhileStmt();
            }
            case ForKeyword -> {
                return parseForStmt();
            }
            case VarKeyword, LetKeyword, ConstKeyword -> {
                return parseVariableDeclarationStatement();
            }

            default -> {
                ExpressionSyntax target = parseExpr();
                matchToken(TokenKind.Semicolon);

                if (!(target instanceof ErrorExpressionSyntax) && !isAcceptedExpressionStmt(target)) {
                    diagnostics.report(DiagnosticFactory.parserInvalidExpressionStatement(target));
                }

                return new ExpressionStmt(target);
            }
        }
    }

    private boolean isAcceptedExpressionStmt(ExpressionSyntax expr) {
        if (expr instanceof InvocationExpr) {
            return true;
        }

        return expr instanceof AssignmentExpr;
    }

    private boolean isExpressionStart() {
        return tokenIs(TokenKind.LParen)
                || tokenIs(TokenKind.Identifier)
                || LiteralExpr.isLiteral(tokenKind())
                || SyntaxFacts.isUnaryOperator(tokenKind());
    }

    private boolean isStatementStart() {
        return switch (tokenKind()) {
            case LBrace, ReturnKeyword, IfKeyword, WhileKeyword, ForKeyword, VarKeyword, LetKeyword, ConstKeyword ->
                true;
            default ->
                isExpressionStart();
        };
    }

    private void skipToNextStatementStart() {
        while (!isAtEnd() && !isStatementStart() && !tokenIs(TokenKind.RBrace)) {
            nextToken();
        }
    }

    private VariableDeclaratorSyntax parseVariableDeclarator() {
        SyntaxToken modifierToken = matchTokenOptionnal(TokenKind.VarKeyword, TokenKind.LetKeyword, TokenKind.ConstKeyword);
        SimpleIdentifierSyntax name = parseSimpleIdentifier();

        TypeSyntax type = null;
        ExpressionSyntax initializer = null;
        if (matchTokenOptionnal(TokenKind.ColonEquals) != null) {
            // Support `var x := 5;` syntax for local variable declarations, in addition to the standard `var x: int = 5;`.
            initializer = parseExpr();
        } else {
            matchToken(TokenKind.Colon);
            type = parseType();

            if (matchTokenOptionnal(TokenKind.Equals) != null) {
                initializer = parseExpr();
            }
        }

        return new VariableDeclaratorSyntax(modifierToken, name, type, initializer);
    }

    private VariableDeclarationSyntax parseVariableDeclaration() {
        VariableDeclaratorSyntax variableDeclarator = parseVariableDeclarator();
        matchToken(TokenKind.Semicolon);

        return new VariableDeclarationSyntax(variableDeclarator);
    }

    private ImplBlockSyntax parseImplBlock() {
        matchToken(TokenKind.ImplKeyword);
        var typeSyntax = parseType();

        matchToken(TokenKind.LBrace);

        var startPos = token().span().start();

        var members = new ArrayList<MemberSyntax>();
        while (!tokenIs(TokenKind.EOF) && !tokenIs(TokenKind.RBrace)) {
            members.add(parseMember());
        }

        SourceSpan span = SourceSpan.fromBounds(lexer.source(), startPos, token().span().start());

        matchToken(TokenKind.RBrace);

        return new ImplBlockSyntax(typeSyntax, new SyntaxList<>(span, members));
    }

    private BlockStmt parseBlockStmt() {
        int startPos = token().span().start();
        matchToken(TokenKind.LBrace);

        var statements = new ArrayList<StatementSyntax>();
        while (!tokenIs(TokenKind.RBrace) && !isAtEnd()) {
            statements.add(parseStatement());
        }

        matchToken(TokenKind.RBrace);
        int endPos = token().span().start();
        SourceSpan span = SourceSpan.fromBounds(lexer.source(), startPos, endPos);
        return new BlockStmt(span, statements);
    }

    private StatementSyntax parseReturnStmt() {
        SyntaxToken returnToken = matchToken(TokenKind.ReturnKeyword);

        ExpressionSyntax returnValue = null;
        if (!tokenIs(TokenKind.Semicolon)) {
            returnValue = parseExpr();
        }

        matchToken(TokenKind.Semicolon);
        return new ReturnStmt(returnToken, returnValue);
    }

    private StatementSyntax parseIfStmt() {
        IfClauseSyntax ifClause = parseIfClause();

        ElseClauseSyntax elseClause = null;
        ArrayList<IfClauseSyntax> elseIfClauses = new ArrayList<>();

        int startPos = token().span().start();

        while (matchTokenOptionnal(TokenKind.ElseKeyword) != null) {
            if (!tokenIs(TokenKind.IfKeyword)) {
                elseClause = new ElseClauseSyntax(parseBlockStmt());
                break;
            }

            elseIfClauses.add(parseIfClause());
        }

        int endPos = token().span().start();
        SourceSpan span = SourceSpan.fromBounds(lexer.source(), startPos, endPos);
        return new IfStmt(ifClause, new SyntaxList<>(span, elseIfClauses), elseClause);
    }

    private StatementSyntax parseWhileStmt() {
        matchToken(TokenKind.WhileKeyword);
        matchToken(TokenKind.LParen);
        ExpressionSyntax condition = parseExpr();
        matchToken(TokenKind.RParen);
        BlockStmt body = parseBlockStmt();

        return new WhileStmt(condition, body);
    }

    private MethodDeclarationSyntax parseFunctionDecl() {
        matchToken(TokenKind.DefKeyword);

        SimpleIdentifierSyntax functionName = parseSimpleIdentifier();

        matchToken(TokenKind.LParen);

        int startPos = token().span().start();

        var parameters = new ArrayList<ParameterSyntax>();
        if (!tokenIs(TokenKind.RParen)) {
            do {
                ParameterSyntax parameter = parseParameter();
                parameters.add(parameter);
            } while (matchTokenOptionnal(TokenKind.Comma) != null);
        }

        SyntaxToken rParen = matchToken(TokenKind.RParen);

        TypeSyntax returnType = null;
        if (tokenIs(TokenKind.Colon)) {
            matchToken(TokenKind.Colon);
            returnType = parseType();
        }

        int endPos = rParen.span().start();
        SourceSpan parametersSpan = SourceSpan.fromBounds(lexer.source(), startPos, endPos);

        BlockStmt body = parseBlockStmt();

        return new MethodDeclarationSyntax(functionName, returnType, new SyntaxList<>(parametersSpan, parameters), body);
    }

    private StatementSyntax parseForStmt() {
        matchToken(TokenKind.ForKeyword);
        matchToken(TokenKind.LParen);

        VariableDeclaratorSyntax loopVariable = parseVariableDeclarator();
        matchToken(TokenKind.Semicolon);

        ExpressionSyntax conditionExpr = parseExpr();
        matchToken(TokenKind.Semicolon);

        ExpressionSyntax stepExpr = parseExpr();
        matchToken(TokenKind.RParen);

        BlockStmt body = parseBlockStmt();

        return new ForStmt(loopVariable, conditionExpr, stepExpr, body);
    }

    private CollectionDeclarationSyntax parseCollectionDecl() {
        matchToken(TokenKind.CollKeyword);
        SimpleIdentifierSyntax collectionName = parseSimpleIdentifier();
        ArrayList<FieldDeclarationSyntax> fields = new ArrayList<>();

        matchToken(TokenKind.LBrace);

        int startPos = token().span().start();

        while (!tokenIs(TokenKind.RBrace) && !isAtEnd()) {
            fields.add(parseField());
        }

        SyntaxToken rBrace = matchToken(TokenKind.RBrace);

        int endPos = rBrace.span().start();
        SourceSpan span = lexer.source().spanAt(startPos, Math.max(0, endPos - startPos));
        return new CollectionDeclarationSyntax(collectionName, new SyntaxList<>(span, fields));
    }

    private FieldDeclarationSyntax parseField() {
        TypeSyntax elementType = parseType();
        SimpleIdentifierSyntax elementName = parseSimpleIdentifier();
        matchToken(TokenKind.Semicolon);

        return new FieldDeclarationSyntax(elementName, elementType);
    }

    private VariableDeclarationStatement parseVariableDeclarationStatement() {
        VariableDeclaratorSyntax variableDeclarator = parseVariableDeclarator();
        matchToken(TokenKind.Semicolon);

        return new VariableDeclarationStatement(variableDeclarator.modifierToken(), variableDeclarator.identifier(), variableDeclarator.type(), variableDeclarator.initializer());
    }

    public ExpressionSyntax parseExpr() {
        if (!isExpressionStart()) {
            return new ErrorExpressionSyntax(consumeUnexpectedToken(token(), "an expression"));
        }

        return parseAssignmentExpr();
    }

    private ExpressionSyntax parseAssignmentExpr() {
        ExpressionSyntax left = parseBinaryExpr(0);

        if (SyntaxFacts.isAssignmentOperator(tokenKind())) {
            var op = nextToken();

            // right-associative: a = b = c
            ExpressionSyntax right = parseAssignmentExpr();

            left = new AssignmentExpr(left, op, right);
        }

        return left;
    }

    private ExpressionSyntax parseBinaryExpr(int parentPrecedence) {
        ExpressionSyntax left = parseUnaryExpr();

        while (true) {
            int precedence = SyntaxFacts.getBinaryOperatorPrecedence(tokenKind());
            if (precedence == 0) {
                break;
            }
            if (precedence < parentPrecedence) {
                break;
            }

            if (tokenIs(TokenKind.IsKeyword)) {
                SyntaxToken isKeyword = nextToken();
                TypeSyntax type = parseType();

                // Optional binding identifier: `x is Vector2 vx`
                SyntaxToken ident = null;
                if (tokenIs(TokenKind.Identifier)) {
                    ident = nextToken();
                }

                left = new TypeTestExpr(left, isKeyword, type, ident);
                continue;
            }

            SyntaxToken operator = nextToken();
            ExpressionSyntax right = parseBinaryExpr(precedence + 1);
            left = new BinaryExpr(operator, left, right);
        }

        return left;
    }

    private ExpressionSyntax parseUnaryExpr() {
        Token maybeUnaryOperator = token();

        if (SyntaxFacts.isUnaryOperator(maybeUnaryOperator.kind())) {
            var operator = nextToken();
            var operand = parseUnaryExpr();

            return new UnaryExpr(operator, operand);
        }

        return parsePostfixExpr();
    }

    private ExpressionSyntax parsePostfixExpr() {
        ExpressionSyntax expr = parsePrimaryExpr();

        while (true) {
            switch (tokenKind()) {
                case PlusPlus, MinusMinus -> {
                    var op = nextToken();
                    expr = new UnaryAssignmentExpr(op, expr);
                }
                case Dot -> {
                    expr = parseMemberAccessExpr(expr);
                }
                case LBracket -> {
                    expr = parseArrayAccessExpr(expr);
                }
                case LParen -> {
                    expr = parseFunctionCallExpr(expr);
                }
                default -> {
                    return expr;
                }
            }
        }
    }

    private ExpressionSyntax parseMemberAccessExpr(ExpressionSyntax object) {
        matchToken(TokenKind.Dot);
        SimpleIdentifierSyntax memberName = parseSimpleIdentifier();

        return new MemberAccessExpr(object, memberName);
    }

    private ExpressionSyntax parseArrayAccessExpr(ExpressionSyntax arrayExpr) {
        matchToken(TokenKind.LBracket);
        ExpressionSyntax index = parseExpr();
        matchToken(TokenKind.RBracket);

        return new ArrayAccessExpr(arrayExpr, index);
    }

    private ExpressionSyntax parsePrimaryExpr() {
        if (matchTokenOptionnal(TokenKind.LParen) != null) {
            ExpressionSyntax expr = parseExpr();
            matchToken(TokenKind.RParen);
            return expr;
        }

        if (tokenIs(TokenKind.Identifier)) {
            if (tokenAtIs(1, TokenKind.LBracket) && tokenAtIs(2, TokenKind.RBracket)) {
                return parseArrayConstructExpr();
            }

            var identifier = parseIdentifier();
            return new IdentifierExpr(identifier);
        }

        if (LiteralExpr.isLiteral(tokenKind())) {
            SyntaxToken literal = nextToken();
            return new LiteralExpr(literal);
        }

        return new ErrorExpressionSyntax(consumeUnexpectedToken(token(), "an expression"));
    }

    private IdentifierSyntax parseIdentifier() {
        IdentifierSyntax identifier;
        if (SyntaxFacts.isSpecialTypeKeyword(token().text())) {
            SyntaxToken keyword = nextToken();
            var kind = SyntaxFacts.getSpecialTypeKind(keyword.text());
            identifier = new SpecialTypeIdentifierSyntax(kind, keyword);
        } else {
            identifier = parseSimpleIdentifier();
        }

        int line = identifier.loc().line();
        while (line == token().loc().line() && tokenIs(TokenKind.DoubleColon)) {
            var doubleColon = matchToken(TokenKind.DoubleColon);

            SimpleIdentifierSyntax right;
            if (token().loc().line() != line) {
                diagnostics.report(DiagnosticFactory.parserExpectedIdentifierAfterDoubleColon(doubleColon));
                right = new SimpleIdentifierSyntax(synthesized(TokenKind.Identifier, doubleColon.token));
            } else {
                right = parseSimpleIdentifier();
            }

            identifier = new QualifiedIdentifierSyntax(identifier, doubleColon, right);
        }

        return identifier;
    }

    private SimpleIdentifierSyntax parseSimpleIdentifier() {
        SyntaxToken identifier = matchToken(TokenKind.Identifier);
        return new SimpleIdentifierSyntax(identifier);
    }

    private ExpressionSyntax parseArrayConstructExpr() {
        var elementType = parseType();

        matchToken(TokenKind.LBracket);
        matchToken(TokenKind.RBracket);
        matchToken(TokenKind.LParen);
        ExpressionSyntax lengthExpr = parseExpr();
        matchToken(TokenKind.RParen);

        return new ArrayLiteralExpression(elementType, lengthExpr);
    }

    private ExpressionSyntax parseFunctionCallExpr(ExpressionSyntax callee) {
        matchToken(TokenKind.LParen);

        var startPos = tokenPos();

        var args = new ArrayList<ExpressionSyntax>();
        if (!tokenIs(TokenKind.RParen)) {
            do {
                args.add(parseExpr());
            } while (matchTokenOptionnal(TokenKind.Comma) != null);
        }

        var endPos = tokenPos();
        SourceSpan argumentsSpan = SourceSpan.fromBounds(lexer.source(), startPos, endPos);

        matchToken(TokenKind.RParen);

        return new InvocationExpr(callee, new SyntaxList<>(argumentsSpan, args));
    }

    private IfClauseSyntax parseIfClause() {
        matchToken(TokenKind.IfKeyword);
        matchToken(TokenKind.LParen);
        ExpressionSyntax condition = parseExpr();
        matchToken(TokenKind.RParen);
        BlockStmt body = parseBlockStmt();

        return new IfClauseSyntax(condition, body);
    }

    private ParameterSyntax parseParameter() {
        boolean skipType = tokenIs(TokenKind.Identifier) && (tokenAtIs(1, TokenKind.Comma) || tokenAtIs(1, TokenKind.RParen));

        if (skipType) {
            var identifier = parseSimpleIdentifier();
            return ParameterSyntax.createReceiver(identifier);
        }

        TypeSyntax type = parseType();
        SimpleIdentifierSyntax identifier = parseSimpleIdentifier();

        return ParameterSyntax.createTyped(type, identifier);
    }

    private boolean isTypeStart() {
        return tokenIs(TokenKind.Identifier)
                || tokenIs(TokenKind.ArrayKeyword)
                || SyntaxFacts.isSpecialTypeKeyword(token().text());
    }

    private TypeSyntax parseType() {
        if (!isTypeStart()) {
            return new ErrorTypeSyntax(consumeUnexpectedToken(token(), "a type"));
        }

        TypeSyntax typeSyntax = parseIdentifiedType();

        while (tokenIs(TokenKind.LBracket) && tokenAtIs(1, TokenKind.RBracket) && !tokenAtIs(2, TokenKind.LParen)) {
            typeSyntax = parseArrayType(typeSyntax);
        }

        return typeSyntax;
    }

    private TypeSyntax parseIdentifiedType() {
        if (SyntaxFacts.isSpecialTypeKeyword(token().text())) {
            var keyword = nextToken();
            var kind = SyntaxFacts.getSpecialTypeKind(keyword.text());
            return new SpecialTypeSyntax(keyword, kind);
        }

        IdentifierSyntax identifier = parseIdentifier();
        return new IdentifierTypeSyntax(identifier);
    }

    private TypeSyntax parseArrayType(TypeSyntax elementType) {
        matchToken(TokenKind.LBracket);
        matchToken(TokenKind.RBracket);

        return new ArrayTypeSyntax(elementType);
    }

    private boolean isAtEnd() {
        return tokenIs(TokenKind.EOF);
    }

    private Token token() {
        return tokenAt(0);
    }

    private int tokenPos() {
        return token().span().start();
    }

    private TokenKind tokenKind() {
        return token().kind();
    }

    private boolean tokenIs(TokenKind kind) {
        return token().is(kind);
    }

    private boolean tokenAtIs(int offset, TokenKind kind) {
        return tokenAt(offset).is(kind);
    }

    private Token tokenAt(int offset) {
        ensureBuffered(offset);

        if (buffer.isEmpty()) {
            // Defensive: should never happen, but keep parsing errors explicit.
            SourceSpan span = new SourceSpan(lexer.source(), 0, 0);
            return new Token(TokenKind.EOF, "", span, null);
        }

        int pos = Math.min(this.position + offset, this.buffer.size() - 1);
        return this.buffer.get(pos);
    }

    private SyntaxToken nextToken() {
        Token token = token();

        this.position++;
        compactBufferIfNeeded();

        return new SyntaxToken(token);
    }

    private void skipToNextMemberOrImportStart() {
        while (!isAtEnd() && !isMemberStart() && !isImportDeclStart() && !tokenIs(TokenKind.RBrace)) {
            nextToken();
        }
    }

    private void skipToNextMemberStart() {
        while (!isAtEnd() && !isMemberStart() && !tokenIs(TokenKind.RBrace)) {
            nextToken();
        }
    }

    private void ensureBuffered(int offset) {
        int targetIndex = this.position + offset;
        while (this.buffer.size() <= targetIndex) {
            if (!this.buffer.isEmpty() && this.buffer.get(this.buffer.size() - 1).isEOF()) {
                break;
            }

            this.buffer.add(this.lexer.getNextSymbol());
        }
    }

    private void compactBufferIfNeeded() {
        if (this.position < BUFFER_COMPACT_THRESHOLD) {
            return;
        }

        if (this.position > this.buffer.size()) {
            this.position = this.buffer.size();
        }

        this.buffer.subList(0, this.position).clear();
        this.position = 0;
    }

    /**
     * Try to match the current token with the expected kind. If it matches,
     * consume the token and return it. If it doesn't match, return null without
     * consuming any token.
     *
     * @param kind the expected kind of the current token
     * @return the matched token if it matches the expected kind, or null if it
     * doesn't match if an error occurs during parsing (e.g., if the token
     * stream is exhausted)
     */
    private SyntaxToken matchTokenOptionnal(TokenKind... expectedKinds) {
        Token actual = token();
        for (TokenKind expected : expectedKinds) {
            if (actual.is(expected)) {
                return nextToken();
            }
        }

        return null;
    }

    /**
     * Try to match the current token with the expected kind. If it matches,
     * consume the token and return it. If it doesn't match, throw a
     * ParserException.
     *
     * @param expected the expected kind of the current token
     * @return the matched token if it matches the expected kind if the current
     * token does not match the expected kind or if an error occurs during
     * parsing (e.g., if the token stream is exhausted)
     */
    private SyntaxToken matchToken(TokenKind expected) {
        Token actual = token();
        if (!actual.is(expected)) {
            // Error recovery: for structural tokens, prefer inserting a synthesized token
            // instead of consuming the current token. This prevents cascading errors
            // and avoids getting stuck later on (e.g. missing parentheses/braces).
            if (canInsertMissingToken(expected)) {
                if (expected == TokenKind.Semicolon && buffer.size() >= 2) {
                    var previous = buffer.get(buffer.size() - 2);
                    var afterPrevious = SourceSpan.fromBounds(previous.span(), actual.span());
                    if (afterPrevious.length() > 0) {
                        diagnostics.report(DiagnosticFactory.parserMissingSemicolon(afterPrevious));
                    } else {
                        diagnostics.report(DiagnosticFactory.parserMissingSemicolon(actual.span()));
                    }
                } else if (!actual.is(TokenKind.BadToken)) {
                    diagnostics.report(DiagnosticFactory.parserUnexpectedToken(actual.span(), expected.toString(), actual.kind().name()));
                }

                return synthesized(expected, actual);
            }

            return consumeUnexpectedToken(actual, expected.toString());
        }

        return nextToken();
    }

    private boolean canInsertMissingToken(TokenKind expected) {
        return switch (expected) {
            case LParen, RParen, LBrace, RBrace, LBracket, RBracket, Semicolon, Comma, Arrow, Dot, Equals, Identifier ->
                true;
            default ->
                false;
        };
    }

    private SyntaxToken synthesized(TokenKind expectedKind, Token anchor) {
        int start = anchor.span().start();
        var span = lexer.source().spanAt(start, 0);
        var token = new Token(expectedKind, "", span, null);
        return new MissingSyntaxToken(token);
    }

    private SyntaxToken consumeUnexpectedToken(Token token, String expected) {
        if (!token.is(TokenKind.BadToken)) {
            diagnostics.report(DiagnosticFactory.parserUnexpectedToken(token.span(), expected, token.kind().name()));
        }

        // Never try to advance past EOF — it would not move the lookahead anyway.
        if (token.is(TokenKind.EOF)) {
            return new SyntaxToken(token);
        }

        return nextToken();
    }
}
