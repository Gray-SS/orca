package com.orca.compiler.core.syntax.statements;

import com.orca.compiler.core.syntax.ExpressionSyntax;
import com.orca.compiler.core.syntax.StatementSyntax;
import com.orca.compiler.core.syntax.SyntaxVisitor;
import com.orca.compiler.core.syntax.nodes.VariableDeclaratorSyntax;

public class ForStmt extends StatementSyntax {

    private final VariableDeclaratorSyntax variableDeclarator;

    private final ExpressionSyntax conditionExpr;
    private final ExpressionSyntax stepExpr;

    private final BlockStmt body;

    public ForStmt(VariableDeclaratorSyntax variableDeclarator, ExpressionSyntax conditionExpr, ExpressionSyntax stepExpr, BlockStmt body) {
        this.variableDeclarator = variableDeclarator;
        this.conditionExpr = conditionExpr;
        this.stepExpr = stepExpr;
        this.body = body;
    }

    /**
     * Gets the variable declarator for the loop variable of this for statement.
     *
     * @return The variable declarator for the loop variable of this for
     * statement.
     */
    public VariableDeclaratorSyntax variableDeclarator() {
        return variableDeclarator;
    }

    public ExpressionSyntax conditionExpr() {
        return conditionExpr;
    }

    /**
     * Gets the step expression of this for statement.
     *
     * @return The step expression of this for statement.
     */
    public ExpressionSyntax stepExpr() {
        return stepExpr;
    }

    /**
     * Gets the body of this for statement.
     *
     * @return The body of this for statement.
     */
    public BlockStmt body() {
        return body;
    }

    @Override
    public void accept(SyntaxVisitor visitor) {
        visitor.visitForStmt(this);
    }

    @Override
    public String toString() {
        return "ForStatement";
    }

}
