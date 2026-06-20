package com.orca.compiler.core.boundtree;

import com.orca.compiler.core.boundtree.constants.BoundConstant;
import com.orca.compiler.core.typesystem.LangType;

public abstract class BoundExpression extends BoundNode {

    public abstract LangType type();

    public boolean isCompileTimeFoldable() {
        return false;
    }

    public BoundConstant getConstant() {
        if (!isCompileTimeFoldable()) {
            throw new IllegalStateException("Expression is not a constant expression");
        }

        return ConstantFolding.fold(this);
    }
}
