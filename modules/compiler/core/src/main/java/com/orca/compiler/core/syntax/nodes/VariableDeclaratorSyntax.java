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
    private final SyntaxToken mutToken;
    private final SimpleIdentifierSyntax identifier;
    private final TypeSyntax type;

    public VariableDeclaratorSyntax(SyntaxToken mutToken, SimpleIdentifierSyntax identifier, TypeSyntax type, ExpressionSyntax initializer) {
        this.mutToken = mutToken;
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
     * Determines whether the variable is mutable (i.e., declared with the "mut"
     * keyword).
     *
     * @return True if the variable is mutable; false otherwise.
     */
    public boolean isMutable() {
        return mutToken != null;
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
