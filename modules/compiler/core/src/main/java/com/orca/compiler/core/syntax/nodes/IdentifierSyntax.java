package com.orca.compiler.core.syntax.nodes;

import com.orca.compiler.core.syntax.SyntaxNode;
import com.orca.compiler.core.syntax.SyntaxToken;

public sealed abstract class IdentifierSyntax extends SyntaxNode permits
        SimpleIdentifierSyntax, QualifiedIdentifierSyntax, SpecialTypeIdentifierSyntax {

    public abstract SyntaxToken identifierToken();

    public String name() {
        return identifierToken().text();
    }

    public String qualifiedName() {
        if (this instanceof QualifiedIdentifierSyntax qualified) {
            return qualified.left().qualifiedName() + "." + name();
        } else {
            return name();
        }
    }
}
