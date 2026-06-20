package com.orca.compiler.core.boundtree.expressions;

import com.orca.compiler.core.boundtree.BoundExpression;
import com.orca.compiler.core.boundtree.BoundNodeKind;
import com.orca.compiler.core.boundtree.BoundVisitor;
import com.orca.compiler.core.boundtree.Child;
import com.orca.compiler.core.typesystem.LangType;

public final class BoundAssignmentExpr extends BoundExpression {

    @Child private final BoundExpression targetExpr;
    @Child private final BoundExpression valueExpr;
    private final BoundOperator.Assignment operator;

    public BoundAssignmentExpr(BoundExpression targetExpr, BoundOperator.Assignment operator, BoundExpression valueExpr) {
        this.targetExpr = targetExpr;
        this.operator = operator;
        this.valueExpr = valueExpr;
    }

    public static BoundAssignmentExpr simpleAssignment(BoundExpression targetExpr, BoundExpression valueExpr) {
        return new BoundAssignmentExpr(targetExpr, BoundOperator.Assignment.SIMPLE_ASSIGNMENT, valueExpr);
    }

    public BoundExpression targetExpr() {
        return targetExpr;
    }

    public BoundExpression valueExpr() {
        return valueExpr;
    }

    public BoundOperator.Assignment operator() {
        return operator;
    }

    @Override
    public LangType type() {
        return valueExpr.type();
    }

    @Override
    public BoundNodeKind kind() {
        return BoundNodeKind.ASSIGNMENT_EXPR;
    }

    @Override
    public <R> R accept(BoundVisitor<R> visitor) {
        return visitor.visitAssignmentExpr(this);
    }
}
