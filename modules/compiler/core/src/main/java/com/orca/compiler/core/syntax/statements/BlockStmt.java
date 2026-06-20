package com.orca.compiler.core.syntax.statements;

import java.util.List;

import com.orca.compiler.core.syntax.StatementSyntax;
import com.orca.compiler.core.syntax.SyntaxNode;
import com.orca.compiler.core.syntax.SyntaxVisitor;
import com.orca.compiler.core.text.SourceSpan;

public class BlockStmt extends StatementSyntax {

    private final SourceSpan span;
    private final List<StatementSyntax> statements;

    public BlockStmt(SourceSpan span, List<StatementSyntax> statements) {
        this.span = span;
        this.statements = statements;
    }

    /**
     * Gets the list of statements contained in this block statement.
     *
     * @return The list of statements in this block statement.
     */
    public List<StatementSyntax> statements() {
        return statements;
    }

    @Override
    public SourceSpan span() {
        return span;
    }

    @Override
    public void accept(SyntaxVisitor visitor) {
        visitor.visitBlockStmt(this);
    }

    @Override
    public List<SyntaxNode> children() {
        return List.copyOf(statements);
    }

    @Override
    public String toString() {
        return "BlockStatement";
    }
}
