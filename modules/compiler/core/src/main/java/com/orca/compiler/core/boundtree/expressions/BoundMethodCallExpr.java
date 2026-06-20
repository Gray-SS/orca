package com.orca.compiler.core.boundtree.expressions;

import java.util.List;

import com.google.common.base.Preconditions;
import com.orca.compiler.core.boundtree.BoundExpression;
import com.orca.compiler.core.boundtree.BoundNodeKind;
import com.orca.compiler.core.boundtree.BoundVisitor;
import com.orca.compiler.core.boundtree.Child;
import com.orca.compiler.core.symbols.MethodSymbol;
import com.orca.compiler.core.typesystem.LangType;

public final class BoundMethodCallExpr extends BoundExpression {

    public final MethodSymbol methodSymbol;
    @Child public final List<BoundExpression> arguments;
    @Child public final BoundReferenceExpr methodRef;

    public BoundMethodCallExpr(BoundReferenceExpr refExpr, List<BoundExpression> arguments) {
        Preconditions.checkNotNull(refExpr, "refExpr cannot be null");
        Preconditions.checkNotNull(arguments, "arguments cannot be null");
        Preconditions.checkArgument(refExpr.getReferencedSymbol() instanceof MethodSymbol, "refExpr must reference a method symbol");

        this.methodSymbol = (MethodSymbol) refExpr.getReferencedSymbol();
        this.methodRef = refExpr;
        this.arguments = arguments;
    }

    @Override
    public BoundNodeKind kind() {
        return BoundNodeKind.CALL_EXPR;
    }

    @Override
    public <R> R accept(BoundVisitor<R> visitor) {
        return visitor.visitMethodCallExpr(this);
    }

    @Override
    public LangType type() {
        return methodSymbol.returnType();
    }

    @Override
    public boolean isCompileTimeFoldable() {
        return false;
    }
}
