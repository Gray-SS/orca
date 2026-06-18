package com.orca.compiler.core.syntax.types;

import com.orca.compiler.core.lexer.SpecialTypeKind;
import com.orca.compiler.core.syntax.SyntaxToken;
import com.orca.compiler.core.syntax.SyntaxVisitor;

public final class SpecialTypeSyntax extends TypeSyntax {
    private final SyntaxToken token;
    private final SpecialTypeKind kind;

    public SpecialTypeSyntax(SyntaxToken token, SpecialTypeKind kind) {
        this.token = token;
        this.kind = kind;
    }

    public SyntaxToken getToken() {
        return token;
    }

    public SpecialTypeKind getKind() {
        return kind;
    }

    @Override
    public void accept(SyntaxVisitor visitor) {
        visitor.visitSpecialTypeSyntax(this);
    }

    @Override
    public String toString() {
        return token.text();
    }
}
