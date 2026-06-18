package com.orca.compiler.core.boundtree.statements;

import com.orca.compiler.core.boundtree.BoundExpression;
import com.orca.compiler.core.boundtree.BoundNodeKind;
import com.orca.compiler.core.boundtree.BoundStatement;
import com.orca.compiler.core.boundtree.BoundVariableDeclarator;

public final class BoundForStmt extends BoundStatement {

    public final BoundVariableDeclarator declarator;
    public final BoundExpression conditionExpr;
    public final BoundExpression stepExpr;
    public final BoundBlockStmt body;

    public BoundForStmt(BoundVariableDeclarator initializer, BoundExpression conditionExpr, BoundExpression stepExpr, BoundBlockStmt body) {
        this.declarator = initializer;
        this.conditionExpr = conditionExpr;
        this.stepExpr = stepExpr;
        this.body = body;
    }

    @Override
    public BoundNodeKind kind() {
        return BoundNodeKind.FOR_STMT;
    }
}
