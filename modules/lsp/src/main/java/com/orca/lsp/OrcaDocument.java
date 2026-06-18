package com.orca.lsp;

import java.util.Map;
import java.util.Optional;

import com.orca.compiler.core.diagnostics.Diagnostic;
import com.orca.compiler.core.diagnostics.attachments.CodeSnippetAttachment;
import com.orca.compiler.core.diagnostics.attachments.SourceAttachment;
import com.orca.compiler.core.semantics.SemanticModel;
import com.orca.compiler.core.syntax.SyntaxNode;

public class OrcaDocument {

    private final String uri;
    private final OrcaProject project;
    private volatile String content;
    private volatile SemanticModel semanticModel;
    private volatile Map<SyntaxNode, OrcaSemanticToken> tokens;

    OrcaDocument(String uri, String content, OrcaProject project) {
        this.uri = uri;
        this.content = content;
        this.project = project;
    }

    public String uri() {
        return uri;
    }

    public String content() {
        return content;
    }

    public OrcaProject project() {
        return project;
    }

    public boolean isDiagnosticPartOfDocument(Diagnostic diagnostic) {
        if (diagnostic == null) {
            return false;
        }

        var snippetAttachment = diagnostic.getFirstAttachment(CodeSnippetAttachment.class);
        if (snippetAttachment != null) {
            var snippetSource = snippetAttachment.span().span().source();
            return snippetSource != null && snippetSource.name().equals(uri);
        }

        var sourceAttachment = diagnostic.getFirstAttachment(SourceAttachment.class);
        if (sourceAttachment != null) {
            var source = sourceAttachment.source();
            return source != null && source.name().equals(uri);
        }

        return false;
    }

    public Optional<LspSource> getSource() {
        return Optional.ofNullable(semanticModel)
                .map(SemanticModel::getSource)
                .map(source -> (LspSource) source);
    }

    public SemanticModel semanticModel() {
        return semanticModel;
    }

    public Map<SyntaxNode, OrcaSemanticToken> tokens() {
        return tokens;
    }

    public void setTokens(Map<SyntaxNode, OrcaSemanticToken> tokens) {
        this.tokens = tokens;
    }

    void updateContent(String newContent) {
        this.content = newContent;
        this.semanticModel = null;
    }

    void setSemanticModel(SemanticModel model) {
        this.semanticModel = model;
    }
}
