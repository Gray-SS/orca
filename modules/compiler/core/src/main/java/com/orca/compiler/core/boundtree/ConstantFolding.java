package com.orca.compiler.core.boundtree;

import com.orca.compiler.core.Debug;
import com.orca.compiler.core.boundtree.constants.BoundConstant;
import com.orca.compiler.core.boundtree.expressions.BoundBinaryExpr;
import com.orca.compiler.core.boundtree.expressions.BoundConversionExpr;
import com.orca.compiler.core.boundtree.expressions.BoundLiteralExpr;
import com.orca.compiler.core.boundtree.expressions.BoundReferenceExpr;
import com.orca.compiler.core.boundtree.expressions.BoundUnaryExpr;

public final class ConstantFolding {

    public static BoundConstant fold(BoundExpression expr) {
        Debug.requireNotNull(expr, "Expression cannot be null for folding");
        Debug.assertTrue(expr.isCompileTimeFoldable(), "Expression is not a compile-time constant: " + expr);

        var constant = foldInternal(expr);
        Debug.requireNotNull(constant, "Folded constant is null");

        return constant;
    }

    public static BoundExpression foldExpression(BoundExpression expr) {
        Debug.requireNotNull(expr, "Expression cannot be null for folding");
        Debug.assertTrue(expr.isCompileTimeFoldable(), "Expression is not a compile-time constant: " + expr);

        var constant = foldInternal(expr);
        Debug.requireNotNull(constant, "Folded constant is null");

        return new BoundLiteralExpr(constant);
    }

    private static BoundConstant foldInternal(BoundExpression expr) {
        Debug.requireNotNull(expr, "Expression cannot be null in foldInternal");

        return switch (expr) {
            case BoundLiteralExpr e ->
                e.getConstant();
            case BoundUnaryExpr u ->
                foldUnary(u);
            case BoundBinaryExpr b ->
                foldBinary(b);
            case BoundConversionExpr c ->
                foldConversion(c);
            case BoundReferenceExpr.VariableRef v ->
                foldVariableRef(v);
            default ->
                throw new IllegalArgumentException("Unsupported expression type for constant folding: " + expr.getClass().getName());
        };
    }

    private static BoundConstant foldVariableRef(BoundReferenceExpr.VariableRef v) {
        var variable = v.getReferencedSymbol();
        if (!variable.isConstantVariable()) {
            throw new IllegalArgumentException("Variable " + variable.name() + " is not compile-time foldable.");
        }

        return variable.getConstant();
    }

    private static BoundConstant foldConversion(BoundConversionExpr c) {
        BoundConstant foldedOperand = fold(c.operand());
        return foldedOperand.convertTo(c.type());
    }

    private static BoundConstant foldUnary(BoundUnaryExpr u) {
        BoundConstant foldedOperand = fold(u.operand);

        return foldedOperand.applyUnaryOperator(u.operator);
    }

    private static BoundConstant foldBinary(BoundBinaryExpr b) {
        BoundConstant foldedLeft = fold(b.left);
        BoundConstant foldedRight = fold(b.right);

        return foldedLeft.applyBinaryOperator(b.operator, foldedRight);
    }
}
