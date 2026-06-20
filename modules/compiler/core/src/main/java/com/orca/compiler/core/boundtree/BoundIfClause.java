package com.orca.compiler.core.boundtree;

public final class BoundIfClause extends BoundNode {

    @Child
    public final BoundExpression condition;
    @Child
    public final BoundStatement body;

    public BoundIfClause(BoundExpression condition, BoundStatement body) {
        this.condition = condition;
        this.body = body;
    }

    @Override
    public BoundNodeKind kind() {
        return BoundNodeKind.IF_CLAUSE;
    }

    @Override
    public <R> R accept(BoundVisitor<R> visitor) {
        return visitor.visitIfClause(this);
    }
}
