package com.orca.compiler.core.syntax.expressions;

import com.orca.compiler.core.syntax.ExpressionSyntax;
import com.orca.compiler.core.syntax.SyntaxVisitor;

public class ArrayAccessExpr extends ExpressionSyntax {

    private final ExpressionSyntax arrayExpr;
    private final ExpressionSyntax indexExpr;

    public ArrayAccessExpr(ExpressionSyntax arrayExpr, ExpressionSyntax indexExpr) {
        this.arrayExpr = arrayExpr;
        this.indexExpr = indexExpr;
    }

    /**
     * Gets the expression representing the array being accessed.
     *
     * @return The expression representing the array being accessed.
     */
    public ExpressionSyntax arrayExpr() {
        return arrayExpr;
    }

    /**
     * Gets the expression representing the index of the element being accessed.
     *
     * @return The expression representing the index of the element being
     * accessed.
     */
    public ExpressionSyntax indexExpr() {
        return indexExpr;
    }

    @Override
    public void accept(SyntaxVisitor visitor) {
        visitor.visitArrayAccessExpr(this);
    }

    @Override
    public String toString() {
        return "ArrayAccessExpression";
    }
}
