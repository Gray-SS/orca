package com.orca.compiler.core.boundtree.expressions;

import com.orca.compiler.core.typesystem.LangType;

public final class BoundIndexOperator {

    private final LangType baseType;
    private final LangType indexType;
    private final LangType resultType;

    private BoundIndexOperator(LangType baseType, LangType indexType, LangType resultType) {
        this.baseType = baseType;
        this.indexType = indexType;
        this.resultType = resultType;
    }

    public LangType baseType() {
        return baseType;
    }

    public LangType indexType() {
        return indexType;
    }

    public LangType resultType() {
        return resultType;
    }

    // private static final BoundIndexOperator arrayIndexOperator = new BoundIndexOperator(TypeShape.arrayOf(TypeShape.ANY), TypeShape.INT, TypeShape.ANY);
}
