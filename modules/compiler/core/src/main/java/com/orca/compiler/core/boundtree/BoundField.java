package com.orca.compiler.core.boundtree;

import com.orca.compiler.core.symbols.FieldSymbol;

public class BoundField extends BoundNode {

    private final FieldSymbol fieldSymbol;
    private final BoundType owner;

    public BoundField(BoundType owner, FieldSymbol fieldSymbol) {
        this.owner = owner;
        this.fieldSymbol = fieldSymbol;
    }

    public String getName() {
        return fieldSymbol.name();
    }

    public BoundType getOwner() {
        return owner;
    }

    public FieldSymbol getSymbol() {
        return fieldSymbol;
    }

    @Override
    public BoundNodeKind kind() {
        return BoundNodeKind.FIELD;
    }
}
