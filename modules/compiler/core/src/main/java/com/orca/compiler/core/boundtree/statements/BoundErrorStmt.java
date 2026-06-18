package com.orca.compiler.core.boundtree.statements;

import com.orca.compiler.core.boundtree.BoundNodeKind;
import com.orca.compiler.core.boundtree.BoundStatement;
import com.orca.compiler.core.syntax.statements.ErrorStatementSyntax;
import com.orca.compiler.core.text.SourceSpan;

public final class BoundErrorStmt extends BoundStatement {

    private final ErrorStatementSyntax syntax;

    public BoundErrorStmt(ErrorStatementSyntax syntax) {
        this.syntax = syntax;
    }

    @Override
    public SourceSpan span() {
        return syntax.span();
    }

    @Override
    public BoundNodeKind kind() {
        return BoundNodeKind.ERROR_STATEMENT;
    }
}
