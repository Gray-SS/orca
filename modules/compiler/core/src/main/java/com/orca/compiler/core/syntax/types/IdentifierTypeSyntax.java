package com.orca.compiler.core.syntax.types;

import com.orca.compiler.core.syntax.SyntaxVisitor;
import com.orca.compiler.core.syntax.nodes.IdentifierSyntax;

public final class IdentifierTypeSyntax extends TypeSyntax {
    private final IdentifierSyntax identifier;

    public IdentifierTypeSyntax(IdentifierSyntax identifier) {
        this.identifier = identifier;
    }

    /**
     * Gets the identifier of this type syntax.
     * @return The identifier of this type syntax.
     */
    public IdentifierSyntax identifier() {
        return identifier;
    }

    @Override
    public void accept(SyntaxVisitor visitor) {
        visitor.visitIdentifierTypeSyntax(this);
    }

    @Override
    public String toString() {
        return "IdentifierTypeSyntax(" + identifier.text() + ")";
    }
}
