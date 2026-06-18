package com.orca.compiler.core.syntax.nodes;

import com.orca.compiler.core.syntax.SyntaxNode;
import com.orca.compiler.core.syntax.SyntaxVisitor;

public final class PackageDirectiveSyntax extends SyntaxNode {
    private final IdentifierSyntax packageIdentifier;

    public PackageDirectiveSyntax(IdentifierSyntax packageIdentifier) {
        this.packageIdentifier = packageIdentifier;
    }

    @Override
    public void accept(SyntaxVisitor visitor) {
        visitor.visitPackage(this);
    }

    /**
     * Gets the name of this package.
     * @return The name of this package.
     */
    public String name() {
        return packageIdentifier.name();
    }

    /**
     * Gets the identifier syntax of this package.
     * @return The identifier syntax of this package.
     */
    public IdentifierSyntax packageIdentifier() {
        return packageIdentifier;
    }

    @Override
    public String toString() {
        return "Package(" + packageIdentifier + ")";
    }
}
