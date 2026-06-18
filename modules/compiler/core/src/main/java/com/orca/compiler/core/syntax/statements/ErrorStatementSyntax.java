package com.orca.compiler.core.syntax.statements;

import java.util.List;

import com.orca.compiler.core.syntax.StatementSyntax;
import com.orca.compiler.core.syntax.SyntaxNode;
import com.orca.compiler.core.syntax.SyntaxToken;
import com.orca.compiler.core.syntax.SyntaxVisitor;
import com.orca.compiler.core.text.SourceSpan;

public final class ErrorStatementSyntax extends StatementSyntax {
    private final SyntaxToken unexpectedToken;

    public ErrorStatementSyntax(SyntaxToken unexpectedToken) {
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
        visitor.visitErrorStatement(this);
    }

    @Override
    public String toString() {
        return "ErrorStatement";
    }
}
