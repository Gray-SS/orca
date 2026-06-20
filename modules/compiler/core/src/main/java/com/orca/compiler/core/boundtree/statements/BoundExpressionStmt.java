package com.orca.compiler.core.boundtree.statements;

import com.orca.compiler.core.boundtree.BoundExpression;
import com.orca.compiler.core.boundtree.BoundNodeKind;
import com.orca.compiler.core.boundtree.BoundStatement;

public final class BoundExpressionStmt extends BoundStatement {

    private final BoundExpression expression;

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
}
