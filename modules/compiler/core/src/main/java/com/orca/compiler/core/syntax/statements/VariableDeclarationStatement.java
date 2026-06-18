package com.orca.compiler.core.syntax.statements;

import com.orca.compiler.core.syntax.ExpressionSyntax;
import com.orca.compiler.core.syntax.StatementSyntax;
import com.orca.compiler.core.syntax.SyntaxToken;
import com.orca.compiler.core.syntax.SyntaxVisitor;
import com.orca.compiler.core.syntax.nodes.SimpleIdentifierSyntax;
import com.orca.compiler.core.syntax.nodes.VariableDeclaratorSyntax;
import com.orca.compiler.core.syntax.types.TypeSyntax;

public class VariableDeclarationStatement extends StatementSyntax {

    private final VariableDeclaratorSyntax variableDeclarator;

    public VariableDeclarationStatement(SyntaxToken modifierToken, SimpleIdentifierSyntax identifier, TypeSyntax type, ExpressionSyntax initializer) {
        this.variableDeclarator = new VariableDeclaratorSyntax(modifierToken, identifier, type, initializer);
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
