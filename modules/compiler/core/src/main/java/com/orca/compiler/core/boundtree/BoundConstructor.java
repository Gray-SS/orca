package com.orca.compiler.core.boundtree;

import com.orca.compiler.core.boundtree.statements.BoundBlockStmt;
import com.orca.compiler.core.symbols.ConstructorSymbol;

public final class BoundConstructor extends BoundNode {

    private final ConstructorSymbol constructor;
    private final BoundBlockStmt body;

    public BoundConstructor(ConstructorSymbol constructor, BoundBlockStmt body) {
        this.constructor = constructor;
        this.body = body;
    }

    public ConstructorSymbol getSymbol() {
        return constructor;
    }

    public BoundBlockStmt getBody() {
        return body;
    }

    @Override
    public BoundNodeKind kind() {
        return BoundNodeKind.CONSTRUCTOR;
    }
}
