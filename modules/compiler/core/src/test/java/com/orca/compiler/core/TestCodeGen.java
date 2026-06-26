package com.orca.compiler.core;

import java.util.function.Consumer;

import org.junit.Assert;
import org.junit.Test;

import com.orca.compiler.core.tests.CompilerTestHelper;
import com.orca.compiler.core.tests.SourceBuilder;
import com.orca.compiler.core.tests.TopLevelSourceBuilder.LocalContextSourceBuilder;

public class TestCodeGen {

    private static void assertOutput(String source, String expected) throws Exception {
        var result = CompilerTestHelper.compileAndRun(source);
        switch (result) {
            case CompilationResult.Success<String> success -> {
                String actual = success.value();
                Assert.assertEquals(expected.trim(), actual.trim());
            }
            case CompilationResult.Failure<String> failure -> {
                var diagnostics = failure.diagnostics();
                Assert.fail("Compilation failed with diagnostics: " + diagnostics);
            }
        }
    }

    private static void assertPrintln(String expression, String expected) throws Exception {
        assertOutput(SourceBuilder.create().withMainFunction(b -> b.withPrintln(expression)).build(), expected);
    }

    private static void assertMainOutput(Consumer<LocalContextSourceBuilder> body, String expected) throws Exception {
        assertOutput(SourceBuilder.create().withMainFunction(body).build(), expected);
    }

    // =========================================================================
    // Literals & basic output
    // =========================================================================
    @Test
    public void testPrintlnInt() throws Exception {
        assertPrintln("42", "42");
    }

    @Test
    public void testPrintlnNegativeInt() throws Exception {
        assertPrintln("-7", "-7");
    }

    @Test
    public void testPrintlnBoolTrue() throws Exception {
        assertPrintln("true", "true");
    }

    @Test
    public void testPrintlnBoolFalse() throws Exception {
        assertPrintln("false", "false");
    }

    @Test
    public void testPrintlnString() throws Exception {
        assertPrintln("\"hello world\"", "hello world");
    }

    @Test
    public void testPrintlnFloat() throws Exception {
        assertPrintln("3.5", "3.5");
    }

    @Test
    public void testPrintNoNewline() throws Exception {
        assertMainOutput(b -> {
            b.withPrint("\"foo\"");
            b.withPrintln("\"bar\"");
        }, "foobar");
    }

    @Test
    public void testPrintlnEmpty() throws Exception {
        assertMainOutput(b -> {
            b.withPrintln("\"line1\"");
            b.withPrintln();
            b.withPrintln("\"line3\"");
        }, "line1\n\nline3");
    }

    @Test
    public void testPrintInt() throws Exception {
        assertMainOutput(b -> {
            b.withPrintln("99");
            b.withPrintln();
        }, "99");
    }

    @Test
    public void testPrintFloat() throws Exception {
        assertMainOutput(b -> {
            b.withPrintln("2.5");
            b.withPrintln();
        }, "2.5");
    }

    @Test
    public void testPrintString() throws Exception {
        assertMainOutput(b -> {
            b.withPrintln("\"ok\"");
            b.withPrintln();
        }, "ok");
    }

    // =========================================================================
    // Arithmetic — int
    // =========================================================================
    @Test
    public void testIntAddition() throws Exception {
        assertPrintln("3 + 4", "7");
    }

    @Test
    public void testIntSubtraction() throws Exception {
        assertPrintln("10 - 3", "7");
    }

    @Test
    public void testIntMultiplication() throws Exception {
        assertPrintln("6 * 7", "42");
    }

    @Test
    public void testIntDivision() throws Exception {
        assertPrintln("20 / 4", "5");
    }

    @Test
    public void testIntModulo() throws Exception {
        assertPrintln("17 % 5", "2");
    }

    @Test
    public void testIntUnaryNegation() throws Exception {
        assertMainOutput(b -> {
            b.declareLetVariable("x", "5");
            b.withPrintln("-x");
        }, "-5");
    }

    @Test
    public void testIntArithmeticChained() throws Exception {
        assertPrintln("2 + 3 * 4", "14");
    }

    // =========================================================================
    // Arithmetic — float
    // =========================================================================
    @Test
    public void testFloatAddition() throws Exception {
        assertMainOutput(b -> {
            b.declareLetVariable("a", "1.5", "float");
            b.declareLetVariable("b", "2.5", "float");
            b.withPrintln("a + b");
        }, "4.0");
    }

    @Test
    public void testFloatSubtraction() throws Exception {
        assertMainOutput(b -> {
            b.declareLetVariable("a", "5.0", "float");
            b.declareLetVariable("b", "1.5", "float");
            b.withPrintln("a - b");
        }, "3.5");
    }

    @Test
    public void testFloatMultiplication() throws Exception {
        assertPrintln("2.0 * 3.5", "7.0");
    }

    @Test
    public void testFloatDivision() throws Exception {
        assertPrintln("9.0 / 4.0", "2.25");
    }

    @Test
    public void testIntToFloatPromotion() throws Exception {
        assertMainOutput(b -> {
            b.declareLetVariable("f", "3", "float");
            b.withPrintln("f");
        }, "3.0");
    }

    @Test
    public void testMixedIntFloatArithmetic() throws Exception {
        assertMainOutput(b -> {
            b.declareLetVariable("x", "1");
            b.declareLetVariable("y", "x + 1.5", "float");
            b.withPrintln("y");
        }, "2.5");
    }

    @Test
    public void testFloatArithmetic() throws Exception {
        assertMainOutput(b -> {
            b.declareLetVariable("x", "1", "float");
            b.declareLetVariable("y", "x + 1.5", "float");
            b.withPrintln("y");
        }, "2.5");
    }

    @Test
    public void testFloatUnaryNegation() throws Exception {
        assertMainOutput(b -> {
            b.declareLetVariable("x", "3.5", "float");
            b.withPrintln("-x");
        }, "-3.5");
    }

    // =========================================================================
    // String
    // =========================================================================
    @Test
    public void testStringConcatenation() throws Exception {
        assertPrintln("\"foo\" + \"bar\"", "foobar");
    }

    @Test
    public void testStringLength() throws Exception {
        assertMainOutput(b -> {
            b.declareLetVariable("s", "\"hello\"");
            b.withPrintln("length(s)");
        }, "5");
    }

    @Test
    public void testStringIndexReturnsCharCode() throws Exception {
        assertMainOutput(b -> {
            b.declareLetVariable("s", "\"hello\"");
            b.withPrintln("s[0]");
        }, "104");
    }

    @Test
    public void testStringEquality() throws Exception {
        assertMainOutput(b -> {
            b.declareLetVariable("a", "\"abc\"");
            b.declareLetVariable("b", "\"abc\"");
            b.withPrintln("a == b");
        }, "true");
    }

    @Test
    public void testStringInequality() throws Exception {
        assertMainOutput(b -> {
            b.declareLetVariable("a", "\"abc\"");
            b.declareLetVariable("b", "\"xyz\"");
            b.withPrintln("a == b");
        }, "false");
    }

    // =========================================================================
    // Boolean operators
    // =========================================================================
    @Test
    public void testBooleanAndTrue() throws Exception {
        assertPrintln("true && true", "true");
    }

    @Test
    public void testBooleanAndFalse() throws Exception {
        assertPrintln("true && false", "false");
    }

    @Test
    public void testBooleanOrTrue() throws Exception {
        assertPrintln("false || true", "true");
    }

    @Test
    public void testBooleanOrFalse() throws Exception {
        assertPrintln("false || false", "false");
    }

    @Test
    public void testBooleanNotTrue() throws Exception {
        assertPrintln("not(false)", "true");
    }

    @Test
    public void testBooleanNotFalse() throws Exception {
        assertPrintln("not(true)", "false");
    }

    @Test
    public void testBooleanEquality() throws Exception {
        assertPrintln("true == true", "true");
    }

    @Test
    public void testBooleanInequality() throws Exception {
        assertPrintln("true != false", "true");
    }

    // =========================================================================
    // Comparisons
    // =========================================================================
    @Test
    public void testLessThanTrue() throws Exception {
        assertPrintln("3 < 5", "true");
    }

    @Test
    public void testLessThanFalse() throws Exception {
        assertPrintln("5 < 3", "false");
    }

    @Test
    public void testGreaterThanTrue() throws Exception {
        assertPrintln("5 > 3", "true");
    }

    @Test
    public void testGreaterThanFalse() throws Exception {
        assertPrintln("3 > 5", "false");
    }

    @Test
    public void testLessThanOrEqualTrue() throws Exception {
        assertPrintln("5 <= 5", "true");
    }

    @Test
    public void testLessThanOrEqualFalse() throws Exception {
        assertPrintln("6 <= 5", "false");
    }

    @Test
    public void testGreaterThanOrEqualTrue() throws Exception {
        assertPrintln("5 >= 5", "true");
    }

    @Test
    public void testGreaterThanOrEqualFalse() throws Exception {
        assertPrintln("4 >= 5", "false");
    }

    @Test
    public void testIntEquality() throws Exception {
        assertPrintln("42 == 42", "true");
    }

    @Test
    public void testIntInequality() throws Exception {
        assertPrintln("42 != 43", "true");
    }

    // =========================================================================
    // Variables
    // =========================================================================
    @Test
    public void testLocalVariableDeclarationAndUse() throws Exception {
        assertMainOutput(b -> {
            b.declareLetVariable("x", "10", "int");
            b.declareLetVariable("y", "20", "int");
            b.withPrintln("x + y");
        }, "30");
    }

    @Test
    public void testLocalVariableReassignment() throws Exception {
        assertMainOutput(b -> {
            b.declareLetMutVariable("x", "1", "int");
            b.withAssignment("x", "42");
            b.withPrintln("x");
        }, "42");
    }

    @Test
    public void testGlobalVariable() throws Exception {
        assertOutput(SourceBuilder.create()
                .declareLetVariable("g", "100", "int")
                .withMainFunction(b -> b.withPrintln("g"))
                .build(), "100");
    }

    @Test
    public void testGlobalVariableReassignment() throws Exception {
        assertOutput(SourceBuilder.create()
                .declareLetMutVariable("g", "10", "int")
                .withMainFunction(b -> {
                    b.withAssignment("g", "99");
                    b.withPrintln("g");
                })
                .build(), "99");
    }

    @Test
    public void testConstant() throws Exception {
        assertOutput(SourceBuilder.create()
                .declareConstVariable("c", "42", "int")
                .withMainFunction(b -> b.withPrintln("c"))
                .build(), "42");
    }

    @Test
    public void testMultipleConstants() throws Exception {
        assertOutput(SourceBuilder.create()
                .declareConstVariable("a", "3", "int")
                .declareConstVariable("b", "7", "int")
                .declareConstVariable("c", "a + b", "int")
                .withMainFunction(b -> b.withPrintln("c"))
                .build(), "10");
    }

    @Test
    public void testGlobalInitFromFunction() throws Exception {
        assertOutput(SourceBuilder.create()
                .withFunction("int", "compute", b -> b.withReturn("42"))
                .declareLetVariable("g", "compute()")
                .withMainFunction(b -> b.withPrintln("g"))
                .build(), "42");
    }

    // =========================================================================
    // Control flow — if / else
    // =========================================================================
    @Test
    public void testIfTaken() throws Exception {
        assertMainOutput(b -> b.withIf("true", body -> body.withPrintln("1")), "1");
    }

    @Test
    public void testIfNotTaken() throws Exception {
        assertMainOutput(b -> {
            b.withIf("false", body -> body.withPrintln("1"));
            b.withPrintln("2");
        }, "2");
    }

    @Test
    public void testIfElseTrueBranch() throws Exception {
        assertMainOutput(b -> {
            b.withIf("true", body -> body.withPrintln("1"));
            b.withElse(body -> body.withPrintln("2"));
        }, "1");
    }

    @Test
    public void testIfElseFalseBranch() throws Exception {
        assertMainOutput(b -> {
            b.withIf("false", body -> body.withPrintln("1"));
            b.withElse(body -> body.withPrintln("2"));
        }, "2");
    }

    @Test
    public void testNestedIfElse() throws Exception {
        assertMainOutput(b -> {
            b.declareLetVariable("x", "5", "int");
            b.withIf("x > 10", body -> body.withPrintln("3"));
            b.withElse(body -> {
                body.withIf("x > 3", inner -> inner.withPrintln("2"));
                body.withElse(inner -> inner.withPrintln("1"));
            });
        }, "2");
    }

    @Test
    public void testIfWithComparison() throws Exception {
        assertMainOutput(b -> {
            b.declareLetVariable("x", "7", "int");
            b.withIf("x == 7", body -> body.withPrintln("1"));
            b.withElse(body -> body.withPrintln("0"));
        }, "1");
    }

    // =========================================================================
    // Control flow — while
    // =========================================================================
    @Test
    public void testWhileLoopSum() throws Exception {
        assertMainOutput(b -> {
            b.declareLetMutVariable("i", "0", "int");
            b.declareLetMutVariable("sum", "0", "int");
            b.withWhile("i < 5", wb -> {
                wb.withAssignment("sum", "sum + i");
                wb.withAssignment("i", "i + 1");
            });
            b.withPrintln("sum");
        }, "10");
    }

    @Test
    public void testWhileLoopNotEntered() throws Exception {
        assertMainOutput(b -> {
            b.declareLetMutVariable("i", "10", "int");
            b.declareLetMutVariable("x", "0", "int");
            b.withWhile("i < 5", wb -> wb.withAssignment("x", "1"));
            b.withPrintln("x");
        }, "0");
    }

    @Test
    public void testWhileLoopCountdown() throws Exception {
        assertMainOutput(b -> {
            b.declareLetMutVariable("n", "3", "int");
            b.withWhile("n > 0", wb -> wb.withAssignment("n", "n - 1"));
            b.withPrintln("n");
        }, "0");
    }

    // =========================================================================
    // Control flow — for
    // =========================================================================
    @Test
    public void testForLoopSum() throws Exception {
        assertMainOutput(b -> {
            b.declareLetMutVariable("sum", "0", "int");
            b.withFor("var i := 1", "i < 6", "i++", fb -> fb.withStatement("sum += i"));
            b.withPrintln("sum");
        }, "15");
    }

    @Test
    public void testForLoopEmptyRange() throws Exception {
        assertMainOutput(b -> {
            b.declareLetMutVariable("x", "99", "int");
            b.withFor("var i := 5", "i < 5", "i++", fb -> fb.withAssignment("x", "0"));
            b.withPrintln("x");
        }, "99");
    }

    @Test
    public void testForLoopCounter() throws Exception {
        assertMainOutput(b -> {
            b.declareLetMutVariable("count", "0", "int");
            b.withFor("var i := 0", "i < 10", "i++", fb -> fb.withStatement("count += 1"));
            b.withPrintln("count");
        }, "10");
    }

    // =========================================================================
    // Functions
    // =========================================================================
    @Test
    public void testUserDefinedFunction() throws Exception {
        assertOutput(SourceBuilder.create()
                .withFunction("int", "square", p -> p.withParameter("v", "int"), b -> b.withReturn("v * v"))
                .withMainFunction(b -> b.withPrintln("square(7)"))
                .build(), "49");
    }

    @Test
    public void testFunctionWithMultipleParams() throws Exception {
        assertOutput(SourceBuilder.create()
                .withFunction("int", "add", p -> p.withParameter("a", "int").withParameter("b", "int"), b -> b.withReturn("a + b"))
                .withMainFunction(b -> b.withPrintln("add(3, 4)"))
                .build(), "7");
    }

    @Test
    public void testRecursiveFactorial() throws Exception {
        assertOutput(SourceBuilder.create()
                .withFunction("int", "fact", p -> p.withParameter("n", "int"), b -> {
                    b.withIf("n <= 1", body -> body.withReturn("1"));
                    b.withReturn("n * fact(n - 1)");
                })
                .withMainFunction(b -> b.withPrintln("fact(5)"))
                .build(), "120");
    }

    @Test
    public void testRecursiveFibonacci() throws Exception {
        assertOutput(SourceBuilder.create()
                .withFunction("int", "fib", p -> p.withParameter("n", "int"), b -> {
                    b.withIf("n <= 1", body -> body.withReturn("n"));
                    b.withReturn("fib(n-1) + fib(n-2)");
                })
                .withMainFunction(b -> b.withPrintln("fib(8)"))
                .build(), "21");
    }

    @Test
    public void testVoidFunctionWithReturn() throws Exception {
        assertOutput(SourceBuilder.create()
                .withVoidFunction("printTwice", p -> p.withParameter("x", "int"), b -> {
                    b.withPrintln("x");
                    b.withPrintln("x");
                    b.withReturn();
                })
                .withMainFunction(b -> b.withStatement("printTwice(5)"))
                .build(), "5\n5");
    }

    @Test
    public void testFunctionForwardReference() throws Exception {
        assertOutput(SourceBuilder.create()
                .withMainFunction(b -> b.withPrintln("add(10, 32)"))
                .withFunction("int", "add", p -> p.withParameter("a", "int").withParameter("b", "int"), b -> b.withReturn("a + b"))
                .build(), "42");
    }

    @Test
    public void testParameterShadowsGlobal() throws Exception {
        assertOutput(SourceBuilder.create()
                .declareLetVariable("x", "100", "int")
                .withFunction("int", "get", p -> p.withParameter("x", "int"), b -> b.withReturn("x"))
                .withMainFunction(b -> b.withPrintln("get(5)"))
                .build(), "5");
    }

    // =========================================================================
    // Arrays
    // =========================================================================
    @Test
    public void testIntArrayCreateAndRead() throws Exception {
        assertMainOutput(b -> {
            b.declareLetVariable("arr", "int[](5)");
            b.withAssignment("arr[2]", "42");
            b.withPrintln("arr[2]");
        }, "42");
    }

    @Test
    public void testArrayLength() throws Exception {
        assertMainOutput(b -> {
            b.declareLetVariable("arr", "int[](7)");
            b.withPrintln("length(arr)");
        }, "7");
    }

    @Test
    public void testArraySumElements() throws Exception {
        assertMainOutput(b -> {
            b.declareLetVariable("arr", "int[](3)");
            b.withAssignment("arr[0]", "10");
            b.withAssignment("arr[1]", "20");
            b.withAssignment("arr[2]", "30");
            b.withPrintln("arr[0] + arr[1] + arr[2]");
        }, "60");
    }

    @Test
    public void testArrayPassedByReference() throws Exception {
        assertOutput(SourceBuilder.create()
                .withVoidFunction("fill", p -> p.withParameter("arr", "int[]"), b -> {
                    b.withAssignment("arr[0]", "99");
                    b.withReturn();
                })
                .withMainFunction(b -> {
                    b.declareLetVariable("a", "int[](3)");
                    b.withStatement("fill(a)");
                    b.withPrintln("a[0]");
                })
                .build(), "99");
    }

    @Test
    public void testBoolArray() throws Exception {
        assertMainOutput(b -> {
            b.declareLetVariable("flags", "bool[](3)");
            b.withAssignment("flags[0]", "true");
            b.withAssignment("flags[1]", "false");
            b.withPrintln("flags[0]");
            b.withPrintln("flags[1]");
        }, "true\nfalse");
    }

    @Test
    public void testStringArray() throws Exception {
        assertMainOutput(b -> {
            b.declareLetVariable("words", "string[](2)");
            b.withAssignment("words[0]", "\"hello\"");
            b.withAssignment("words[1]", "\"world\"");
            b.withPrintln("words[0] + \" \" + words[1]");
        }, "hello world");
    }

    // =========================================================================
    // Collections
    // =========================================================================
    @Test
    public void testCollectionCreateAndFieldAccess() throws Exception {
        assertOutput(SourceBuilder.create()
                .withCollection("Point", f -> f.withField("int", "x").withField("int", "y"))
                .withMainFunction(b -> {
                    b.declareLetVariable("p", "Point(3, 4)");
                    b.withPrintln("p.x + p.y");
                })
                .build(), "7");
    }

    @Test
    public void testCollectionFieldAssignment() throws Exception {
        assertOutput(SourceBuilder.create()
                .withCollection("Point", f -> f.withField("int", "x").withField("int", "y"))
                .withMainFunction(b -> {
                    b.declareLetVariable("p", "Point(3, 4)");
                    b.withAssignment("p.x", "10");
                    b.withPrintln("p.x");
                })
                .build(), "10");
    }

    @Test
    public void testCollectionPassedByReference() throws Exception {
        assertOutput(SourceBuilder.create()
                .withCollection("Point", f -> f.withField("int", "x").withField("int", "y"))
                .withVoidFunction("move", p -> {
                    p.withParameter("p", "Point");
                    p.withParameter("dx", "int");
                }, b -> {
                    b.withAssignment("p.x", "p.x + dx");
                    b.withReturn();
                })
                .withMainFunction(b -> {
                    b.declareLetVariable("p", "Point(3, 4)");
                    b.withStatement("move(p, 10)");
                    b.withPrintln("p.x");
                })
                .build(), "13");
    }

    @Test
    public void testCollectionWithStringField() throws Exception {
        assertOutput(SourceBuilder.create()
                .withCollection("Person", f -> {
                    f.withField("string", "name");
                    f.withField("int", "age");
                })
                .withMainFunction(b -> {
                    b.declareLetVariable("p", "Person(\"Alice\", 30)");
                    b.withPrintln("p.name");
                    b.withPrintln("p.age");
                })
                .build(), "Alice\n30");
    }

    @Test
    public void testCollectionWithArrayField() throws Exception {
        assertOutput(SourceBuilder.create()
                .withCollection("Data", f -> {
                    f.withField("int[]", "values");
                    f.withField("int", "n");
                })
                .withMainFunction(b -> {
                    b.declareLetVariable("arr", "int[](3)");
                    b.withAssignment("arr[0]", "5");
                    b.declareLetVariable("d", "Data(arr, 3)");
                    b.withPrintln("d.values[0]");
                })
                .build(), "5");
    }

    @Test
    public void testStructuralTypingConversion() throws Exception {
        assertOutput(SourceBuilder.create()
                .withCollection("A", f -> f.withField("int", "x"))
                .withCollection("B", f -> f.withField("int", "x"))
                .withMainFunction(b -> {
                    b.declareLetVariable("a", "A(7)");
                    b.declareLetVariable("b", "a", "B");
                    b.withPrintln("b.x");
                })
                .build(), "7");
    }

    // =========================================================================
    // Impl methods
    // =========================================================================
    @Test
    public void testImplInstanceMethod() throws Exception {
        assertOutput(SourceBuilder.create()
                .withCollection("Vec", f -> {
                    f.withField("int", "x");
                    f.withField("int", "y");
                })
                .withImpl("Vec", impl -> {
                    impl.withMethod("int", "len2", p -> p.withSelfParameter(),
                            b -> b.withReturn("self.x * self.x + self.y * self.y"));
                })
                .withMainFunction(b -> {
                    b.declareLetVariable("v", "Vec(3, 4)");
                    b.withPrintln("v.len2()");
                })
                .build(), "25");
    }

    @Test
    public void testImplStaticMethod() throws Exception {
        assertOutput(SourceBuilder.create()
                .withCollection("Point", f -> {
                    f.withField("int", "x");
                    f.withField("int", "y");
                })
                .withImpl("Point", impl -> {
                    impl.withMethod("Point", "origin", b -> b.withReturn("Point(0, 0)"));
                })
                .withMainFunction(b -> {
                    b.declareLetVariable("p", "Point::origin()");
                    b.withPrintln("p.x + p.y");
                })
                .build(), "0");
    }

    @Test
    public void testImplMethodChain() throws Exception {
        assertOutput(SourceBuilder.create()
                .withCollection("Foo", f -> {
                    f.withField("int", "a");
                    f.withField("int", "b");
                })
                .withImpl("Foo", impl -> {
                    impl.withMethod("int", "add", p -> p.withSelfParameter(), b -> b.withReturn("self.a + self.b"));
                    impl.withMethod("Foo", "make", p -> {
                        p.withParameter("a", "int");
                        p.withParameter("b", "int");
                    }, b -> b.withReturn("Foo(a, b)"));
                })
                .withMainFunction(b -> {
                    b.declareLetVariable("r", "Foo::make(3, 4).add()");
                    b.withPrintln("r");
                })
                .build(), "7");
    }

    @Test
    public void testImplStaticAndTopLevelFunctionCoexist() throws Exception {
        assertOutput(SourceBuilder.create()
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
                .withMainFunction(b -> {
                    b.declareLetVariable("foo", "Foo(10, 20)");
                    b.declareLetVariable("result", "Foo::create(4, 4).add() + foo.add() + create(1, 2).add()");
                    b.withPrintln("result");
                })
                .build(), "41");
    }

    // =========================================================================
    // Built-in functions
    // =========================================================================
    @Test
    public void testBuiltinFloor() throws Exception {
        assertPrintln("floor(3.9)", "3");
    }

    @Test
    public void testBuiltinFloorNegative() throws Exception {
        assertPrintln("floor(-1.1)", "-2");
    }

    @Test
    public void testBuiltinCeil() throws Exception {
        assertPrintln("ceil(3.1)", "4");
    }

    @Test
    public void testBuiltinCeilExact() throws Exception {
        assertPrintln("ceil(3.0)", "3");
    }

    @Test
    public void testBuiltinStr() throws Exception {
        assertPrintln("str(42)", "42");
    }

    // =========================================================================
    // any type
    // =========================================================================
    @Test
    public void testAnyHoldingInt() throws Exception {
        assertMainOutput(b -> {
            b.declareLetVariable("a", "42", "any");
            b.withPrintln("a");
        }, "42");
    }

    // =========================================================================
    // Compound / integration tests
    // =========================================================================
    @Test
    public void testSumArrayWithForLoop() throws Exception {
        assertOutput(SourceBuilder.create()
                .withFunction("int", "sumArr", p -> {
                    p.withParameter("arr", "int[]");
                    p.withParameter("n", "int");
                }, b -> {
                    b.declareLetMutVariable("s", "0", "int");
                    b.withFor("var i := 0", "i < n", "i++", fb -> fb.withStatement("s += arr[i]"));
                    b.withReturn("s");
                })
                .withMainFunction(b -> {
                    b.declareLetVariable("a", "int[](5)");
                    b.withAssignment("a[0]", "1");
                    b.withAssignment("a[1]", "2");
                    b.withAssignment("a[2]", "3");
                    b.withAssignment("a[3]", "4");
                    b.withAssignment("a[4]", "5");
                    b.withPrintln("sumArr(a, 5)");
                })
                .build(), "15");
    }

    @Test
    public void testStringBuilding() throws Exception {
        assertMainOutput(b -> {
            b.declareLetMutVariable("result", "\"\"", "string");
            b.declareLetMutVariable("i", "0", "int");
            b.withWhile("i < 3", wb -> {
                wb.withAssignment("result", "result + str(i)");
                wb.withAssignment("i", "i + 1");
            });
            b.withPrintln("result");
        }, "012");
    }

    @Test
    public void testConstantsUsedInExpressions() throws Exception {
        assertOutput(SourceBuilder.create()
                .declareConstVariable("base", "10", "int")
                .declareConstVariable("mult", "3", "int")
                .withMainFunction(b -> b.withPrintln("base * mult + base"))
                .build(), "40");
    }

    @Test
    public void testComplexCollectionUsage() throws Exception {
        assertOutput(SourceBuilder.create()
                .withCollection("Point", f -> f.withField("int", "x").withField("int", "y"))
                .withFunction("int", "dist2", p -> {
                    p.withParameter("a", "Point");
                    p.withParameter("b", "Point");
                }, b -> {
                    b.declareLetVariable("dx", "a.x - b.x");
                    b.declareLetVariable("dy", "a.y - b.y");
                    b.withReturn("dx*dx + dy*dy");
                })
                .withMainFunction(b -> {
                    b.declareLetVariable("p1", "Point(0, 0)");
                    b.declareLetVariable("p2", "Point(3, 4)");
                    b.withPrintln("dist2(p1, p2)");
                })
                .build(), "25");
    }

    @Test
    public void testNestedCollections() throws Exception {
        assertOutput(SourceBuilder.create()
                .withCollection("Inner", f -> f.withField("int", "v"))
                .withCollection("Outer", f -> {
                    f.withField("Inner", "inner");
                    f.withField("int", "w");
                })
                .withMainFunction(b -> {
                    b.declareLetVariable("i", "Inner(10)");
                    b.declareLetVariable("o", "Outer(i, 5)");
                    b.withPrintln("o.inner.v + o.w");
                })
                .build(), "15");
    }

    @Test
    public void testLocalVariableShadowing() throws Exception {
        assertOutput(SourceBuilder.create()
                .declareLetVariable("x", "1", "int")
                .withMainFunction(b -> {
                    b.declareLetVariable("x", "2", "int");
                    b.withPrintln("x");
                })
                .build(), "2");
    }

    @Test
    public void testMultiplePrintlnLines() throws Exception {
        assertMainOutput(b -> {
            b.withPrintln("1");
            b.withPrintln("2");
            b.withPrintln("3");
        }, "1\n2\n3");
    }
}
