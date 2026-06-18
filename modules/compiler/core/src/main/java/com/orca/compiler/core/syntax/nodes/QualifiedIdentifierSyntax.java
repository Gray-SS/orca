package com.orca.compiler.core.syntax.nodes;

import java.util.List;

import com.orca.compiler.core.syntax.SyntaxNode;
import com.orca.compiler.core.syntax.SyntaxToken;
import com.orca.compiler.core.syntax.SyntaxVisitor;

public final class QualifiedIdentifierSyntax extends IdentifierSyntax {

    private final IdentifierSyntax left;
    private final SyntaxToken doubleColon;
    private final SimpleIdentifierSyntax right;

    public QualifiedIdentifierSyntax(IdentifierSyntax left, SyntaxToken doubleColon, SimpleIdentifierSyntax right) {
        this.left = left;
        this.doubleColon = doubleColon;
        this.right = right;
    }

    @Override
    public SyntaxToken identifierToken() {
        return right.identifierToken();
    }

    /**
     * Get the left part of the member identifier, which can be another member
     * identifier or a simple identifier.
     *
     * @return The left part of the member identifier.
     */
    public IdentifierSyntax left() {
        return left;
    }

    /**
     * Get the right part of the member identifier, which is a simple
     * identifier.
     *
     * @return The right part of the member identifier.
     */
    public SimpleIdentifierSyntax right() {
        return right;
    }

    @Override
    public List<SyntaxNode> children() {
        return List.of(left, doubleColon, right);
    }

    @Override
    public void accept(SyntaxVisitor visitor) {
        visitor.visitQualifiedIdentifierSyntax(this);
    }

    @Override
    public String toString() {
        return "MemberIdentifier(" + left.toString() + "." + right.text() + ")";
    }
}
