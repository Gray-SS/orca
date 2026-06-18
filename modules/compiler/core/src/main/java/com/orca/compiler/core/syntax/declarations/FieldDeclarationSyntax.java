package com.orca.compiler.core.syntax.declarations;

import com.orca.compiler.core.syntax.SyntaxNode;
import com.orca.compiler.core.syntax.SyntaxVisitor;
import com.orca.compiler.core.syntax.nodes.SimpleIdentifierSyntax;
import com.orca.compiler.core.syntax.types.TypeSyntax;

public class FieldDeclarationSyntax extends SyntaxNode {

    private final SimpleIdentifierSyntax identifier;
    private final TypeSyntax type;

    public FieldDeclarationSyntax(SimpleIdentifierSyntax identifier, TypeSyntax type) {
        this.identifier = identifier;
        this.type = type;
    }

    public SimpleIdentifierSyntax identifier() {
        return identifier;
    }

    public TypeSyntax type() {
        return type;
    }

    @Override
    public void accept(SyntaxVisitor visitor) {
        visitor.visitFieldDeclaration(this);
    }

    @Override
    public String toString() {
        return "FieldDeclaration(" + identifier.text() + ")";
    }
}
