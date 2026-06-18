package com.orca.compiler.core.syntax.types;

import com.orca.compiler.core.syntax.SyntaxVisitor;

public final class ArrayTypeSyntax extends TypeSyntax {
    private final TypeSyntax elementType;

    public ArrayTypeSyntax(TypeSyntax elementType) {
        this.elementType = elementType;
    }

    /**
     * Gets the element type of the array.
     * @return The element type of the array.
     */
    public TypeSyntax elementType() {
        return elementType;
    }

    @Override
    public void accept(SyntaxVisitor visitor) {
        visitor.visitArrayTypeSyntax(this);
    }

    @Override
    public String toString() {
        return "ArrayTypeSyntax";
    }
}
