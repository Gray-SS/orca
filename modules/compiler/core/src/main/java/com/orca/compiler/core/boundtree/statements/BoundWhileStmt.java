package com.orca.compiler.core.boundtree.statements;

import com.orca.compiler.core.boundtree.BoundExpression;
import com.orca.compiler.core.boundtree.BoundNodeKind;
import com.orca.compiler.core.boundtree.BoundStatement;
import com.orca.compiler.core.boundtree.BoundVisitor;
import com.orca.compiler.core.boundtree.Child;

public final class BoundWhileStmt extends BoundStatement {

    @Child private final BoundExpression condition;
    @Child private final BoundBlockStmt body;

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

    @Override
    public <R> R accept(BoundVisitor<R> visitor) {
        return visitor.visitWhileStmt(this);
    }
}
