package com.orca.compiler.core.boundtree.statements;

import javax.annotation.Nullable;

import com.orca.compiler.core.boundtree.BoundExpression;
import com.orca.compiler.core.boundtree.BoundNodeKind;
import com.orca.compiler.core.boundtree.BoundStatement;
import com.orca.compiler.core.boundtree.BoundVisitor;
import com.orca.compiler.core.boundtree.Child;

public final class BoundReturnStmt extends BoundStatement {

    @Nullable
    @Child public final BoundExpression expression;

    public BoundReturnStmt(@Nullable BoundExpression expression) {
        this.expression = expression;
    }

    @Override
    public BoundNodeKind kind() {
        return BoundNodeKind.RETURN_STMT;
    }

    @Override
    public <R> R accept(BoundVisitor<R> visitor) {
        return visitor.visitReturnStmt(this);
    }
}
