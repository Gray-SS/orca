package com.orca.compiler.core.controlflow;

import java.util.List;
import java.util.Map;

import com.orca.compiler.core.boundtree.BoundExpression;
import com.orca.compiler.core.boundtree.BoundLabel;
import com.orca.compiler.core.boundtree.BoundStatement;
import com.orca.compiler.core.boundtree.expressions.BoundLiteralExpr;
import com.orca.compiler.core.boundtree.expressions.BoundOperator;
import com.orca.compiler.core.boundtree.expressions.BoundUnaryExpr;
import com.orca.compiler.core.boundtree.statements.BoundConditionalGotoStmt;
import com.orca.compiler.core.boundtree.statements.BoundGotoStmt;
import com.orca.compiler.core.boundtree.statements.BoundLabelStmt;
import com.orca.compiler.core.boundtree.statements.BoundReturnStmt;

public final class GraphBuilder {

    private final List<BasicBlockBranch> branches = new java.util.ArrayList<>();
    private final Map<BoundLabel, BasicBlock> labelToBlock = new java.util.HashMap<>();

    private final BasicBlock start = new BasicBlock(true);
    private final BasicBlock end = new BasicBlock(false);

    public ControlFlowGraph build(List<BasicBlock> blocks) {
        if (blocks.isEmpty()) {
            connect(start, end, null);
        } else {
            connect(start, blocks.get(0), null);
        }

        for (BasicBlock block : blocks) {
            for (BoundStatement statement : block.statements()) {
                if (!(statement instanceof BoundLabelStmt labelStatement)) {
                    continue;
                }

                BoundLabel label = labelStatement.label;
                if (labelToBlock.get(label) != null) {
                    throw new IllegalStateException("Duplicate label: " + label.name());
                }

                labelToBlock.put(labelStatement.label, block);
            }
        }

        for (int i = 0; i < blocks.size(); i++) {
            var current = blocks.get(i);
            var next = i == blocks.size() - 1 ? end : blocks.get(i + 1);

            for (BoundStatement statement : current.statements()) {
                boolean isLastStatementInBlock = statement == current.statements().get(current.statements().size() - 1);
                switch (statement) {
                    case BoundGotoStmt gs -> {
                        var toBlock = labelToBlock.get(gs.label);
                        connect(current, toBlock, null);
                    }
                    case BoundConditionalGotoStmt cgs -> {
                        var thenBlock = labelToBlock.get(cgs.label);
                        var elseBlock = next;
                        var jumpCondition = cgs.inverseCondition ? negate(cgs.condition) : cgs.condition;
                        var fallthroughCondition = cgs.inverseCondition ? cgs.condition : negate(cgs.condition);
                        connect(current, thenBlock, jumpCondition);
                        connect(current, elseBlock, fallthroughCondition);
                    }
                    case BoundReturnStmt rs ->
                        connect(current, end, null);
                    default -> {
                        if (!isLastStatementInBlock) {
                            continue;
                        }
                        connect(current, next, null);
                    }
                }
            }
        }

        while (true) {
            boolean removed = false;
            for (var block : new java.util.ArrayList<>(blocks)) {
                if (block.incomingBranches().isEmpty() && block != start) {
                    removeBlock(blocks, block);
                    removed = true;
                    break;
                }
            }

            if (!removed) {
                break;
            }
        }

        blocks.add(0, start);
        blocks.add(end);

        return new ControlFlowGraph(start, end, blocks, branches);
    }

    private BoundExpression negate(BoundExpression condition) {
        if (condition instanceof BoundLiteralExpr l) {
            var constant = l.getConstantAsBool();
            return new BoundLiteralExpr(constant.negate());
        }

        return new BoundUnaryExpr(BoundOperator.Unary.LOGICAL_NOT, condition);
    }

    private void connect(BasicBlock from, BasicBlock to, BoundExpression condition) {
        if (condition instanceof BoundLiteralExpr l) {
            var constant = l.getConstantAsBool();
            if (!constant.value()) {
                // Condition is always false, so we don't need to create a branch.
                return;
            }

            condition = null;
        }

        var branch = new BasicBlockBranch(from, to, condition);
        from.addOutgoingBranch(branch);
        to.addIncomingBranch(branch);
        branches.add(branch);
    }

    private void removeBlock(List<BasicBlock> blocks, BasicBlock block) {
        for (BasicBlockBranch branch : block.incomingBranches()) {
            branch.from().removeOutgoingBranch(branch);
            branches.remove(branch);
        }

        for (BasicBlockBranch branch : block.outgoingBranches()) {
            branch.to().removeIncomingBranch(branch);
            branches.remove(branch);
        }

        blocks.remove(block);
    }
}
