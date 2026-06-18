package com.orca.compiler.core.syntax.members;

import javax.annotation.Nullable;

import com.orca.compiler.core.syntax.SyntaxList;
import com.orca.compiler.core.syntax.SyntaxVisitor;
import com.orca.compiler.core.syntax.nodes.ParameterSyntax;
import com.orca.compiler.core.syntax.nodes.SimpleIdentifierSyntax;
import com.orca.compiler.core.syntax.statements.BlockStmt;
import com.orca.compiler.core.syntax.types.TypeSyntax;

public final class MethodDeclarationSyntax extends MemberSyntax {

    /**
     * The return type of the function. Can be null for void functions.
     */
    private final @Nullable
    TypeSyntax returnType;

    private final SimpleIdentifierSyntax identifier;
    private final SyntaxList<ParameterSyntax> parameters;
    private final BlockStmt body;

    public MethodDeclarationSyntax(SimpleIdentifierSyntax identifier, TypeSyntax returnType, SyntaxList<ParameterSyntax> parameters, BlockStmt body) {
        this.identifier = identifier;
        this.returnType = returnType;
        this.parameters = parameters;
        this.body = body;
    }

    public SimpleIdentifierSyntax identifier() {
        return identifier;
    }

    /**
     * Gets the return type of the function, or null if it is a void function.
     *
     * @return The return type of the function, or null if void.
     */
    public @Nullable
    TypeSyntax returnType() {
        return returnType;
    }

    /**
     * Gets the list of parameters of the function.
     *
     * @return The list of parameters of the function.
     */
    public SyntaxList<ParameterSyntax> parameters() {
        return parameters;
    }

    /**
     * Gets the body of the function as a block statement.
     *
     * @return The body of the function.
     */
    public BlockStmt body() {
        return body;
    }

    /**
     * Checks if the function has a receiver parameter (i.e., is an instance
     * method).
     *
     * @return true if any parameter is a receiver, false otherwise.
     */
    public boolean hasReceiver() {
        for (ParameterSyntax param : parameters) {
            if (param.isReceiver()) {
                return true;
            }
        }

        return false;
    }

    @Override
    public void accept(SyntaxVisitor visitor) {
        visitor.visitMethodDeclaration(this);
    }

    @Override
    public String toString() {
        return "MethodDeclaration(" + identifier.text() + ")";
    }
}
