package com.orca.compiler.core.boundtree.expressions;

import com.orca.compiler.core.boundtree.BoundExpression;
import com.orca.compiler.core.boundtree.BoundNodeKind;
import com.orca.compiler.core.boundtree.BoundVisitor;
import com.orca.compiler.core.boundtree.Child;
import com.orca.compiler.core.typesystem.LangType;

public final class BoundArrayLiteralExpr extends BoundExpression {

    public final LangType elementType;
    @Child public final BoundExpression lengthExpr;

    public BoundArrayLiteralExpr(LangType elementType, BoundExpression lengthExpr) {
        this.elementType = elementType;
        this.lengthExpr = lengthExpr;
    }

    @Override
    public BoundNodeKind kind() {
        return BoundNodeKind.ARRAY_LITERAL_EXPR;
    }

    @Override
    public <R> R accept(BoundVisitor<R> visitor) {
        return visitor.visitArrayLiteralExpr(this);
    }

    @Override
    public LangType type() {
        return LangType.arrayOf(elementType);
    }
}
