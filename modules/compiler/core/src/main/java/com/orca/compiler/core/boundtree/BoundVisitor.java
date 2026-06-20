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

public interface BoundVisitor<R> {

    // Top-level declarations
    R visitProgram(BoundProgram node);

    R visitNamespace(BoundNamespace node);

    R visitType(BoundType node);

    R visitMethod(BoundMethod node);

    R visitField(BoundField node);

    R visitConstructor(BoundConstructor node);

    R visitVariable(BoundVariable node);

    R visitVariableDeclarator(BoundVariableDeclarator node);

    R visitIfClause(BoundIfClause node);

    // Expressions
    R visitArrayAccessExpr(BoundArrayAccessExpr node);

    R visitArrayLiteralExpr(BoundArrayLiteralExpr node);

    R visitAssignmentExpr(BoundAssignmentExpr node);

    R visitBinaryExpr(BoundBinaryExpr node);

    R visitCollectionLiteralExpr(BoundCollectionLiteralExpr node);

    R visitConstructObjectExpr(BoundConstructObjectExpr node);

    R visitConversionExpr(BoundConversionExpr node);

    R visitDefaultValueExpr(BoundDefaultValueExpr node);

    R visitErrorExpr(BoundErrorExpr node);

    R visitLiteralExpr(BoundLiteralExpr node);

    R visitMethodCallExpr(BoundMethodCallExpr node);

    R visitReferenceExpr(BoundReferenceExpr node);

    R visitSequenceExpr(BoundSequenceExpr node);

    R visitTypeTestExpr(BoundTypeTestExpr node);

    R visitUnaryExpr(BoundUnaryExpr node);

    // Statements
    R visitBlockStmt(BoundBlockStmt node);

    R visitConditionalGotoStmt(BoundConditionalGotoStmt node);

    R visitErrorStmt(BoundErrorStmt node);

    R visitExpressionStmt(BoundExpressionStmt node);

    R visitForStmt(BoundForStmt node);

    R visitGotoStmt(BoundGotoStmt node);

    R visitIfStmt(BoundIfStmt node);

    R visitLabelStmt(BoundLabelStmt node);

    R visitReturnStmt(BoundReturnStmt node);

    R visitVariableDeclStmt(BoundVariableDeclStmt node);

    R visitWhileStmt(BoundWhileStmt node);
}
