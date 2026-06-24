package com.orca.lsp;

import com.orca.compiler.core.boundtree.expressions.BoundConstructObjectExpr;
import com.orca.compiler.core.boundtree.expressions.BoundMethodCallExpr;
import com.orca.compiler.core.boundtree.expressions.BoundReferenceExpr;
import com.orca.compiler.core.semantics.SemanticModel;
import com.orca.compiler.core.semantics.SymbolInfo;
import com.orca.compiler.core.symbols.Symbol;
import com.orca.compiler.core.symbols.TypeSymbol;
import com.orca.compiler.core.syntax.SyntaxNode;
import com.orca.compiler.core.syntax.SyntaxToken;
import com.orca.compiler.core.syntax.declarations.FieldDeclarationSyntax;
import com.orca.compiler.core.syntax.expressions.InvocationExpr;
import com.orca.compiler.core.syntax.expressions.MemberAccessExpr;
import com.orca.compiler.core.syntax.members.CollectionDeclarationSyntax;
import com.orca.compiler.core.syntax.nodes.IdentifierSyntax;
import com.orca.compiler.core.syntax.nodes.QualifiedIdentifierSyntax;

public final class SymbolResolver {

    private static final Resolver[] RESOLVERS = new Resolver[]{
        SymbolResolver::tryGetFieldSymbolInfo,
        SymbolResolver::tryGetInvocationExprSymbolInfo,
        SymbolResolver::tryGetMemberAccessExprSymbolInfo,
        SymbolResolver::tryGetExactIdentifierSymbolInfo
    };

    private SymbolResolver() {
    }

    public static SymbolInfo getSymbolInfo(SemanticModel semanticModel, SyntaxNode syntax) {
        System.out.println("[SymbolResolver] attempting to resolve symbol for syntax: " + syntax.getClass().getSimpleName() + " at " + syntax.span().loc());

        if (syntax instanceof SyntaxToken token) {
            syntax = token.parent();
            System.out.println("[SymbolResolver] adjusted syntax to parent: " + syntax.getClass().getSimpleName() + " at " + syntax.span().loc());
        }

        for (Resolver resolver : RESOLVERS) {
            try {
                var symbolInfo = resolver.get(semanticModel, syntax);
                if (symbolInfo != null) {
                    System.out.println("[SymbolResolver] resolved symbol: " + symbolInfo.symbol().displayName() + " for syntax: " + syntax.getClass().getSimpleName() + " at " + syntax.span().loc());
                    return symbolInfo;
                }
            } catch (Exception e) {
                System.err.println("[SymbolResolver] resolver failed for "
                        + syntax.getClass().getSimpleName() + ": " + e.getMessage());
            }
        }

        System.err.println("[SymbolResolver] failed to resolve symbol for syntax: " + syntax.getClass().getSimpleName() + " at " + syntax.span().loc());
        return null;
    }

    private static SymbolInfo tryGetFieldSymbolInfo(SemanticModel semanticModel, SyntaxNode syntax) {
        var rootSyntax = getRootSyntax(syntax);
        if (!(rootSyntax instanceof FieldDeclarationSyntax fieldDeclarationSyntax)) {
            return null;
        }

        System.out.println("[SymbolResolver] trying to resolve symbol for FieldDeclarationSyntax at " + fieldDeclarationSyntax.span().loc());

        var collectionDeclarationSyntax = fieldDeclarationSyntax.getFirstAncestorOfType(CollectionDeclarationSyntax.class);
        if (collectionDeclarationSyntax == null) {
            System.err.println("[SymbolResolver] FieldDeclarationSyntax is not part of any CollectionDeclarationSyntax at " + fieldDeclarationSyntax.span().loc());
            return null;
        }

        var declaredCollectionOpt = semanticModel.getDeclaredSymbol(collectionDeclarationSyntax);
        if (!declaredCollectionOpt.isPresent()) {
            System.err.println("[SymbolResolver] CollectionDeclarationSyntax does not have a declared symbol at " + collectionDeclarationSyntax.span().loc());
            return null;
        }

        if (!(declaredCollectionOpt.get() instanceof TypeSymbol typeSymbol)) {
            System.err.println("[SymbolResolver] Declared symbol for CollectionDeclarationSyntax is not a TypeSymbol at " + collectionDeclarationSyntax.span().loc());
            return null;
        }

        String fieldName = fieldDeclarationSyntax.identifier().text();
        var fieldSymbol = typeSymbol.getField(fieldName);

        if (fieldSymbol == null || fieldSymbol.isMissing()) {
            System.err.println("[SymbolResolver] Field '" + fieldName + "' not found in TypeSymbol '" + typeSymbol.displayName() + "' at " + fieldDeclarationSyntax.span().loc());
            return new SymbolInfo(Symbol.missing(fieldName), fieldDeclarationSyntax.identifier());
        }

        return new SymbolInfo(fieldSymbol, fieldDeclarationSyntax.identifier());
    }

    private static SymbolInfo tryGetInvocationExprSymbolInfo(SemanticModel semanticModel, SyntaxNode syntax) {
        var rootSyntax = getRootSyntax(syntax);
        if (!(rootSyntax instanceof InvocationExpr invocationExpr)) {
            return null;
        }

        System.out.println("[SymbolResolver] trying to resolve symbol for InvocationExpr at " + invocationExpr.span().loc());

        var binder = semanticModel.getBinder(invocationExpr);
        var boundNode = binder.bind(invocationExpr);

        return switch (boundNode) {
            case BoundMethodCallExpr methodCall -> {
                var methodSymbol = methodCall.methodSymbol;
                if (methodSymbol == null || methodSymbol.isMissing()) {
                    yield null;
                }

                yield new SymbolInfo(methodSymbol, syntax);
            }
            case BoundConstructObjectExpr constructObject -> {
                var constructorSymbol = constructObject.getConstructor();
                if (constructorSymbol == null || constructorSymbol.isMissing()) {
                    yield null;
                }

                yield new SymbolInfo(constructorSymbol, syntax);
            }
            default -> {
                yield null;
            }
        };
    }

    private static SymbolInfo tryGetMemberAccessExprSymbolInfo(SemanticModel semanticModel, SyntaxNode syntax) {
        var rootSyntax = getRootSyntax(syntax);
        if (!(rootSyntax instanceof MemberAccessExpr memberAccessExpr)) {
            return null;
        }

        System.out.println("[SymbolResolver] trying to resolve symbol for MemberAccessExpr at " + memberAccessExpr.span().loc());

        var binder = semanticModel.getBinder(memberAccessExpr);
        var boundNode = binder.bind(memberAccessExpr);
        if (!(boundNode instanceof BoundReferenceExpr.MemberAccessRef boundMemberAccessRef)) {
            return null;
        }

        var memberSymbol = boundMemberAccessRef.getFirstReferencedSymbol();
        if (memberSymbol == null || memberSymbol.isMissing()) {
            return null;
        }

        return new SymbolInfo(memberSymbol, memberAccessExpr.memberIdentifier());
    }

    private static SymbolInfo tryGetExactIdentifierSymbolInfo(SemanticModel semanticModel, SyntaxNode syntax) {
        var rootSyntax = getRootSyntax(syntax);
        if (!(rootSyntax instanceof IdentifierSyntax identifierSyntax)) {
            return null;
        }

        System.out.println("[SymbolResolver] trying to resolve symbol for IdentifierSyntax: " + identifierSyntax.text());

        return getSymbolInfoPrivate(semanticModel, identifierSyntax);
    }

    private static SyntaxNode getRootSyntax(SyntaxNode syntax) {
        return switch (syntax.parent()) {
            case QualifiedIdentifierSyntax qualified when qualified.right() == syntax ->
                getRootSyntax(qualified);
            case MemberAccessExpr memberAccess when memberAccess.memberIdentifier() == syntax ->
                getRootSyntax(memberAccess);
            case InvocationExpr invocation when invocation.callee() == syntax ->
                getRootSyntax(invocation);
            case FieldDeclarationSyntax fieldDeclaration when fieldDeclaration.identifier() == syntax ->
                getRootSyntax(fieldDeclaration);
            default ->
                syntax;
        };
    }

    private static SymbolInfo getSymbolInfoPrivate(SemanticModel semanticModel, IdentifierSyntax syntax) {
        try {
            return semanticModel.getSymbolInfo(syntax);
        } catch (Exception e) {
            System.err.println("[SymbolResolver] unexpected exception for "
                    + syntax.getClass().getSimpleName() + " at " + syntax.span().loc()
                    + ": " + e.getMessage());
            return null;
        }
    }

    @FunctionalInterface
    interface Resolver {

        SymbolInfo get(SemanticModel semanticModel, SyntaxNode syntax);
    }
}
