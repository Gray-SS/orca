package com.orca.compiler.core.syntax.statements;

import com.orca.compiler.core.syntax.StatementSyntax;
import com.orca.compiler.core.syntax.SyntaxVisitor;
import com.orca.compiler.core.syntax.nodes.VariableDeclaratorSyntax;

public class VariableDeclarationStatement extends StatementSyntax {

    private final VariableDeclaratorSyntax variableDeclarator;

    public VariableDeclarationStatement(VariableDeclaratorSyntax variableDeclarator) {
        this.variableDeclarator = variableDeclarator;
    }

    /**
     * Gets the variable declarator for the variable declaration.
     *
     * @return The variable declarator.
     */
    public VariableDeclaratorSyntax variableDeclarator() {
        return variableDeclarator;
    }

    @Override
    public void accept(SyntaxVisitor visitor) {
        visitor.visitVariableDeclarationStatement(this);
    }

    @Override
    public String toString() {
        return "VariableDeclarationStatement(variableDeclarator=" + variableDeclarator + ")";
    }
}
