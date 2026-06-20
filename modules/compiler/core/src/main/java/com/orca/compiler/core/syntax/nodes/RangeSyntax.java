package com.orca.compiler.core.syntax.nodes;

import com.orca.compiler.core.syntax.ExpressionSyntax;
import com.orca.compiler.core.syntax.SyntaxNode;
import com.orca.compiler.core.syntax.SyntaxVisitor;

public class RangeSyntax extends SyntaxNode {

    private final ExpressionSyntax start;
    private final ExpressionSyntax end;

    public RangeSyntax(ExpressionSyntax start, ExpressionSyntax end) {
        this.start = start;
        this.end = end;
    }

    /**
     * Gets the start expression of this range syntax.
     *
     * @return The start expression of this range syntax.
     */
    public ExpressionSyntax start() {
        return start;
    }

    /**
     * Gets the end expression of this range syntax.
     *
     * @return The end expression of this range syntax.
     */
    public ExpressionSyntax end() {
        return end;
    }

    @Override
    public void accept(SyntaxVisitor visitor) {
        visitor.visitRangeSyntax(this);
    }

    @Override
    public String toString() {
        return "RangeSyntax";
    }
}
