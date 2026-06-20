package com.orca.compiler.core.syntax.types;

import com.orca.compiler.core.syntax.SyntaxNode;

public sealed abstract class TypeSyntax extends SyntaxNode permits
        IdentifierTypeSyntax,
        ArrayTypeSyntax,
        SpecialTypeSyntax,
        ErrorTypeSyntax {
}
