package com.orca.compiler.core.syntax.members;

import com.orca.compiler.core.syntax.StatementSyntax;
import com.orca.compiler.core.syntax.SyntaxVisitor;

public final class GlobalStatementSyntax extends CompilationMemberSyntax {
    private final StatementSyntax statement;

    public GlobalStatementSyntax(StatementSyntax statement) {
        this.statement = statement;
    }

    /**
     * Gets the statement contained in this global statement syntax node.
     * @return The statement contained in this global statement syntax node.
     */
    public StatementSyntax statement() {
        return statement;
    }

    @Override
    public void accept(SyntaxVisitor visitor) {
        visitor.visitGlobalStatement(this);
    }

    @Override
    public String toString() {
        return "GlobalStatement(" + statement.toString() + ")";
    }
}
