package com.orca.compiler.core.syntax.members;

public sealed abstract class MemberSyntax extends CompilationMemberSyntax permits
        CollectionDeclarationSyntax,
        VariableDeclarationSyntax,
        MethodDeclarationSyntax,
        ImplBlockSyntax,
        ErrorMemberSyntax {
}
