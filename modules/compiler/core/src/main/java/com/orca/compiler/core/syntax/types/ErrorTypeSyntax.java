package com.orca.compiler.core.syntax.types;

import java.util.List;

import com.orca.compiler.core.syntax.SyntaxNode;
import com.orca.compiler.core.syntax.SyntaxToken;
import com.orca.compiler.core.syntax.SyntaxVisitor;
import com.orca.compiler.core.text.SourceSpan;

public final class ErrorTypeSyntax extends TypeSyntax {

    private final SyntaxToken unexpectedToken;

    public ErrorTypeSyntax(SyntaxToken unexpectedToken) {
        this.unexpectedToken = unexpectedToken;
    }

    public SyntaxToken unexpectedToken() {
        return unexpectedToken;
    }

    @Override
    public SourceSpan span() {
        return unexpectedToken.span();
    }

    @Override
    public List<SyntaxNode> children() {
        return List.of(unexpectedToken);
    }

    @Override
    public void accept(SyntaxVisitor visitor) {
        visitor.visitErrorType(this);
    }

    @Override
    public String toString() {
        return "ErrorType";
    }
}
