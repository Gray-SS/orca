package com.orca.compiler.core.boundtree.expressions;

import com.orca.compiler.core.boundtree.BoundExpression;
import com.orca.compiler.core.boundtree.BoundNodeKind;
import com.orca.compiler.core.typesystem.LangType;

public final class BoundConversionExpr extends BoundExpression {

    private final BoundExpression operand;
    private final LangType targetType;

    public BoundConversionExpr(BoundExpression operand, LangType targetType) {
        this.operand = operand;
        this.targetType = targetType;
    }

    @Override
    public boolean isCompileTimeFoldable() {
        return operand.isCompileTimeFoldable();
    }

    @Override
    public BoundNodeKind kind() {
        return BoundNodeKind.CONVERSION_EXPR;
    }

    @Override
    public LangType type() {
        return targetType;
    }

    public BoundExpression operand() {
        return operand;
    }
}
