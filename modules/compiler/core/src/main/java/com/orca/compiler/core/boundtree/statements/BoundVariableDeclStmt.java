package com.orca.compiler.core.boundtree.statements;

import com.orca.compiler.core.boundtree.BoundExpression;
import com.orca.compiler.core.boundtree.BoundNodeKind;
import com.orca.compiler.core.boundtree.BoundStatement;
import com.orca.compiler.core.boundtree.BoundVariableDeclarator;
import com.orca.compiler.core.symbols.VariableSymbol;

public final class BoundVariableDeclStmt extends BoundStatement {

    private final BoundVariableDeclarator variableDeclarator;

    public BoundVariableDeclStmt(BoundVariableDeclarator variableDeclarator) {
        this.variableDeclarator = variableDeclarator;
    }

    public BoundVariableDeclStmt(VariableSymbol variable, BoundExpression initializer) {
        this.variableDeclarator = new BoundVariableDeclarator(variable, initializer);
    }

    public VariableSymbol variable() {
        return variableDeclarator.variable();
    }

    public BoundExpression initializer() {
        return variableDeclarator.initializer();
    }

    @Override
    public BoundNodeKind kind() {
        return BoundNodeKind.VARIABLE_DECL_STMT;
    }
}
