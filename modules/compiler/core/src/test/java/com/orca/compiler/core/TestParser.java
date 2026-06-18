
// import org.junit.Test;

// import com.orca.compiler.core.Test.CompilerTestHelper;
// import com.orca.compiler.core.Diagnostics.DiagnosticCode;
// import com.orca.compiler.core.Lexer.Lexer;
// import com.orca.compiler.core.Text.*;
// import com.orca.compiler.core.Syntax.CompilationUnit;
// import com.orca.compiler.core.Syntax.ExpressionSyntax;
// import com.orca.compiler.core.Syntax.Parser;
// import com.orca.compiler.core.Syntax.StatementSyntax;
// import com.orca.compiler.core.Syntax.Declarations.*;
// import com.orca.compiler.core.Syntax.Expressions.*;
// import com.orca.compiler.core.Syntax.Nodes.*;
// import com.orca.compiler.core.Syntax.Statements.*;
// import com.orca.compiler.core.Syntax.Types.*;

// /**
//  * Tests pour le Parser (analyse syntaxique). Utilise la nouvelle architecture
//  * avec CompilationContext pour l'isolation.
//  */
// public class TestParser {

//     private CompilationUnit parseUnit(String source) throws Exception {
//         return CompilerTestHelper.parseUnit(source);
//     }

//     private List<GlobalStatementSyntax> parseGlobalStatements(String source) throws Exception {
//         CompilationUnit unit = parseUnit(source);
//         return unit.members().stream()
//                 .filter(member -> member instanceof GlobalStatementSyntax)
//                 .map(member -> (GlobalStatementSyntax) member)
//                 .toList();
//     }

//     private void assertDiagnosticsContains(String source, DiagnosticCode expectedCode) {
//         var parser = new Parser(new Lexer(new StringSource(source)));
//         parser.parseCompilationUnit();

//         var diagnostics = parser.diagnostics();
//         assertTrue("Expected parser to report errors", diagnostics.hasDiagnostic(expectedCode));
//     }

//     private MemberSyntax parseSingleMember(String source) throws Exception {
//         CompilationUnit unit = parseUnit(source);
//         assertEquals("Expected exactly one member", 1, unit.members().size());
//         return unit.members().get(0);
//     }

//     private GlobalStatementSyntax parseSingleGlobalStatement(String source) throws Exception {
//         MemberSyntax member = parseSingleMember(source);
//         assertTrue(member instanceof GlobalStatementSyntax, "Expected a global statement");
//         return (GlobalStatementSyntax) member;
//     }

//     private StatementSyntax parseSingleStatement(String source) throws Exception {
//         GlobalStatementSyntax globalStmt = parseSingleGlobalStatement(source);
//         return globalStmt.statement();
//     }

//     private ExpressionSyntax parseSingleExpression(String expressionSource) throws Exception {
//         var statement = parseSingleStatement(expressionSource + ";");
//         assertTrue(statement instanceof ExpressionStmt, "Expected an expression statement");

//         return ((ExpressionStmt) statement).expression();
//     }

//     @Test
//     public void testConstantDeclarationParses() throws Exception {
//         MemberSyntax memberSyntax = parseSingleMember("final INT i = 3;");
//         assertTrue(memberSyntax instanceof ConstantVariableDeclarationSyntax);

//         var decl = (ConstantVariableDeclarationSyntax) memberSyntax;
//         assertEquals("i", decl.identifier().text());
//         assertTrue(decl.type() instanceof IdentifierTypeSyntax);
//         assertEquals("INT", ((IdentifierTypeSyntax) decl.type()).identifier().text());

//         ExpressionSyntax init = decl.initializer();
//         assertNotNull(init);
//         assertTrue(init instanceof LiteralExpr);
//         assertEquals(3, ((LiteralExpr) init).value);
//     }

//     @Test
//     public void testVariableDeclarationWithoutInitializerParses() throws Exception {
//         var stmt = parseSingleStatement("INT x;");
//         assertTrue(stmt instanceof VariableDeclarationStatement);

//         var decl = (VariableDeclarationStatement) stmt;
//         assertEquals("x", decl.identifier().text());
//         assertNull(decl.initializer());
//     }

//     @Test
//     public void testArrayTypeVariableDeclarationParses() throws Exception {
//         var stmt = parseSingleStatement("INT[] xs;");
//         assertTrue(stmt instanceof VariableDeclarationStatement);

//         var decl = (VariableDeclarationStatement) stmt;
//         assertTrue(decl.type() instanceof ArrayTypeSyntax);
//         TypeAsserts.assertSimpleType(((ArrayTypeSyntax) decl.type()).elementType(), "INT");
//     }

//     @Test
//     public void testCollectionDeclarationParses() throws Exception {
//         MemberSyntax member = parseSingleMember("coll Point { INT x; INT y; }");
//         assertTrue(member instanceof CollectionDeclarationSyntax);

//         var decl = (CollectionDeclarationSyntax) member;
//         assertEquals("Point", decl.identifier().text());
//         assertEquals(2, decl.fields().size());
//     }

//     @Test
//     public void testFunctionDeclarationWithReturnTypeParses() throws Exception {
//         MemberSyntax member = parseSingleMember("def INT square(INT v) { return v*v; }");
//         assertTrue(member instanceof MethodDeclarationSyntax);

//         var decl = (MethodDeclarationSyntax) member;
//         assertEquals("square", decl.identifier().text());
//         TypeAsserts.assertSimpleType(decl.returnType(), "INT");
//         assertEquals(1, decl.parameters().size());
//         assertEquals("v", decl.parameters().get(0).identifier().text());
//         assertEquals(1, decl.body().statements().size());
//         assertTrue(decl.body().statements().get(0) instanceof ReturnStmt);
//     }

//     @Test
//     public void testVoidFunctionDeclarationParses() throws Exception {
//         MemberSyntax member = parseSingleMember("def main() { return; }");
//         assertTrue(member instanceof MethodDeclarationSyntax);

//         var decl = (MethodDeclarationSyntax) member;
//         assertEquals("main", decl.identifier().text());
//         assertNull(decl.returnType());
//         assertEquals(0, decl.parameters().size());

//         ReturnStmt ret = (ReturnStmt) decl.body().statements().get(0);
//         assertNull(ret.expression());
//     }

//     @Test
//     public void testIfElseIfElseParses() throws Exception {
//         StatementSyntax stmt = parseSingleStatement("if (true) { } else if (false) { } else { }");
//         assertTrue(stmt instanceof IfStmt);

//         IfStmt ifStmt = (IfStmt) stmt;
//         assertNotNull(ifStmt.ifClause);
//         assertEquals(1, ifStmt.elseIfClauses.elements.size());
//         assertNotNull(ifStmt.elseClause);

//         IfClauseSyntax elseIf = ifStmt.elseIfClauses.elements.get(0);
//         assertTrue(elseIf.condition instanceof LiteralExpr);
//     }

//     @Test
//     public void testWhileParses() throws Exception {
//         StatementSyntax stmt = parseSingleStatement("while (1 == 1) { INT x = 0; }");
//         assertTrue(stmt instanceof WhileStmt);

//         WhileStmt whileStmt = (WhileStmt) stmt;
//         assertTrue(whileStmt.condition() instanceof BinaryExpr);
//         assertTrue(whileStmt.body() instanceof BlockStmt);
//         assertEquals(1, whileStmt.body().statements().size());
//     }

//     @Test
//     public void testTypeTestExpressionParses() throws Exception {
//         ExpressionSyntax expr = parseSingleExpression("x is Vector2");
//         assertTrue(expr instanceof TypeTestExpr);

//         TypeTestExpr tt = (TypeTestExpr) expr;
//         assertNull(tt.identifier());
//         TypeAsserts.assertSimpleType(tt.type(), "Vector2");
//     }

//     @Test
//     public void testTypeTestPatternBindingParses() throws Exception {
//         ExpressionSyntax expr = parseSingleExpression("x is Vector2 vx");
//         assertTrue(expr instanceof TypeTestExpr);

//         TypeTestExpr tt = (TypeTestExpr) expr;
//         assertNotNull(tt.identifier());
//         assertEquals("vx", tt.identifier().text());
//         TypeAsserts.assertSimpleType(tt.type(), "Vector2");
//     }

//     @Test
//     public void testTypeTestWithLogicalAndParses() throws Exception {
//         StatementSyntax stmt = parseSingleStatement("if (x is Vector2 vx && y is Vector2 vy) { }");
//         assertTrue(stmt instanceof IfStmt);

//         IfStmt ifStmt = (IfStmt) stmt;
//         assertTrue(ifStmt.ifClause().condition() instanceof BinaryExpr);

//         BinaryExpr andExpr = (BinaryExpr) ifStmt.ifClause().condition();
//         assertEquals(BinaryOperator.LogicalAnd, andExpr.operator());
//         assertTrue(andExpr.left() instanceof TypeTestExpr);
//         assertTrue(andExpr.right() instanceof TypeTestExpr);
//     }

//     @Test
//     public void testForParsesWithAllParts() throws Exception {
//         StatementSyntax stmt = parseSingleStatement("for (INT i; 1 -> 10; i + 1) { } ");
//         assertTrue(stmt instanceof ForStmt);

//         ForStmt forStmt = (ForStmt) stmt;
//         TypeAsserts.assertSimpleType(forStmt.loopVarType(), "INT");
//         assertEquals("i", forStmt.loopVarSymbol().text());

//         assertNotNull(forStmt.range());
//         assertNotNull(forStmt.step());
//         assertTrue(forStmt.range() instanceof RangeSyntax);
//         assertTrue(forStmt.step() instanceof BinaryExpr);
//     }

//     @Test
//     public void testForParsesWithPreviouslyDeclaredVariable() throws Exception {
//         // Variable declared earlier, then referenced in the for header.
//         CompilationUnit unit = parseUnit("INT i; for (i; 1 -> 10; i + 1) { }");
//         assertEquals("Unexpected number of global statements", 2, unit.members().size());

//         assertTrue(unit.statements.get(0) instanceof VariableDeclarationStatement);
//         assertTrue(unit.statements.get(1) instanceof ForStmt);

//         ForStmt forStmt = (ForStmt) unit.statements.get(1);
//         assertNull("Loop variable type should be implicit when using an existing variable", forStmt.loopVarType);
//         assertEquals("i", forStmt.loopVarSymbol.text());

//         assertNotNull(forStmt.range);
//         assertNotNull(forStmt.step);
//         assertTrue(forStmt.range instanceof RangeSyntax);
//         assertTrue(forStmt.step instanceof BinaryExpr);
//     }

//     @Test
//     public void testForParsesWithMissingParts() throws Exception {
//         assertDiagnosticsContains("for (; ; ) { return; }", DiagnosticCode.PARSER_UNEXPECTED_TOKEN);
//     }

//     @Test
//     public void testExpressionPrecedenceMultiplicationBeforeAddition() throws Exception {
//         ExpressionSyntax expr = parseSingleExpression("1 + 2 * 3");
//         assertTrue(expr instanceof BinaryExpr);

//         BinaryExpr add = (BinaryExpr) expr;
//         assertEquals(BinaryOperator.NumericAddition, add.operator);
//         assertTrue(add.left instanceof LiteralExpr);
//         assertTrue(add.right instanceof BinaryExpr);

//         BinaryExpr mul = (BinaryExpr) add.right;
//         assertEquals(BinaryOperator.NumericMultiplication, mul.operator);
//     }

//     @Test
//     public void testExpressionLeftAssociativity() throws Exception {
//         ExpressionSyntax expr = parseSingleExpression("10 - 3 - 2");
//         assertTrue(expr instanceof BinaryExpr);

//         BinaryExpr outer = (BinaryExpr) expr;
//         assertEquals(BinaryOperator.NumericSubtraction, outer.operator);
//         assertTrue(outer.left instanceof BinaryExpr);
//         assertTrue(outer.right instanceof LiteralExpr);

//         BinaryExpr inner = (BinaryExpr) outer.left;
//         assertEquals(BinaryOperator.NumericSubtraction, inner.operator);
//     }

//     @Test
//     public void testUnaryBindsTighterThanBinary() throws Exception {
//         ExpressionSyntax expr = parseSingleExpression("-1 * 2");
//         assertTrue(expr instanceof BinaryExpr);

//         BinaryExpr mul = (BinaryExpr) expr;
//         assertEquals(BinaryOperator.NumericMultiplication, mul.operator);
//         assertTrue(mul.left instanceof UnaryExpr);
//         assertEquals(UnaryOperator.NumericNegation, ((UnaryExpr) mul.left).operator);
//     }

//     @Test
//     public void testPostfixChainingArrayThenMember() throws Exception {
//         ExpressionSyntax expr = parseSingleExpression("p[0].x");
//         assertTrue(expr instanceof MemberAccessExpr);

//         MemberAccessExpr member = (MemberAccessExpr) expr;
//         assertEquals("x", member.memberName);
//         assertTrue(member.instanceExpr instanceof ArrayAccessExpr);

//         ArrayAccessExpr arrayAccess = (ArrayAccessExpr) member.instanceExpr;
//         assertTrue(arrayAccess.arrayExpr instanceof IdentifierExpr);
//         assertEquals("p", ((IdentifierExpr) arrayAccess.arrayExpr).identifier.text());
//     }

//     @Test
//     public void testArrayLiteralParses() throws Exception {
//         StatementSyntax stmt = parseSingleMember("INT[] xs = INT ARRAY [5];");
//         assertTrue(stmt instanceof VariableDeclarationStatement);

//         ExpressionSyntax init = ((VariableDeclarationStatement) stmt).initializer;
//         assertTrue(init instanceof ArrayLiteralExpression);

//         ArrayLiteralExpression arr = (ArrayLiteralExpression) init;
//         TypeAsserts.assertSimpleType(arr.elementType, "INT");
//         assertTrue(arr.length instanceof LiteralExpr);
//     }

//     @Test
//     public void testCollectionLiteralParses() throws Exception {
//         StatementSyntax stmt = parseSingleMember("Point p = Point(3, 7);");
//         assertTrue(stmt instanceof VariableDeclarationStatement);

//         ExpressionSyntax init = ((VariableDeclarationStatement) stmt).initializer;
//         assertTrue(init instanceof CollectionLiteralExpression);

//         CollectionLiteralExpression lit = (CollectionLiteralExpression) init;
//         assertEquals("Point", lit.identifier.text());
//         assertEquals(2, lit.arguments.size());
//     }

//     @Test
//     public void testFunctionCallParses() throws Exception {
//         ExpressionSyntax expr = parseSingleExpression("foo(1, 2, 3)");
//         assertTrue(expr instanceof FunctionCallExpr);

//         FunctionCallExpr call = (FunctionCallExpr) expr;
//         assertEquals("foo", call.callee.text());
//         assertTrue(call.callee instanceof IdentifierExpr);
//         assertEquals(3, call.arguments.size());
//     }

//     @Test
//     public void testBuiltinNotParsesAsFunctionCall() throws Exception {
//         ExpressionSyntax expr = parseSingleExpression("not(true)");
//         assertTrue(expr instanceof FunctionCallExpr);

//         FunctionCallExpr call = (FunctionCallExpr) expr;
//         assertEquals("not", call.callee.text());
//         assertEquals(1, call.arguments.size());
//         assertTrue(call.arguments.get(0) instanceof LiteralExpr);
//         assertEquals(true, ((LiteralExpr) call.arguments.get(0)).value);
//     }

//     @Test
//     public void testMissingSemicolonFails() {
//         assertDiagnosticsContains("INT x = 3", DiagnosticCode.PARSER_UNEXPECTED_TOKEN);
//     }

//     @Test
//     public void testWhileMissingParenthesesFails() {
//         assertDiagnosticsContains("while true { }", DiagnosticCode.PARSER_UNEXPECTED_TOKEN);
//     }

//     @Test
//     public void testUnterminatedBlockFails() {
//         // The parser keeps parsing statements until it sees '}', so EOF typically surfaces as
//         // an unexpected token while trying to parse the next statement.
//         assertDiagnosticsContains("{ INT x;", DiagnosticCode.PARSER_UNEXPECTED_TOKEN);
//     }

//     @Test
//     public void testTypeIdentifierWithoutCtorParensFails() {
//         // With statement-level lookahead disambiguation, a leading TypeIdentifier is not always
//         // a variable declaration. In expressions, TypeIdentifier can only start a collection literal
//         // `TypeIdentifier(...)` or an array literal `TypeIdentifier ARRAY [expr]`.
//         // `Point[0];` is therefore invalid and fails when the parser expects '(' after `Point`.
//         assertDiagnosticsContains("Point[0];", DiagnosticCode.PARSER_UNEXPECTED_TOKEN);
//     }

//     @Test
//     public void testTypeIdentifierCtorCallAsExpressionStatementParses() throws Exception {
//         StatementSyntax stmt = parseSingleMember("Point(1, 2);");
//         assertTrue(stmt instanceof ExpressionStmt);

//         ExpressionSyntax expr = ((ExpressionStmt) stmt).expression;
//         assertTrue(expr instanceof CollectionLiteralExpression);

//         CollectionLiteralExpression lit = (CollectionLiteralExpression) expr;
//         assertEquals("Point", lit.identifier.text());
//         assertEquals(2, lit.arguments.size());
//     }

//     private static final class TypeAsserts {

//         private static void assertSimpleType(compiler.Syntax.Types.TypeSyntax type, String expectedLexeme) {
//             assertNotNull(type);
//             assertTrue("Expected SimpleTypeSyntax, got: " + type.getClass().getSimpleName(), type instanceof IdentifierTypeSyntax);
//             assertEquals(expectedLexeme, ((IdentifierTypeSyntax) type).identifier.text());
//         }
//     }
// }
