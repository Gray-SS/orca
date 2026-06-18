package com.orca.compiler.core.syntax.expressions;

import com.orca.compiler.core.syntax.ExpressionSyntax;
import com.orca.compiler.core.syntax.SyntaxVisitor;
import com.orca.compiler.core.syntax.nodes.SimpleIdentifierSyntax;

public class MemberAccessExpr extends ExpressionSyntax {

    private final ExpressionSyntax instanceExpr;
    private final SimpleIdentifierSyntax memberIdentifier;

    public MemberAccessExpr(ExpressionSyntax instanceExpr, SimpleIdentifierSyntax memberIdentifier) {
        this.instanceExpr = instanceExpr;
        this.memberIdentifier = memberIdentifier;
    }

    /**
     * Gets the expression representing the instance whose member is being
     * accessed.
     *
     * @return The expression representing the instance whose member is being
     * accessed.
     */
    public ExpressionSyntax instanceExpr() {
        return instanceExpr;
    }

    /**
     * Gets the token representing the identifier of the member being accessed.
     *
     * @return The token representing the identifier of the member being
     * accessed.
     */
    public SimpleIdentifierSyntax memberIdentifier() {
        return memberIdentifier;
    }

    /**
     * Gets the name of the member being accessed.
     *
     * @return The name of the member being accessed.
     */
    public String memberName() {
        return memberIdentifier.text();
    }

    @Override
    public void accept(SyntaxVisitor visitor) {
        visitor.visitMemberAccessExpr(this);
    }

    @Override
    public String toString() {
        return "MemberAccessExpression(" + memberName() + ")";
    }
}
