package com.orca.compiler.core.controlflow;

import java.util.List;

import com.orca.compiler.core.boundtree.BoundStatement;

public final class BasicBlock {
    private final boolean isStart;
    private final boolean isEnd;
    private final List<BoundStatement> statements;
    private final List<BasicBlockBranch> outgoingBranches;
    private final List<BasicBlockBranch> incomingBranches;

    public BasicBlock(List<BoundStatement> statements) {
        this(false);
        this.statements.addAll(statements);
    }

    public BasicBlock(boolean isStart) {
        this.isStart = isStart;
        this.isEnd = !isStart;
        this.statements = new java.util.ArrayList<>();
        this.outgoingBranches = new java.util.ArrayList<>();
        this.incomingBranches = new java.util.ArrayList<>();
    }

    public List<BoundStatement> statements() {
        return statements;
    }

    public List<BasicBlockBranch> outgoingBranches() {
        return outgoingBranches;
    }

    public List<BasicBlockBranch> incomingBranches() {
        return incomingBranches;
    }

    public void addStatement(BoundStatement statement) {
        statements.add(statement);
    }

    public void addStatements(List<BoundStatement> statements) {
        this.statements.addAll(statements);
    }

    public void addOutgoingBranch(BasicBlockBranch branch) {
        outgoingBranches.add(branch);
    }

    public void removeOutgoingBranch(BasicBlockBranch branch) {
        outgoingBranches.remove(branch);
    }

    public void addIncomingBranch(BasicBlockBranch branch) {
        incomingBranches.add(branch);
    }

    public void removeIncomingBranch(BasicBlockBranch branch) {
        incomingBranches.remove(branch);
    }

    public boolean isStart() {
        return isStart;
    }

    public boolean isEnd() {
        return isEnd;
    }
}
