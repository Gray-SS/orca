package com.orca.compiler.core.syntax;

import com.orca.compiler.core.diagnostics.DiagnosticCollector;
import com.orca.compiler.core.lexer.Lexer;
import com.orca.compiler.core.text.TextSource;

public class SyntaxTree {

    private final TextSource source;
    private final SyntaxNode root;

    private final DiagnosticCollector diagnostics;

    public SyntaxTree(DiagnosticCollector diagnostics, SyntaxNode root) {
        this.root = root;
        this.source = root.source();
        this.diagnostics = diagnostics;
        this.initialize(root);
    }

    public SyntaxNode getSyntaxAtPosition(int position) {
        return getSyntaxAtPosition(this.root, position);
    }

    /**
     * Like getSyntaxAtPosition but also tries position-1 as fallback. Useful
     * for completion where the cursor sits just past the last typed character.
     */
    public SyntaxNode getSyntaxAtPositionForCompletion(int position) {
        var result = getSyntaxAtPosition(this.root, position);
        if (result == this.root && position > 0) {
            var fallback = getSyntaxAtPosition(this.root, position - 1);
            if (fallback != null && fallback != this.root) {
                return fallback;
            }
        }
        return result != null ? result : this.root;
    }

    public static SyntaxTree parse(TextSource source) {
        var diagnostics = new DiagnosticCollector();
        Parser parser = new Parser(new Lexer(source, diagnostics), diagnostics);
        return new SyntaxTree(diagnostics, parser.parseCompilationUnit());
    }

    public DiagnosticCollector getDiagnostics() {
        return DiagnosticCollector.copyOf(diagnostics);
    }

    public TextSource source() {
        return this.source;
    }

    public SyntaxNode root() {
        return this.root;
    }

    private void initialize(SyntaxNode node) {
        for (var child : node.children()) {
            if (child != null) {
                child.setParent(node);
                initialize(child);
            }
        }
    }

    private SyntaxNode getSyntaxAtPosition(SyntaxNode node, int position) {
        if (node.span().contains(position)) {
            for (var child : node.children()) {
                if (child != null) {
                    var result = getSyntaxAtPosition(child, position);
                    if (result != null) {
                        return result;
                    }
                }
            }
            return node;
        }
        return null;
    }
}
