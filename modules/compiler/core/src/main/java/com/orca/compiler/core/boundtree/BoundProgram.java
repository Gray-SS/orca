package com.orca.compiler.core.boundtree;

public final class BoundProgram extends BoundNode {

    @Child
    private final BoundNamespace globalNamespace;

    public BoundProgram(BoundNamespace globalNamespace) {
        this.globalNamespace = globalNamespace;
    }

    public BoundNamespace getGlobalNamespace() {
        return globalNamespace;
    }

    @Override
    public BoundNodeKind kind() {
        return BoundNodeKind.PROGRAM;
    }

    @Override
    public <R> R accept(BoundVisitor<R> visitor) {
        return visitor.visitProgram(this);
    }
}
