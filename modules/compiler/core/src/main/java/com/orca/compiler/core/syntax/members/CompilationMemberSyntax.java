package com.orca.compiler.core.syntax.members;

import com.orca.compiler.core.syntax.SyntaxNode;

public sealed abstract class CompilationMemberSyntax extends SyntaxNode permits
    MemberSyntax,
    GlobalStatementSyntax {
}
