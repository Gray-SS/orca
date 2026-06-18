package com.orca.compiler.core.syntax.members;

import com.orca.compiler.core.syntax.SyntaxList;
import com.orca.compiler.core.syntax.SyntaxVisitor;
import com.orca.compiler.core.syntax.types.TypeSyntax;

public final class ImplBlockSyntax extends MemberSyntax {

    private final TypeSyntax typeSyntax;
    private final SyntaxList<MemberSyntax> members;

    public ImplBlockSyntax(TypeSyntax typeSyntax, SyntaxList<MemberSyntax> members) {
        this.typeSyntax = typeSyntax;
        this.members = members;
    }

    /**
     * Gets the type for which this impl block is defined.
     *
     * @return The type.
     */
    public TypeSyntax type() {
        return typeSyntax;
    }

    /**
     * Gets the list of members defined in this impl block.
     *
     * @return The list of member declarations.
     */
    public SyntaxList<MemberSyntax> members() {
        return members;
    }

    @Override
    public void accept(SyntaxVisitor visitor) {
        visitor.visitImplDecl(this);
    }

    @Override
    public String toString() {
        return "ImplDecl(" + type().toString() + ")";
    }
}
