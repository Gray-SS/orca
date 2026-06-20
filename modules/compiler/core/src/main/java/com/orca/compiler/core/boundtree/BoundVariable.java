package com.orca.compiler.core.boundtree;

import com.orca.compiler.core.Debug;
import com.orca.compiler.core.symbols.VariableSymbol;

public final class BoundVariable extends BoundNode {

    private final BoundNode owner;
    private final VariableSymbol symbol;
    @Child private final BoundExpression initializer;

    public BoundVariable(BoundNode owner, VariableSymbol symbol, BoundExpression initializer) {
        Debug.requireNotNull(owner, "Owner cannot be null for a BoundVariable.");
        Debug.assertTrue(owner instanceof BoundType || owner instanceof BoundNamespace, "Owner of a BoundVariable must be either a BoundType or a BoundNamespace.");
        Debug.requireNotNull(symbol, "Symbol cannot be null for a BoundVariable.");

        this.owner = owner;
        this.symbol = symbol;
        this.initializer = initializer;
    }

    public String getName() {
        return symbol.name();
    }

    public BoundNode getOwner() {
        return owner;
    }

    public VariableSymbol getSymbol() {
        return symbol;
    }

    public BoundExpression initializer() {
        return initializer;
    }

    @Override
    public BoundNodeKind kind() {
        return BoundNodeKind.VARIABLE;
    }

    @Override
    public <R> R accept(BoundVisitor<R> visitor) {
        return visitor.visitVariable(this);
    }
}
