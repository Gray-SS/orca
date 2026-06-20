package com.orca.compiler.core.boundtree;

import java.util.List;

import com.orca.compiler.core.Debug;
import com.orca.compiler.core.JvmTypeMapper;
import com.orca.compiler.core.boundtree.statements.BoundBlockStmt;
import com.orca.compiler.core.symbols.MethodSymbol;
import com.orca.compiler.core.symbols.ParameterSymbol;
import com.orca.compiler.core.typesystem.LangType;

public final class BoundMethod extends BoundNode {

    private final BoundNode owner;
    @Child private final BoundBlockStmt body;
    private final MethodSymbol symbol;

    public BoundMethod(BoundNode owner, MethodSymbol symbol, BoundBlockStmt body) {
        Debug.requireNotNull(owner, "Owner cannot be null for a BoundMethod.");
        Debug.assertTrue(owner instanceof BoundType || owner instanceof BoundNamespace, "Owner of a BoundMethod must be either a BoundType or a BoundNamespace.");

        Debug.requireNotNull(symbol, "Symbol cannot be null for a BoundMethod.");
        Debug.requireNotNull(body, "Body cannot be null for a BoundMethod.");

        this.owner = owner;
        this.symbol = symbol;
        this.body = body;
    }

    public String getName() {
        return symbol.name();
    }

    public String getDescriptor() {
        return JvmTypeMapper.getCallableDescriptor(symbol);
    }

    public BoundNode getOwner() {
        return owner;
    }

    public MethodSymbol getSymbol() {
        return symbol;
    }

    public LangType returnType() {
        return symbol.returnType();
    }

    public List<? extends ParameterSymbol> parameters() {
        return symbol.parameters();
    }

    public BoundBlockStmt getBody() {
        return body;
    }

    public String name() {
        return symbol.name();
    }

    @Override
    public BoundNodeKind kind() {
        return BoundNodeKind.METHOD;
    }

    @Override
    public <R> R accept(BoundVisitor<R> visitor) {
        return visitor.visitMethod(this);
    }
}
