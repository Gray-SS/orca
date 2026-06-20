package com.orca.compiler.core.boundtree;

import java.util.ArrayList;

import com.orca.compiler.core.boundtree.expressions.*;
import com.orca.compiler.core.boundtree.statements.*;

public abstract class BoundTreeRewriter implements BoundVisitor<BoundNode> {

    // Typed entry points for internal recursion
    public BoundExpression rewriteExpression(BoundExpression node) {
        if (node == null) {
            return null;
        }
        return (BoundExpression) node.accept(this);
    }

    public BoundStatement rewriteStatement(BoundStatement node) {
        if (node == null) {
            return null;
        }
        return (BoundStatement) node.accept(this);
    }

    public BoundBlockStmt rewriteBlockStmt(BoundBlockStmt node) {
        return (BoundBlockStmt) node.accept(this);
    }

    // Top-level declarations — not visited by default
    @Override
    public BoundNode visitProgram(BoundProgram node) {
        return node;
    }

    @Override
    public BoundNode visitNamespace(BoundNamespace node) {
        return node;
    }

    @Override
    public BoundNode visitType(BoundType node) {
        return node;
    }

    @Override
    public BoundNode visitMethod(BoundMethod node) {
        return node;
    }

    @Override
    public BoundNode visitField(BoundField node) {
        return node;
    }

    @Override
    public BoundNode visitConstructor(BoundConstructor node) {
        return node;
    }

    @Override
    public BoundNode visitVariable(BoundVariable node) {
        return node;
    }

    @Override
    public BoundNode visitVariableDeclarator(BoundVariableDeclarator node) {
        return node;
    }

    @Override
    public BoundNode visitIfClause(BoundIfClause node) {
        var condition = rewriteExpression(node.condition);
        var body = rewriteStatement(node.body);
        if (condition == node.condition && body == node.body) {
            return node;
        }
        return new BoundIfClause(condition, body);
    }

    // Expressions
    @Override
    public BoundNode visitAssignmentExpr(BoundAssignmentExpr node) {
        var target = rewriteExpression(node.targetExpr());
        var value = rewriteExpression(node.valueExpr());
        if (target == node.targetExpr() && value == node.valueExpr()) {
            return node;
        }
        return new BoundAssignmentExpr(target, node.operator(), value);
    }

    @Override
    public BoundNode visitArrayLiteralExpr(BoundArrayLiteralExpr node) {
        var length = rewriteExpression(node.lengthExpr);
        if (length == node.lengthExpr) {
            return node;
        }
        return new BoundArrayLiteralExpr(node.elementType, length);
    }

    @Override
    public BoundNode visitArrayAccessExpr(BoundArrayAccessExpr node) {
        var array = rewriteExpression(node.arrayExpr());
        var index = rewriteExpression(node.indexExpr());
        if (array == node.arrayExpr() && index == node.indexExpr()) {
            return node;
        }
        return new BoundArrayAccessExpr(array, index);
    }

    @Override
    public BoundNode visitBinaryExpr(BoundBinaryExpr node) {
        var left = rewriteExpression(node.left);
        var right = rewriteExpression(node.right);
        if (left == node.left && right == node.right) {
            return node;
        }
        return new BoundBinaryExpr(node.operator, left, right);
    }

    @Override
    public BoundNode visitUnaryExpr(BoundUnaryExpr node) {
        var operand = rewriteExpression(node.operand);
        if (operand == node.operand) {
            return node;
        }
        return new BoundUnaryExpr(node.operator, operand);
    }

    @Override
    public BoundNode visitConversionExpr(BoundConversionExpr node) {
        var operand = rewriteExpression(node.operand());
        if (operand == node.operand()) {
            return node;
        }
        return new BoundConversionExpr(operand, node.type());
    }

    @Override
    public BoundNode visitMethodCallExpr(BoundMethodCallExpr node) {
        var args = new ArrayList<BoundExpression>();
        boolean anyRewritten = false;
        for (var arg : node.arguments) {
            var rewritten = rewriteExpression(arg);
            args.add(rewritten);
            if (rewritten != arg) {
                anyRewritten = true;
            }
        }
        if (!anyRewritten) {
            return node;
        }
        return new BoundMethodCallExpr(node.methodRef, args);
    }

    @Override
    public BoundNode visitCollectionLiteralExpr(BoundCollectionLiteralExpr node) {
        var args = new ArrayList<BoundExpression>();
        boolean anyRewritten = false;
        for (var arg : node.arguments) {
            var rewritten = rewriteExpression(arg);
            args.add(rewritten);
            if (rewritten != arg) {
                anyRewritten = true;
            }
        }
        if (!anyRewritten) {
            return node;
        }
        return new BoundCollectionLiteralExpr(node.symbol, args);
    }

    @Override
    public BoundNode visitReferenceExpr(BoundReferenceExpr node) {
        if (node instanceof BoundReferenceExpr.MemberAccessRef ma) {
            var receiver = rewriteExpression(ma.getReceiver());
            if (receiver == ma.getReceiver()) {
                return node;
            }
            return new BoundReferenceExpr.MemberAccessRef(receiver, ma.getMemberExpr());
        }
        return node;
    }

    @Override
    public BoundNode visitLiteralExpr(BoundLiteralExpr node) {
        return node;
    }

    @Override
    public BoundNode visitDefaultValueExpr(BoundDefaultValueExpr node) {
        return node;
    }

    @Override
    public BoundNode visitErrorExpr(BoundErrorExpr node) {
        return node;
    }

    @Override
    public BoundNode visitConstructObjectExpr(BoundConstructObjectExpr node) {
        var args = new ArrayList<BoundExpression>();
        boolean anyRewritten = false;

        for (var arg : node.getArguments()) {
            var rewritten = rewriteExpression(arg);
            args.add(rewritten);
            if (rewritten != arg) {
                anyRewritten = true;
            }
        }

        if (!anyRewritten) {
            return node;
        }

        return new BoundConstructObjectExpr(node.getConstructor(), args);
    }

    @Override
    public BoundNode visitSequenceExpr(BoundSequenceExpr node) {
        var rewrittenSideEffects = new ArrayList<BoundStatement>();
        boolean anyRewritten = false;
        for (var stmt : node.sideEffects()) {
            var rewritten = rewriteStatement(stmt);
            rewrittenSideEffects.add(rewritten);
            if (rewritten != stmt) {
                anyRewritten = true;
            }
        }

        var rewrittenValue = rewriteExpression(node.value());
        if (!anyRewritten && rewrittenValue == node.value()) {
            return node;
        }

        return new BoundSequenceExpr(rewrittenSideEffects, rewrittenValue);
    }

    @Override
    public BoundNode visitTypeTestExpr(BoundTypeTestExpr node) {
        return node;
    }

    // Statements
    @Override
    public BoundNode visitBlockStmt(BoundBlockStmt node) {
        var rewritten = new ArrayList<BoundStatement>();
        boolean anyRewritten = false;
        for (var stmt : node.statements()) {
            var r = rewriteStatement(stmt);
            rewritten.add(r);
            if (r != stmt) {
                anyRewritten = true;
            }
        }
        if (!anyRewritten) {
            return node;
        }
        return new BoundBlockStmt(rewritten);
    }

    @Override
    public BoundNode visitExpressionStmt(BoundExpressionStmt node) {
        var expr = rewriteExpression(node.expression());
        if (expr == node.expression()) {
            return node;
        }
        return new BoundExpressionStmt(expr);
    }

    @Override
    public BoundNode visitIfStmt(BoundIfStmt node) {
        var ifClause = (BoundIfClause) node.ifClause.accept(this);
        var elseIfClauses = new ArrayList<BoundIfClause>();
        boolean anyRewritten = ifClause != node.ifClause;
        for (var clause : node.elseIfClauses) {
            var r = (BoundIfClause) clause.accept(this);
            elseIfClauses.add(r);
            if (r != clause) {
                anyRewritten = true;
            }
        }
        var elseClause = node.elseBlock() == null ? null : rewriteStatement(node.elseBlock());
        if (!anyRewritten && elseClause == node.elseBlock()) {
            return node;
        }
        return new BoundIfStmt(ifClause, elseIfClauses, elseClause);
    }

    @Override
    public BoundNode visitWhileStmt(BoundWhileStmt node) {
        var condition = rewriteExpression(node.condition());
        var body = rewriteBlockStmt(node.body());
        if (condition == node.condition() && body == node.body()) {
            return node;
        }
        return new BoundWhileStmt(condition, body);
    }

    @Override
    public BoundNode visitForStmt(BoundForStmt node) {
        var condition = rewriteExpression(node.conditionExpr);
        var step = rewriteExpression(node.stepExpr);
        var body = rewriteBlockStmt(node.body);
        if (condition == node.conditionExpr && step == node.stepExpr && body == node.body) {
            return node;
        }
        return new BoundForStmt(node.declarator, condition, step, body);
    }

    @Override
    public BoundNode visitVariableDeclStmt(BoundVariableDeclStmt node) {
        var init = rewriteExpression(node.initializer());
        if (init == node.initializer()) {
            return node;
        }
        return new BoundVariableDeclStmt(node.variable(), init);
    }

    @Override
    public BoundNode visitReturnStmt(BoundReturnStmt node) {
        if (node.expression == null) {
            return node;
        }
        var expr = rewriteExpression(node.expression);
        if (expr == node.expression) {
            return node;
        }
        return new BoundReturnStmt(expr);
    }

    @Override
    public BoundNode visitGotoStmt(BoundGotoStmt node) {
        return node;
    }

    @Override
    public BoundNode visitConditionalGotoStmt(BoundConditionalGotoStmt node) {
        return node;
    }

    @Override
    public BoundNode visitLabelStmt(BoundLabelStmt node) {
        return node;
    }

    @Override
    public BoundNode visitErrorStmt(BoundErrorStmt node) {
        return node;
    }
}
