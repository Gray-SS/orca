package com.orca.compiler.core.syntax.statements;

import javax.annotation.Nullable;

import com.orca.compiler.core.syntax.ExpressionSyntax;
import com.orca.compiler.core.syntax.StatementSyntax;
import com.orca.compiler.core.syntax.SyntaxToken;
import com.orca.compiler.core.syntax.SyntaxVisitor;

public class ReturnStmt extends StatementSyntax {
    private final SyntaxToken returnToken;
    private final @Nullable ExpressionSyntax expression;

    public ReturnStmt(SyntaxToken returnToken, ExpressionSyntax expression) {
        this.returnToken = returnToken;
        this.expression = expression;
    }

    /**
     * Gets the return token of this return statement.
     * @return The return token of this return statement.
     */
    public SyntaxToken returnToken() {
        return returnToken;
    }

    /**
     * Gets the expression being returned by this return statement, or null if this is a void return statement.
     * @return The expression being returned by this return statement, or null if this is a void return statement.
     */
    @Nullable
    public ExpressionSyntax expression() {
        return expression;
    }

    @Override
    public void accept(SyntaxVisitor visitor) {
        visitor.visitReturnStmt(this);
    }

    @Override
    public String toString() {
        return "ReturnStatement";
    }
}
