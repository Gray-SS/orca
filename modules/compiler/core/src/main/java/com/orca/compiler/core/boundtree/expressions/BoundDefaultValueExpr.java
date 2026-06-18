package com.orca.compiler.core.boundtree.expressions;

import com.orca.compiler.core.boundtree.BoundExpression;
import com.orca.compiler.core.boundtree.BoundNodeKind;
import com.orca.compiler.core.typesystem.LangType;

public final class BoundDefaultValueExpr extends BoundExpression {
    private final LangType type;

    public BoundDefaultValueExpr(LangType type) {
        this.type = type;
    }

    @Override
    public LangType type() {
        return type;
    }

    @Override
    public BoundNodeKind kind() {
        return BoundNodeKind.DEFAULT_VALUE_EXPR;
    }
}
