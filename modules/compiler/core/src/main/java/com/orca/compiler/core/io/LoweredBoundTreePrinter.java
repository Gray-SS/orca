package com.orca.compiler.core.io;

import java.util.ArrayList;
import java.util.List;

import com.orca.compiler.core.boundtree.BoundExpression;
import com.orca.compiler.core.boundtree.BoundField;
import com.orca.compiler.core.boundtree.BoundIfClause;
import com.orca.compiler.core.boundtree.BoundLabel;
import com.orca.compiler.core.boundtree.BoundMethod;
import com.orca.compiler.core.boundtree.BoundNamespace;
import com.orca.compiler.core.boundtree.BoundProgram;
import com.orca.compiler.core.boundtree.BoundStatement;
import com.orca.compiler.core.boundtree.BoundType;
import com.orca.compiler.core.boundtree.BoundVariable;
import com.orca.compiler.core.boundtree.BoundVariableDeclarator;
import com.orca.compiler.core.boundtree.expressions.BoundArrayAccessExpr;
import com.orca.compiler.core.boundtree.expressions.BoundArrayLiteralExpr;
import com.orca.compiler.core.boundtree.expressions.BoundAssignmentExpr;
import com.orca.compiler.core.boundtree.expressions.BoundBinaryExpr;
import com.orca.compiler.core.boundtree.expressions.BoundCollectionLiteralExpr;
import com.orca.compiler.core.boundtree.expressions.BoundConstructObjectExpr;
import com.orca.compiler.core.boundtree.expressions.BoundConversionExpr;
import com.orca.compiler.core.boundtree.expressions.BoundLiteralExpr;
import com.orca.compiler.core.boundtree.expressions.BoundMethodCallExpr;
import com.orca.compiler.core.boundtree.expressions.BoundReferenceExpr;
import com.orca.compiler.core.boundtree.expressions.BoundTypeTestExpr;
import com.orca.compiler.core.boundtree.expressions.BoundUnaryExpr;
import com.orca.compiler.core.boundtree.statements.BoundBlockStmt;
import com.orca.compiler.core.boundtree.statements.BoundConditionalGotoStmt;
import com.orca.compiler.core.boundtree.statements.BoundExpressionStmt;
import com.orca.compiler.core.boundtree.statements.BoundForStmt;
import com.orca.compiler.core.boundtree.statements.BoundGotoStmt;
import com.orca.compiler.core.boundtree.statements.BoundIfStmt;
import com.orca.compiler.core.boundtree.statements.BoundLabelStmt;
import com.orca.compiler.core.boundtree.statements.BoundReturnStmt;
import com.orca.compiler.core.boundtree.statements.BoundVariableDeclStmt;
import com.orca.compiler.core.boundtree.statements.BoundWhileStmt;
import com.orca.compiler.core.typesystem.LangType;

public final class LoweredBoundTreePrinter {

    private LoweredBoundTreePrinter() {
    }

    public static void print(BoundProgram program, int indentSize) {
        System.out.print(render(program, indentSize));
    }

    public static String render(BoundProgram program, int indentSize) {
        if (program == null) {
            return "<empty program>\n";
        }

        DumpVisitor visitor = new DumpVisitor(indentSize);
        visitor.dumpProgram(program);
        return visitor.text();
    }

    private static final class DumpVisitor {

        private final StringBuilder out = new StringBuilder();
        private final int indentSize;
        private int indentLevel = 0;

        DumpVisitor(int indentSize) {
            this.indentSize = indentSize;
        }

        private String text() {
            return out.toString();
        }

        private void indent() {
            indentLevel++;
        }

        private void dedent() {
            indentLevel--;
        }

        private void println(String text) {
            out.append(" ".repeat(indentLevel * indentSize)).append(text).append('\n');
        }

        private void dumpProgram(BoundProgram program) {
            var globalNamespace = program.getGlobalNamespace();
            dumpNamespace(globalNamespace);
        }

        private void dumpNamespace(BoundNamespace boundNamespace) {
            var namespaceSymbol = boundNamespace.getSymbol();

            if (!namespaceSymbol.isGlobalNamespace()) {
                println("namespace " + namespaceSymbol.name() + " {");
                indent();
            }

            for (var type : boundNamespace.getTypes()) {
                dumpType(type);
                out.append('\n');
            }

            for (var method : boundNamespace.getMethods()) {
                dumpMethod(method);
                out.append('\n');
            }

            for (var variable : boundNamespace.getVariables()) {
                dumpVariable(variable);
                out.append('\n');
            }

            for (var innerNamespace : boundNamespace.getNamespaces()) {
                dumpNamespace(innerNamespace);
                out.append('\n');
            }

            if (!namespaceSymbol.isGlobalNamespace()) {
                dedent();
                println("}");
            }
        }

        private void dumpType(BoundType boundType) {
            var typeSymbol = boundType.getSymbol();
            var typeShape = typeSymbol.type();
            println("type " + typeShapeString(typeShape) + " {");
            indent();
            for (var field : boundType.getFields()) {
                dumpField(field);
                out.append('\n');
            }

            for (var method : boundType.getMethods()) {
                dumpMethod(method);
                out.append('\n');
            }

            for (var variable : boundType.getVariables()) {
                dumpVariable(variable);
                out.append('\n');
            }
            dedent();
            println("}");
        }

        private void dumpField(BoundField field) {
            String fieldName = field.getSymbol().name();
            LangType fieldType = field.getSymbol().type();
            String isFinalStr = field.getSymbol().isCompileTimeConstant() ? "final " : "";

            println(isFinalStr + typeShapeString(fieldType) + " " + fieldName + ";");
        }

        private void dumpVariable(BoundVariable variable) {
            String varName = variable.getSymbol().name();
            LangType varType = variable.getSymbol().type();
            String isFinalStr = variable.getSymbol().isCompileTimeConstant() ? "final " : "";

            if (variable.initializer() != null) {
                print(isFinalStr + typeShapeString(varType) + " " + varName + " = ");
                dumpExpression(variable.initializer());
                out.append(";");
            } else {
                println(isFinalStr + typeShapeString(varType) + " " + varName + ";");
            }
        }

        private void dumpMethod(BoundMethod function) {
            String funcName = function.getSymbol().name();
            String returnType = typeShapeString(function.returnType());

            var params = function.parameters();
            var paramSb = new StringBuilder();
            for (int i = 0; i < params.size(); i++) {
                if (i > 0) {
                    paramSb.append(", ");
                }
                var p = params.get(i);
                var paramTypeStr = typeShapeString(p.type());
                paramSb.append(paramTypeStr).append(' ').append(p.name());
            }

            println("def " + returnType + " " + funcName + "(" + paramSb + ") {");
            // Dump statements with label-aware indentation
            dumpBlockStmt(function.getBody(), false);
            println("}");
        }

        private void dumpLocalVariables(BoundBlockStmt block) {
            List<BoundVariableDeclStmt> varDecls = new ArrayList<>();
            collectVariableDecls(block, varDecls);

            for (BoundVariableDeclStmt varDecl : varDecls) {
                println(typeShapeString(varDecl.variable().type()) + " " + varDecl.variable().name() + ";");
            }

            if (!varDecls.isEmpty()) {
                out.append('\n');
            }
        }

        private void collectVariableDecls(BoundBlockStmt block, List<BoundVariableDeclStmt> varDecls) {
            if (block == null) {
                return;
            }

            for (BoundStatement stmt : block.statements()) {
                if (stmt instanceof BoundVariableDeclStmt varDecl) {
                    varDecls.add(varDecl);
                }
            }
        }

        private void dumpStatementsWithLabelIndentation(BoundBlockStmt block) {
            int baseIndent = indentLevel;

            for (BoundStatement stmt : block.statements()) {
                if (stmt instanceof BoundLabelStmt labelStmt) {
                    // Return to base indentation level for the label
                    while (indentLevel > baseIndent) {
                        dedent();
                    }
                    dumpLabelStmt(labelStmt);
                    // Indent for statements following this label
                    indent();
                } else {
                    dumpStatement(stmt);
                }
            }

            // Return to base indentation level at the end
            while (indentLevel > baseIndent) {
                dedent();
            }
        }

        private void dumpStatement(BoundStatement stmt) {
            switch (stmt) {
                case BoundExpressionStmt exprStmt ->
                    dumpExpressionStmt(exprStmt);
                case BoundVariableDeclStmt varDecl -> {
                    // Variable declarations are already printed in dumpLocalVariables.
                    // If they have an initializer, print it as an assignment to preserve execution order.
                    if (varDecl.initializer() != null) {
                        print(varDecl.variable().name() + " = ");
                        dumpExpression(varDecl.initializer());
                        out.append(";\n");
                    }
                }
                case BoundIfStmt ifStmt ->
                    dumpIfStmt(ifStmt);
                case BoundWhileStmt whileStmt ->
                    dumpWhileStmt(whileStmt);
                case BoundForStmt forStmt ->
                    dumpForStmt(forStmt);
                case BoundBlockStmt blockStmt ->
                    dumpBlockStmt(blockStmt, true);
                case BoundReturnStmt returnStmt ->
                    dumpReturnStmt(returnStmt);
                case BoundGotoStmt gotoStmt ->
                    dumpGotoStmt(gotoStmt);
                case BoundLabelStmt labelStmt ->
                    dumpLabelStmt(labelStmt);
                case BoundConditionalGotoStmt condGotoStmt ->
                    dumpConditionalGotoStmt(condGotoStmt);
                case null -> {
                    println("<null>");
                }
                default ->
                    println("/* unknown statement: " + stmt.kind() + " */");
            }
        }

        private void dumpExpressionStmt(BoundExpressionStmt exprStmt) {
            print("");
            dumpExpression(exprStmt.expression());
            out.append(";\n");
        }

        private void dumpAssignmentExpr(BoundAssignmentExpr assignExpr) {
            dumpExpression(assignExpr.targetExpr());
            out.append(" ").append(assignExpr.operator().getOperatorText()).append(" ");
            dumpExpression(assignExpr.valueExpr());
        }

        private void dumpIfStmt(BoundIfStmt ifStmt) {
            // Print main if clause
            print("if (");
            dumpExpression(ifStmt.ifClause.condition);
            out.append(") {");
            out.append('\n');

            indent();
            dumpStatement(ifStmt.ifClause.body);
            dedent();

            println("}");

            // Print else if clauses
            for (BoundIfClause clause : ifStmt.elseIfClauses) {
                print("else if (");
                dumpExpression(clause.condition);
                out.append(") {");
                out.append('\n');

                indent();
                dumpStatement(clause.body);
                dedent();

                println("}");
            }

            // Print else clause
            if (ifStmt.elseClause != null) {
                println("else {");
                indent();
                dumpStatement(ifStmt.elseClause);
                dedent();
                println("}");
            }
        }

        private void dumpWhileStmt(BoundWhileStmt whileStmt) {
            print("while (");
            dumpExpression(whileStmt.condition());
            out.append(") {");
            out.append('\n');

            indent();
            dumpStatement(whileStmt.body());
            dedent();

            println("}");
        }

        private void dumpForStmt(BoundForStmt forStmt) {
            print("for (");
            dumpVariableDeclarator(forStmt.declarator);
            out.append("; ");
            dumpExpression(forStmt.conditionExpr);
            out.append("; ");
            dumpExpression(forStmt.stepExpr);
            out.append(") {");
            out.append('\n');

            indent();
            dumpStatement(forStmt.body);
            dedent();

            println("}");
        }

        private void dumpBlockStmt(BoundBlockStmt blockStmt, boolean preserveBraces) {
            if (preserveBraces) {
                println("{");
            }

            indent();
            dumpLocalVariables(blockStmt);
            dumpStatementsWithLabelIndentation(blockStmt);
            dedent();

            if (preserveBraces) {
                println("}");
            }
        }

        private void dumpReturnStmt(BoundReturnStmt returnStmt) {
            if (returnStmt.expression != null) {
                print("return ");
                dumpExpression(returnStmt.expression);
                out.append(";\n");
            } else {
                println("return;");
            }
        }

        private void dumpGotoStmt(BoundGotoStmt gotoStmt) {
            println("goto " + formatLabel(gotoStmt.label) + ";");
        }

        private void dumpLabelStmt(BoundLabelStmt labelStmt) {
            println("label " + formatLabel(labelStmt.label) + ":");
        }

        private void dumpConditionalGotoStmt(BoundConditionalGotoStmt condGotoStmt) {
            print("if (");
            if (condGotoStmt.inverseCondition) {
                out.append("!");
            }
            dumpExpression(condGotoStmt.condition);
            out.append(") goto ");
            out.append(formatLabel(condGotoStmt.label));
            out.append(";\n");
        }

        private String formatLabel(BoundLabel label) {
            return "#" + label.name();
        }

        private void dumpExpression(BoundExpression expr) {
            switch (expr) {
                case BoundLiteralExpr literal ->
                    dumpLiteralExpr(literal);
                case BoundAssignmentExpr assignExpr ->
                    dumpAssignmentExpr(assignExpr);
                case BoundReferenceExpr identifierExpr ->
                    dumpReferenceExpr(identifierExpr);
                case BoundBinaryExpr binaryExpr ->
                    dumpBinaryExpr(binaryExpr);
                case BoundUnaryExpr unaryExpr ->
                    dumpUnaryExpr(unaryExpr);
                case BoundMethodCallExpr callExpr ->
                    dumpFunctionCallExpr(callExpr);
                case BoundArrayAccessExpr arrayAccessExpr ->
                    dumpArrayAccessExpr(arrayAccessExpr);
                case BoundCollectionLiteralExpr collLiteralExpr ->
                    dumpCollectionLiteralExpr(collLiteralExpr);
                case BoundArrayLiteralExpr arrayLiteralExpr ->
                    dumpArrayLiteralExpr(arrayLiteralExpr);
                case BoundConversionExpr convExpr ->
                    dumpConversionExpr(convExpr);
                case BoundTypeTestExpr typeTestExpr ->
                    dumpTypeTestExpr(typeTestExpr);
                case BoundConstructObjectExpr constructExpr ->
                    dumpConstructObjectExpr(constructExpr);
                case null ->
                    out.append("<null>");
                default ->
                    out.append("/* unknown expression: ").append(expr.kind()).append(" */");
            }
        }

        private void dumpConstructObjectExpr(BoundConstructObjectExpr constructExpr) {
            out.append("new ").append(typeShapeString(constructExpr.type()));
            out.append("(");

            var arguments = constructExpr.getArguments();
            for (int i = 0; i < arguments.size(); i++) {
                if (i > 0) {
                    out.append(", ");
                }
                dumpExpression(arguments.get(i));
            }
            out.append(")");
        }

        private void dumpTypeTestExpr(BoundTypeTestExpr typeTestExpr) {
            dumpExpression(typeTestExpr.operand());
            out.append(" is ").append(typeShapeString(typeTestExpr.targetType()));
        }

        private void dumpLiteralExpr(BoundLiteralExpr literal) {
            var constant = literal.getConstant();
            if (constant.type().isString()) {
                out.append('"').append(constant.getDisplayValue()).append('"');
                return;
            }

            out.append(constant.getDisplayValue());
        }

        private void dumpReferenceExpr(BoundReferenceExpr identifierExpr) {
            if (identifierExpr instanceof BoundReferenceExpr.MemberAccessRef ma) {
                dumpExpression(ma.getReceiver());
                out.append(".");
                dumpReferenceExpr(ma.getMemberExpr());
                return;
            }

            out.append(identifierExpr.getReferencedSymbol().name());
        }

        private void dumpBinaryExpr(BoundBinaryExpr binaryExpr) {
            dumpExpression(binaryExpr.left);
            out.append(" ").append(binaryExpr.operator.getOperatorText()).append(" ");
            dumpExpression(binaryExpr.right);
        }

        private void dumpUnaryExpr(BoundUnaryExpr unaryExpr) {
            out.append(unaryExpr.operator.getOperatorText());
            dumpExpression(unaryExpr.operand);
        }

        private void dumpFunctionCallExpr(BoundMethodCallExpr callExpr) {
            out.append(callExpr.methodSymbol.name()).append("(");

            for (int i = 0; i < callExpr.arguments.size(); i++) {
                if (i > 0) {
                    out.append(", ");
                }
                dumpExpression(callExpr.arguments.get(i));
            }

            out.append(")");
        }

        private void dumpArrayAccessExpr(BoundArrayAccessExpr arrayAccessExpr) {
            dumpExpression(arrayAccessExpr.arrayExpr());
            out.append("[");
            dumpExpression(arrayAccessExpr.indexExpr());
            out.append("]");
        }

        private void dumpCollectionLiteralExpr(BoundCollectionLiteralExpr collLiteralExpr) {
            if (collLiteralExpr.symbol != null) {
                out.append(collLiteralExpr.symbol.name()).append("(");
            } else {
                out.append("<unknown collection>(");
            }

            for (int i = 0; i < collLiteralExpr.arguments.size(); i++) {
                if (i > 0) {
                    out.append(", ");
                }
                dumpExpression(collLiteralExpr.arguments.get(i));
            }

            out.append(")");
        }

        private void dumpArrayLiteralExpr(BoundArrayLiteralExpr arrayLiteralExpr) {
            out.append("new ").append(typeShapeString(arrayLiteralExpr.elementType));
            out.append(" [");
            dumpExpression(arrayLiteralExpr.lengthExpr);
            out.append("]");
        }

        private void dumpConversionExpr(BoundConversionExpr convExpr) {
            // Show the original expression, not the conversion
            out.append("(").append(typeShapeString(convExpr.type())).append(")");
            dumpExpression(convExpr.operand());
        }

        private static String typeShapeString(LangType typeShape) {
            if (typeShape == null) {
                return "<null>";
            }

            var sb = new StringBuilder();
            sb.append(typeShape.displayName());
            if (typeShape.unwrap() != typeShape) {
                sb.append("(").append(typeShape.unwrap().displayName()).append(")");
            }

            return sb.toString();
        }

        private void print(String text) {
            out.append(" ".repeat(indentLevel * indentSize)).append(text);
        }

        private void dumpVariableDeclarator(BoundVariableDeclarator varDecl) {
            println(typeShapeString(varDecl.variable().type()) + " " + varDecl.variable().name() + ";");
        }
    }
}
