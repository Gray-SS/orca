package com.orca.compiler.core.syntax.nodes;

import javax.annotation.Nullable;

import com.orca.compiler.core.syntax.SyntaxNode;
import com.orca.compiler.core.syntax.SyntaxVisitor;
import com.orca.compiler.core.syntax.types.TypeSyntax;

public class ParameterSyntax extends SyntaxNode {
    private final SimpleIdentifierSyntax identifier;

    @Nullable
    private final TypeSyntax type;

    private ParameterSyntax(@Nullable TypeSyntax type, SimpleIdentifierSyntax identifier) {
        this.type = type;
        this.identifier = identifier;
    }

    public static ParameterSyntax createReceiver(SimpleIdentifierSyntax identifier) {
        return new ParameterSyntax(null, identifier);
    }

    public static ParameterSyntax createTyped(TypeSyntax type, SimpleIdentifierSyntax identifier) {
        return new ParameterSyntax(type, identifier);
    }

    /**
     * Determines whether this parameter is a typed parameter (i.e. a parameter that has an explicit type annotation).
     * @return True if this parameter is a typed parameter, false otherwise.
     */
    public boolean isTyped() {
        return type != null;
    }

    /**
     * Determines whether this parameter is a receiver parameter (i.e. a parameter that represents the instance of the type that the method is being called on).
     * @return True if this parameter is a receiver parameter, false otherwise.
     */
    public boolean isReceiver() {
        return type == null;
    }

    /**
     * Gets the identifier token for this parameter.
     * @return The identifier token for this parameter.
     */
    public SimpleIdentifierSyntax identifier() {
        return identifier;
    }

    /**
     * Gets the type of this parameter, or null if this parameter is a receiver parameter.
     * @return The type of this parameter, or null if this parameter is a receiver parameter.
     */
    @Nullable
    public TypeSyntax type() {
        return type;
    }

    @Override
    public void accept(SyntaxVisitor visitor) {
        visitor.visitParameterSyntax(this);
    }

    @Override
    public String toString() {
        return "ParameterSyntax(" + identifier.text() + ")";
    }
}
