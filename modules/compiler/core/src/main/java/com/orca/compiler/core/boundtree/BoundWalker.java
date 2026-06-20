package com.orca.compiler.core.boundtree;

import com.orca.compiler.core.boundtree.expressions.BoundArrayAccessExpr;
import com.orca.compiler.core.boundtree.expressions.BoundArrayLiteralExpr;
import com.orca.compiler.core.boundtree.expressions.BoundAssignmentExpr;
import com.orca.compiler.core.boundtree.expressions.BoundBinaryExpr;
import com.orca.compiler.core.boundtree.expressions.BoundCollectionLiteralExpr;
import com.orca.compiler.core.boundtree.expressions.BoundConstructObjectExpr;
import com.orca.compiler.core.boundtree.expressions.BoundConversionExpr;
import com.orca.compiler.core.boundtree.expressions.BoundDefaultValueExpr;
import com.orca.compiler.core.boundtree.expressions.BoundErrorExpr;
import com.orca.compiler.core.boundtree.expressions.BoundLiteralExpr;
import com.orca.compiler.core.boundtree.expressions.BoundMethodCallExpr;
import com.orca.compiler.core.boundtree.expressions.BoundReferenceExpr;
import com.orca.compiler.core.boundtree.expressions.BoundSequenceExpr;
import com.orca.compiler.core.boundtree.expressions.BoundTypeTestExpr;
import com.orca.compiler.core.boundtree.expressions.BoundUnaryExpr;
import com.orca.compiler.core.boundtree.statements.BoundBlockStmt;
import com.orca.compiler.core.boundtree.statements.BoundConditionalGotoStmt;
import com.orca.compiler.core.boundtree.statements.BoundErrorStmt;
import com.orca.compiler.core.boundtree.statements.BoundExpressionStmt;
import com.orca.compiler.core.boundtree.statements.BoundForStmt;
import com.orca.compiler.core.boundtree.statements.BoundGotoStmt;
import com.orca.compiler.core.boundtree.statements.BoundIfStmt;
import com.orca.compiler.core.boundtree.statements.BoundLabelStmt;
import com.orca.compiler.core.boundtree.statements.BoundReturnStmt;
import com.orca.compiler.core.boundtree.statements.BoundVariableDeclStmt;
import com.orca.compiler.core.boundtree.statements.BoundWhileStmt;

public abstract class BoundWalker implements BoundVisitor<Void> {

    protected void defaultVisit(BoundNode node) {
        for (var child : node.children()) {
            child.accept(this);
        }
    }

    @Override
    public Void visitProgram(BoundProgram node) {
        defaultVisit(node);
        return null;
    }

    @Override
    public Void visitNamespace(BoundNamespace node) {
        defaultVisit(node);
        return null;
    }

    @Override
    public Void visitType(BoundType node) {
        defaultVisit(node);
        return null;
    }

    @Override
    public Void visitMethod(BoundMethod node) {
        defaultVisit(node);
        return null;
    }

    @Override
    public Void visitField(BoundField node) {
        defaultVisit(node);
        return null;
    }

    @Override
    public Void visitConstructor(BoundConstructor node) {
        defaultVisit(node);
        return null;
    }

    @Override
    public Void visitVariable(BoundVariable node) {
        defaultVisit(node);
        return null;
    }

    @Override
    public Void visitVariableDeclarator(BoundVariableDeclarator node) {
        defaultVisit(node);
        return null;
    }

    @Override
    public Void visitIfClause(BoundIfClause node) {
        defaultVisit(node);
        return null;
    }

    @Override
    public Void visitArrayAccessExpr(BoundArrayAccessExpr node) {
        defaultVisit(node);
        return null;
    }

    @Override
    public Void visitArrayLiteralExpr(BoundArrayLiteralExpr node) {
        defaultVisit(node);
        return null;
    }

    @Override
    public Void visitAssignmentExpr(BoundAssignmentExpr node) {
        defaultVisit(node);
        return null;
    }

    @Override
    public Void visitBinaryExpr(BoundBinaryExpr node) {
        defaultVisit(node);
        return null;
    }

    @Override
    public Void visitCollectionLiteralExpr(BoundCollectionLiteralExpr node) {
        defaultVisit(node);
        return null;
    }

    @Override
    public Void visitConstructObjectExpr(BoundConstructObjectExpr node) {
        defaultVisit(node);
        return null;
    }

    @Override
    public Void visitConversionExpr(BoundConversionExpr node) {
        defaultVisit(node);
        return null;
    }

    @Override
    public Void visitDefaultValueExpr(BoundDefaultValueExpr node) {
        defaultVisit(node);
        return null;
    }

    @Override
    public Void visitErrorExpr(BoundErrorExpr node) {
        defaultVisit(node);
        return null;
    }

    @Override
    public Void visitLiteralExpr(BoundLiteralExpr node) {
        defaultVisit(node);
        return null;
    }

    @Override
    public Void visitMethodCallExpr(BoundMethodCallExpr node) {
        defaultVisit(node);
        return null;
    }

    @Override
    public Void visitReferenceExpr(BoundReferenceExpr node) {
        defaultVisit(node);
        return null;
    }

    @Override
    public Void visitSequenceExpr(BoundSequenceExpr node) {
        defaultVisit(node);
        return null;
    }

    @Override
    public Void visitTypeTestExpr(BoundTypeTestExpr node) {
        defaultVisit(node);
        return null;
    }

    @Override
    public Void visitUnaryExpr(BoundUnaryExpr node) {
        defaultVisit(node);
        return null;
    }

    @Override
    public Void visitBlockStmt(BoundBlockStmt node) {
        defaultVisit(node);
        return null;
    }

    @Override
    public Void visitConditionalGotoStmt(BoundConditionalGotoStmt node) {
        defaultVisit(node);
        return null;
    }

    @Override
    public Void visitErrorStmt(BoundErrorStmt node) {
        defaultVisit(node);
        return null;
    }

    @Override
    public Void visitExpressionStmt(BoundExpressionStmt node) {
        defaultVisit(node);
        return null;
    }

    @Override
    public Void visitForStmt(BoundForStmt node) {
        defaultVisit(node);
        return null;
    }

    @Override
    public Void visitGotoStmt(BoundGotoStmt node) {
        defaultVisit(node);
        return null;
    }

    @Override
    public Void visitIfStmt(BoundIfStmt node) {
        defaultVisit(node);
        return null;
    }

    @Override
    public Void visitLabelStmt(BoundLabelStmt node) {
        defaultVisit(node);
        return null;
    }

    @Override
    public Void visitReturnStmt(BoundReturnStmt node) {
        defaultVisit(node);
        return null;
    }

    @Override
    public Void visitVariableDeclStmt(BoundVariableDeclStmt node) {
        defaultVisit(node);
        return null;
    }

    @Override
    public Void visitWhileStmt(BoundWhileStmt node) {
        defaultVisit(node);
        return null;
    }
}
