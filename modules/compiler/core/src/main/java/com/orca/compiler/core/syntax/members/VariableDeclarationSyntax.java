package com.orca.compiler.core.syntax.members;

import com.orca.compiler.core.syntax.SyntaxVisitor;
import com.orca.compiler.core.syntax.nodes.VariableDeclaratorSyntax;

public final class VariableDeclarationSyntax extends MemberSyntax {

    private final VariableDeclaratorSyntax variableDeclarator;

    public VariableDeclarationSyntax(VariableDeclaratorSyntax variableDeclarator) {
        this.variableDeclarator = variableDeclarator;
    }

    public VariableDeclaratorSyntax variableDeclarator() {
        return variableDeclarator;
    }

    @Override
    public void accept(SyntaxVisitor visitor) {
        visitor.visitVariableDeclaration(this);
    }

    @Override
    public String toString() {
        return "VariableDeclarationSyntax(variableDeclarator=" + variableDeclarator + ")";
    }
}
