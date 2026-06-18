package com.orca.compiler.core.controlflow;

import com.orca.compiler.core.boundtree.BoundExpression;

public final class BasicBlockBranch {
    private final BasicBlock from;
    private final BasicBlock to;
    private final BoundExpression condition;

    public BasicBlockBranch(BasicBlock from, BasicBlock to, BoundExpression condition) {
        this.from = from;
        this.to = to;
        this.condition = condition;
    }

    public BasicBlock from() {
        return from;
    }

    public BasicBlock to() {
        return to;
    }

    public BoundExpression condition() {
        return condition;
    }
}
