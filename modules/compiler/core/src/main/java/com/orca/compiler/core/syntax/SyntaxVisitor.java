package com.orca.compiler.core.syntax;

import com.orca.compiler.core.syntax.declarations.FieldDeclarationSyntax;
import com.orca.compiler.core.syntax.expressions.ArrayAccessExpr;
import com.orca.compiler.core.syntax.expressions.ArrayLiteralExpression;
import com.orca.compiler.core.syntax.expressions.AssignmentExpr;
import com.orca.compiler.core.syntax.expressions.BinaryExpr;
import com.orca.compiler.core.syntax.expressions.ErrorExpressionSyntax;
import com.orca.compiler.core.syntax.expressions.IdentifierExpr;
import com.orca.compiler.core.syntax.expressions.InvocationExpr;
import com.orca.compiler.core.syntax.expressions.LiteralExpr;
import com.orca.compiler.core.syntax.expressions.MemberAccessExpr;
import com.orca.compiler.core.syntax.expressions.TypeTestExpr;
import com.orca.compiler.core.syntax.expressions.UnaryExpr;
import com.orca.compiler.core.syntax.members.CollectionDeclarationSyntax;
import com.orca.compiler.core.syntax.members.ErrorMemberSyntax;
import com.orca.compiler.core.syntax.members.GlobalStatementSyntax;
import com.orca.compiler.core.syntax.members.ImplBlockSyntax;
import com.orca.compiler.core.syntax.members.MethodDeclarationSyntax;
import com.orca.compiler.core.syntax.members.VariableDeclarationSyntax;
import com.orca.compiler.core.syntax.nodes.ElseClauseSyntax;
import com.orca.compiler.core.syntax.nodes.IfClauseSyntax;
import com.orca.compiler.core.syntax.nodes.ImportSyntax;
import com.orca.compiler.core.syntax.nodes.PackageDirectiveSyntax;
import com.orca.compiler.core.syntax.nodes.ParameterSyntax;
import com.orca.compiler.core.syntax.nodes.QualifiedIdentifierSyntax;
import com.orca.compiler.core.syntax.nodes.RangeSyntax;
import com.orca.compiler.core.syntax.nodes.SimpleIdentifierSyntax;
import com.orca.compiler.core.syntax.nodes.SpecialTypeIdentifierSyntax;
import com.orca.compiler.core.syntax.nodes.VariableDeclaratorSyntax;
import com.orca.compiler.core.syntax.statements.BlockStmt;
import com.orca.compiler.core.syntax.statements.ErrorStatementSyntax;
import com.orca.compiler.core.syntax.statements.ExpressionStmt;
import com.orca.compiler.core.syntax.statements.ForStmt;
import com.orca.compiler.core.syntax.statements.IfStmt;
import com.orca.compiler.core.syntax.statements.ReturnStmt;
import com.orca.compiler.core.syntax.statements.VariableDeclarationStatement;
import com.orca.compiler.core.syntax.statements.WhileStmt;
import com.orca.compiler.core.syntax.types.ArrayTypeSyntax;
import com.orca.compiler.core.syntax.types.ErrorTypeSyntax;
import com.orca.compiler.core.syntax.types.IdentifierTypeSyntax;
import com.orca.compiler.core.syntax.types.SpecialTypeSyntax;

public abstract class SyntaxVisitor {

    public abstract void visitCompilationUnit(CompilationUnit compilationUnit);

    // Members
    public abstract void visitMethodDeclaration(MethodDeclarationSyntax syntax);

    public abstract void visitCollectionDeclaration(CollectionDeclarationSyntax syntax);

    public abstract void visitGlobalStatement(GlobalStatementSyntax syntax);

    public abstract void visitVariableDeclaration(VariableDeclarationSyntax syntax);

    // Declarations
    public abstract void visitPackage(PackageDirectiveSyntax syntax);

    public abstract void visitImport(ImportSyntax syntax);

    public abstract void visitFieldDeclaration(FieldDeclarationSyntax syntax);

    public abstract void visitImplDecl(ImplBlockSyntax syntax);

    // Statements
    public abstract void visitIfStmt(IfStmt syntax);

    public abstract void visitForStmt(ForStmt syntax);

    public abstract void visitWhileStmt(WhileStmt syntax);

    public abstract void visitReturnStmt(ReturnStmt syntax);

    public abstract void visitAssignmentExpr(AssignmentExpr syntax);

    public abstract void visitBlockStmt(BlockStmt syntax);

    public abstract void visitExpressionStmt(ExpressionStmt syntax);

    public abstract void visitVariableDeclarationStatement(VariableDeclarationStatement syntax);

    // Expressions
    public abstract void visitBinaryExpr(BinaryExpr syntax);

    public abstract void visitLiteralExpr(LiteralExpr syntax);

    public abstract void visitIdentifierExpr(IdentifierExpr syntax);

    public abstract void visitInvocationExpr(InvocationExpr syntax);

    public abstract void visitMemberAccessExpr(MemberAccessExpr syntax);

    public abstract void visitUnaryExpr(UnaryExpr syntax);

    public abstract void visitArrayAccessExpr(ArrayAccessExpr syntax);

    public abstract void visitArrayLiteralExpr(ArrayLiteralExpression syntax);

    public abstract void visitTypeTestExpr(TypeTestExpr syntax);

    // Types
    public abstract void visitIdentifierTypeSyntax(IdentifierTypeSyntax syntax);

    public abstract void visitArrayTypeSyntax(ArrayTypeSyntax syntax);

    public abstract void visitSpecialTypeSyntax(SpecialTypeSyntax syntax);

    // Others
    public abstract void visitSimpleIdentifierSyntax(SimpleIdentifierSyntax syntax);

    public abstract void visitQualifiedIdentifierSyntax(QualifiedIdentifierSyntax syntax);

    public abstract void visitSpecialTypeIdentifier(SpecialTypeIdentifierSyntax syntax);

    public abstract void visitVariableDeclarator(VariableDeclaratorSyntax syntax);

    public abstract void visitSyntaxList(SyntaxList<? extends SyntaxNode> syntax);

    public abstract void visitRangeSyntax(RangeSyntax syntax);

    public abstract void visitIfClauseSyntax(IfClauseSyntax syntax);

    public abstract void visitElseClauseSyntax(ElseClauseSyntax syntax);

    public abstract void visitParameterSyntax(ParameterSyntax syntax);

    public abstract void visitToken(SyntaxToken syntax);

    // Error nodes — non-abstract so existing visitors don't need to implement them
    public abstract void visitErrorExpression(ErrorExpressionSyntax syntax);

    public abstract void visitErrorType(ErrorTypeSyntax syntax);

    public abstract void visitErrorStatement(ErrorStatementSyntax syntax);

    public abstract void visitErrorMember(ErrorMemberSyntax syntax);
}
