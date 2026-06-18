package com.orca.compiler.core.syntax.expressions;

import com.orca.compiler.core.syntax.ExpressionSyntax;
import com.orca.compiler.core.syntax.SyntaxToken;
import com.orca.compiler.core.syntax.SyntaxVisitor;

public class BinaryExpr extends ExpressionSyntax {

    private final SyntaxToken operatorToken;
    private final ExpressionSyntax left;
    private final ExpressionSyntax right;

    public BinaryExpr(SyntaxToken operatorToken, ExpressionSyntax left, ExpressionSyntax right) {
        this.operatorToken = operatorToken;
        this.left = left;
        this.right = right;
    }

    /**
     * Gets the operator of this binary expression.
     *
     * @return The operator of this binary expression.
     */
    public SyntaxToken operatorToken() {
        return operatorToken;
    }

    /**
     * Gets the left-hand side of this binary expression.
     *
     * @return The left-hand side of this binary expression.
     */
    public ExpressionSyntax left() {
        return left;
    }

    /**
     * Gets the right-hand side of this binary expression.
     *
     * @return The right-hand side of this binary expression.
     */
    public ExpressionSyntax right() {
        return right;
    }

    @Override
    public void accept(SyntaxVisitor visitor) {
        visitor.visitBinaryExpr(this);
    }

    @Override
    public String toString() {
        return "BinaryExpression(" + operatorToken + ")";
    }
}
