package com.orca.compiler.core.syntax.expressions;

import com.orca.compiler.core.syntax.ExpressionSyntax;
import com.orca.compiler.core.syntax.SyntaxToken;
import com.orca.compiler.core.syntax.SyntaxVisitor;

public class UnaryExpr extends ExpressionSyntax {

    private final SyntaxToken operatorToken;
    private final ExpressionSyntax operand;

    public UnaryExpr(SyntaxToken operatorToken, ExpressionSyntax operand) {
        this.operatorToken = operatorToken;
        this.operand = operand;
    }

    /**
     * Gets the operator of this unary expression.
     *
     * @return The operator of this unary expression.
     */
    public SyntaxToken operatorToken() {
        return operatorToken;
    }

    /**
     * Gets the operand of this unary expression.
     *
     * @return The operand of this unary expression.
     */
    public ExpressionSyntax operand() {
        return operand;
    }

    @Override
    public void accept(SyntaxVisitor visitor) {
        visitor.visitUnaryExpr(this);
    }

    @Override
    public String toString() {
        return "UnaryExpression(" + operatorToken + ")";
    }
}
