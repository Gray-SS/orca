package com.orca.compiler.core.syntax.statements;

import com.orca.compiler.core.syntax.ExpressionSyntax;
import com.orca.compiler.core.syntax.StatementSyntax;
import com.orca.compiler.core.syntax.SyntaxVisitor;

public class ExpressionStmt extends StatementSyntax {

    private final ExpressionSyntax expression;

    public ExpressionStmt(ExpressionSyntax expression) {
        this.expression = expression;
    }

    /**
     * Gets the expression contained in this statement.
     *
     * @return The expression contained in this statement.
     */
    public ExpressionSyntax expression() {
        return expression;
    }

    @Override
    public void accept(SyntaxVisitor visitor) {
        visitor.visitExpressionStmt(this);
    }

    @Override
    public String toString() {
        return "ExpressionStmt";
    }
}
