package com.orca.compiler.core.syntax.nodes;

import com.orca.compiler.core.syntax.SyntaxNode;
import com.orca.compiler.core.syntax.SyntaxVisitor;

public final class ImportSyntax extends SyntaxNode {
    private final IdentifierSyntax packageIdentifier;

    public ImportSyntax(IdentifierSyntax packageIdentifier) {
        this.packageIdentifier = packageIdentifier;
    }

    /**
     * Returns the identifier of the package being imported.
     * @return the package identifier
     */
    public IdentifierSyntax identifier() {
        return packageIdentifier;
    }

    @Override
    public void accept(SyntaxVisitor visitor) {
        visitor.visitImport(this);
    }

    @Override
    public String toString() {
        return "Import(" + packageIdentifier + ")";
    }
}
