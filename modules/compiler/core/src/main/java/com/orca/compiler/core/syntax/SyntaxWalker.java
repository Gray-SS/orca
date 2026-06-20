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
import com.orca.compiler.core.syntax.expressions.UnaryAssignmentExpr;
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

public abstract class SyntaxWalker extends SyntaxVisitor {

    public void walk(SyntaxNode node) {
        if (node == null) {
            return;
        }

        beforeVisit(node);
        node.accept(this);
        afterVisit(node);
    }

    protected void defaultVisit(SyntaxNode node) {
        for (SyntaxNode child : node.children()) {
            walk(child);
        }
    }

    protected void beforeVisit(SyntaxNode node) {
        // Default implementation does nothing
    }

    protected void afterVisit(SyntaxNode node) {
        // Default implementation does nothing
    }

    @Override
    public void visitArrayAccessExpr(ArrayAccessExpr syntax) {
        defaultVisit(syntax);
    }

    @Override
    public void visitArrayLiteralExpr(ArrayLiteralExpression syntax) {
        defaultVisit(syntax);
    }

    @Override
    public void visitArrayTypeSyntax(ArrayTypeSyntax syntax) {
        defaultVisit(syntax);
    }

    @Override
    public void visitAssignmentExpr(AssignmentExpr syntax) {
        defaultVisit(syntax);
    }

    @Override
    public void visitUnaryAssignmentExpr(UnaryAssignmentExpr syntax) {
        defaultVisit(syntax);
    }

    @Override
    public void visitBinaryExpr(BinaryExpr syntax) {
        defaultVisit(syntax);
    }

    @Override
    public void visitBlockStmt(BlockStmt syntax) {
        defaultVisit(syntax);
    }

    @Override
    public void visitCollectionDeclaration(CollectionDeclarationSyntax syntax) {
        defaultVisit(syntax);
    }

    @Override
    public void visitCompilationUnit(CompilationUnit syntax) {
        defaultVisit(syntax);
    }

    @Override
    public void visitElseClauseSyntax(ElseClauseSyntax syntax) {
        defaultVisit(syntax);
    }

    @Override
    public void visitErrorExpression(ErrorExpressionSyntax syntax) {
        defaultVisit(syntax);
    }

    @Override
    public void visitErrorMember(ErrorMemberSyntax syntax) {
        defaultVisit(syntax);
    }

    @Override
    public void visitErrorStatement(ErrorStatementSyntax syntax) {
        defaultVisit(syntax);
    }

    @Override
    public void visitErrorType(ErrorTypeSyntax syntax) {
        defaultVisit(syntax);
    }

    @Override
    public void visitExpressionStmt(ExpressionStmt syntax) {
        defaultVisit(syntax);
    }

    @Override
    public void visitFieldDeclaration(FieldDeclarationSyntax syntax) {
        defaultVisit(syntax);
    }

    @Override
    public void visitForStmt(ForStmt syntax) {
        defaultVisit(syntax);
    }

    @Override
    public void visitGlobalStatement(GlobalStatementSyntax syntax) {
        defaultVisit(syntax);
    }

    @Override
    public void visitIdentifierExpr(IdentifierExpr syntax) {
        defaultVisit(syntax);
    }

    @Override
    public void visitIdentifierTypeSyntax(IdentifierTypeSyntax syntax) {
        defaultVisit(syntax);
    }

    @Override
    public void visitIfClauseSyntax(IfClauseSyntax syntax) {
        defaultVisit(syntax);
    }

    @Override
    public void visitIfStmt(IfStmt syntax) {
        defaultVisit(syntax);
    }

    @Override
    public void visitImplDecl(ImplBlockSyntax syntax) {
        defaultVisit(syntax);
    }

    @Override
    public void visitImport(ImportSyntax syntax) {
        defaultVisit(syntax);
    }

    @Override
    public void visitInvocationExpr(InvocationExpr syntax) {
        defaultVisit(syntax);
    }

    @Override
    public void visitLiteralExpr(LiteralExpr syntax) {
        defaultVisit(syntax);
    }

    @Override
    public void visitMemberAccessExpr(MemberAccessExpr syntax) {
        defaultVisit(syntax);
    }

    @Override
    public void visitMethodDeclaration(MethodDeclarationSyntax syntax) {
        defaultVisit(syntax);
    }

    @Override
    public void visitPackage(PackageDirectiveSyntax syntax) {
        defaultVisit(syntax);
    }

    @Override
    public void visitParameterSyntax(ParameterSyntax syntax) {
        defaultVisit(syntax);
    }

    @Override
    public void visitQualifiedIdentifierSyntax(QualifiedIdentifierSyntax syntax) {
        defaultVisit(syntax);
    }

    @Override
    public void visitRangeSyntax(RangeSyntax syntax) {
        defaultVisit(syntax);
    }

    @Override
    public void visitReturnStmt(ReturnStmt syntax) {
        defaultVisit(syntax);
    }

    @Override
    public void visitSimpleIdentifierSyntax(SimpleIdentifierSyntax syntax) {
        defaultVisit(syntax);
    }

    @Override
    public void visitSpecialTypeIdentifier(SpecialTypeIdentifierSyntax syntax) {
        defaultVisit(syntax);
    }

    @Override
    public void visitSpecialTypeSyntax(SpecialTypeSyntax syntax) {
        defaultVisit(syntax);
    }

    @Override
    public void visitSyntaxList(SyntaxList<? extends SyntaxNode> syntax) {
        defaultVisit(syntax);
    }

    @Override
    public void visitToken(SyntaxToken syntax) {
        defaultVisit(syntax);
    }

    @Override
    public void visitTypeTestExpr(TypeTestExpr syntax) {
        defaultVisit(syntax);

    }

    @Override
    public void visitUnaryExpr(UnaryExpr syntax) {
        defaultVisit(syntax);
    }

    @Override
    public void visitVariableDeclaration(VariableDeclarationSyntax syntax) {
        defaultVisit(syntax);
    }

    @Override
    public void visitVariableDeclarationStatement(VariableDeclarationStatement syntax) {
        defaultVisit(syntax);
    }

    @Override
    public void visitVariableDeclarator(VariableDeclaratorSyntax syntax) {
        defaultVisit(syntax);
    }

    @Override
    public void visitWhileStmt(WhileStmt syntax) {
        defaultVisit(syntax);
    }
}
