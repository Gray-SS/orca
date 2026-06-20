package com.orca.compiler.core.syntax.expressions;

import com.orca.compiler.core.syntax.ExpressionSyntax;
import com.orca.compiler.core.syntax.SyntaxToken;
import com.orca.compiler.core.syntax.SyntaxVisitor;

public final class UnaryAssignmentExpr extends ExpressionSyntax {

    private final SyntaxToken operatorToken;
    private final ExpressionSyntax operand;

    public UnaryAssignmentExpr(SyntaxToken operatorToken, ExpressionSyntax operand) {
        this.operatorToken = operatorToken;
        this.operand = operand;
    }

    public SyntaxToken operatorToken() {
        return operatorToken;
    }

    public ExpressionSyntax operand() {
        return operand;
    }

    @Override
    public void accept(SyntaxVisitor visitor) {
        visitor.visitUnaryAssignmentExpr(this);
    }

    @Override
    public String toString() {
        return "UnaryAssignmentExpr";
    }
}
