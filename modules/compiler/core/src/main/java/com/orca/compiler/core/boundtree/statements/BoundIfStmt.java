package com.orca.compiler.core.boundtree.statements;

import java.util.List;

import javax.annotation.Nullable;

import com.orca.compiler.core.boundtree.BoundIfClause;
import com.orca.compiler.core.boundtree.BoundNodeKind;
import com.orca.compiler.core.boundtree.BoundStatement;
import com.orca.compiler.core.boundtree.BoundVisitor;
import com.orca.compiler.core.boundtree.Child;

public final class BoundIfStmt extends BoundStatement {

    @Child public final BoundIfClause ifClause;
    @Child public final List<BoundIfClause> elseIfClauses;

    @Nullable
    @Child public final BoundStatement elseClause;

    public BoundIfStmt(BoundIfClause ifClause, List<BoundIfClause> elseIfClauses, @Nullable BoundStatement elseClause) {
        this.ifClause = ifClause;
        this.elseIfClauses = elseIfClauses;
        this.elseClause = elseClause;
    }

    public @Nullable BoundStatement elseBlock() {
        return elseClause;
    }

    public List<BoundIfClause> thenClauses() {
        var clauses = new java.util.ArrayList<BoundIfClause>();
        clauses.add(ifClause);
        clauses.addAll(elseIfClauses);
        return clauses;
    }

    @Override
    public BoundNodeKind kind() {
        return BoundNodeKind.IF_STMT;
    }

    @Override
    public <R> R accept(BoundVisitor<R> visitor) {
        return visitor.visitIfStmt(this);
    }
}
