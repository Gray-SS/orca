package com.orca.compiler.core.syntax.expressions;

import com.orca.compiler.core.lexer.TokenKind;
import com.orca.compiler.core.syntax.ExpressionSyntax;
import com.orca.compiler.core.syntax.SyntaxToken;
import com.orca.compiler.core.syntax.SyntaxVisitor;

public class LiteralExpr extends ExpressionSyntax {

    private final SyntaxToken token;
    private final Object value;

    public LiteralExpr(SyntaxToken token) {
        this.token = token;
        this.value = token.token.value();
    }

    /**
     * Gets the token representing this literal expression.
     *
     * @return The token representing this literal expression.
     */
    public SyntaxToken token() {
        return token;
    }

    /**
     * Gets the value of this literal expression.
     *
     * @return The value of this literal expression.
     */
    public Object value() {
        return value;
    }

    @Override
    public void accept(SyntaxVisitor visitor) {
        visitor.visitLiteralExpr(this);
    }

    @Override
    public String toString() {
        return "LiteralExpression(" + this.token.kind() + ", " + value + ")";
    }

    public static boolean isLiteral(TokenKind kind) {
        switch (kind) {
            case BoolLiteral:
            case IntegerLiteral:
            case FloatLiteral:
            case StringLiteral:
                return true;
            default:
                return false;
        }
    }
}
