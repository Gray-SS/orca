package com.orca.compiler.core.syntax.statements;

import com.orca.compiler.core.syntax.ExpressionSyntax;
import com.orca.compiler.core.syntax.StatementSyntax;
import com.orca.compiler.core.syntax.SyntaxVisitor;

public class WhileStmt extends StatementSyntax {

    private final ExpressionSyntax condition;
    private final BlockStmt body;

    public WhileStmt(ExpressionSyntax condition, BlockStmt body) {
        this.condition = condition;
        this.body = body;
    }

    /**
     * Gets the condition of this while statement.
     *
     * @return The condition of this while statement.
     */
    public ExpressionSyntax condition() {
        return condition;
    }

    /**
     * Gets the body of this while statement.
     *
     * @return The body of this while statement.
     */
    public BlockStmt body() {
        return body;
    }

    @Override
    public void accept(SyntaxVisitor visitor) {
        visitor.visitWhileStmt(this);
    }

    @Override
    public String toString() {
        return "WhileStatement";
    }
}
