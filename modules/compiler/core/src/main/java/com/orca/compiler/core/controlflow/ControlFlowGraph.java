package com.orca.compiler.core.controlflow;

import java.util.List;

import com.orca.compiler.core.boundtree.statements.BoundBlockStmt;
import com.orca.compiler.core.boundtree.statements.BoundReturnStmt;

public final class ControlFlowGraph {

    private final BasicBlock start;
    private final BasicBlock end;
    private final List<BasicBlock> blocks;
    private final List<BasicBlockBranch> branches;

    public ControlFlowGraph(BasicBlock start, BasicBlock end, List<BasicBlock> blocks, List<BasicBlockBranch> branches) {
        this.start = start;
        this.end = end;
        this.blocks = blocks;
        this.branches = branches;
    }

    public BasicBlock start() {
        return start;
    }

    public BasicBlock end() {
        return end;
    }

    public List<BasicBlock> blocks() {
        return blocks;
    }

    public List<BasicBlockBranch> branches() {
        return branches;
    }

    public static ControlFlowGraph create(BoundBlockStmt block) {
        BasicBlockBuilder blockBuilder = new BasicBlockBuilder();
        List<BasicBlock> blocks = blockBuilder.build(block);

        GraphBuilder builder = new GraphBuilder();
        return builder.build(blocks);
    }

    public static boolean allPathReturns(BoundBlockStmt body) {
        var graph = create(body);

        for (var branch : graph.end().incomingBranches()) {
            var statements = branch.from().statements();
            if (statements.isEmpty()) {
                return false;
            }

            var lastStatement = statements.getLast();
            if (lastStatement == null || !(lastStatement instanceof BoundReturnStmt)) {
                return false;
            }
        }

        return true;
    }
}
