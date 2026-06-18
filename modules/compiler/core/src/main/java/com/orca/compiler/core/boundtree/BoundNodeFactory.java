package com.orca.compiler.core.boundtree;

import java.util.List;

import com.orca.compiler.core.boundtree.expressions.BoundAssignmentExpr;
import com.orca.compiler.core.boundtree.statements.BoundBlockStmt;
import com.orca.compiler.core.boundtree.statements.BoundConditionalGotoStmt;
import com.orca.compiler.core.boundtree.statements.BoundGotoStmt;
import com.orca.compiler.core.boundtree.statements.BoundLabelStmt;
import com.orca.compiler.core.boundtree.statements.BoundReturnStmt;
import com.orca.compiler.core.text.SourceSpan;

public final class BoundNodeFactory {

    public static <T extends BoundNode> T createSynthesized(SourceSpan span, T bound) {
        bound.setSynthesizedSpan(span);
        return bound;
    }

    public static BoundAssignmentExpr synthesizedSimpleAssignment(SourceSpan span, BoundExpression target, BoundExpression value) {
        return createSynthesized(span, BoundAssignmentExpr.simpleAssignment(target, value));
    }

    public static BoundBlockStmt synthesizedBlock(SourceSpan span, BoundStatement... statements) {
        return createSynthesized(span, new BoundBlockStmt(List.of(statements)));
    }

    public static BoundBlockStmt synthesizedBlock(SourceSpan span, List<BoundStatement> statements) {
        return createSynthesized(span, new BoundBlockStmt(statements));
    }

    public static BoundLabelStmt synthesizedLabel(SourceSpan span, BoundLabel label) {
        return createSynthesized(span, new BoundLabelStmt(label));
    }

    public static BoundGotoStmt synthesizedGoto(SourceSpan span, BoundLabel label) {
        return createSynthesized(span, new BoundGotoStmt(label));
    }

    public static BoundConditionalGotoStmt synthesizedConditionalGoto(SourceSpan span, BoundLabel label, BoundExpression condition, boolean inverseCondition) {
        return createSynthesized(span, new BoundConditionalGotoStmt(label, condition, inverseCondition));
    }

    public static BoundReturnStmt synthesizedReturn(SourceSpan span, BoundExpression expression) {
        return createSynthesized(span, new BoundReturnStmt(expression));
    }
}
