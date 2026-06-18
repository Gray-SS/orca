package com.orca.compiler.core.boundtree.statements;

import com.orca.compiler.core.boundtree.BoundLabel;
import com.orca.compiler.core.boundtree.BoundNodeKind;
import com.orca.compiler.core.boundtree.BoundStatement;

public final class BoundGotoStmt extends BoundStatement {
    public final BoundLabel label;

    public BoundGotoStmt(BoundLabel label) {
        this.label = label;
    }

    @Override
    public BoundNodeKind kind() {
        return BoundNodeKind.GOTO_STMT;
    }
}
