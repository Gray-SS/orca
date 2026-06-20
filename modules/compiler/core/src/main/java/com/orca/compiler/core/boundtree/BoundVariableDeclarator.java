package com.orca.compiler.core.boundtree;

import com.orca.compiler.core.symbols.VariableSymbol;

public final class BoundVariableDeclarator extends BoundNode {

    private final VariableSymbol variable;
    @Child private final BoundExpression initializer;

    public BoundVariableDeclarator(VariableSymbol variable, BoundExpression initializer) {
        this.variable = variable;
        this.initializer = initializer;
    }

    public VariableSymbol variable() {
        return variable;
    }

    public BoundExpression initializer() {
        return initializer;
    }

    @Override
    public BoundNodeKind kind() {
        return BoundNodeKind.VARIABLE_DECLARATOR;
    }

    @Override
    public <R> R accept(BoundVisitor<R> visitor) {
        return visitor.visitVariableDeclarator(this);
    }
}
