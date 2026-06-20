package com.orca.compiler.core.boundtree.statements;

import java.util.List;

import com.orca.compiler.core.boundtree.BoundNodeKind;
import com.orca.compiler.core.boundtree.BoundStatement;

public final class BoundBlockStmt extends BoundStatement {

    private final List<BoundStatement> statements;

    public BoundBlockStmt(List<BoundStatement> statements) {
        this.statements = statements;
    }

    public BoundBlockStmt(BoundStatement... statements) {
        this.statements = List.of(statements);
    }

    public List<BoundStatement> statements() {
        return statements;
    }

    @Override
    public BoundNodeKind kind() {
        return BoundNodeKind.BLOCK_STMT;
    }
}
