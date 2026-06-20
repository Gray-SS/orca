package com.orca.compiler.core.syntax.expressions;

import com.orca.compiler.core.syntax.ExpressionSyntax;
import com.orca.compiler.core.syntax.SyntaxVisitor;
import com.orca.compiler.core.syntax.types.TypeSyntax;

public class ArrayLiteralExpression extends ExpressionSyntax {

    private final TypeSyntax elementType;
    private final ExpressionSyntax length;

    public ArrayLiteralExpression(TypeSyntax elementType, ExpressionSyntax length) {
        this.elementType = elementType;
        this.length = length;
    }

    /**
     * Gets the syntax of the element type of this array literal expression.
     *
     * @return The syntax of the element type of this array literal expression.
     */
    public TypeSyntax elementType() {
        return elementType;
    }

    /**
     * Gets the syntax of the length of this array literal expression.
     *
     * @return The syntax of the length of this array literal expression.
     */
    public ExpressionSyntax length() {
        return length;
    }

    @Override
    public void accept(SyntaxVisitor visitor) {
        visitor.visitArrayLiteralExpr(this);
    }

    @Override
    public String toString() {
        return "ArrayLiteralExpression";
    }
}
