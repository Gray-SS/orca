package com.orca.compiler.core.syntax.expressions;

import com.orca.compiler.core.syntax.ExpressionSyntax;
import com.orca.compiler.core.syntax.SyntaxList;
import com.orca.compiler.core.syntax.SyntaxVisitor;

public class InvocationExpr extends ExpressionSyntax {

    private final ExpressionSyntax callee;
    private final SyntaxList<ExpressionSyntax> arguments;

    public InvocationExpr(ExpressionSyntax callee, SyntaxList<ExpressionSyntax> arguments) {
        this.callee = callee;
        this.arguments = arguments;
    }

    public ExpressionSyntax callee() {
        return callee;
    }

    public SyntaxList<ExpressionSyntax> arguments() {
        return arguments;
    }

    @Override
    public void accept(SyntaxVisitor visitor) {
        visitor.visitInvocationExpr(this);
    }

    @Override
    public String toString() {
        return "InvocationExpr";
    }
}
