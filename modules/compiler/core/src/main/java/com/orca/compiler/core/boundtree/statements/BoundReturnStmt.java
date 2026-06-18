package com.orca.compiler.core.boundtree.statements;

import javax.annotation.Nullable;

import com.orca.compiler.core.boundtree.BoundExpression;
import com.orca.compiler.core.boundtree.BoundNodeKind;
import com.orca.compiler.core.boundtree.BoundStatement;

public final class BoundReturnStmt extends BoundStatement {
    @Nullable
    public final BoundExpression expression;

    public BoundReturnStmt(@Nullable BoundExpression expression) {
        this.expression = expression;
    }

    @Override
    public BoundNodeKind kind() {
        return BoundNodeKind.RETURN_STMT;
    }
}
