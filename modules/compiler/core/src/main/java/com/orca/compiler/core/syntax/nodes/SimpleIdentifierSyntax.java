package com.orca.compiler.core.syntax.nodes;

import java.util.List;

import com.orca.compiler.core.syntax.SyntaxNode;
import com.orca.compiler.core.syntax.SyntaxToken;
import com.orca.compiler.core.syntax.SyntaxVisitor;

public final class SimpleIdentifierSyntax extends IdentifierSyntax {
    private final SyntaxToken identifierToken;

    public SimpleIdentifierSyntax(SyntaxToken identifierToken) {
        this.identifierToken = identifierToken;
    }

    @Override
    public SyntaxToken identifierToken() {
        return identifierToken;
    }

    @Override
    public List<SyntaxNode> children() {
        return List.of(identifierToken);
    }

    @Override
    public void accept(SyntaxVisitor visitor) {
        visitor.visitSimpleIdentifierSyntax(this);
    }

    @Override
    public String toString() {
        return "SimpleIdentifier(" + identifierToken().text() + ")";
    }
}
