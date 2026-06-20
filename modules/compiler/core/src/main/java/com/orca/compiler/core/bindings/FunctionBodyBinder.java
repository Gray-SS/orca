package com.orca.compiler.core.bindings;

import java.util.List;

import com.orca.compiler.core.Debug;
import com.orca.compiler.core.boundtree.BoundNodeForm;
import com.orca.compiler.core.boundtree.statements.BoundBlockStmt;
import com.orca.compiler.core.symbols.CallableSymbol;
import com.orca.compiler.core.symbols.Symbol;
import com.orca.compiler.core.syntax.statements.BlockStmt;

public final class FunctionBodyBinder extends LocalBinder {

    private final CallableSymbol callableSymbol;

    public FunctionBodyBinder(Binder parent, CallableSymbol callableSymbol) {
        super(parent);
        Debug.requireNotNull(callableSymbol, "callableSymbol cannot be null in FunctionBodyBinder");

        this.callableSymbol = callableSymbol;
    }

    public CallableSymbol getCallableSymbol() {
        return callableSymbol;
    }

    public BoundBlockStmt bindBody(BlockStmt bodySyntax) {
        var boundBlock = getSemanticModel().bind(bodySyntax, BoundNodeForm.LOWERED);
        return (BoundBlockStmt) boundBlock;
    }

    @Override
    public Symbol lookupSymbol(String name) {
        // Check the function's parameters
        for (var param : callableSymbol.parameters()) {
            if (param.name().equals(name)) {
                return param;
            }
        }

        // If not found, check the outer scopes (global, etc.)
        return super.lookupSymbol(name);
    }

    @Override
    public List<Symbol> lookupSymbols(String name) {
        var symbols = super.lookupSymbols(name);

        // Check the function's parameters
        for (var param : callableSymbol.parameters()) {
            if (param.name().equals(name)) {
                symbols.add(param);
            }
        }

        return symbols;
    }
}
