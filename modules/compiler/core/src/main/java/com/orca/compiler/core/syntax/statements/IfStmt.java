package com.orca.compiler.core.syntax.statements;

import com.orca.compiler.core.syntax.StatementSyntax;
import com.orca.compiler.core.syntax.SyntaxList;
import com.orca.compiler.core.syntax.SyntaxVisitor;
import com.orca.compiler.core.syntax.nodes.ElseClauseSyntax;
import com.orca.compiler.core.syntax.nodes.IfClauseSyntax;

public class IfStmt extends StatementSyntax {
    private final IfClauseSyntax ifClause;
    private final SyntaxList<IfClauseSyntax> elseIfClauses;
    private final ElseClauseSyntax elseClause;

    public IfStmt(IfClauseSyntax ifClause, SyntaxList<IfClauseSyntax> elseIfClauses, ElseClauseSyntax elseClause) {
        this.ifClause = ifClause;
        this.elseIfClauses = elseIfClauses;
        this.elseClause = elseClause;
    }

    /**
     * Gets the if clause of this if statement.
     * @return The if clause of this if statement.
     */
    public IfClauseSyntax ifClause() {
        return ifClause;
    }

    /**
     * Gets the else-if clauses of this if statement.
     * @return The else-if clauses of this if statement.
     */
    public SyntaxList<IfClauseSyntax> elseIfClauses() {
        return elseIfClauses;
    }

    /**
     * Gets the else clause of this if statement, or null if this if statement does not have an else clause.
     * @return The else clause of this if statement, or null if this if statement does not have an else clause.
     */
    public ElseClauseSyntax elseClause() {
        return elseClause;
    }

    @Override
    public void accept(SyntaxVisitor visitor) {
        visitor.visitIfStmt(this);
    }

    @Override
    public String toString() {
        return "IfStatement";
    }
}
