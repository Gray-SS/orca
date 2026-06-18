package com.orca.compiler.core.syntax.expressions;

import com.orca.compiler.core.syntax.ExpressionSyntax;
import com.orca.compiler.core.syntax.SyntaxToken;
import com.orca.compiler.core.syntax.SyntaxVisitor;

public class AssignmentExpr extends ExpressionSyntax {

    private final ExpressionSyntax target;
    private final SyntaxToken operatorToken;
    private final ExpressionSyntax initializer;

    public AssignmentExpr(ExpressionSyntax target, SyntaxToken operatorToken, ExpressionSyntax initializer) {
        this.target = target;
        this.operatorToken = operatorToken;
        this.initializer = initializer;
    }

    /**
     * Gets the operator token of this assignment expression.
     *
     * @return The operator token of this assignment expression.
     */
    public SyntaxToken operatorToken() {
        return operatorToken;
    }

    /**
     * Gets the target of this assignment expression.
     *
     * @return The target of this assignment expression.
     */
    public ExpressionSyntax target() {
        return target;
    }

    /**
     * Gets the initializer of this assignment expression.
     *
     * @return The initializer of this assignment expression.
     */
    public ExpressionSyntax initializer() {
        return initializer;
    }

    @Override
    public void accept(SyntaxVisitor visitor) {
        visitor.visitAssignmentExpr(this);
    }

    @Override
    public String toString() {
        return "AssignmentExpression";
    }
}
