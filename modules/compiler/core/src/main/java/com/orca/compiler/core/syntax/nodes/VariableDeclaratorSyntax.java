package com.orca.compiler.core.syntax.nodes;

import com.orca.compiler.core.syntax.ExpressionSyntax;
import com.orca.compiler.core.syntax.SyntaxNode;
import com.orca.compiler.core.syntax.SyntaxToken;
import com.orca.compiler.core.syntax.SyntaxVisitor;
import com.orca.compiler.core.syntax.types.TypeSyntax;

public final class VariableDeclaratorSyntax extends SyntaxNode {

    /**
     * The initializer expression for the variable declaration.
     *
     * @implNote This can be null if the variable is declared without an
     * initializer (e.g. {@code var x: int;}).
     */
    private final ExpressionSyntax initializer;
    private final SyntaxToken modifierToken;
    private final SimpleIdentifierSyntax identifier;
    private final TypeSyntax type;

    public VariableDeclaratorSyntax(SyntaxToken modifierToken, SimpleIdentifierSyntax identifier, TypeSyntax type, ExpressionSyntax initializer) {
        this.modifierToken = modifierToken;
        this.identifier = identifier;
        this.type = type;
        this.initializer = initializer;
    }

    /**
     * Gets the initializer expression for the variable declaration, or null if
     * there is no initializer.
     *
     * @return The initializer expression, or null if none.
     */
    public SimpleIdentifierSyntax identifier() {
        return identifier;
    }

    /**
     * Gets the modifier token for the variable declaration.
     *
     * @return The modifier token.
     */
    public SyntaxToken modifierToken() {
        return modifierToken;
    }

    /**
     * Gets the type of the variable declaration.
     *
     * @return The type of the variable declaration.
     */
    public TypeSyntax type() {
        return type;
    }

    /**
     * Gets the initializer expression for the variable declaration, or null if
     * there is no initializer.
     *
     * @return The initializer expression, or null if none.
     */
    public ExpressionSyntax initializer() {
        return initializer;
    }

    @Override
    public void accept(SyntaxVisitor visitor) {
        visitor.visitVariableDeclarator(this);
    }

    @Override
    public String toString() {
        return "VariableDeclarator(identifier='" + identifier.text() + "', type=" + type + ", initializer=" + initializer + ")";
    }
}
