package com.orca.compiler.core;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import org.junit.Test;

import com.orca.compiler.core.diagnostics.Diagnostic;
import com.orca.compiler.core.diagnostics.DiagnosticBag;
import com.orca.compiler.core.diagnostics.DiagnosticCode;
import com.orca.compiler.core.tests.CompilerTestHelper;
import com.orca.compiler.core.tests.SourceBuilder;

public class TestTypeChecker {

    /**
     * Helper method to build qualified names in tests, e.g. qn("Foo", "Bar") ->
     * "Foo::Bar"
     */
    private static String qn(String... parts) {
        return SourceBuilder.buildQualifiedName(parts);
    }

    private static String invoke(String functionName, String... args) {
        return functionName + "(" + String.join(", ", args) + ")";
    }

    private static void assertChecksForLibrary(String... sources) throws Exception {
        var result = CompilerTestHelper.parseAndBindLibrary(sources);
        assertTrue("Expected type check to succeed, but it failed with diagnostics: " + formatDiagnosticList(result.diagnostics()) + "\nSource:\n " + String.join("\n", sources), result.isSuccess());
    }

    private static void assertErrorForLibrary(String source, DiagnosticCode expectedCode) throws Exception {
        var result = CompilerTestHelper.parseAndBindLibrary(source);
        assertTrue("Expected type check to fail, but it succeeded. Diagnostics: " + formatDiagnosticList(result.diagnostics()) + "\nSource:\n " + source, result.isFailure());

        var diagnostics = result.diagnostics();
        Diagnostic foundDiagnostic = null;
        for (Diagnostic diagnostic : diagnostics) {
            if (diagnostic.code() == expectedCode) {
                foundDiagnostic = diagnostic;
                break;
            }
        }

        assertNotNull("Expected diagnostic with code '" + expectedCode + "' but found: " + formatDiagnosticList(diagnostics) + ".", foundDiagnostic);
    }

    private static void assertErrorForApplication(String source, DiagnosticCode expectedCode) throws Exception {
        var result = CompilerTestHelper.parseAndBindApplication(source);
        assertTrue("Expected type check to fail, but it succeeded. Diagnostics: " + formatDiagnosticList(result.diagnostics()) + "\nSource:\n " + source, result.isFailure());

        var diagnostics = result.diagnostics();
        Diagnostic foundDiagnostic = null;
        for (Diagnostic diagnostic : diagnostics) {
            if (diagnostic.code() == expectedCode) {
                foundDiagnostic = diagnostic;
                break;
            }
        }

        assertNotNull("Expected diagnostic with code '" + expectedCode + "' but found: " + formatDiagnosticList(diagnostics) + ".", foundDiagnostic);
    }

    private static String formatDiagnosticList(DiagnosticBag diagnostics) {
        StringBuilder sb = new StringBuilder();
        for (Diagnostic diagnostic : diagnostics) {
            sb.append(diagnostic.code()).append(": ").append(diagnostic.message()).append("\n");
        }
        return sb.toString();
    }

    // =========================================================================
    // Structural typing
    // =========================================================================
    @Test
    public void testStructuralTypingAllowsSameShapeAssignment() throws Exception {
        String source = SourceBuilder.create()
                .withCollection("A", f -> f.withField("int", "x"))
                .withCollection("B", f -> f.withField("int", "x"))
                .withVoidFunction("test", b -> {
                    b.declareLetVariable("a", invoke("A", "1"));
                    b.declareLetVariable("b", "a", "B");
                })
                .build();

        assertChecksForLibrary(source);
    }

    @Test
    public void testStructuralTypingRejectsDifferentShapeAssignment() throws Exception {
        String sourceA = SourceBuilder.create()
                .withCollection("A", f -> f.withField("int", "x"))
                .withCollection("B", f -> f.withField("int", "y"))
                .withVoidFunction("test", b -> {
                    b.declareLetVariable("a", invoke("A", "1"));
                    b.declareLetVariable("b", "a", "B");
                })
                .build();

        String sourceB = SourceBuilder.create()
                .withCollection("A", f -> f.withField("int", "x"))
                .withCollection("B", f -> {
                    f.withField("int", "x");
                    f.withField("int", "y");
                })
                .withVoidFunction("test", b -> {
                    b.declareLetVariable("a", invoke("A", "1"));
                    b.declareLetVariable("b", "a", "B");
                })
                .build();

        assertErrorForLibrary(sourceA, DiagnosticCode.SEM_TYPE_MISMATCH);
        assertErrorForLibrary(sourceB, DiagnosticCode.SEM_TYPE_MISMATCH);
    }

    @Test
    public void testIntToFloatPromotionOnInitialization() throws Exception {
        String source = SourceBuilder.create()
                .declareLetVariable("a", "1", "float")
                .declareConstVariable("b", "1", "float")
                .withVoidFunction("test", b -> {
                    b.declareLetVariable("c", "1", "float");
                })
                .build();

        assertChecksForLibrary(source);
    }

    @Test
    public void testMemberAccessTypeChecks() throws Exception {
        String source = SourceBuilder.create()
                .withCollection("Point", f -> f.withField("int", "x").withField("int", "y"))
                .withVoidFunction("test", b -> {
                    b.declareLetVariable("p", invoke("Point", "1", "2"), "Point");
                    b.declareLetVariable("x", "p.x", "int");
                    b.declareLetVariable("y", "p.y", "int");
                })
                .build();

        assertChecksForLibrary(source);
    }

    @Test
    public void testArrayAndIndexingTypeChecks() throws Exception {
        String source = SourceBuilder.create()
                .withVoidFunction("test", b -> {
                    b.declareLetVariable("arr", invoke("int[]", "5"));
                    b.declareLetVariable("v", "arr[0]");
                })
                .build();

        assertChecksForLibrary(source);
    }

    @Test
    public void testStringIndexingReturnsInt() throws Exception {
        String source = SourceBuilder.create()
                .withVoidFunction("test", b -> {
                    b.declareLetVariable("s", "\"abc\"");
                    b.declareLetVariable("c", "s[0]");
                })
                .build();

        assertChecksForLibrary(source);
    }

    @Test
    public void testAssignToStringIndexRejected() throws Exception {
        String source = SourceBuilder.create()
                .withVoidFunction("test", b -> {
                    b.declareLetVariable("s", "\"abc\"");
                    b.withAssignment("s[0]", "1");
                })
                .build();

        assertErrorForLibrary(source, DiagnosticCode.SEM_STRING_INDEX_ASSIGNMENT);
    }

    @Test
    public void testOperatorErrorKeywordOnBadBinaryOperator() throws Exception {
        String source = SourceBuilder.create()
                .withVoidFunction("test", b -> {
                    b.declareLetVariable("x", "true + 1");
                })
                .build();

        assertErrorForLibrary(source, DiagnosticCode.SEM_UNSUPPORTED_BINARY_OPERATOR);
    }

    @Test
    public void testNoMatchingOverloadOnBadFunctionCall() throws Exception {
        String source = SourceBuilder.create()
                .withFunction("int", "square", p -> p.withParameter("v", "int"), b -> b.withReturn("v * v"))
                .withVoidFunction("test", b -> {
                    b.declareLetVariable("x", invoke("square", "true"));
                })
                .build();

        assertErrorForLibrary(source, DiagnosticCode.SEM_ARGUMENT_TYPE_MISMATCH);
    }

    @Test
    public void testArgumentErrorKeywordOnTooFewArguments() throws Exception {
        String source = SourceBuilder.create()
                .withFunction("int", "add", p -> p.withParameter("a", "int").withParameter("b", "int"), b -> b.withReturn("a + b"))
                .withVoidFunction("test", b -> {
                    b.declareLetVariable("x", invoke("add", "1"));
                })
                .build();

        assertErrorForLibrary(source, DiagnosticCode.SEM_ARGUMENT_COUNT_MISMATCH);
    }

    @Test
    public void testArgumentErrorKeywordOnCollectionCtor() throws Exception {
        String source = SourceBuilder.create()
                .withCollection("Point", f -> {
                    f.withField("int", "x");
                    f.withField("int", "y");
                })
                .withVoidFunction("test", b -> {
                    b.declareLetVariable("p", invoke("Point", "true", "2"));
                })
                .build();

        assertErrorForLibrary(source, DiagnosticCode.SEM_ARGUMENT_TYPE_MISMATCH);
    }

    @Test
    public void testArgumentErrorKeywordOnTooManyArguments() throws Exception {
        String source = SourceBuilder.create()
                .withFunction("int", "add", p -> {
                    p.withParameter("a", "int");
                    p.withParameter("b", "int");
                }, b -> b.withReturn("a + b"))
                .withVoidFunction("test", b -> {
                    b.declareLetVariable("x", invoke("add", "1", "2", "3"));
                })
                .build();

        assertErrorForLibrary(source, DiagnosticCode.SEM_ARGUMENT_COUNT_MISMATCH);
    }

    @Test
    public void testValueNotCallableOnFunctionCall() throws Exception {
        String source = SourceBuilder.create()
                .withVoidFunction("test", b -> {
                    b.declareLetVariable("x", "1");
                    b.declareLetVariable("y", invoke("x", "1"));
                })
                .build();

        assertErrorForLibrary(source, DiagnosticCode.SEM_VALUE_NOT_CALLABLE);
    }

    @Test
    public void testMissingConditionErrorKeywordOnIfCondition() throws Exception {
        String source = SourceBuilder.create()
                .withVoidFunction("test", b -> {
                    b.withIf("1", body -> {
                    });
                })
                .build();

        assertErrorForLibrary(source, DiagnosticCode.SEM_MISSING_CONDITION);
    }

    @Test
    public void testMissingConditionErrorKeywordOnWhileCondition() throws Exception {
        String source = SourceBuilder.create()
                .withVoidFunction("test", b -> {
                    b.withWhile("1", body -> {
                    });
                })
                .build();

        assertErrorForLibrary(source, DiagnosticCode.SEM_MISSING_CONDITION);
    }

    @Test
    public void testReturnErrorKeywordOnNonVoidMissingValue() throws Exception {
        String source = SourceBuilder.create()
                .withFunction("int", "f", b -> b.withReturn())
                .build();

        assertErrorForLibrary(source, DiagnosticCode.SEM_MISSING_RETURN_VALUE);
    }

    @Test
    public void testScopeErrorKeywordOnUnknownIdentifier() throws Exception {
        String source = SourceBuilder.create()
                .withVoidFunction("test", b -> b.declareLetVariable("y", "x"))
                .build();

        assertErrorForLibrary(source, DiagnosticCode.SEM_UNDECLARED_IDENTIFIER);
    }

    @Test
    public void testScopeErrorKeywordOnUseBeforeDeclaration() throws Exception {
        String source = SourceBuilder.create()
                .withVoidFunction("test", b -> {
                    b.declareLetVariable("y", "x");
                    b.declareLetVariable("x", "1");
                })
                .build();

        assertErrorForLibrary(source, DiagnosticCode.SEM_UNDECLARED_IDENTIFIER);
    }

    @Test
    public void testCollectionErrorKeywordOnDuplicateCollectionName() throws Exception {
        String source = SourceBuilder.create()
                .withCollection("Point", f -> f.withField("int", "x"))
                .withCollection("Point", f -> f.withField("int", "y"))
                .build();

        assertErrorForLibrary(source, DiagnosticCode.SEM_SYMBOL_REDECLARED);
    }

    @Test
    public void testArgumentErrorKeywordOnBadConstructorArguments() throws Exception {
        String source = SourceBuilder.create()
                .withCollection("Point", f -> {
                    f.withField("int", "x");
                    f.withField("int", "y");
                })
                .withVoidFunction("test", b -> {
                    b.declareLetVariable("p", invoke("Point", "true", "2"));
                })
                .build();

        assertErrorForLibrary(source, DiagnosticCode.SEM_ARGUMENT_TYPE_MISMATCH);
    }

    @Test
    public void testStaticAndInstanceMethodsResolveSeparatelyFromTopLevelFunctions() throws Exception {
        String source = SourceBuilder.create()
                .withCollection("Foo", f -> {
                    f.withField("int", "a");
                    f.withField("int", "b");
                })
                .withImpl("Foo", impl -> {
                    impl.withMethod("int", "add", p -> p.withSelfParameter(), b -> b.withReturn("self.a + self.b"));
                    impl.withMethod("Foo", "create", p -> {
                        p.withParameter("a", "int");
                        p.withParameter("b", "int");
                    }, b -> b.withReturn("Foo(a, b)"));
                })
                .withFunction("Foo", "create", p -> {
                    p.withParameter("a", "int");
                    p.withParameter("b", "int");
                }, b -> b.withReturn("Foo(a, b)"))
                .withVoidFunction("test", b -> {
                    b.declareLetVariable("foo", "Foo(10, 20)");
                    b.declareLetVariable("result", qn("Foo", "create") + "(4, 4).add() + foo.add() + create(1, 2).add()");
                })
                .build();

        assertChecksForLibrary(source);
    }

    @Test
    public void testInstanceMethodCanBeCalledAsStaticMember() throws Exception {
        String source = SourceBuilder.create()
                .withCollection("Foo", f -> {
                    f.withField("int", "a");
                    f.withField("int", "b");
                })
                .withImpl("Foo", impl -> {
                    impl.withMethod("int", "add", p -> p.withSelfParameter(), b -> b.withReturn("self.a + self.b"));
                })
                .withVoidFunction("test", b -> {
                    b.declareLetVariable("result", invoke(qn("Foo", "add"), invoke("Foo", "1", "2")));
                })
                .build();

        assertChecksForLibrary(source);
    }

    @Test
    public void testStaticMethodCannotBeCalledAsInstanceMember() throws Exception {
        String source = SourceBuilder.create()
                .withCollection("Foo", f -> {
                    f.withField("int", "a");
                    f.withField("int", "b");
                })
                .withImpl("Foo", impl -> {
                    impl.withMethod("Foo", "create", p -> {
                        p.withParameter("a", "int");
                        p.withParameter("b", "int");
                    }, b -> b.withReturn("Foo(a, b)"));
                })
                .withVoidFunction("test", b -> {
                    b.declareLetVariable("foo", invoke("Foo", "1", "2"));
                    b.declareLetVariable("other", invoke("foo.create", "3", "4"));
                    b.withReturn();
                })
                .build();

        assertErrorForLibrary(source, DiagnosticCode.SEM_STATIC_MEMBER_ACCESS_ON_INSTANCE);
    }

    @Test
    public void testVariableDeclaredInsideForLoopIsAccessible() throws Exception {
        String source = SourceBuilder.create()
                .withVoidFunction("test", b -> {
                    b.withFor("var i := 0", "i < 10", "i++", forBody -> {
                        forBody.declareVarVariable("k", "i * 2");
                    });
                })
                .build();

        assertChecksForLibrary(source);
    }

    @Test
    public void testVariableDeclaredOutsideForLoopIsAccessible() throws Exception {
        String source = SourceBuilder.create()
                .withVoidFunction("test", b -> {
                    b.withFor("var i := 0", "i < 10", "i++", forBody -> {
                        forBody.declareVarVariable("k", "i * 2");
                    });
                })
                .build();

        assertChecksForLibrary(source);
    }

    @Test
    public void testVariableDeclaredInsideForLoopIsNotAccessibleOutside() throws Exception {
        String source = SourceBuilder.create()
                .withVoidFunction("test", b -> {
                    b.withFor("var i := 0", "i < 10", "i++", forBody -> {
                        forBody.declareVarVariable("k", "i * 2");
                    });
                    b.declareVarVariable("l", "k + i");
                })
                .build();

        assertErrorForLibrary(source, DiagnosticCode.SEM_UNDECLARED_IDENTIFIER);
    }

    @Test
    public void testCollectionMethodAccessibleInImpls() throws Exception {
        String source = SourceBuilder.create()
                .withCollection("Point", f -> f.withField("int", "x").withField("int", "y"))
                .withImpl("Point", impl -> {
                    impl.withMethod("int", "sum", p -> p.withSelfParameter(), b -> b.withReturn("self.x + self.y"));
                })
                .withImpl("Point", impl -> {
                    impl.withMethod("int", "doubleSum", p -> p.withSelfParameter(), b -> b.withReturn("2 * sum(self)"));
                })
                .withVoidFunction("test", b -> b.declareLetVariable("p", invoke("Point", "1", "2")))
                .build();

        assertChecksForLibrary(source);
    }

    // =========================================================================
    // Constants
    // =========================================================================
    @Test
    public void testConstantCannotBeAssigned() throws Exception {
        String source = SourceBuilder.create()
                .declareConstVariable("k", "1", "int")
                .withVoidFunction("test", b -> b.withAssignment("k", "2"))
                .build();

        assertErrorForLibrary(source, DiagnosticCode.SEM_IMMUTABLE_ASSIGNMENT);
    }

    @Test
    public void testConstantRequiresBaseType() throws Exception {
        String source = SourceBuilder.create()
                .withCollection("Point", f -> f.withField("int", "x"))
                .declareConstVariable("p", invoke("Point", "1"), "Point")
                .build();

        assertErrorForLibrary(source, DiagnosticCode.SEM_CONSTANT_MISSING_BASE_TYPE);
    }

    @Test
    public void testConstantRequiresConstexpr() throws Exception {
        String source = SourceBuilder.create()
                .withFunction("int", "compute", b -> b.withReturn("1"))
                .declareConstVariable("k", invoke("compute"), "int")
                .build();

        assertErrorForLibrary(source, DiagnosticCode.SEM_CONSTANT_NON_COMPILE_TIME_FOLDABLE_INITIALIZER);
    }

    // =========================================================================
    // Collections
    // =========================================================================
    @Test
    public void testCollectionNameMustStartWithCapital() throws Exception {
        String source = SourceBuilder.create()
                .withCollection("point", f -> f.withField("int", "x"))
                .build();

        assertErrorForLibrary(source, DiagnosticCode.SEM_LOWERCASE_COLLECTION_NAME);
    }

    @Test
    public void testDuplicateFieldName() throws Exception {
        String source = SourceBuilder.create()
                .withCollection("Pair", f -> f.withField("int", "x").withField("int", "x"))
                .build();

        assertErrorForLibrary(source, DiagnosticCode.SEM_FIELD_REDECLARED);
    }

    @Test
    public void testFieldAccessOnNonCollection() throws Exception {
        String source = SourceBuilder.create()
                .withVoidFunction("test", b -> {
                    b.declareLetVariable("x", "5");
                    b.declareLetVariable("y", "x.field");
                })
                .build();

        assertErrorForLibrary(source, DiagnosticCode.SEM_MEMBER_NOT_FOUND);
    }

    // =========================================================================
    // Impl blocks
    // =========================================================================
    @Test
    public void testImplTargetOnUndeclaredIndentifier() throws Exception {
        String source = SourceBuilder.create()
                .withImpl("NonExistent", impl -> {
                    impl.withMethod("int", "foo", b -> b.withReturn("1"));
                })
                .build();

        assertErrorForLibrary(source, DiagnosticCode.SEM_UNDECLARED_IDENTIFIER);
    }

    @Test
    public void testImplMethodReceiverMustBeFirst() throws Exception {
        String source = SourceBuilder.create()
                .withCollection("Foo", f -> f.withField("int", "v"))
                .withImpl("Foo", impl -> {
                    impl.withMethod("int", "get", p -> p.withParameter("x", "int").withSelfParameter(), b -> b.withReturn("self.v"));
                })
                .build();

        assertErrorForLibrary(source, DiagnosticCode.SEM_INVALID_RECEIVER_PARAMETER);
    }

    @Test
    public void testImplMethodDuplicateDeclaration() throws Exception {
        String source = SourceBuilder.create()
                .withCollection("Foo", f -> f.withField("int", "v"))
                .withImpl("Foo", impl -> {
                    impl.withMethod("int", "get", p -> p.withSelfParameter(), b -> b.withReturn("self.v"));
                    impl.withMethod("int", "get", p -> p.withSelfParameter(), b -> b.withReturn("self.v"));
                })
                .build();

        assertErrorForLibrary(source, DiagnosticCode.SEM_METHOD_REDECLARED);
    }

    @Test
    public void testMethodsAreDirectlyReferenceableInMemberContext() throws Exception {
        String source = SourceBuilder.create()
                .withCollection("Point", f -> f.withField("int", "x").withField("int", "y"))
                .withImpl("Point", impl -> {
                    impl.withMethod("int", "sum", p -> p.withSelfParameter(), b -> b.withReturn("self.x + self.y"));
                })
                .withImpl("Point", impl -> {
                    impl.withMethod("int", "doubleSum", p -> p.withSelfParameter(), b -> b.withReturn("2 * sum(self)"));
                })
                .withVoidFunction("test", b -> {
                    b.declareLetVariable("p", invoke("Point", "1", "2"));
                    b.declareLetVariable("s", invoke("p.sum"));
                    b.declareLetVariable("ds", invoke("p.doubleSum"));
                })
                .build();

        assertChecksForLibrary(source);
    }

    // =========================================================================
    // Operators
    // =========================================================================
    @Test
    public void testUndefinedUnaryOperator() throws Exception {
        String source = SourceBuilder.create()
                .withVoidFunction("test", b -> b.declareLetVariable("x", "-true"))
                .build();

        assertErrorForLibrary(source, DiagnosticCode.SEM_UNSUPPORTED_UNARY_OPERATOR);
    }

    @Test
    public void testUndefinedIndexOperator() throws Exception {
        String source = SourceBuilder.create()
                .withVoidFunction("test", b -> {
                    b.declareLetVariable("x", "5");
                    b.declareLetVariable("y", "x[0]");
                })
                .build();

        assertErrorForLibrary(source, DiagnosticCode.SEM_UNSUPPORTED_INDEX_OPERATOR);
    }

    // =========================================================================
    // Return statements
    // =========================================================================
    @Test
    public void testCannotReturnValueFromVoidFunction() throws Exception {
        String source = SourceBuilder.create()
                .withVoidFunction("test", b -> b.withReturn("42"))
                .build();

        assertErrorForLibrary(source, DiagnosticCode.SEM_RETURN_VALUE_IN_VOID_FUNCTION);
    }

    @Test
    public void testAllPathsMustReturn() throws Exception {
        String source = SourceBuilder.create()
                .withFunction("int", "f", p -> p.withParameter("x", "int"), b -> {
                    b.withIf("x > 0", ifBody -> ifBody.withReturn("x"));
                })
                .build();

        assertErrorForLibrary(source, DiagnosticCode.SEM_INCOMPLETE_RETURN_PATHS);
    }

    @Test
    public void testReturnTypeMismatchRejected() throws Exception {
        String source = SourceBuilder.create()
                .withFunction("int", "f", b -> b.withReturn("\"not an int\""))
                .build();

        assertErrorForLibrary(source, DiagnosticCode.SEM_TYPE_MISMATCH);
    }

    // =========================================================================
    // Scope / declarations
    // =========================================================================
    @Test
    public void testMissingMainFunctionStrict() throws Exception {
        String source = SourceBuilder.create()
                .withFunction("int", "square", p -> p.withParameter("v", "int"), b -> b.withReturn("v * v"))
                .build();

        assertErrorForApplication(source, DiagnosticCode.SEM_MISSING_MAIN_FUNCTION);
    }

    // =========================================================================
    // Definite assignment
    // =========================================================================
    @Test
    public void testInitializedVariablePassesDefiniteAssignment() throws Exception {
        String source = SourceBuilder.create()
                .withVoidFunction("test", b -> {
                    b.declareLetVariable("x", "5");
                    b.declareLetVariable("y", "x");
                })
                .build();

        assertChecksForLibrary(source);
    }

    @Test
    public void testUninitializedVariableInitializedInBothBranches() throws Exception {
        String source = SourceBuilder.create()
                .withVoidFunction("test", b -> {
                    b.declareVarVariable("x", "true", "bool");
                    b.withIf("true", ifBody -> ifBody.withAssignment("x", "true"));
                    b.withElse(elseBody -> elseBody.withAssignment("x", "false"));
                    b.declareLetVariable("y", "x", "bool");
                })
                .build();

        assertChecksForLibrary(source);
    }

    // =========================================================================
    // Type checks — valid programs
    // =========================================================================
    @Test
    public void testFullProgramTypeChecks() throws Exception {
        String source = SourceBuilder.create()
                .withCollection("Point", f -> f.withField("int", "x").withField("int", "y"))
                .declareVarVariable("globalCounter", "0", "int")
                .declareConstVariable("size", "5", "int")
                .withFunction("int", "sum", p -> p.withParameter("a", "int").withParameter("b", "int"), b -> b.withReturn("a + b"))
                .withVoidFunction("test", b -> {
                    b.declareLetVariable("p", invoke("Point", "1", "2"));
                    b.declareLetVariable("s", invoke("sum", "p.x", "p.y"));
                    b.declareLetVariable("arr", invoke("int[]", "size"));
                    b.withAssignment("arr[0]", "s");
                    b.withReturn();
                })
                .build();

        assertChecksForLibrary(source);
    }

    @Test
    public void testForLoopTypeChecks() throws Exception {
        String source = SourceBuilder.create()
                .withVoidFunction("test", b -> {
                    b.declareVarVariable("sum", "0", "int");
                    b.withFor("var i := 0", "i < 10", "i++", forBody -> {
                        forBody.withStatement("sum += i");
                    });
                })
                .build();

        assertChecksForLibrary(source);
    }

    @Test
    public void testWhileLoopTypeChecks() throws Exception {
        String source = SourceBuilder.create()
                .withVoidFunction("test", b -> {
                    b.declareVarVariable("n", "10");
                    b.withWhile("n > 0", whileBody -> whileBody.withAssignment("n", "n - 1"));
                    b.withReturn();
                })
                .build();

        assertChecksForLibrary(source);
    }

    @Test
    public void testAnyTypeAssignmentFromPrimitive() throws Exception {
        String source = SourceBuilder.create()
                .withVoidFunction("foo", b -> {
                    b.declareLetVariable("a", "42", "any");
                    b.declareLetVariable("b", "\"hello\"", "any");
                    b.declareLetVariable("c", "true", "any");
                })
                .build();

        assertChecksForLibrary(source);
    }

    @Test
    public void testFloatArrayTypeChecks() throws Exception {
        String source = SourceBuilder.create()
                .withVoidFunction("foo", b -> {
                    b.declareLetVariable("arr", invoke("float[]", "3"));
                    b.withAssignment("arr[0]", "1.5");
                    b.declareLetVariable("v", "arr[0]");
                    b.withReturn();
                })
                .build();

        assertChecksForLibrary(source);
    }

    @Test
    public void testParameterRedeclaredInFunction() throws Exception {
        String source = SourceBuilder.create()
                .withFunction("int", "foo", p -> p.withParameter("x", "int").withParameter("x", "int"), b -> b.withReturn("x"))
                .build();

        assertErrorForLibrary(source, DiagnosticCode.SEM_SYMBOL_REDECLARED);
    }

    @Test
    public void testParameterRedeclaredInInstanceMethod() throws Exception {
        String source = SourceBuilder.create()
                .withCollection("Foo", f -> f.withField("int", "x"))
                .withImpl("Foo", impl -> {
                    impl.withMethod("int", "foo", p -> p.withSelfParameter().withParameter("x", "int").withParameter("x", "int"), b -> b.withReturn("x"));
                })
                .build();

        assertErrorForLibrary(source, DiagnosticCode.SEM_SYMBOL_REDECLARED);
    }

    @Test
    public void testParameterRedeclaredInStaticMethod() throws Exception {
        String source = SourceBuilder.create()
                .withCollection("Foo", f -> f.withField("int", "x"))
                .withImpl("Foo", impl -> {
                    impl.withMethod("int", "foo", p -> p.withParameter("x", "int").withParameter("x", "int"), b -> b.withReturn("x"));
                })
                .build();

        assertErrorForLibrary(source, DiagnosticCode.SEM_SYMBOL_REDECLARED);
    }

    @Test
    public void testReceiverParameterNotAllowedInFunction() throws Exception {
        String source = SourceBuilder.create()
                .withFunction("int", "foo", p -> p.withSelfParameter(), b -> b.withReturn("1"))
                .build();

        assertErrorForLibrary(source, DiagnosticCode.SEM_INVALID_RECEIVER_PARAMETER);
    }

    // =========================================================================
    // Type checks — multiple source
    // =========================================================================
    @Test
    public void testImportNamespaceCanBeUsedToAccessTypes() throws Exception {
        String sourceA = SourceBuilder.create("A")
                .withCollection("Point", f -> {
                    f.withField("int", "x");
                    f.withField("int", "y");
                })
                .build();

        String sourceB = SourceBuilder.create("B")
                .withImport("A")
                .withVoidFunction("test", b -> {
                    b.declareLetVariable("p", invoke(qn("A", "Point"), "1", "2"));
                })
                .build();

        TestTypeChecker.assertChecksForLibrary(sourceA, sourceB);
    }

    @Test
    public void testImportNamespaceCanBeUsedToAccessFunctions() throws Exception {
        String sourceA = SourceBuilder.create("A")
                .withFunction("int", "add", p -> {
                    p.withParameter("a", "int");
                    p.withParameter("b", "int");
                }, b -> b.withReturn("a + b"))
                .build();

        String sourceB = SourceBuilder.create("B")
                .withImport("A")
                .withVoidFunction("test", b -> b.declareLetVariable("s", invoke(qn("A", "add"), "3", "4")))
                .build();

        TestTypeChecker.assertChecksForLibrary(sourceA, sourceB);
    }

    @Test
    public void testImportNamespaceCanBeUsedToAccessImplMethods() throws Exception {
        String sourceA = SourceBuilder.create("A")
                .withCollection("Point", f -> {
                    f.withField("int", "x");
                    f.withField("int", "y");
                })
                .withImpl("Point", impl -> {
                    impl.withMethod("int", "sum", p -> p.withSelfParameter(), b -> b.withReturn("self.x + self.y"));
                })
                .build();

        String sourceB = SourceBuilder.create("B")
                .withImport("A")
                .withVoidFunction("test", b -> b.declareLetVariable("p", invoke(qn("A", "Point"), "1", "2")))
                .build();

        TestTypeChecker.assertChecksForLibrary(sourceA, sourceB);
    }

    @Test
    public void testImportFunctionsFromOtherNamespace() throws Exception {
        String sourceA = SourceBuilder.create("A")
                .withFunction("int", "add", p -> p.withParameter("a", "int").withParameter("b", "int"), b -> b.withReturn("a + b"))
                .build();

        String sourceB = SourceBuilder.create("B")
                .withImport(qn("A", "add"))
                .withVoidFunction("test", b -> b.declareLetVariable("s", invoke(qn("A", "add"), "3", "4")))
                .build();

        TestTypeChecker.assertChecksForLibrary(sourceA, sourceB);
    }

    @Test
    public void testImportImplMethodsFromOtherNamespace() throws Exception {
        String sourceA = SourceBuilder.create("A")
                .withCollection("Point", f -> {
                    f.withField("int", "x");
                    f.withField("int", "y");
                })
                .withImpl("Point", impl -> {
                    impl.withMethod("int", "sum", p -> p.withSelfParameter(), b -> b.withReturn("self.x + self.y"));
                })
                .build();

        String sourceB = SourceBuilder.create("B")
                .withImport(qn("A", "Point", "sum"))
                .withVoidFunction("test", b -> b.declareLetVariable("p", invoke(qn("A", "Point"), "1", "2")))
                .build();

        TestTypeChecker.assertChecksForLibrary(sourceA, sourceB);
    }
}
