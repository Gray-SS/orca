package com.orca.compiler.core;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;
import org.junit.Test;

import com.orca.compiler.core.diagnostics.DiagnosticCode;

/**
 * Tests pour le Type Checker (analyse sémantique et vérification de types).
 * Utilise la nouvelle architecture avec CompilationContext pour l'isolation.
 */
public class TestTypeChecker {

    private static void assertTypeChecks(String source) throws Exception {
        try {
            CompilerTestHelper.parseAndBind(source);
        } catch (CompilerException e) {
            CompilerTestHelper.throwReadableDiagnosticException(e);
            fail("Expected type check to succeed, but it failed with: " + e.getMessage());
        }
    }

    private static void assertTypeChecks(String... sources) throws Exception {
        try {
            CompilerTestHelper.parseAndBind(sources);
        } catch (CompilerException e) {
            CompilerTestHelper.throwReadableDiagnosticException(e);
            fail("Expected type check to succeed, but it failed with: " + e.getMessage());
        }
    }

    private static void assertTypeErrorContainsAll(String source, DiagnosticCode expectedCode) throws Exception {
        try {
            CompilerTestHelper.parseAndBind(source);
            fail("Expected CompilerException with diagnostic code '" + expectedCode + "' but type check succeeded.");
        } catch (CompilerException ex) {
            assertNotNull("Expected a diagnostic on the CompilerException", ex.diagnostic());
            assertEquals(
                    "Expected diagnostic code '" + expectedCode + "' but was: " + ex.diagnostic().code(),
                    expectedCode,
                    ex.diagnostic().code()
            );
        }
    }

    /**
     * Like assertTypeErrorContainsAll but uses strict binding (no implicit
     * main).
     */
    private static void assertTypeErrorStrict(String source, DiagnosticCode expectedCode) throws Exception {
        try {
            CompilerTestHelper.parseAndBindStrict(source);
            fail("Expected CompilerException with diagnostic code '" + expectedCode + "' but type check succeeded.");
        } catch (CompilerException ex) {
            assertNotNull("Expected a diagnostic on the CompilerException", ex.diagnostic());
            assertEquals(
                    "Expected diagnostic code '" + expectedCode + "' but was: " + ex.diagnostic().code(),
                    expectedCode,
                    ex.diagnostic().code()
            );
        }
    }

    @Test
    public void testStructuralTypingAllowsSameShapeAssignment() throws Exception {
        String source = """
            coll A { int x; }
            coll B { int x; }

            def main() {
                A a = A(1);
                B b = a;
            }
        """;

        assertTypeChecks(source);
    }

    @Test
    public void testStructuralTypingRejectsDifferentShapeAssignment() throws Exception {
        String sourceA = """
                coll A { int x; }
                coll B { int y; }

                def main() {
                    A a = A(1);
                    B b = a;
                }
                """;

        String sourceB = """
            coll A { int x; }
            coll B { int x; int y; }
            def main() {
                A a = A(1);
                B b = a;
            }
        """;

        assertTypeErrorContainsAll(sourceA, DiagnosticCode.SEM_TYPE_MISMATCH);
        assertTypeErrorContainsAll(sourceB, DiagnosticCode.SEM_TYPE_MISMATCH);
    }

    @Test
    public void testIntToFloatPromotionOnInitialization() throws Exception {
        String source = """
            float gGlobalVar = 1;
            final float gFinalVar = 1;

            def main() {
                float lLocalVar = 1;
            }
        """;

        assertTypeChecks(source);
    }

    @Test
    public void testMemberAccessTypeChecks() throws Exception {
        String source = """
            coll Point { int x; int y; }
            def main() {
                Point p = Point(1, 2);
                int x = p.x;
                int y = p.y;
            }
        """;

        assertTypeChecks(source);
    }

    @Test
    public void testArrayAndIndexingTypeChecks() throws Exception {
        String source = """
            int[] arr = int[](5);
            int v = arr[0];
        """;

        assertTypeChecks(CompilerTestHelper.wrapInMain(source));
    }

    @Test
    public void testStringIndexingReturnsInt() throws Exception {
        String source = """
            string s = "abc";
            int c = s[0];
        """;

        assertTypeChecks(CompilerTestHelper.wrapInMain(source));
    }

    @Test
    public void testAssignToStringIndexRejected() throws Exception {
        String source = """
            string s = "abc";
            s[0] = 1;
        """;

        assertTypeErrorContainsAll(CompilerTestHelper.wrapInMain(source), DiagnosticCode.SEM_STRING_INDEX_ASSIGNMENT);
    }

    @Test
    public void testOperatorErrorKeywordOnBadBinaryOperator() throws Exception {
        String source = """
            int x = true + 1;
        """;

        assertTypeErrorContainsAll(
                CompilerTestHelper.wrapInMain(source),
                DiagnosticCode.SEM_UNSUPPORTED_BINARY_OPERATOR
        );
    }

    @Test
    public void testNoMatchingOverloadOnBadFunctionCall() throws Exception {
        String source = """
            def int square(int v) { return v * v; }
            def main() {
                int x = square(true);
            }
        """;

        assertTypeErrorContainsAll(
                source,
                DiagnosticCode.SEM_ARGUMENT_TYPE_MISMATCH
        );
    }

    @Test
    public void testArgumentErrorKeywordOnTooFewArguments() throws Exception {
        String source = """
            def int add(int a, int b) { return a + b; }
            def main() {
                int x = add(1);
            }
        """;

        assertTypeErrorContainsAll(source, DiagnosticCode.SEM_ARGUMENT_COUNT_MISMATCH);
    }

    @Test
    public void testArgumentErrorKeywordOnCollectionCtor() throws Exception {
        String source = """
             coll Point { int x; int y; }
             def main() {
                 Point p = Point(true, 2);
             }
        """;

        assertTypeErrorContainsAll(source, DiagnosticCode.SEM_ARGUMENT_TYPE_MISMATCH);
    }

    @Test
    public void testArgumentErrorKeywordOnTooManyArguments() throws Exception {
        String source = """
             def int add(int a, int b) { return a + b; }
             def main() {
                 int x = add(1, 2, 3);
             }
        """;

        assertTypeErrorContainsAll(source, DiagnosticCode.SEM_ARGUMENT_COUNT_MISMATCH);
    }

    @Test
    public void testValueNotCallableOnFunctionCall() throws Exception {
        String source = """
            int x = 1;
            int y = x(1);
        """;

        assertTypeErrorContainsAll(
                CompilerTestHelper.wrapInMain(source),
                DiagnosticCode.SEM_VALUE_NOT_CALLABLE
        );
    }

    @Test
    public void testMissingConditionErrorKeywordOnIfCondition() throws Exception {
        String source = """
            if (1) {
            }
        """;

        assertTypeErrorContainsAll(
                CompilerTestHelper.wrapInMain(source),
                DiagnosticCode.SEM_MISSING_CONDITION
        );
    }

    @Test
    public void testMissingConditionErrorKeywordOnWhileCondition() throws Exception {
        String source = """
            while (1) {
            }
        """;

        assertTypeErrorContainsAll(
                CompilerTestHelper.wrapInMain(source),
                DiagnosticCode.SEM_MISSING_CONDITION
        );
    }

    @Test
    public void testReturnErrorKeywordOnNonVoidMissingValue() throws Exception {
        String source = """
            def int f() {
                return;
            }

            def main() {}
        """;

        assertTypeErrorContainsAll(source, DiagnosticCode.SEM_MISSING_RETURN_VALUE);
    }

    @Test
    public void testScopeErrorKeywordOnUnknownIdentifier() throws Exception {
        String source = """
            def main() {
                int y = x;
            }
        """;

        assertTypeErrorContainsAll(source, DiagnosticCode.SEM_UNDECLARED_IDENTIFIER);
    }

    @Test
    public void testScopeErrorKeywordOnUseBeforeDeclaration() throws Exception {
        String source = """
            def main() {
                int y = x;
                int x = 1;
            }
        """;

        assertTypeErrorContainsAll(source, DiagnosticCode.SEM_UNDECLARED_IDENTIFIER);
    }

    @Test
    public void testCollectionErrorKeywordOnDuplicateCollectionName() throws Exception {
        String source = """
            coll Point { int x; }
            coll Point { int y; }

            def main() {}
        """;

        assertTypeErrorContainsAll(source, DiagnosticCode.SEM_SYMBOL_REDECLARED);
    }

    @Test
    public void testArgumentErrorKeywordOnBadConstructorArguments() throws Exception {
        String source = """
             coll Point { int x; int y; }
             def main() {
                 Point p = Point(true, 2);
             }
        """;

        assertTypeErrorContainsAll(source, DiagnosticCode.SEM_ARGUMENT_TYPE_MISMATCH);
    }

    // @Test
    // public void testAnyTypePatternBindingTypeChecksInIfBlock() throws Exception {
    //     assertTypeChecks(
    //             "coll Vector2 { int x; int y; }\n"
    //             + "def main() {\n"
    //             + "  any a = Vector2(1, 2);\n"
    //             + "  any b = Vector2(3, 4);\n"
    //             + "  if (a is Vector2 vx && b is Vector2 vy) {\n"
    //             + "    int s = vx.x + vy.y;\n"
    //             + "    std::io::println(s);\n"
    //             + "  }\n"
    //             + "  return;\n"
    //             + "}\n"
    //     );
    // }
    @Test
    public void testStaticAndInstanceMethodsResolveSeparatelyFromTopLevelFunctions() throws Exception {
        String source = """
            coll Foo { int a; int b; }
            impl Foo {
                def int add(self) {
                    return self.a + self.b;
                }
                def Foo create(int a, int b) {
                    return Foo(a, b);
                }
            }

            def Foo create(int a, int b) {
                return Foo(a, b);
            }

            def main() {
                Foo foo = Foo(10, 20);
                int result = Foo::create(4, 4).add() + foo.add() + create(1, 2).add();
                std::io::println(result);
            }
        """;

        assertTypeChecks(source);
    }

    @Test
    public void testInstanceMethodCanBeCalledAsStaticMember() throws Exception {
        String source = """
            coll Foo { int a; int b; }
            impl Foo {
                def int add(self) {
                    return self.a + self.b;
                }
            }

            def main() {
                int result = Foo::add(Foo(1, 2));
                std::io::println(result);
            }
        """;

        assertTypeChecks(source);
    }

    @Test
    public void testStaticMethodCannotBeCalledAsInstanceMember() throws Exception {
        String source = """
            coll Foo { int a; int b; }
            impl Foo {
                def Foo create(int a, int b) {
                    return Foo(a, b);
                }
            }

            def main() {
                Foo foo = Foo(1, 2);
                Foo other = foo.create(3, 4);
                return;
            }
        """;

        assertTypeErrorContainsAll(source, DiagnosticCode.SEM_STATIC_MEMBER_ACCESS_ON_INSTANCE);
    }

    @Test
    public void testVariableDeclaredInsideForLoopIsAccessible() throws Exception {
        String source = """
            def main() {
                for (int i; 1 -> 10; i + 1) {
                    std::io::println(i);
                }
            }
        """;

        assertTypeChecks(source);
    }

    @Test
    public void testVariableDeclaredOutsideForLoopIsAccessible() throws Exception {
        String source = """
            def main() {
                int i;
                for (i; 1 -> 10; i + 1) {
                    std::io::println(i);
                }
            }
        """;

        assertTypeChecks(source);
    }

    @Test
    public void testVariableDeclaredInsideForLoopIsNotAccessibleOutside() throws Exception {
        String source = """
            def main() {
                for (int i; 1 -> 10; i + 1) {
                    std::io::println(i);
                }
                std::io::println(i);
            }
        """;

        assertTypeErrorContainsAll(source, DiagnosticCode.SEM_UNDECLARED_IDENTIFIER);
    }

    @Test
    public void testCollectionMethodAccessibleInImpls() throws Exception {
        String source = """
             coll Point { int x; int y; }
             impl Point {
                 def int sum(self) { return self.x + self.y; }
             }
             impl Point {
                 def int doubleSum(self) { return 2 * sum(self); }
             }
             def main() {
                 Point p = Point(1, 2);
                 std::io::println(p.doubleSum());
             }
        """;

        assertTypeChecks(source);
    }

    // =========================================================================
    // Constants
    // =========================================================================
    @Test
    public void testConstantRequiresInitializer() throws Exception {
        String source = """
            final int k;
            def main() { }
        """;

        assertTypeErrorContainsAll(source, DiagnosticCode.SEM_CONSTANT_MISSING_INITIALIZER);
    }

    @Test
    public void testConstantCannotBeAssigned() throws Exception {
        String source = """
            final int k = 1;
            def main() { k = 2; }
        """;

        assertTypeErrorContainsAll(source, DiagnosticCode.SEM_IMMUTABLE_ASSIGNMENT);
    }

    @Test
    public void testConstantRequiresBaseType() throws Exception {
        String source = """
            coll Point { int x; }
            final Point p = Point(1);

            def main() {}
        """;

        // Constants must come before collection declarations; collection types are pre-registered.
        assertTypeErrorContainsAll(source, DiagnosticCode.SEM_CONSTANT_MISSING_BASE_TYPE);
    }

    @Test
    public void testConstantRequiresConstexpr() throws Exception {
        String source = """
            def int compute() { return 1; }
            final int k = compute();

            def main() {}
        """;

        assertTypeErrorContainsAll(source, DiagnosticCode.SEM_CONSTANT_NON_COMPILE_TIME_FOLDABLE_INITIALIZER);
    }

    //WARNING: Currently forbidden by the parser, may need to be decommented if we allow final variables to be parsed in local scopes.
    // @Test
    // public void testConstantCannotBeLocal() throws Exception {
    //     assertTypeErrorContainsAll(
    //             "def main() { final int k = 1; }\n",
    //             DiagnosticCode.SEM_LOCAL_CONSTANT_DECLARATION
    //     );
    // }
    // =========================================================================
    // Collections
    // =========================================================================
    @Test
    public void testCollectionNameMustStartWithCapital() throws Exception {
        String source = """
            coll point { int x; }

            def main() {}
        """;

        assertTypeErrorContainsAll(source, DiagnosticCode.SEM_LOWERCASE_COLLECTION_NAME);
    }

    // @Test
    // public void testRecursiveCollectionDeclaration() throws Exception {
    //     assertTypeErrorContainsAll(
    //             "coll Node { Node child; }\n",
    //             DiagnosticCode.SEM_RECURSIVE_COLLECTION_DECLARATION
    //     );
    // }
    @Test
    public void testDuplicateFieldName() throws Exception {
        String source = """
            coll Pair {
                int x;
                int x;
            }

            def main() {}
        """;

        assertTypeErrorContainsAll(source, DiagnosticCode.SEM_FIELD_REDECLARED);
    }

    @Test
    public void testFieldAccessOnNonCollection() throws Exception {
        assertTypeErrorContainsAll("""
                int x = 5;
                def main() {
                    int y = x.field;
                }
                """,
                DiagnosticCode.SEM_MEMBER_NOT_FOUND
        );
    }

    // =========================================================================
    // Impl blocks
    // =========================================================================
    @Test
    public void testImplTargetOnUndeclaredIndentifier() throws Exception {
        String source = """
             impl NonExistent {
                 def int foo() { return 1; }
             }

             def main() {}
        """;

        assertTypeErrorContainsAll(source, DiagnosticCode.SEM_UNDECLARED_IDENTIFIER);
    }

    @Test
    public void testImplMethodReceiverMustBeFirst() throws Exception {
        String source = """
            coll Foo { int v; }
            impl Foo {
              def int get(int x, self) { return self.v; }
            }

            def main() {}
        """;

        assertTypeErrorContainsAll(source, DiagnosticCode.SEM_INVALID_RECEIVER_PARAMETER);
    }

    @Test
    public void testImplMethodDuplicateDeclaration() throws Exception {
        String source = """
            coll Foo { int v; }
            impl Foo {
              def int get(self) { return self.v; }
              def int get(self) { return self.v; }
            }

            def main() {}
        """;

        assertTypeErrorContainsAll(source, DiagnosticCode.SEM_METHOD_REDECLARED);
    }

    @Test
    public void testMethodsAreDirectlyReferenceableInMemberContext() throws Exception {
        String source = """
             coll Point { int x; int y; }
             impl Point {
                 def int sum(self) { return self.x + self.y; }
             }
             impl Point {
                 def int doubleSum(self) { return 2 * sum(self); }
             }
             def main() {
                 Point p = Point(1, 2);
                 std::io::println(p.doubleSum());
             }
        """;

        assertTypeChecks(source);
    }

    // =========================================================================
    // Operators
    // =========================================================================
    @Test
    public void testUndefinedUnaryOperator() throws Exception {
        String source = """
            def main() {
                int x = -true;
            }
        """;

        assertTypeErrorContainsAll(source, DiagnosticCode.SEM_UNSUPPORTED_UNARY_OPERATOR);
    }

    @Test
    public void testUndefinedIndexOperator() throws Exception {
        String source = """
            def main() {
                int x = 5;
                int y = x[0];
            }
        """;

        assertTypeErrorContainsAll(source, DiagnosticCode.SEM_UNSUPPORTED_INDEX_OPERATOR);
    }

    // =========================================================================
    // Return statements
    // =========================================================================
    @Test
    public void testCannotReturnValueFromVoidFunction() throws Exception {
        String source = """
            def main() {
                return 42;
            }
        """;

        assertTypeErrorContainsAll(source, DiagnosticCode.SEM_RETURN_VALUE_IN_VOID_FUNCTION);
    }

    @Test
    public void testAllPathsMustReturn() throws Exception {
        String source = """
            def int f(int x) {
                if (x > 0) {
                    return x;
                }
            }

            def main() {}
        """;

        assertTypeErrorContainsAll(source, DiagnosticCode.SEM_INCOMPLETE_RETURN_PATHS);
    }

    @Test
    public void testReturnTypeMismatchRejected() throws Exception {
        String source = """
            def int f() {
                return "not an int";
            }

            def main() {}
        """;

        assertTypeErrorContainsAll(source, DiagnosticCode.SEM_TYPE_MISMATCH);
    }

    // =========================================================================
    // Scope / declarations
    // =========================================================================
    @Test
    public void testMissingMainFunctionStrict() throws Exception {
        assertTypeErrorStrict(
                "def int square(int v) { return v * v; }\n",
                DiagnosticCode.SEM_MISSING_MAIN_FUNCTION
        );
    }

    // =========================================================================
    // Definite assignment
    // =========================================================================
    @Test
    public void testUseOfUninitializedVariable() throws Exception {
        // Variable declared without initializer used directly (no any conversion that would hide the ref)
        try {
            CompilerTestHelper.parseAndBind(
                    "def main() { int x; int y = x; }\n"
            );
            fail("Expected compilation error for uninitialized variable use.");
        } catch (CompilerException ex) {
            assertEquals(DiagnosticCode.SEM_UNINITIALIZED_VARIABLE, ex.diagnostic().code());
        }
    }

    @Test
    public void testInitializedVariablePassesDefiniteAssignment() throws Exception {
        assertTypeChecks(
                "def main() { int x = 5; std::io::println(x); }\n"
        );
    }

    @Test
    public void testUninitializedVariableInitializedInBothBranches() throws Exception {
        // Definite assignment: both branches initialize x, so it is definitely assigned after the if.
        assertTypeChecks(
                "def main() {\n"
                + "  int x;\n"
                + "  if (true) { x = 1; } else { x = 2; }\n"
                + "  std::io::println(x);\n"
                + "}\n"
        );
    }

    // =========================================================================
    // Type checks — valid programs
    // =========================================================================
    @Test
    public void testFullProgramTypeChecks() throws Exception {
        String source = """
            coll Point { int x; int y; }

            int globalCounter = 0;
            final int size = 5;

            def int sum(int a, int b) {
                return a + b;
            }

            def main() {
              Point p = Point(1, 2);
              int s = sum(p.x, p.y);
              int[] arr = int[](size);
              arr[0] = s;
              std::io::println(arr[0]);
              return;
            }
        """;

        assertTypeChecks(source);
    }

    @Test
    public void testForLoopTypeChecks() throws Exception {
        String source = """
            def main() {
                int sum = 0;
                int i = 0;
                for (i; 0 -> 10; i+1) {
                    sum = sum + i;
                }
                std::io::println(sum);
            }
        """;

        assertTypeChecks(source);
    }

    @Test
    public void testWhileLoopTypeChecks() throws Exception {
        String source = """
            def main() {
                int n = 10;
                while (n > 0) { n = n - 1; }
                std::io::println(n);
                return;
            }
        """;

        assertTypeChecks(source);
    }

    @Test
    public void testAnyTypeAssignmentFromPrimitive() throws Exception {
        String source = """
            def main() {
                any a = 42;
                any b = "hello";
                any c = true;
                return;
            }
        """;

        assertTypeChecks(source);
    }

    @Test
    public void testFloatArrayTypeChecks() throws Exception {
        String source = """
            def main() {
                float[] arr = float[](3);
                arr[0] = 1.5;
                float v = arr[0];
                std::io::println(v);
                return;
            }
        """;

        assertTypeChecks(source);
    }

    @Test
    public void testParameterRedeclaredInFunction() throws Exception {
        String source = """
            def int foo(int x, int x) {
                return x;
            }

            def main() {}
        """;

        assertTypeErrorContainsAll(source, DiagnosticCode.SEM_SYMBOL_REDECLARED);
    }

    @Test
    public void testParameterRedeclaredInInstanceMethod() throws Exception {
        String source = """
            coll Foo { int x; }

            impl Foo {
                def int foo(self, int x, int x) {
                    return x;
                }
            }

            def main() {}
        """;

        assertTypeErrorContainsAll(source, DiagnosticCode.SEM_SYMBOL_REDECLARED);
    }

    @Test
    public void testParameterRedeclaredInStaticMethod() throws Exception {
        String source = """
            coll Foo { int x; }

            impl Foo {
                def int foo(int x, int x) {
                    return x;
                }
            }

            def main() {}
        """;

        assertTypeErrorContainsAll(source, DiagnosticCode.SEM_SYMBOL_REDECLARED);
    }

    @Test
    public void testReceiverParameterNotAllowedInFunction() throws Exception {
        String source = """
            def int foo(self) {
                return 1;
            }

            def main() {}
        """;

        assertTypeErrorContainsAll(source, DiagnosticCode.SEM_INVALID_RECEIVER_PARAMETER);
    }

    // =========================================================================
    // Type checks — multiple source
    // =========================================================================
    @Test
    public void testImportNamespaceCanBeUsedToAccessTypes() throws Exception {
        String sourceA = """
            package A;

            coll Point { int x; int y; }
        """;

        String sourceB = """
            import A;

            def main() {
                A::Point p = A::Point(1, 2);
                std::io::println(p.x + p.y);
            }
        """;

        assertTypeChecks(sourceA, sourceB);
    }

    @Test
    public void testImportNamespaceCanBeUsedToAccessFunctions() throws Exception {
        String sourceA = """
            package A;

            def int add(int a, int b) { return a + b; }
        """;

        String sourceB = """
            import A;

            def main() {
                int s = A::add(3, 4);
                std::io::println(s);
            }
        """;

        assertTypeChecks(sourceA, sourceB);
    }

    @Test
    public void testImportNamespaceCanBeUsedToAccessImplMethods() throws Exception {
        String sourceA = """
            package A;

            coll Point { int x; int y; }
            impl Point {
                def int sum(self) { return self.x + self.y; }
            }
        """;

        String sourceB = """
            import A;

            def main() {
                A::Point p = A::Point(1, 2);
                std::io::println(p.sum());
            }
        """;

        assertTypeChecks(sourceA, sourceB);
    }

    @Test
    public void testImportFunctionsFromOtherNamespace() throws Exception {
        String sourceA = """
            package A;

            def int add(int a, int b) { return a + b; }
        """;

        String sourceB = """
            import A::add;

            def main() {
                int s = add(3, 4);
                std::io::println(s);
            }
        """;

        assertTypeChecks(sourceA, sourceB);
    }

    @Test
    public void testImportImplMethodsFromOtherNamespace() throws Exception {
        // NOTE: Should this even be allowed for instance methods ?
        String sourceA = """
            package A;

            coll Point { int x; int y; }
            impl Point {
                def int sum(self) { return self.x + self.y; }
            }
        """;

        String sourceB = """
            import A::Point::sum;

            def main() {
                A::Point p = A::Point(1, 2);
                std::io::println(sum(p));
            }
        """;

        assertTypeChecks(sourceA, sourceB);
    }
}
