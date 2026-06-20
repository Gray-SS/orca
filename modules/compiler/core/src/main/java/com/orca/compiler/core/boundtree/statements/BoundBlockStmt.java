package com.orca.compiler.core.boundtree.statements;

import java.util.List;

import com.orca.compiler.core.boundtree.BoundNodeKind;
import com.orca.compiler.core.boundtree.BoundStatement;
import com.orca.compiler.core.boundtree.BoundVisitor;
import com.orca.compiler.core.boundtree.Child;

public final class BoundBlockStmt extends BoundStatement {

    @Child private final List<BoundStatement> statements;

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

    @Override
    public <R> R accept(BoundVisitor<R> visitor) {
        return visitor.visitBlockStmt(this);
    }
}
