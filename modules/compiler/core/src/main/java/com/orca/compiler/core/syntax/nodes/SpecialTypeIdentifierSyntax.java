package com.orca.compiler.core.syntax.nodes;

import com.orca.compiler.core.lexer.SpecialTypeKind;
import com.orca.compiler.core.syntax.SyntaxToken;
import com.orca.compiler.core.syntax.SyntaxVisitor;

public final class SpecialTypeIdentifierSyntax extends IdentifierSyntax {

    private final SpecialTypeKind kind;
    private final SyntaxToken token;

    public SpecialTypeIdentifierSyntax(SpecialTypeKind kind, SyntaxToken token) {
        this.kind = kind;
        this.token = token;
    }

    @Override
    public SyntaxToken identifierToken() {
        return token;
    }

    public SpecialTypeKind getKind() {
        return kind;
    }

    @Override
    public void accept(SyntaxVisitor visitor) {
        visitor.visitSpecialTypeIdentifier(this);
    }

    @Override
    public String toString() {
        return "SpecialTypeIdentifierSyntax{"
                + "kind=" + kind
                + ", token=" + token
                + '}';
    }
}
