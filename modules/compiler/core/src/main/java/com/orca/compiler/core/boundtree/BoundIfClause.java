package com.orca.compiler.core.boundtree;

public final class BoundIfClause {

    public final BoundExpression condition;
    public final BoundStatement body;

    public BoundIfClause(BoundExpression condition, BoundStatement body) {
        this.condition = condition;
        this.body = body;
    }
}
