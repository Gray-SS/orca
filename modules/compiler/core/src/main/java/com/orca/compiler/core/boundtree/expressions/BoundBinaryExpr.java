package com.orca.compiler.core.boundtree.expressions;

import com.orca.compiler.core.boundtree.BoundExpression;
import com.orca.compiler.core.boundtree.BoundNodeKind;
import com.orca.compiler.core.typesystem.LangType;

public final class BoundBinaryExpr extends BoundExpression {

    public final BoundExpression left;
    public final BoundExpression right;
    public final BoundOperator.Binary operator;

    public BoundBinaryExpr(BoundOperator.Binary operator, BoundExpression left, BoundExpression right) {
        if (operator == null) {
            throw new IllegalArgumentException("Operator is null");
        }
        if (left.type() != right.type()) {
            throw new IllegalArgumentException("Left and right expressions must have the same type. Left type: " + left.type().toString() + ", right type: " + right.type().toString());
        }

        this.operator = operator;
        this.left = left;
        this.right = right;
    }

    @Override
    public BoundNodeKind kind() {
        return BoundNodeKind.BINARY_EXPR;
    }

    @Override
    public LangType type() {
        return operator.resultType();
    }

    @Override
    public boolean isCompileTimeFoldable() {
        return left.isCompileTimeFoldable() && right.isCompileTimeFoldable();
    }
}
