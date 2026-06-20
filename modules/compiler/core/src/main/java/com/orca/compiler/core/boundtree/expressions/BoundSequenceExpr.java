package com.orca.compiler.core.boundtree.expressions;

import java.util.List;

import com.orca.compiler.core.boundtree.BoundExpression;
import com.orca.compiler.core.boundtree.BoundNodeKind;
import com.orca.compiler.core.boundtree.BoundStatement;
import com.orca.compiler.core.boundtree.BoundVisitor;
import com.orca.compiler.core.boundtree.Child;
import com.orca.compiler.core.typesystem.LangType;

public final class BoundSequenceExpr extends BoundExpression {

    @Child private final List<BoundStatement> sideEffects;
    @Child private final BoundExpression value;

    public BoundSequenceExpr(List<BoundStatement> sideEffects, BoundExpression value) {
        this.sideEffects = sideEffects;
        this.value = value;
    }

    public List<BoundStatement> sideEffects() {
        return sideEffects;
    }

    public BoundExpression value() {
        return value;
    }

    @Override
    public LangType type() {
        return value.type();
    }

    @Override
    public BoundNodeKind kind() {
        return BoundNodeKind.SEQUENCE_EXPR;
    }

    @Override
    public <R> R accept(BoundVisitor<R> visitor) {
        return visitor.visitSequenceExpr(this);
    }
}
