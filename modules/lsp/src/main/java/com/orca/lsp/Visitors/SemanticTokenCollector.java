package com.orca.lsp.Visitors;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;

import com.orca.compiler.core.semantics.SemanticModel;
import com.orca.compiler.core.semantics.SymbolInfo;
import com.orca.compiler.core.symbols.Symbol;
import com.orca.compiler.core.symbols.TypeSymbol;
import com.orca.compiler.core.symbols.ValueSymbol;
import com.orca.compiler.core.syntax.SyntaxNode;
import com.orca.compiler.core.syntax.SyntaxWalker;
import com.orca.compiler.core.syntax.nodes.SimpleIdentifierSyntax;
import com.orca.compiler.core.syntax.nodes.SpecialTypeIdentifierSyntax;
import com.orca.compiler.core.text.SourceSpan;
import com.orca.lsp.OrcaSemanticToken;
import com.orca.lsp.SymbolResolver;

public final class SemanticTokenCollector extends SyntaxWalker {

    private int gracefulFailureCounter;
    private int unexpectedExceptionCounter;

    private SourceSpan lastSpan = null;

    private final SemanticModel semanticModel;
    private final List<OrcaSemanticToken> tokens;

    private SemanticTokenCollector(SemanticModel semanticModel, List<OrcaSemanticToken> tokens) {
        this.semanticModel = semanticModel;
        this.tokens = tokens;
    }

    public static List<OrcaSemanticToken> collectTokens(SemanticModel semanticModel, SyntaxNode node) {
        System.out.println("Collecting semantic tokens for node: " + node.getClass().getSimpleName() + " at " + node.span().loc());

        var tokens = new ArrayList<OrcaSemanticToken>();
        var collector = new SemanticTokenCollector(semanticModel, tokens);
        collector.walk(node);

        System.out.println("Collected " + tokens.size() + " semantic tokens. " + collector.gracefulFailureCounter + " failed.\n");
        return tokens;
    }

    @Override
    protected void defaultVisit(SyntaxNode node) {
        if (node.hasError()) {
            return;
        }

        super.defaultVisit(node);
    }

    @Override
    public void visitSimpleIdentifierSyntax(SimpleIdentifierSyntax syntax) {
        try {
            var symbolInfo = SymbolResolver.getSymbolInfo(semanticModel, syntax);
            if (symbolInfo.isFailure()) {
                handleFailedResolution(symbolInfo);
                return;
            }

            addTokenForSymbol(symbolInfo.symbol(), symbolInfo.syntax().span());
        } catch (Exception e) {
            handleExceptionWhileResolving(syntax.text(), syntax.span(), e);
        }
    }

    // @Override
    // public void visitQualifiedIdentifierSyntax(QualifiedIdentifierSyntax syntax) {
    //     syntax.left().accept(this);
    //     // The right part of a qualified identifier should not be visited alone.
    //     try {
    //         var symbolInfo = semanticModel.getSymbolInfo(syntax);
    //         if (symbolInfo.isFailure()) {
    //             handleFailedResolution(symbolInfo);
    //             return;
    //         }
    //         addTokenForSymbol(symbolInfo.symbol(), symbolInfo.syntax().span());
    //     } catch (Exception e) {
    //         handleExceptionWhileResolving(syntax.text(), syntax.span(), e);
    //     }
    // }
    // @Override
    // public void visitAssignmentExpr(AssignmentExpr syntax) {
    //     int beforeTokenCount = tokens.size();
    //     syntax.target().accept(this);
    //     if (tokens.size() > beforeTokenCount) {
    //         // If the left-hand side of the assignment produced a token, mark it as a write
    //         var lastToken = tokens.get(tokens.size() - 1);
    //         lastToken.addModifier(OrcaSemanticToken.Modifier.WRITE);
    //     }
    //     syntax.initializer().accept(this);
    // }
    // @Override
    // public void visitMemberAccessExpr(MemberAccessExpr syntax) {
    //     try {
    //         syntax.instanceExpr().accept(this);
    //         var boundMemberAccess = (BoundReferenceExpr.MemberAccessRef) semanticModel.bind(syntax);
    //         var memberSymbol = boundMemberAccess.getFirstReferencedSymbol();
    //         if (memberSymbol == null || memberSymbol.isMissing()) {
    //             System.out.println("Failed to resolve symbol for member access: " + syntax.memberIdentifier().text() + " at " + syntax.memberIdentifier().span().loc());
    //             return;
    //         }
    //         var span = syntax.memberIdentifier().span();
    //         addTokenForSymbol(memberSymbol, span);
    //     } catch (CompilerException e) {
    //         handleExceptionWhileResolving(syntax.text(), syntax.span(), e);
    //     }
    // }
    @Override
    public void visitSpecialTypeIdentifier(SpecialTypeIdentifierSyntax syntax) {
        var lineAndChar = getLineAndChar(syntax.span());
        var semanticToken = new OrcaSemanticToken(OrcaSemanticToken.Type.KEYWORD, EnumSet.noneOf(OrcaSemanticToken.Modifier.class), lineAndChar.line(), lineAndChar.character(), syntax.span().length());

        tokens.add(semanticToken);
    }

    private void addTokenForSymbol(Symbol symbol, SourceSpan span) {
        if (span.equals(lastSpan)) {
            System.out.println("[token] duplicate span for " + symbol.name() + " at " + span.loc());
            return;
        }

        var modifiers = getModifiersForSymbol(symbol);
        var tokenType = getTokenTypeForSymbol(symbol);

        if (tokenType == null) {
            System.out.println("[token] skipped (no token type)");
            return;
        }

        var lineAndChar = getLineAndChar(span);
        var semanticToken = new OrcaSemanticToken(tokenType, modifiers, lineAndChar.line(), lineAndChar.character(), span.length());
        tokens.add(semanticToken);

        lastSpan = span;
    }

    private void handleFailedResolution(SymbolInfo symbolInfo) {
        if (gracefulFailureCounter < 5) {
            System.err.println("Failed to resolve symbol for " + symbolInfo.syntax() + ": " + symbolInfo.failureReason());
        }
        gracefulFailureCounter++;
    }

    private void handleExceptionWhileResolving(String name, SourceSpan span, Exception e) {
        System.err.println("Exception occurred while resolving symbol for " + name + " at " + span.loc() + ": " + e.getClass().getSimpleName());
        e.printStackTrace(System.err);
        unexpectedExceptionCounter++;
    }

    private LineAndChar getLineAndChar(SourceSpan span) {
        int lineDelta, colDelta;
        if (lastSpan == null) {
            // 1-based → 0-based
            lineDelta = span.loc().line() - 1;
            colDelta = span.loc().col() - 1;
        } else {
            lineDelta = span.loc().line() - lastSpan.loc().line();
            colDelta = (lineDelta == 0)
                    ? span.loc().col() - lastSpan.loc().col()
                    : span.loc().col() - 1;
        }

        return new LineAndChar(lineDelta, colDelta);
    }

    private record LineAndChar(int line, int character) {

    }

    private static OrcaSemanticToken.Type getTokenTypeForSymbol(Symbol symbol) {
        return switch (symbol) {
            case TypeSymbol typeSymbol -> {
                var underlying = typeSymbol.type().unwrap();

                if (underlying.isNumeric()) {
                    yield OrcaSemanticToken.Type.NUMBER;
                }
                if (underlying.isString()) {
                    yield OrcaSemanticToken.Type.STRING;
                }

                yield OrcaSemanticToken.Type.TYPE;
            }
            default ->
                switch (symbol.kind()) {
                    case NAMESPACE, GLOBAL_NAMESPACE ->
                        OrcaSemanticToken.Type.NAMESPACE;
                    case TYPE ->
                        OrcaSemanticToken.Type.CLASS;
                    case PARAMETER ->
                        OrcaSemanticToken.Type.PARAMETER;
                    case VARIABLE, FIELD ->
                        OrcaSemanticToken.Type.VARIABLE;
                    case METHOD, CONSTRUCTOR ->
                        OrcaSemanticToken.Type.METHOD;
                    case MISSING, METHOD_GROUP ->
                        null;
                };
        };
    }

    private static EnumSet<OrcaSemanticToken.Modifier> getModifiersForSymbol(Symbol symbol) {
        var modifiers = EnumSet.noneOf(OrcaSemanticToken.Modifier.class);
        if (symbol.isStatic()) {
            modifiers.add(OrcaSemanticToken.Modifier.STATIC);
        }
        if (symbol instanceof ValueSymbol valueSymbol && valueSymbol.isCompileTimeConstant()) {
            modifiers.add(OrcaSemanticToken.Modifier.IMMUTABLE);
        }
        return modifiers;
    }
}
