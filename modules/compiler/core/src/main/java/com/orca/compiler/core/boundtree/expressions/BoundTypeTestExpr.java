package com.orca.compiler.core.boundtree.expressions;

import com.orca.compiler.core.boundtree.BoundExpression;
import com.orca.compiler.core.boundtree.BoundNodeKind;
import com.orca.compiler.core.symbols.TypeSymbol;
import com.orca.compiler.core.typesystem.LangType;

public final class BoundTypeTestExpr extends BoundExpression {
    private final BoundExpression operand;
    private final LangType targetType;
    private final TypeSymbol targetTypeSymbol;

    public BoundTypeTestExpr(BoundExpression operand, TypeSymbol targetTypeSymbol) {
        this.operand = operand;
        this.targetTypeSymbol = targetTypeSymbol;
        this.targetType = targetTypeSymbol.type();
    }

    public BoundExpression operand() {
        return operand;
    }

    public TypeSymbol targetTypeSymbol() {
        return targetTypeSymbol;
    }

    public LangType targetType() {
        return targetType;
    }

    @Override
    public LangType type() {
        return LangType.Bool;
    }

    @Override
    public BoundNodeKind kind() {
        return BoundNodeKind.TYPE_TEST_EXPR;
    }
}
