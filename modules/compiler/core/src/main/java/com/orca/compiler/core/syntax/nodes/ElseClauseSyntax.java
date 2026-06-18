package com.orca.compiler.core.syntax.nodes;

import com.orca.compiler.core.syntax.SyntaxNode;
import com.orca.compiler.core.syntax.SyntaxVisitor;
import com.orca.compiler.core.syntax.statements.BlockStmt;

public class ElseClauseSyntax extends SyntaxNode {
    private final BlockStmt body;

    public ElseClauseSyntax(BlockStmt body) {
        this.body = body;
    }

    /**
     * Gets the body of this else clause.
     * @return The body of this else clause.
     */
    public BlockStmt body() {
        return body;
    }

    @Override
    public void accept(SyntaxVisitor visitor) {
        visitor.visitElseClauseSyntax(this);
    }

    @Override
    public String toString() {
        return "ElseClauseSyntax";
    }
}
