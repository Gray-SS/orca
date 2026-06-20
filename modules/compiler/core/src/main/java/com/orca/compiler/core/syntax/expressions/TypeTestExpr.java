package com.orca.compiler.core.syntax.expressions;

import javax.annotation.Nullable;

import com.orca.compiler.core.syntax.ExpressionSyntax;
import com.orca.compiler.core.syntax.SyntaxToken;
import com.orca.compiler.core.syntax.SyntaxVisitor;
import com.orca.compiler.core.syntax.types.TypeSyntax;

public final class TypeTestExpr extends ExpressionSyntax {

    private final ExpressionSyntax expression;
    private final SyntaxToken isKeyword;
    private final TypeSyntax type;
    private final @Nullable
    SyntaxToken identifier;

    public TypeTestExpr(ExpressionSyntax expression, SyntaxToken isKeyword, TypeSyntax type, @Nullable SyntaxToken identifier) {
        this.expression = expression;
        this.isKeyword = isKeyword;
        this.type = type;
        this.identifier = identifier;
    }

    /**
     * Gets the expression being tested by this type test expression.
     *
     * @return The expression being tested by this type test expression.
     */
    public ExpressionSyntax expression() {
        return expression;
    }

    /**
     * Gets the 'is' keyword of this type test expression.
     *
     * @return The 'is' keyword of this type test expression.
     */
    public SyntaxToken isKeyword() {
        return isKeyword;
    }

    /**
     * Gets the type being tested by this type test expression.
     *
     * @return The type being tested by this type test expression.
     */
    public TypeSyntax type() {
        return type;
    }

    /**
     * Gets the identifier to which the tested expression will be cast if the
     * type test succeeds, or null if there is no such identifier.
     *
     * @return The identifier to which the tested expression will be cast if the
     * type test succeeds, or null if there is no such identifier.
     */
    public @Nullable
    SyntaxToken identifier() {
        return identifier;
    }

    @Override
    public void accept(SyntaxVisitor visitor) {
        visitor.visitTypeTestExpr(this);
    }

    @Override
    public String toString() {
        return identifier == null
                ? "TypeTestExpr(is, type=" + type + ")"
                : "TypeTestExpr(is, type=" + type + ", ident=" + identifier.text() + ")";
    }
}
