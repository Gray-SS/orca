package com.orca.compiler.core.boundtree.statements;

import com.orca.compiler.core.boundtree.BoundExpression;
import com.orca.compiler.core.boundtree.BoundNodeKind;
import com.orca.compiler.core.boundtree.BoundStatement;
import com.orca.compiler.core.boundtree.BoundVisitor;
import com.orca.compiler.core.boundtree.Child;

public final class BoundExpressionStmt extends BoundStatement {

    @Child private final BoundExpression expression;

    public BoundExpressionStmt(BoundExpression expression) {
        this.expression = expression;
    }

    public BoundExpression expression() {
        return expression;
    }

    @Override
    public BoundNodeKind kind() {
        return BoundNodeKind.EXPRESSION_STMT;
    }

    @Override
    public <R> R accept(BoundVisitor<R> visitor) {
        return visitor.visitExpressionStmt(this);
    }
}
