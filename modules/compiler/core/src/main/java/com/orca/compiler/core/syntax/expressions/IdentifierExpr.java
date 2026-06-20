package com.orca.compiler.core.syntax.expressions;

import com.orca.compiler.core.syntax.ExpressionSyntax;
import com.orca.compiler.core.syntax.SyntaxVisitor;
import com.orca.compiler.core.syntax.nodes.IdentifierSyntax;

public class IdentifierExpr extends ExpressionSyntax {

    private final IdentifierSyntax identifier;

    public IdentifierExpr(IdentifierSyntax identifier) {
        this.identifier = identifier;
    }

    /**
     * Gets the identifier syntax of this identifier expression.
     *
     * @return The identifier syntax of this identifier expression.
     */
    public IdentifierSyntax identifier() {
        return identifier;
    }

    @Override
    public void accept(SyntaxVisitor visitor) {
        visitor.visitIdentifierExpr(this);
    }

    @Override
    public String toString() {
        return "IdentifierExpression(" + identifier.name() + ")";
    }
}
