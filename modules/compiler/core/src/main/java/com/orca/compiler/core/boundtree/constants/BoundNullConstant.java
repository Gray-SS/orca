package com.orca.compiler.core.boundtree.constants;

import com.orca.compiler.core.typesystem.LangType;

public final class BoundNullConstant extends BoundConstant {
    @Override
    public LangType type() {
        return LangType.Unknown;
    }

    @Override
    public BoundBoolConstant isEqualTo(BoundConstant other) {
        if (other instanceof BoundNullConstant) {
            return new BoundBoolConstant(true);
        } else {
            return new BoundBoolConstant(false);
        }
    }

    @Override
    public BoundConstant convertTo(LangType targetType) {
        throw new UnsupportedOperationException("Cannot convert null constant to any type.");
    }
}
