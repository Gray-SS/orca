package com.orca.compiler.core.boundtree;

import java.util.ArrayList;

import com.orca.compiler.core.boundtree.expressions.*;
import com.orca.compiler.core.boundtree.statements.*;

public abstract class BoundTreeRewriter {

    public BoundExpression rewriteExpression(BoundExpression node) {
        return switch (node) {
            case BoundArrayAccessExpr b ->
                rewriteArrayAccessExpr(b);
            case BoundArrayLiteralExpr b ->
                rewriteArrayLiteralExpr(b);
            case BoundBinaryExpr b ->
                rewriteBinaryExpr(b);
            case BoundCollectionLiteralExpr b ->
                rewriteCollectionLiteralExpr(b);
            case BoundMethodCallExpr b ->
                rewriteFunctionCallExpr(b);
            case BoundLiteralExpr b ->
                rewriteLiteralExpr(b);
            case BoundUnaryExpr b ->
                rewriteUnaryExpr(b);
            case BoundReferenceExpr b ->
                rewriteReferenceExpr(b);
            case BoundAssignmentExpr b ->
                rewriteAssignmentExpr(b);
            case BoundConversionExpr b ->
                rewriteConversionExpr(b);
            case null ->
                null;
            default ->
                node;
        };
    }

    public BoundStatement rewriteStatement(BoundStatement node) {
        return switch (node) {
            case BoundBlockStmt b ->
                rewriteBlockStmt(b);
            case BoundExpressionStmt b ->
                rewriteExpressionStmt(b);
            case BoundIfStmt b ->
                rewriteIfStmt(b);
            case BoundWhileStmt b ->
                rewriteWhileStmt(b);
            case BoundForStmt b ->
                rewriteForStmt(b);
            case BoundVariableDeclStmt b ->
                rewriteVariableDeclarationStmt(b);
            case BoundReturnStmt b ->
                rewriteReturnStmt(b);
            case null ->
                null;
            default ->
                node;
        };
    }

    public BoundExpression rewriteAssignmentExpr(BoundAssignmentExpr node) {
        var rewrittenTargetExpr = rewriteExpression(node.targetExpr());
        var rewrittenInitializerExpr = rewriteExpression(node.valueExpr());
        if (rewrittenTargetExpr == node.targetExpr() && rewrittenInitializerExpr == node.valueExpr()) {
            return node;
        }

        return new BoundAssignmentExpr(rewrittenTargetExpr, node.operator(), rewrittenInitializerExpr);
    }

    public BoundBlockStmt rewriteBlockStmt(BoundBlockStmt node) {
        var rewrittenStatements = new ArrayList<BoundStatement>();
        boolean anyRewritten = false;
        for (BoundStatement stmt : node.statements()) {
            var rewrittenStmt = rewriteStatement(stmt);
            rewrittenStatements.add(rewrittenStmt);
            if (rewrittenStmt != stmt) {
                anyRewritten = true;
            }
        }

        if (!anyRewritten) {
            return node;
        }

        return new BoundBlockStmt(rewrittenStatements);
    }

    public BoundStatement rewriteExpressionStmt(BoundExpressionStmt node) {
        var rewrittenExpr = rewriteExpression(node.expression());
        if (rewrittenExpr == node.expression()) {
            return node;
        }

        return new BoundExpressionStmt(rewrittenExpr);
    }

    public BoundStatement rewriteIfStmt(BoundIfStmt node) {
        var rewrittenIfClause = rewriteIfClause(node.ifClause);
        var rewrittenElseIfClauses = new ArrayList<BoundIfClause>();
        boolean anyRewritten = false;
        for (BoundIfClause elseIfClause : node.elseIfClauses) {
            var rewrittenElseIfClause = rewriteIfClause(elseIfClause);
            rewrittenElseIfClauses.add(rewrittenElseIfClause);
            if (rewrittenElseIfClause != elseIfClause) {
                anyRewritten = true;
            }
        }

        var rewrittenElseClause = node.elseBlock() == null ? null : rewriteStatement(node.elseBlock());
        if (rewrittenIfClause == node.ifClause && !anyRewritten && rewrittenElseClause == node.elseBlock()) {
            return node;
        }

        return new BoundIfStmt(rewrittenIfClause, rewrittenElseIfClauses, rewrittenElseClause);
    }

    public BoundIfClause rewriteIfClause(BoundIfClause node) {
        var rewrittenCondition = rewriteExpression(node.condition);
        var rewrittenBody = rewriteStatement(node.body);
        if (rewrittenCondition == node.condition && rewrittenBody == node.body) {
            return node;
        }

        return new BoundIfClause(rewrittenCondition, rewrittenBody);
    }

    public BoundStatement rewriteWhileStmt(BoundWhileStmt node) {
        var rewrittenCondition = rewriteExpression(node.condition());
        var rewrittenBody = rewriteBlockStmt(node.body());
        if (rewrittenCondition == node.condition() && rewrittenBody == node.body()) {
            return node;
        }

        return new BoundWhileStmt(rewrittenCondition, rewrittenBody);
    }

    public BoundStatement rewriteForStmt(BoundForStmt node) {
        var rewrittenConditionExpr = rewriteExpression(node.conditionExpr);
        var rewrittenStepExpr = rewriteExpression(node.stepExpr);
        var rewrittenBody = rewriteBlockStmt(node.body);
        if (rewrittenConditionExpr == node.conditionExpr && rewrittenStepExpr == node.stepExpr && rewrittenBody == node.body) {
            return node;
        }

        return new BoundForStmt(node.declarator, rewrittenConditionExpr, rewrittenStepExpr, rewrittenBody);
    }

    public BoundStatement rewriteVariableDeclarationStmt(BoundVariableDeclStmt node) {
        var rewrittenInitializerExpr = rewriteExpression(node.initializer());
        if (rewrittenInitializerExpr == node.initializer()) {
            return node;
        }

        return new BoundVariableDeclStmt(node.variable(), rewrittenInitializerExpr);
    }

    public BoundStatement rewriteReturnStmt(BoundReturnStmt node) {
        if (node.expression == null) {
            return node;
        }

        var rewrittenExpr = rewriteExpression(node.expression);
        if (rewrittenExpr == node.expression) {
            return node;
        }

        return new BoundReturnStmt(rewrittenExpr);
    }

    public BoundExpression rewriteLiteralExpr(BoundLiteralExpr node) {
        return node;
    }

    public BoundExpression rewriteArrayLiteralExpr(BoundArrayLiteralExpr node) {
        var rewrittenLengthExpr = rewriteExpression(node.lengthExpr);
        if (rewrittenLengthExpr == node.lengthExpr) {
            return node;
        }

        return new BoundArrayLiteralExpr(node.elementType, rewrittenLengthExpr);
    }

    public BoundExpression rewriteConversionExpr(BoundConversionExpr node) {
        var rewrittenOperand = rewriteExpression(node.operand());
        if (rewrittenOperand == node.operand()) {
            return node;
        }
        return new BoundConversionExpr(rewrittenOperand, node.type());
    }

    public BoundExpression rewriteFunctionCallExpr(BoundMethodCallExpr node) {
        var rewrittenArgs = new ArrayList<BoundExpression>();

        boolean anyRewritten = false;
        for (BoundExpression arg : node.arguments) {
            var rewrittenArg = rewriteExpression(arg);
            rewrittenArgs.add(rewrittenArg);
            if (rewrittenArg != arg) {
                anyRewritten = true;
            }
        }

        if (!anyRewritten) {
            return node;
        }

        return new BoundMethodCallExpr(node.methodRef, rewrittenArgs);
    }

    public BoundExpression rewriteCollectionLiteralExpr(BoundCollectionLiteralExpr node) {
        var rewrittenArgs = new ArrayList<BoundExpression>();
        boolean anyRewritten = false;
        for (BoundExpression arg : node.arguments) {
            var rewrittenArg = rewriteExpression(arg);
            rewrittenArgs.add(rewrittenArg);
            if (rewrittenArg != arg) {
                anyRewritten = true;
            }
        }

        if (!anyRewritten) {
            return node;
        }

        return new BoundCollectionLiteralExpr(node.symbol, rewrittenArgs);
    }

    public BoundExpression rewriteReferenceExpr(BoundReferenceExpr node) {
        if (node instanceof BoundReferenceExpr.MemberAccessRef ma) {
            var rewrittenReceiver = rewriteExpression(ma.getReceiver());
            if (rewrittenReceiver == ma.getReceiver()) {
                return node;
            }

            return new BoundReferenceExpr.MemberAccessRef(rewrittenReceiver, ma.getMemberExpr());
        }

        return node;
    }

    public BoundExpression rewriteBinaryExpr(BoundBinaryExpr node) {
        var rewrittenLeft = rewriteExpression(node.left);
        var rewrittenRight = rewriteExpression(node.right);
        if (rewrittenLeft == node.left && rewrittenRight == node.right) {
            return node;
        }

        return new BoundBinaryExpr(node.operator, rewrittenLeft, rewrittenRight);
    }

    public BoundExpression rewriteUnaryExpr(BoundUnaryExpr node) {
        var rewrittenOperand = rewriteExpression(node.operand);
        if (rewrittenOperand == node.operand) {
            return node;
        }

        return new BoundUnaryExpr(node.operator, rewrittenOperand);
    }

    public BoundExpression rewriteArrayAccessExpr(BoundArrayAccessExpr node) {
        var rewrittenArray = rewriteExpression(node.arrayExpr());
        var rewrittenIndex = rewriteExpression(node.indexExpr());
        if (rewrittenArray == node.arrayExpr() && rewrittenIndex == node.indexExpr()) {
            return node;
        }

        return new BoundArrayAccessExpr(rewrittenArray, rewrittenIndex);
    }
}
