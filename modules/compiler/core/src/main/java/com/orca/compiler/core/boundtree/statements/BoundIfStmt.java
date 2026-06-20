package com.orca.compiler.core.boundtree.statements;

import java.util.List;

import javax.annotation.Nullable;

import com.orca.compiler.core.boundtree.BoundIfClause;
import com.orca.compiler.core.boundtree.BoundNodeKind;
import com.orca.compiler.core.boundtree.BoundStatement;

public final class BoundIfStmt extends BoundStatement {

    public final BoundIfClause ifClause;
    public final List<BoundIfClause> elseIfClauses;

    @Nullable
    public final BoundStatement elseClause;

    public BoundIfStmt(BoundIfClause ifClause, List<BoundIfClause> elseIfClauses, @Nullable BoundStatement elseClause) {
        this.ifClause = ifClause;
        this.elseIfClauses = elseIfClauses;
        this.elseClause = elseClause;
    }

    public @Nullable
    BoundStatement elseBlock() {
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
}
