package com.orca.compiler.core.boundtree.expressions;

import java.util.Objects;

import com.orca.compiler.core.boundtree.BoundExpression;
import com.orca.compiler.core.boundtree.BoundNodeKind;
import com.orca.compiler.core.boundtree.BoundVisitor;
import com.orca.compiler.core.boundtree.Child;
import com.orca.compiler.core.typesystem.LangType;

public final class BoundUnaryExpr extends BoundExpression {

    @Child public final BoundExpression operand;
    public final BoundOperator.Unary operator;

    public BoundUnaryExpr(BoundOperator.Unary operator, BoundExpression operand) {
        Objects.requireNonNull(operator, "operator cannot be null");
        Objects.requireNonNull(operand, "operand cannot be null");

        this.operator = operator;
        this.operand = operand;
    }

    @Override
    public BoundNodeKind kind() {
        return BoundNodeKind.UNARY_EXPR;
    }

    @Override
    public <R> R accept(BoundVisitor<R> visitor) {
        return visitor.visitUnaryExpr(this);
    }

    @Override
    public LangType type() {
        return operator.resultType();
    }

    @Override
    public boolean isCompileTimeFoldable() {
        return operand.isCompileTimeFoldable();
    }
}
