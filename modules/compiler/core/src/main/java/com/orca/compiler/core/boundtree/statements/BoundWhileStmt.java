package com.orca.compiler.core.boundtree.statements;

import com.orca.compiler.core.boundtree.BoundExpression;
import com.orca.compiler.core.boundtree.BoundNodeKind;
import com.orca.compiler.core.boundtree.BoundStatement;

public final class BoundWhileStmt extends BoundStatement {

    private final BoundExpression condition;
    private final BoundBlockStmt body;

    public BoundWhileStmt(BoundExpression condition, BoundBlockStmt body) {
        this.condition = condition;
        this.body = body;
    }

    public BoundExpression condition() {
        return condition;
    }

    public BoundBlockStmt body() {
        return body;
    }

    @Override
    public BoundNodeKind kind() {
        return BoundNodeKind.WHILE_STMT;
    }
}
