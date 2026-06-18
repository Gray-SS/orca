package com.orca.compiler.core.boundtree.statements;

import com.orca.compiler.core.boundtree.BoundExpression;
import com.orca.compiler.core.boundtree.BoundLabel;
import com.orca.compiler.core.boundtree.BoundNodeKind;
import com.orca.compiler.core.boundtree.BoundStatement;

public final class BoundConditionalGotoStmt extends BoundStatement {
    public final BoundLabel label;
    public final boolean inverseCondition;
    public final BoundExpression condition;

    public BoundConditionalGotoStmt(BoundLabel label, BoundExpression condition) {
        this(label, condition, true);
    }

    public BoundConditionalGotoStmt(BoundLabel label, BoundExpression condition, boolean inverseCondition) {
        this.label = label;
        this.condition = condition;
        this.inverseCondition = inverseCondition;
    }

    @Override
    public BoundNodeKind kind() {
        return BoundNodeKind.CONDITIONAL_GOTO_STMT;
    }
}