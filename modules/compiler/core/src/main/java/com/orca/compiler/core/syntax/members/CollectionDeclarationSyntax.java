package com.orca.compiler.core.syntax.members;

import com.orca.compiler.core.syntax.SyntaxList;
import com.orca.compiler.core.syntax.SyntaxVisitor;
import com.orca.compiler.core.syntax.declarations.FieldDeclarationSyntax;
import com.orca.compiler.core.syntax.nodes.SimpleIdentifierSyntax;

public final class CollectionDeclarationSyntax extends MemberSyntax {

    private final SimpleIdentifierSyntax identifier;
    private final SyntaxList<FieldDeclarationSyntax> fields;

    public CollectionDeclarationSyntax(SimpleIdentifierSyntax declarationName, SyntaxList<FieldDeclarationSyntax> fields) {
        this.identifier = declarationName;
        this.fields = fields;
    }

    /**
     * Gets the identifier token of this collection declaration.
     *
     * @return The identifier token of this collection declaration.
     */
    public SimpleIdentifierSyntax identifier() {
        return identifier;
    }

    /**
     * Gets the list of field declarations within this collection.
     *
     * @return The list of field declarations.
     */
    public SyntaxList<FieldDeclarationSyntax> fields() {
        return fields;
    }

    @Override
    public void accept(SyntaxVisitor visitor) {
        visitor.visitCollectionDeclaration(this);
    }

    @Override
    public String toString() {
        return "CollectionDeclaration(" + identifier.text() + ")";
    }
}
