package com.orca.compiler.core.boundtree;

public final class BoundProgram extends BoundNode {
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
}
