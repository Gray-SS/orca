package com.orca.compiler.core.syntax.nodes;

import com.orca.compiler.core.syntax.ExpressionSyntax;
import com.orca.compiler.core.syntax.SyntaxNode;
import com.orca.compiler.core.syntax.SyntaxVisitor;
import com.orca.compiler.core.syntax.statements.BlockStmt;

public class IfClauseSyntax extends SyntaxNode {

    private final ExpressionSyntax condition;
    private final BlockStmt body;

    public IfClauseSyntax(ExpressionSyntax condition, BlockStmt body) {
        this.condition = condition;
        this.body = body;
    }

    /**
     * Gets the condition of this if clause.
     *
     * @return The condition of this if clause.
     */
    public ExpressionSyntax condition() {
        return condition;
    }

    /**
     * Gets the body of this if clause.
     *
     * @return The body of this if clause.
     */
    public BlockStmt body() {
        return body;
    }

    @Override
    public void accept(SyntaxVisitor visitor) {
        visitor.visitIfClauseSyntax(this);
    }

    @Override
    public String toString() {
        return "IfClauseSyntax";
    }
}
