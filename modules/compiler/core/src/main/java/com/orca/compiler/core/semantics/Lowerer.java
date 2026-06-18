package com.orca.compiler.core.semantics;

import java.util.ArrayList;
import java.util.List;

import com.orca.compiler.core.boundtree.BoundExpression;
import com.orca.compiler.core.boundtree.BoundLabel;
import com.orca.compiler.core.boundtree.BoundNode;
import com.orca.compiler.core.boundtree.BoundNodeFactory;
import com.orca.compiler.core.boundtree.BoundStatement;
import com.orca.compiler.core.boundtree.BoundTreeRewriter;
import com.orca.compiler.core.boundtree.BoundVariableDeclarator;
import com.orca.compiler.core.boundtree.constants.BoundBoolConstant;
import com.orca.compiler.core.boundtree.constants.BoundIntConstant;
import com.orca.compiler.core.boundtree.expressions.BoundAssignmentExpr;
import com.orca.compiler.core.boundtree.expressions.BoundBinaryExpr;
import com.orca.compiler.core.boundtree.expressions.BoundLiteralExpr;
import com.orca.compiler.core.boundtree.expressions.BoundOperators;
import com.orca.compiler.core.boundtree.expressions.BoundReferenceExpr;
import com.orca.compiler.core.boundtree.expressions.BoundSequenceExpr;
import com.orca.compiler.core.boundtree.expressions.BoundUnaryExpr;
import com.orca.compiler.core.boundtree.statements.BoundBlockStmt;
import com.orca.compiler.core.boundtree.statements.BoundExpressionStmt;
import com.orca.compiler.core.boundtree.statements.BoundForStmt;
import com.orca.compiler.core.boundtree.statements.BoundIfStmt;
import com.orca.compiler.core.boundtree.statements.BoundVariableDeclStmt;
import com.orca.compiler.core.boundtree.statements.BoundWhileStmt;
import com.orca.compiler.core.symbols.synthesized.SynthesizedVariableSymbol;
import com.orca.compiler.core.syntax.expressions.AssignmentOperatorKind;
import com.orca.compiler.core.syntax.expressions.BinaryOperatorKind;
import com.orca.compiler.core.text.SourceSpan;
import com.orca.compiler.core.typesystem.LangType;

public final class Lowerer extends BoundTreeRewriter {

    private static int labelCounter = 0;
    private static int tempVarCounter = 0;

    public static BoundBlockStmt lowerBody(BoundBlockStmt body) {
        var lowerer = new Lowerer();
        return flattenBlock(lowerer.rewriteBlockStmt(body));
    }

    public static BoundNode lower(BoundNode node) {
        var lowerer = new Lowerer();
        switch (node) {
            case BoundExpression expr -> {
                return lowerer.rewriteExpression(expr);
            }
            case BoundStatement stmt -> {
                var boundStmt = lowerer.rewriteStatement(stmt);
                if (boundStmt instanceof BoundBlockStmt block) {
                    boundStmt = flattenBlock(block);
                }

                return boundStmt;
            }
            default -> {
                throw new IllegalArgumentException("Cannot lower node of type: " + node.getClass().getName());
            }
        }
    }

    private static BoundBlockStmt flattenBlock(BoundBlockStmt block) {
        var statements = new ArrayList<BoundStatement>();
        for (BoundStatement stmt : block.statements()) {
            if (stmt instanceof BoundBlockStmt nestedBlock) {
                statements.addAll(flattenBlock(nestedBlock).statements());
            } else {
                statements.add(stmt);
            }
        }
        return new BoundBlockStmt(statements);
    }

    @Override
    public BoundStatement rewriteWhileStmt(BoundWhileStmt whileStmt) {
        /**
         * while (condition) { body }
         *
         * Is lowered to:
         *
         * label continue; continue: if (!condition) goto break; body goto
         * continue; break:
         */

        var breakLabel = generateLabel("break");
        var continueLabel = generateLabel("continue");

        var body = rewriteStatement(whileStmt.body());
        var condition = rewriteExpression(whileStmt.condition());

        SourceSpan span = whileStmt.span();
        return BoundNodeFactory.synthesizedBlock(
                span,
                BoundNodeFactory.synthesizedLabel(span, continueLabel),
                // if (!condition) goto break;
                BoundNodeFactory.synthesizedConditionalGoto(span, breakLabel, condition, true),
                body,
                BoundNodeFactory.synthesizedGoto(span, continueLabel),
                BoundNodeFactory.synthesizedLabel(span, breakLabel)
        );
    }

    @Override
    public BoundStatement rewriteIfStmt(BoundIfStmt ifStmt) {
        /**
         * if (condition1) { thenBody1 } else if (condition[n]) { thenBody[n]
         * else { elseBody }
         *
         * Is lowered to:
         *
         * if (!condition1) goto thenBranch2; thenBody1 goto end;
         *
         * thenBranch2: if (!condition2) goto thenBranch3; thenBody2 goto end;
         *
         * ...
         *
         * thenBranch[n]: if (!condition[n]) goto elseBranch; thenBody[n] goto
         * end;
         *
         * elseBranch: elseBody
         *
         * end:
         */

        var loc = ifStmt.span();
        var statements = new ArrayList<BoundStatement>();

        BoundLabel endLabel = generateLabel("end");
        BoundLabel elseLabel = (ifStmt.elseBlock() != null) ? generateLabel("else") : endLabel;

        var clauses = ifStmt.thenClauses();
        for (int i = 0; i < clauses.size(); i++) {
            var clause = clauses.get(i);
            BoundLabel nextLabel = (i + 1 < clauses.size()) ? generateLabel("elseIf") : elseLabel;

            // if (!condition) goto nextLabel;
            statements.add(BoundNodeFactory.synthesizedConditionalGoto(loc, nextLabel, rewriteExpression(clause.condition), true));
            statements.add(rewriteStatement(clause.body));
            statements.add(BoundNodeFactory.synthesizedGoto(loc, endLabel));
            if (nextLabel != endLabel) {
                statements.add(BoundNodeFactory.synthesizedLabel(loc, nextLabel));
            }
        }

        if (ifStmt.elseBlock() != null) {
            statements.add(rewriteStatement(ifStmt.elseBlock()));
        }

        statements.add(BoundNodeFactory.synthesizedLabel(loc, endLabel));
        return BoundNodeFactory.synthesizedBlock(loc, statements.toArray(BoundStatement[]::new));
    }

    @Override
    public BoundExpression rewriteAssignmentExpr(BoundAssignmentExpr node) {
        // Lower compound assignments (e.g., a += b) to simple assignments (e.g., a = a + b)
        if (node.operator().kind() != AssignmentOperatorKind.Simple) {
            var span = node.span();
            var left = rewriteExpression(node.targetExpr());
            var right = rewriteExpression(node.valueExpr());

            var binaryOperatorKind = switch (node.operator().kind()) {
                case AdditionAssignment ->
                    BinaryOperatorKind.Addition;
                case SubtractionAssignment ->
                    BinaryOperatorKind.Subtraction;
                case MultiplicationAssignment ->
                    BinaryOperatorKind.Multiplication;
                case DivisionAssignment ->
                    BinaryOperatorKind.Division;
                case ModuloAssignment ->
                    BinaryOperatorKind.Modulo;
                case Simple ->
                    throw new IllegalStateException("Unexpected simple assignment operator in compound assignment rewrite.");
            };

            var boundOperator = BoundOperators.bindBinaryOperatorOrThrow(binaryOperatorKind, left.type(), right.type());
            var binaryExpr = new BoundBinaryExpr(boundOperator, left, right);

            return BoundNodeFactory.synthesizedSimpleAssignment(span, node.targetExpr(), binaryExpr);
        }

        return super.rewriteAssignmentExpr(node);
    }

    @Override
    public BoundStatement rewriteForStmt(BoundForStmt forStmt) {
        /**
         * for (init; condition; step) { body }
         *
         * Is lowered to: init; while (condition) { body; step; }
         */
        var loweredInit = new BoundVariableDeclarator(forStmt.declarator.variable(), rewriteExpression(forStmt.declarator.initializer()));
        var loweredCondition = rewriteExpression(forStmt.conditionExpr);
        var loweredStep = rewriteExpression(forStmt.stepExpr);
        var loweredBody = rewriteBlockStmt(forStmt.body);

        var loweredForStmt = new BoundBlockStmt(List.of(
                new BoundVariableDeclStmt(loweredInit),
                new BoundWhileStmt(loweredCondition, new BoundBlockStmt(List.of(
                        loweredBody,
                        new BoundExpressionStmt(loweredStep)
                )))
        ));

        return rewriteStatement(loweredForStmt);
    }

    @Override
    public BoundExpression rewriteBinaryExpr(BoundBinaryExpr node) {
        var operatorKind = node.operator.kind();
        if (operatorKind == BinaryOperatorKind.LogicalAnd || operatorKind == BinaryOperatorKind.LogicalOr) {
            // a && b is lowered to:
            // temp = false;
            // if (a) temp = b;
            // return temp;
            // a || b is lowered to:
            // temp = true;
            // if (!a) temp = b;
            // return temp;
            var span = node.span();
            var a = rewriteExpression(node.left);
            var b = rewriteExpression(node.right);

            var isAnd = operatorKind == BinaryOperatorKind.LogicalAnd;

            // temp = isAnd ? false : true;
            var initialValue = new BoundLiteralExpr(isAnd ? BoundBoolConstant.FALSE : BoundBoolConstant.TRUE);

            var tmp = generateTempVariable(LangType.Bool);
            var tmpRef = BoundReferenceExpr.of(tmp);
            var endLabel = generateLabel("andEnd");

            return new BoundSequenceExpr(
                    List.of(
                            new BoundVariableDeclStmt(tmp, initialValue),
                            BoundNodeFactory.synthesizedConditionalGoto(span, endLabel, a, isAnd),
                            new BoundExpressionStmt(BoundAssignmentExpr.simpleAssignment(tmpRef, b)),
                            BoundNodeFactory.synthesizedLabel(span, endLabel)
                    ),
                    tmpRef
            );
        }

        return super.rewriteBinaryExpr(node);
    }

    @Override
    public BoundExpression rewriteUnaryExpr(BoundUnaryExpr node) {
        var operatorKind = node.operator.kind();
        switch (operatorKind) {
            case Increment -> {
                // a++ is lowered to: a = a + 1
                var span = node.span();
                var operand = rewriteExpression(node.operand);
                var one = new BoundLiteralExpr(new BoundIntConstant(1));
                var boundOperator = BoundOperators.bindBinaryOperatorOrThrow(BinaryOperatorKind.Addition, operand.type(), one.type());
                var addition = new BoundBinaryExpr(boundOperator, operand, one);
                return BoundNodeFactory.synthesizedSimpleAssignment(span, (BoundReferenceExpr) node.operand, addition);
            }
            case Decrement -> {
                // a-- is lowered to: a = a - 1
                var span = node.span();
                var operand = rewriteExpression(node.operand);
                var one = new BoundLiteralExpr(new BoundIntConstant(1));
                var boundOperator = BoundOperators.bindBinaryOperatorOrThrow(BinaryOperatorKind.Subtraction, operand.type(), one.type());
                var subtraction = new BoundBinaryExpr(boundOperator, operand, one);
                return BoundNodeFactory.synthesizedSimpleAssignment(span, (BoundReferenceExpr) node.operand, subtraction);
            }
            default -> {
                return super.rewriteUnaryExpr(node);
            }
        }
    }

    private BoundLabel generateLabel(String prefix) {
        String name = prefix + labelCounter++;
        return new BoundLabel(name);
    }

    private SynthesizedVariableSymbol generateTempVariable(LangType type) {
        String name = "temp$" + tempVarCounter++;
        return new SynthesizedVariableSymbol(name, type);
    }
}
