package com.orca.compiler.core.io;

import java.util.ArrayList;

import com.orca.compiler.core.lexer.TokenKind;
import com.orca.compiler.core.syntax.CompilationUnit;
import com.orca.compiler.core.syntax.SyntaxList;
import com.orca.compiler.core.syntax.SyntaxNode;
import com.orca.compiler.core.syntax.SyntaxToken;
import com.orca.compiler.core.syntax.SyntaxTree;
import com.orca.compiler.core.syntax.SyntaxWalker;
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
import com.orca.compiler.core.syntax.types.TypeSyntax;

public final class EnhancedSyntaxTreePrinter {

    private EnhancedSyntaxTreePrinter() {
    }

    public static void print(SyntaxTree syntaxTree, int indentSize) {
        System.out.print(render(syntaxTree));
    }

    public static String render(SyntaxTree syntaxTree) {
        if (syntaxTree == null || syntaxTree.root() == null) {
            return "<empty AST>\n";
        }

        DumpVisitor visitor = new DumpVisitor();
        visitor.walk(syntaxTree.root());

        return visitor.text();
    }

    private static final class DumpVisitor extends SyntaxWalker {

        private final StringBuilder out = new StringBuilder();
        private final ArrayList<Boolean> ancestorHasNext = new ArrayList<>();

        private String text() {
            return out.toString();
        }

        @Override
        public void walk(SyntaxNode node) {
            if (node == null) {
                return;
            }

            if (node.hasError()) {
                AnsiConsole.pushColor(AnsiColor.RED);
            }
            boolean isRoot = ancestorHasNext.isEmpty();
            if (isRoot) {
                printRoot(node.getClass().getSimpleName() + "Decl");
            } else {
                node.accept(this);
            }
            if (node.hasError()) {
                AnsiConsole.popColor();
            }

            var children = node.children();
            if (children == null || children.isEmpty()) {
                return;
            }

            for (int i = 0; i < children.size(); i++) {
                SyntaxNode child = children.get(i);
                boolean childIsLast = (i == children.size() - 1);

                // push whether this child has more siblings (true == has more siblings)
                ancestorHasNext.add(!childIsLast);
                walk(child);
                ancestorHasNext.remove(ancestorHasNext.size() - 1);
            }
        }

        private void printRoot(String text) {
            AnsiConsole.println(System.out, text + '\n');
        }

        private void printLine(String text) {
            StringBuilder prefix = new StringBuilder();

            // For each ancestor (except the current node), print the vertical bar or spaces
            for (int i = 0; i < Math.max(0, ancestorHasNext.size() - 1); i++) {
                boolean hasNext = ancestorHasNext.get(i);
                prefix.append(hasNext ? "| " : "  ");
            }

            // Determine whether the current node is the last among its siblings
            boolean currentIsLast = ancestorHasNext.isEmpty() ? true : !ancestorHasNext.get(ancestorHasNext.size() - 1);
            prefix.append(currentIsLast ? "`-" : "|-");

            AnsiConsole.println(System.out, prefix.append(text).toString());
        }

        private static String q(String s) {
            if (s == null) {
                return "<null>";
            }
            return "'" + s.replace("\\", "\\\\").replace("'", "\\'") + "'";
        }

        private static String typeString(TypeSyntax typeSyntax) {
            if (typeSyntax == null) {
                return "<null>";
            }

            if (typeSyntax instanceof IdentifierTypeSyntax simpleTypeSyntax) {
                return simpleTypeSyntax.identifier() == null ? "<null>" : simpleTypeSyntax.identifier().text();
            }

            if (typeSyntax instanceof ArrayTypeSyntax arrayTypeSyntax) {
                return typeString(arrayTypeSyntax.elementType()) + "[]";
            }

            return typeSyntax.toString();
        }

        private static String literalNodeName(TokenKind kind) {
            if (kind == null) {
                return "Literal";
            }

            return switch (kind) {
                case IntegerLiteral ->
                    "IntegerLiteral";
                case FloatLiteral ->
                    "FloatLiteral";
                case StringLiteral ->
                    "StringLiteral";
                case BoolLiteral ->
                    "BoolLiteral";
                default ->
                    "Literal";
            };
        }

        @Override
        public void visitCompilationUnit(CompilationUnit compilationUnit) {
            printRoot("CompilationUnitDecl");
        }

        @Override
        public void visitErrorMember(ErrorMemberSyntax syntax) {
            AnsiConsole.pushColor(AnsiColor.RED);
            printLine("<<ERROR MEMBER>>");
            AnsiConsole.popColor();
        }

        @Override
        public void visitErrorStatement(ErrorStatementSyntax syntax) {
            AnsiConsole.pushColor(AnsiColor.RED);
            printLine("<<ERROR STATEMENT>>");
            AnsiConsole.popColor();
        }

        @Override
        public void visitErrorType(ErrorTypeSyntax syntax) {
            AnsiConsole.pushColor(AnsiColor.RED);
            printLine("<<ERROR TYPE>>");
            AnsiConsole.popColor();
        }

        @Override
        public void visitErrorExpression(ErrorExpressionSyntax syntax) {
            AnsiConsole.pushColor(AnsiColor.RED);
            printLine("<<ERROR EXPRESSION>>");
            AnsiConsole.popColor();
        }

        // Declarations
        @Override
        public void visitGlobalStatement(GlobalStatementSyntax syntax) {
            String where = syntax.loc().toString();
            printLine("GlobalStatement" + (where.isEmpty() ? "" : (" " + where)));
        }

        @Override
        public void visitMethodDeclaration(MethodDeclarationSyntax functionDecl) {
            String name = functionDecl.identifier() == null ? "<anonymous>" : functionDecl.identifier().text();
            String returnType = functionDecl.returnType() == null ? "void" : typeString(functionDecl.returnType());
            String where = functionDecl.loc().toString();

            String header = "FunctionDecl" + (where.isEmpty() ? "" : (" " + where)) + " " + q(name) + " " + q(returnType);
            printLine(header);
        }

        @Override
        public void visitVariableDeclaration(VariableDeclarationSyntax syntax) {
            String header = "VarDecl";
            printLine(header);
        }

        @Override
        public void visitVariableDeclarationStatement(VariableDeclarationStatement variableDecl) {
            String header = "LocalVarDecl";
            printLine(header);
        }

        @Override
        public void visitVariableDeclarator(VariableDeclaratorSyntax syntax) {
            String name = syntax.identifier() == null ? "<unnamed>" : syntax.identifier().text();
            String type = typeString(syntax.type());
            String where = syntax.loc().toString();

            String header = "VarDeclarator" + (where.isEmpty() ? "" : (" " + where)) + " " + q(name) + " " + q(type);
            printLine(header);
        }

        @Override
        public void visitCollectionDeclaration(CollectionDeclarationSyntax collectionDecl) {
            String name = collectionDecl.identifier() == null ? "<unnamed>" : collectionDecl.identifier().text();
            String where = collectionDecl.loc().toString();

            String header = "CollectionDecl" + (where.isEmpty() ? "" : (" " + where)) + " " + q(name);
            printLine(header);
        }

        @Override
        public void visitFieldDeclaration(FieldDeclarationSyntax fieldSyntax) {
            String name = fieldSyntax.identifier() == null ? "<unnamed>" : fieldSyntax.identifier().text();
            String type = typeString(fieldSyntax.type());
            String where = fieldSyntax.loc().toString();

            printLine("FieldDecl" + (where.isEmpty() ? "" : (" " + where)) + " " + q(name) + " " + q(type));
        }

        @Override
        public void visitImplDecl(ImplBlockSyntax implDecl) {
            String name = implDecl.type() == null ? "<unnamed>" : implDecl.type().toString();
            String where = implDecl.loc().toString();

            printLine("ImplDecl" + (where.isEmpty() ? "" : (" " + where)) + " " + q(name));
        }

        // Statements
        @Override
        public void visitIfStmt(IfStmt ifStatement) {
            String where = ifStatement.loc().toString();
            printLine("IfStmt" + (where.isEmpty() ? "" : (" " + where)));
        }

        @Override
        public void visitForStmt(ForStmt forStatement) {
            String where = forStatement.loc().toString();

            printLine("ForStmt" + (where.isEmpty() ? "" : (" " + where)));
        }

        @Override
        public void visitWhileStmt(WhileStmt whileStatement) {
            String where = whileStatement.loc().toString();
            printLine("WhileStmt" + (where.isEmpty() ? "" : (" " + where)));
        }

        @Override
        public void visitReturnStmt(ReturnStmt returnStatement) {
            String where = returnStatement.loc().toString();
            printLine("ReturnStmt" + (where.isEmpty() ? "" : (" " + where)));
        }

        @Override
        public void visitAssignmentExpr(AssignmentExpr assignStmt) {
            String where = assignStmt.loc().toString();
            printLine("AssignmentStmt" + (where.isEmpty() ? "" : (" " + where)));
        }

        @Override
        public void visitUnaryAssignmentExpr(UnaryAssignmentExpr syntax) {
            String symbol = syntax.operatorToken().text();
            String where = syntax.loc().toString();
            printLine("UnaryAssignmentStmt " + q(symbol) + (where.isEmpty() ? "" : (" " + where)));
        }

        @Override
        public void visitBlockStmt(BlockStmt blockStmt) {
            String where = blockStmt.loc().toString();
            int count = blockStmt.statements() == null ? 0 : blockStmt.statements().size();
            printLine("BlockStmt" + (where.isEmpty() ? "" : (" " + where)) + " " + "{...} " + "(" + count + " stmt" + (count == 1 ? "" : "s") + ")");
        }

        @Override
        public void visitExpressionStmt(ExpressionStmt expressionStmt) {
            String where = expressionStmt.loc().toString();
            printLine("ExprStmt" + (where.isEmpty() ? "" : (" " + where)));
        }

        // Expressions
        @Override
        public void visitBinaryExpr(BinaryExpr binaryExpr) {
            String symbol = binaryExpr.operatorToken().text();

            String where = binaryExpr.loc().toString();
            printLine("BinaryOperator " + q(symbol) + " " + (where.isEmpty() ? "" : (" " + where)));
        }

        @Override
        public void visitLiteralExpr(LiteralExpr literalExpr) {
            SyntaxToken token = literalExpr.token();
            TokenKind kind = token == null ? null : token.kind();
            String where = literalExpr.loc().toString();

            Object value = literalExpr.value();
            String valueStr;
            if (value == null) {
                valueStr = "<null>";
            } else if (value instanceof String s) {
                valueStr = q(s);
            } else {
                valueStr = String.valueOf(value);
            }

            printLine(literalNodeName(kind) + (where.isEmpty() ? "" : (" " + where)) + " " + valueStr);
        }

        @Override
        public void visitIdentifierExpr(IdentifierExpr identifierExpr) {
            String where = identifierExpr.loc().toString();
            String idText = "<null>";
            if (identifierExpr.identifier() != null) {
                idText = identifierExpr.identifier().text();
            }
            printLine("IdentifierExpr" + (where.isEmpty() ? "" : (" " + where)) + " " + q(idText));
        }

        @Override
        public void visitInvocationExpr(InvocationExpr callExpr) {
            int argc = callExpr.arguments() == null ? 0 : callExpr.arguments().size();
            String where = callExpr.loc().toString();

            printLine("FunctionCallExpr" + (where.isEmpty() ? "" : (" " + where)) + " args=" + argc);
        }

        @Override
        public void visitMemberAccessExpr(MemberAccessExpr memberAccessExpr) {
            String where = memberAccessExpr.loc().toString();
            String member = memberAccessExpr.memberName() == null ? "<null>" : memberAccessExpr.memberName();
            printLine("MemberExpr " + q(member) + (where.isEmpty() ? "" : (" " + where)));
        }

        @Override
        public void visitUnaryExpr(UnaryExpr unaryExpr) {
            String symbol = unaryExpr.operatorToken().text();
            String where = unaryExpr.loc().toString();
            printLine("UnaryOperator " + q(symbol) + (where.isEmpty() ? "" : (" " + where)));
        }

        @Override
        public void visitArrayAccessExpr(ArrayAccessExpr arrayAccessExpr) {
            String where = arrayAccessExpr.loc().toString();
            printLine("ArrayAccessExpr" + (where.isEmpty() ? "" : (" " + where)));
        }

        @Override
        public void visitArrayLiteralExpr(ArrayLiteralExpression arrayLiteralExpression) {
            String elementType = typeString(arrayLiteralExpression.elementType());
            String where = arrayLiteralExpression.loc().toString();
            printLine("ArrayLiteralExpr" + (where.isEmpty() ? "" : (" " + where)) + " elementType=" + q(elementType));
        }

        @Override
        public void visitTypeTestExpr(TypeTestExpr typeTestExpr) {
            String type = typeString(typeTestExpr.type());

            var identifier = typeTestExpr.identifier();
            String ident = identifier == null ? "" : (" " + q(identifier.text()));
            String where = typeTestExpr.loc().toString();
            printLine("TypeTestExpr" + (where.isEmpty() ? "" : (" " + where)) + " type=" + q(type) + ident);
        }

        @Override
        public void visitImport(ImportSyntax syntax) {
            String where = syntax.loc().toString();
            printLine("ImportDecl" + (where.isEmpty() ? "" : (" " + where)));
        }

        @Override
        public void visitPackage(PackageDirectiveSyntax syntax) {
            String where = syntax.loc().toString();
            printLine("PackageDecl" + (where.isEmpty() ? "" : (" " + where)));
        }

        // Types
        @Override
        public void visitIdentifierTypeSyntax(IdentifierTypeSyntax simpleTypeSyntax) {
            String where = simpleTypeSyntax.loc().toString();
            printLine("Type " + q(typeString(simpleTypeSyntax)) + (where.isEmpty() ? "" : (" " + where)));
        }

        @Override
        public void visitSpecialTypeSyntax(SpecialTypeSyntax syntax) {
            String where = syntax.loc().toString();
            printLine("Special type " + q(syntax.getToken().text()) + (where.isEmpty() ? "" : (" " + where)));
        }

        @Override
        public void visitArrayTypeSyntax(ArrayTypeSyntax arrayTypeSyntax) {
            String where = arrayTypeSyntax.loc().toString();
            printLine("Array type " + q(typeString(arrayTypeSyntax)) + (where.isEmpty() ? "" : (" " + where)));
        }

        // Others
        @Override
        public void visitSyntaxList(SyntaxList<? extends SyntaxNode> syntaxList) {
            int n = syntaxList.elements() == null ? 0 : syntaxList.elements().size();
            if (n == 0) {
                printLine("List <empty>");
                return;
            }

            printLine("List (" + n + ")");
        }

        @Override
        public void visitRangeSyntax(RangeSyntax rangeSyntax) {
            printLine("RangeExpr" + " (start -> end)");
        }

        @Override
        public void visitIfClauseSyntax(IfClauseSyntax ifClauseSyntax) {
            printLine("IfClause");
        }

        @Override
        public void visitElseClauseSyntax(ElseClauseSyntax elseClauseSyntax) {
            printLine("Else");
        }

        @Override
        public void visitParameterSyntax(ParameterSyntax parameterSyntax) {
            String name = parameterSyntax.identifier() == null ? "<unnamed>" : parameterSyntax.identifier().text();
            String type = typeString(parameterSyntax.type());
            String where = parameterSyntax.loc().toString();

            printLine("Param" + (where.isEmpty() ? "" : (" " + where)) + " " + q(name) + " " + q(type));
        }

        @Override
        public void visitSimpleIdentifierSyntax(SimpleIdentifierSyntax simpleIdentifierSyntax) {
            String where = simpleIdentifierSyntax.loc().toString();
            printLine("SimpleIdentifier " + q(simpleIdentifierSyntax.text()) + (where.isEmpty() ? "" : (" " + where)));
        }

        @Override
        public void visitSpecialTypeIdentifier(SpecialTypeIdentifierSyntax syntax) {
            String where = syntax.loc().toString();
            printLine("SpecialTypeIdentifier " + q(syntax.text()) + (where.isEmpty() ? "" : (" " + where)));
        }

        @Override
        public void visitQualifiedIdentifierSyntax(QualifiedIdentifierSyntax memberIdentifierSyntax) {
            String where = memberIdentifierSyntax.loc().toString();
            printLine("QualifiedIdentifier " + q(memberIdentifierSyntax.text()) + (where.isEmpty() ? "" : (" " + where)));
        }

        @Override
        public void visitToken(SyntaxToken syntax) {
            String where = syntax.loc().toString();
            printLine("Token " + q(syntax.text()) + " kind=" + syntax.kind() + (where.isEmpty() ? "" : (" " + where)));
        }
    }
}
