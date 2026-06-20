package com.orca.compiler.core.boundtree.expressions;

import com.orca.compiler.core.boundtree.BoundExpression;
import com.orca.compiler.core.boundtree.BoundNodeKind;
import com.orca.compiler.core.boundtree.BoundVisitor;
import com.orca.compiler.core.boundtree.Child;
import com.orca.compiler.core.typesystem.ArrayType;
import com.orca.compiler.core.typesystem.LangType;

public final class BoundArrayAccessExpr extends BoundExpression {
    @Child private final BoundExpression arrayExpr;
    @Child private final BoundExpression indexExpr;

    public BoundArrayAccessExpr(BoundExpression arrayExpr, BoundExpression indexExpr) {
        this.arrayExpr = arrayExpr;
        this.indexExpr = indexExpr;
    }

    /**
     * Gets the bound expression representing the array being accessed.
     * @return The bound expression representing the array being accessed.
     */
    public BoundExpression arrayExpr() {
        return arrayExpr;
    }

    /**
     * Gets the bound expression representing the index being accessed in the array.
     * @return The bound expression representing the index being accessed in the array.
     */
    public BoundExpression indexExpr() {
        return indexExpr;
    }

    @Override
    public BoundNodeKind kind() {
        return BoundNodeKind.ARRAY_ACCESS_EXPR;
    }

    @Override
    public <R> R accept(BoundVisitor<R> visitor) {
        return visitor.visitArrayAccessExpr(this);
    }

    @Override
    public LangType type() {
        var arrayType = arrayExpr.type();
        if (arrayType.isArray()) {
            var arr = arrayType.as(ArrayType.class);
            return arr.getElementType();
        }

        if (arrayType.isString()) {
            return LangType.Int;
        }

        throw new RuntimeException("Expected an array type, but got: " + arrayType);
    }
}
