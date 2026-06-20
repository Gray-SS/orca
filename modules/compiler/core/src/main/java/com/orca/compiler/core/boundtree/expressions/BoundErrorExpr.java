package com.orca.compiler.core.boundtree.expressions;

import com.orca.compiler.core.boundtree.BoundExpression;
import com.orca.compiler.core.boundtree.BoundNodeKind;
import com.orca.compiler.core.boundtree.BoundVisitor;
import com.orca.compiler.core.syntax.expressions.ErrorExpressionSyntax;
import com.orca.compiler.core.text.SourceSpan;
import com.orca.compiler.core.typesystem.LangType;

public final class BoundErrorExpr extends BoundExpression {

    private final ErrorExpressionSyntax syntax;

    public BoundErrorExpr(ErrorExpressionSyntax syntax) {
        this.syntax = syntax;
    }

    @Override
    public SourceSpan span() {
        return syntax.span();
    }

    @Override
    public LangType type() {
        return LangType.Error;
    }

    @Override
    public BoundNodeKind kind() {
        return BoundNodeKind.ERROR_EXPRESSION;
    }

    @Override
    public <R> R accept(BoundVisitor<R> visitor) {
        return visitor.visitErrorExpr(this);
    }
}
