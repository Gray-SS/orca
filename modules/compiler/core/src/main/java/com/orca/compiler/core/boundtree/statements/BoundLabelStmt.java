package com.orca.compiler.core.boundtree.statements;

import com.orca.compiler.core.boundtree.BoundLabel;
import com.orca.compiler.core.boundtree.BoundNodeKind;
import com.orca.compiler.core.boundtree.BoundStatement;

public final class BoundLabelStmt extends BoundStatement {

    public final BoundLabel label;

    public BoundLabelStmt(BoundLabel label) {
        this.label = label;
    }

    @Override
    public BoundNodeKind kind() {
        return BoundNodeKind.LABEL_STMT;
    }
}
