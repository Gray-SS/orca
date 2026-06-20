package com.orca.compiler.core.boundtree.statements;

import com.orca.compiler.core.boundtree.BoundExpression;
import com.orca.compiler.core.boundtree.BoundNodeKind;
import com.orca.compiler.core.boundtree.BoundStatement;
import com.orca.compiler.core.boundtree.BoundVariableDeclarator;
import com.orca.compiler.core.boundtree.BoundVisitor;
import com.orca.compiler.core.boundtree.Child;

public final class BoundForStmt extends BoundStatement {

    @Child public final BoundVariableDeclarator declarator;
    @Child public final BoundExpression conditionExpr;
    @Child public final BoundExpression stepExpr;
    @Child public final BoundBlockStmt body;

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

    @Override
    public <R> R accept(BoundVisitor<R> visitor) {
        return visitor.visitForStmt(this);
    }
}
