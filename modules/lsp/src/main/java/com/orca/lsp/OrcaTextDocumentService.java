package com.orca.lsp;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import org.eclipse.lsp4j.CompletionContext;
import org.eclipse.lsp4j.CompletionItem;
import org.eclipse.lsp4j.CompletionItemKind;
import org.eclipse.lsp4j.CompletionList;
import org.eclipse.lsp4j.CompletionParams;
import org.eclipse.lsp4j.CompletionTriggerKind;
import org.eclipse.lsp4j.DefinitionParams;
import org.eclipse.lsp4j.Diagnostic;
import org.eclipse.lsp4j.DidChangeTextDocumentParams;
import org.eclipse.lsp4j.DidCloseTextDocumentParams;
import org.eclipse.lsp4j.DidOpenTextDocumentParams;
import org.eclipse.lsp4j.DidSaveTextDocumentParams;
import org.eclipse.lsp4j.Hover;
import org.eclipse.lsp4j.HoverParams;
import org.eclipse.lsp4j.Location;
import org.eclipse.lsp4j.LocationLink;
import org.eclipse.lsp4j.MarkupContent;
import org.eclipse.lsp4j.MarkupKind;
import org.eclipse.lsp4j.Position;
import org.eclipse.lsp4j.PublishDiagnosticsParams;
import org.eclipse.lsp4j.SemanticTokens;
import org.eclipse.lsp4j.SemanticTokensParams;
import org.eclipse.lsp4j.jsonrpc.messages.Either;
import org.eclipse.lsp4j.services.TextDocumentService;

import com.orca.compiler.core.CompilerException;
import com.orca.compiler.core.boundtree.BoundExpression;
import com.orca.compiler.core.diagnostics.DiagnosticBuilder;
import com.orca.compiler.core.symbols.MethodSymbol;
import com.orca.compiler.core.symbols.NamespaceOrTypeSymbol;
import com.orca.compiler.core.symbols.NamespaceSymbol;
import com.orca.compiler.core.symbols.Symbol;
import com.orca.compiler.core.symbols.TypeSymbol;
import com.orca.compiler.core.symbols.ValueSymbol;
import com.orca.compiler.core.symbols.VariableSymbol;
import com.orca.compiler.core.symbols.sources.SourceSymbol;
import com.orca.compiler.core.syntax.SyntaxToken;
import com.orca.compiler.core.syntax.expressions.MemberAccessExpr;
import com.orca.compiler.core.syntax.nodes.IdentifierSyntax;
import com.orca.compiler.core.syntax.nodes.QualifiedIdentifierSyntax;
import com.orca.compiler.core.typesystem.LangType;
import com.orca.compiler.core.typesystem.NominalType;
import com.orca.lsp.Visitors.SemanticTokenCollector;

/**
 * Orchestrates LSP text-document requests by routing each file to its owning
 * {@link OrcaProject} and mapping compiler results to LSP responses. Document
 * state and compilation caches live in OrcaProject, not here.
 */
public class OrcaTextDocumentService implements TextDocumentService {

    private final OrcaLanguageServer server;

    public OrcaTextDocumentService(OrcaLanguageServer server) {
        this.server = server;
    }

    // -------------------------------------------------------------------------
    // Document lifecycle — delegate to the owning project
    // -------------------------------------------------------------------------
    @Override
    public void didOpen(DidOpenTextDocumentParams params) {
        String uri = params.getTextDocument().getUri();
        server.findProjectForFile(uri).openDocument(uri, params.getTextDocument().getText());
        publishDiagnostics(uri);
    }

    @Override
    public void didChange(DidChangeTextDocumentParams params) {
        String uri = params.getTextDocument().getUri();
        var changes = params.getContentChanges();
        if (!changes.isEmpty()) {
            server.findProjectForFile(uri).changeDocument(uri, changes.get(changes.size() - 1).getText());
        }
        publishDiagnostics(uri);
    }

    @Override
    public void didClose(DidCloseTextDocumentParams params) {
        String uri = params.getTextDocument().getUri();
        server.findProjectForFile(uri).closeDocument(uri);
        server.getClient().publishDiagnostics(new PublishDiagnosticsParams(uri, List.of()));
    }

    @Override
    public void didSave(DidSaveTextDocumentParams params) {
        String uri = params.getTextDocument().getUri();
        server.findProjectForFile(uri).invalidate(uri);
        publishDiagnostics(uri);
    }

    // -------------------------------------------------------------------------
    // LSP features — route through getDocument(), then map to responses
    // -------------------------------------------------------------------------
    @Override
    public CompletableFuture<Either<List<CompletionItem>, CompletionList>> completion(CompletionParams params) {
        try {
            String uri = params.getTextDocument().getUri();
            Position pos = params.getPosition();
            CompletionContext ctx = params.getContext();

            var doc = getDocument(uri);
            if (doc == null) {
                return CompletableFuture.completedFuture(Either.forLeft(List.of()));
            }

            var semanticModel = doc.semanticModel();
            if (semanticModel == null) {
                return CompletableFuture.completedFuture(Either.forLeft(List.of()));
            }

            var compilation = semanticModel.getCompilation();
            var source = (LspSource) semanticModel.getSource();

            if (ctx != null && ctx.getTriggerKind() == CompletionTriggerKind.TriggerCharacter
                    && ":".equals(ctx.getTriggerCharacter())) {
                int charIdx = pos.getCharacter();
                String line = source.getLine(pos.getLine() + 1);
                if (charIdx < 2 || line.charAt(charIdx - 2) != ':') {
                    return CompletableFuture.completedFuture(Either.forLeft(List.of()));
                }
            }

            var orcaPos = LspUtils.toOrcaPosition(source, pos);
            var syntaxNode = semanticModel.getSyntaxAtLocation(orcaPos);
            if (syntaxNode instanceof SyntaxToken token) {
                syntaxNode = token.parent();
            }
            if (syntaxNode == null) {
                return CompletableFuture.completedFuture(Either.forLeft(List.of()));
            }

            System.out.println("Completion at " + pos.getLine() + ":" + pos.getCharacter()
                    + " node=" + syntaxNode.getClass().getSimpleName());

            switch (syntaxNode) {
                case IdentifierSyntax identifierSyntax -> {
                    identifierSyntax = getActualIdentifier(identifierSyntax);
                    if (!(identifierSyntax instanceof QualifiedIdentifierSyntax qualified)) {
                        return CompletableFuture.completedFuture(Either.forLeft(List.of()));
                    }

                    identifierSyntax = qualified.left();
                    var parentSymbolInfo = semanticModel.getSymbolInfo(identifierSyntax);
                    if (parentSymbolInfo.isFailure()) {
                        return CompletableFuture.completedFuture(Either.forLeft(List.of()));
                    }

                    if (!(parentSymbolInfo.symbol() instanceof NamespaceOrTypeSymbol parentNs)) {
                        return CompletableFuture.completedFuture(Either.forLeft(List.of()));
                    }

                    return CompletableFuture.completedFuture(Either.forLeft(
                            toCompletionItems(parentNs.getMembers().stream()
                                    .filter(Symbol::canBeAccessedThroughStatic).toList())));
                }
                case MemberAccessExpr memberAccessExpr -> {
                    compilation.ensureDeclarationsComplete();
                    var boundExpr = (BoundExpression) semanticModel.bind(memberAccessExpr.instanceExpr());
                    var boundExprType = boundExpr.type();
                    if (boundExprType == null) {
                        return CompletableFuture.completedFuture(Either.forLeft(List.of()));
                    }
                    TypeSymbol exprSymbol;
                    try {
                        exprSymbol = compilation.getTypeRegistry().bindType(boundExprType);
                    } catch (Exception e) {
                        exprSymbol = null;
                    }
                    if (exprSymbol == null) {
                        return CompletableFuture.completedFuture(Either.forLeft(List.of()));
                    }
                    return CompletableFuture.completedFuture(Either.forLeft(
                            toCompletionItems(exprSymbol.getMembers().stream()
                                    .filter(Symbol::canBeAccessedThroughInstance).toList())));
                }
                default -> {
                }
            }

            return CompletableFuture.completedFuture(Either.forLeft(List.of()));

        } catch (CompilerException e) {
            var item = new CompletionItem("Error: " + e.diagnostic().message());
            item.setDetail("An error occurred while computing completions");
            return CompletableFuture.completedFuture(Either.forLeft(List.of(item)));
        }
    }

    @Override
    public CompletableFuture<Hover> hover(HoverParams params) {
        try {
            String uri = params.getTextDocument().getUri();
            var doc = getDocument(uri);
            if (doc == null) {
                return CompletableFuture.completedFuture(null);
            }

            var sourceOpt = doc.getSource();
            if (sourceOpt.isEmpty()) {
                System.out.println("No source found for document " + uri);
                return CompletableFuture.completedFuture(null);
            }

            var source = sourceOpt.get();
            var semanticModel = doc.semanticModel();
            var orcaPos = LspUtils.toOrcaPosition(source, params.getPosition());
            var syntaxNode = semanticModel.getSyntaxAtLocation(orcaPos);

            var symbolInfo = SymbolResolver.getSymbolInfo(semanticModel, syntaxNode);
            if (symbolInfo != null && symbolInfo.isSuccess()) {
                var symbol = symbolInfo.symbol();
                var markup = getMarkupContentForSymbol(symbol);
                return CompletableFuture.completedFuture(new Hover(markup, LspUtils.toLspRange(symbolInfo.syntax().span())));
            }
            // if (syntaxNode instanceof SyntaxToken token) {
            //     syntaxNode = token.parent();
            // }

            // if (syntaxNode instanceof IdentifierSyntax identifierSyntax) {
            //     try {
            //         identifierSyntax = getActualIdentifier(identifierSyntax);
            //         var symbolInfo = semanticModel.getSymbolInfo(identifierSyntax);
            //         if (symbolInfo.isSuccess()) {
            //             return CompletableFuture.completedFuture(new Hover(getMarkupContentForSymbol(symbolInfo.symbol()), LspUtils.toLspRange(symbolInfo.syntax().span())));
            //         }
            //         System.out.println("Failed to get symbol info for hover: " + identifierSyntax.text() + " at " + identifierSyntax.span().loc() + " reason: " + symbolInfo.failureReason());
            //         return CompletableFuture.completedFuture(null);
            //     } catch (CompilerException e) {
            //         var markup = new MarkupContent(MarkupKind.MARKDOWN, "**Error:** " + e.diagnostic().message());
            //         var attachment = e.diagnostic().getFirstAttachment(CodeSnippetAttachment.class);
            //         if (attachment != null) {
            //             markup.setValue(markup.getValue() + "\n\n```\n" + attachment.label() + "\n```");
            //         }
            //         return CompletableFuture.completedFuture(new Hover(markup, LspUtils.toLspRange(identifierSyntax.span())));
            //     } catch (Exception e) {
            //         return CompletableFuture.completedFuture(new Hover(
            //                 new MarkupContent(
            //                         MarkupKind.MARKDOWN,
            //                         "**Error:** " + e.getMessage()
            //                 ),
            //                 LspUtils.toLspRange(identifierSyntax.span())
            //         )
            //         );
            //     }
            // }
            return CompletableFuture.completedFuture(null);
        } catch (Exception e) {
            return CompletableFuture.completedFuture(null);
        }
    }

    @Override
    public CompletableFuture<Either<List<? extends Location>, List<? extends LocationLink>>> definition(DefinitionParams params) {
        try {
            String uri = params.getTextDocument().getUri();
            var doc = getDocument(uri);
            if (doc == null) {
                return CompletableFuture.completedFuture(null);
            }

            var sourceOpt = doc.getSource();
            if (sourceOpt.isEmpty()) {
                System.out.println("No source found for document " + uri);
                return CompletableFuture.completedFuture(null);
            }

            var source = sourceOpt.get();
            var semanticModel = doc.semanticModel();
            var orcaPos = LspUtils.toOrcaPosition(source, params.getPosition());
            var syntaxNode = semanticModel.getSyntaxAtLocation(orcaPos);

            if (syntaxNode instanceof SyntaxToken token) {
                syntaxNode = token.parent();
            }

            if (syntaxNode instanceof IdentifierSyntax identifierSyntax) {
                identifierSyntax = getActualIdentifier(identifierSyntax);
                var symbolInfo = semanticModel.getSymbolInfo(identifierSyntax);
                if (symbolInfo.isSuccess()) {
                    var symbol = symbolInfo.symbol();
                    if (symbol instanceof SourceSymbol sourceSymbol) {
                        var span = sourceSymbol.span();
                        return CompletableFuture.completedFuture(Either.forLeft(List.of(LspUtils.toLspLocation(span))));
                    }
                } else {
                    System.out.println("Failed to get symbol info for definition: " + identifierSyntax.text() + " at " + identifierSyntax.span().loc() + " reason: " + symbolInfo.failureReason());
                    return CompletableFuture.completedFuture(null);
                }
            }
            return CompletableFuture.completedFuture(null);
        } catch (CompilerException e) {
            return CompletableFuture.completedFuture(null);
        }
    }

    @Override
    public CompletableFuture<SemanticTokens> semanticTokensFull(SemanticTokensParams params) {
        var data = new ArrayList<Integer>();
        try {
            String uri = params.getTextDocument().getUri();
            var doc = getDocument(uri);
            if (doc == null) {
                return CompletableFuture.completedFuture(new SemanticTokens(data));
            }

            var semanticModel = doc.semanticModel();
            var tokens = SemanticTokenCollector.collectTokens(semanticModel, semanticModel.getSyntaxTree().root());

            tokens.forEach(t -> t.appendTo(data));
            return CompletableFuture.completedFuture(new SemanticTokens(data));
        } catch (Exception e) {
            System.out.println("semanticTokensFull error: " + e.getClass().getSimpleName() + ": " + e.getMessage());
            return CompletableFuture.completedFuture(new SemanticTokens(data));
        }
    }

    // -------------------------------------------------------------------------
    // Diagnostics
    // -------------------------------------------------------------------------
    private void publishDiagnostics(String uri) {
        var lspDiagnostics = new ArrayList<Diagnostic>();
        try {
            var doc = getDocument(uri);
            if (doc == null || doc.semanticModel() == null) {
                return;
            }

            var semanticModel = doc.semanticModel();
            var compilation = semanticModel.getCompilation();
            var context = compilation.getContext();
            try {
                compilation.getBoundProgram();
            } catch (CompilerException e) {
                context.diagnostics().report(e.diagnostic());
            }

            for (var diagnostic : context.diagnostics()) {
                if (!doc.isDiagnosticPartOfDocument(diagnostic)) {
                    continue;
                }

                lspDiagnostics.add(LspUtils.toLspDiagnostic(diagnostic));
            }
        } catch (Exception e) {
            lspDiagnostics.add(LspUtils.toLspDiagnostic(DiagnosticBuilder.create()
                    .withMessage("Internal LSP error: " + e.getMessage())
                    .build()
            ));
        }
        server.getClient().publishDiagnostics(new PublishDiagnosticsParams(uri, lspDiagnostics));
    }

    // -------------------------------------------------------------------------
    // Helpers
    // -------------------------------------------------------------------------
    private OrcaDocument getDocument(String uri) {
        return server.findProjectForFile(uri).getOrComputeDocument(uri, server.getManualClasspath());
    }

    private List<CompletionItem> toCompletionItems(List<Symbol> members) {
        var items = new ArrayList<CompletionItem>();
        for (var member : members) {
            var item = new CompletionItem(member.name());
            item.setKind(completionKindFor(member));
            item.setDocumentation(getMarkupContentForSymbol(member));
            items.add(item);
        }
        return items;
    }

    private MarkupContent getMarkupContentForSymbol(Symbol symbol) {
        var kind = completionKindFor(symbol);
        var sb = new StringBuilder();
        switch (symbol) {
            case MethodSymbol m -> {
                sb.append("def ")
                        .append(formatTypeName(m.returnType()))
                        .append(" ")
                        .append(m.name())
                        .append("(");

                for (var p : m.parameters()) {
                    sb.append(formatTypeName(p.type()))
                            .append(" ")
                            .append(p.name());
                    if (p != m.parameters().getLast()) {
                        sb.append(", ");
                    }
                }
                sb.append(")");
            }
            case TypeSymbol t -> {
                if (t.type() instanceof NominalType) {
                    var underlying = t.type().unwrap();
                    switch (underlying.kind()) {
                        case COLLECTION ->
                            sb.append("coll ");
                        default ->
                            sb.append("type ");
                    }
                }
                sb.append(t.name());
            }
            case VariableSymbol v -> {
                if (v.isCompileTimeConstant()) {
                    sb.append("const ");
                } else if (v.isImmutable()) {
                    sb.append("let ");
                } else {
                    sb.append("var ");
                }

                sb.append(v.name()).append(": ").append(formatTypeName(v.type()));
            }
            case ValueSymbol v ->
                sb.append(formatTypeName(v.type())).append(" ").append(symbol.name());
            case NamespaceSymbol ns ->
                sb.append("package ").append(formatSymbolFullName(ns));
            default -> {
            }
        }

        buildOrcaCodeSnippetMarkdown(sb);
        sb.append("\n\n*Kind: ").append(kind).append("*\n\n");
        sb.append(formatSymbolFullName(symbol)).append("\n\n");

        return new MarkupContent(MarkupKind.MARKDOWN, sb.toString());
    }

    private String formatSymbolFullName(Symbol symbol) {
        String name;
        if (symbol instanceof TypeSymbol typeSymbol) {
            name = formatTypeName(typeSymbol.type());
        } else {
            name = symbol.name();
        }

        return name.replace(".", "::");
    }

    private String formatTypeName(LangType type) {
        if (type instanceof NominalType nominal) {
            var name = nominal.name();
            var lastDot = name.lastIndexOf('.');
            if (lastDot >= 0) {
                name = name.substring(lastDot + 1);
            }
            return name;
        }

        return type.displayName();
    }

    private StringBuilder buildOrcaCodeSnippetMarkdown(StringBuilder sb) {
        if (sb.length() == 0) {
            sb.append("*No code snippet available*");
        } else {
            sb.insert(0, "```orca\n");
            sb.append("\n```");
        }

        return sb;
    }

    private IdentifierSyntax getActualIdentifier(IdentifierSyntax syntax) {
        if (syntax.parent() instanceof QualifiedIdentifierSyntax qualified && syntax == qualified.right()) {
            return qualified;
        }
        return syntax;
    }

    private CompletionItemKind completionKindFor(Symbol symbol) {
        return switch (symbol.kind()) {
            case MISSING ->
                CompletionItemKind.Text;
            case VARIABLE ->
                symbol.isConstantVariable() ? CompletionItemKind.Constant : CompletionItemKind.Variable;
            case CONSTRUCTOR ->
                CompletionItemKind.Constructor;
            case FIELD ->
                CompletionItemKind.Field;
            case METHOD ->
                CompletionItemKind.Method;
            case GLOBAL_NAMESPACE, NAMESPACE ->
                CompletionItemKind.Module;
            case TYPE ->
                CompletionItemKind.Class;
            case METHOD_GROUP ->
                CompletionItemKind.Method;
            case PARAMETER ->
                CompletionItemKind.Variable;
        };
    }
}
