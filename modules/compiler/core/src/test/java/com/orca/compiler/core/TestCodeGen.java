package com.orca.compiler.core;

import org.junit.Assert;
import org.junit.Test;

/**
 * End-to-end code generation tests. Each test compiles a source snippet to JVM
 * bytecode, executes it, and verifies stdout.
 */
public class TestCodeGen {

    private static void assertOutput(String source, String expected) throws Exception {
        String actual = CompilerTestHelper.compileAndRun(source);
        Assert.assertEquals(expected.trim(), actual.trim());
    }

    // =========================================================================
    // Literals & basic output
    // =========================================================================
    @Test
    public void testPrintlnInt() throws Exception {
        assertOutput("def main() { std::io::println(42); }", "42");
    }

    @Test
    public void testPrintlnNegativeInt() throws Exception {
        assertOutput("def main() { std::io::println(-7); }", "-7");
    }

    @Test
    public void testPrintlnBoolTrue() throws Exception {
        assertOutput("def main() { std::io::println(true); }", "true");
    }

    @Test
    public void testPrintlnBoolFalse() throws Exception {
        assertOutput("def main() { std::io::println(false); }", "false");
    }

    @Test
    public void testPrintlnString() throws Exception {
        assertOutput("def main() { std::io::println(\"hello world\"); }", "hello world");
    }

    @Test
    public void testPrintlnFloat() throws Exception {
        assertOutput("def main() { std::io::println(3.5); }", "3.5");
    }

    @Test
    public void testPrintNoNewline() throws Exception {
        assertOutput("def main() { std::io::print(\"foo\"); std::io::println(\"bar\"); }", "foobar");
    }

    @Test
    public void testPrintlnEmpty() throws Exception {
        assertOutput("def main() { std::io::println(\"line1\"); std::io::println(); std::io::println(\"line3\"); }",
                "line1\n\nline3");
    }

    @Test
    public void testPrintInt() throws Exception {
        assertOutput("def main() { std::io::println(99); std::io::println(); }", "99");
    }

    @Test
    public void testPrintFloat() throws Exception {
        assertOutput("def main() { std::io::println(2.5); std::io::println(); }", "2.5");
    }

    @Test
    public void testPrintString() throws Exception {
        assertOutput("def main() { std::io::println(\"ok\"); std::io::println(); }", "ok");
    }

    // =========================================================================
    // Arithmetic — int
    // =========================================================================
    @Test
    public void testIntAddition() throws Exception {
        assertOutput("def main() { std::io::println(3 + 4); }", "7");
    }

    @Test
    public void testIntSubtraction() throws Exception {
        assertOutput("def main() { std::io::println(10 - 3); }", "7");
    }

    @Test
    public void testIntMultiplication() throws Exception {
        assertOutput("def main() { std::io::println(6 * 7); }", "42");
    }

    @Test
    public void testIntDivision() throws Exception {
        assertOutput("def main() { std::io::println(20 / 4); }", "5");
    }

    @Test
    public void testIntModulo() throws Exception {
        assertOutput("def main() { std::io::println(17 % 5); }", "2");
    }

    @Test
    public void testIntUnaryNegation() throws Exception {
        assertOutput("def main() { int x = 5; std::io::println(-x); }", "-5");
    }

    @Test
    public void testIntArithmeticChained() throws Exception {
        assertOutput("def main() { std::io::println(2 + 3 * 4); }", "14");
    }

    // =========================================================================
    // Arithmetic — float
    // =========================================================================
    @Test
    public void testFloatAddition() throws Exception {
        assertOutput("def main() { float a = 1.5; float b = 2.5; std::io::println(a + b); }", "4.0");
    }

    @Test
    public void testFloatSubtraction() throws Exception {
        assertOutput("def main() { float a = 5.0; float b = 1.5; std::io::println(a - b); }", "3.5");
    }

    @Test
    public void testFloatMultiplication() throws Exception {
        assertOutput("def main() { std::io::println(2.0 * 3.5); }", "7.0");
    }

    @Test
    public void testFloatDivision() throws Exception {
        assertOutput("def main() { std::io::println(9.0 / 4.0); }", "2.25");
    }

    @Test
    public void testIntToFloatPromotion() throws Exception {
        assertOutput("def main() { float f = 3; std::io::println(f); }", "3.0");
    }

    @Test
    public void testMixedIntFloatArithmetic() throws Exception {
        // int + float: the int is promoted to float
        assertOutput("def main() { float x = 1; float y = x + 1.5; std::io::println(y); }", "2.5");
    }

    @Test
    public void testFloatUnaryNegation() throws Exception {
        assertOutput("def main() { float x = 3.5; std::io::println(-x); }", "-3.5");
    }

    // =========================================================================
    // String
    // =========================================================================
    @Test
    public void testStringConcatenation() throws Exception {
        assertOutput("def main() { std::io::println(\"foo\" + \"bar\"); }", "foobar");
    }

    @Test
    public void testStringLength() throws Exception {
        assertOutput("def main() { string s = \"hello\"; std::io::println(length(s)); }", "5");
    }

    @Test
    public void testStringIndexReturnsCharCode() throws Exception {
        // 'h' == 104 in ASCII
        assertOutput("def main() { string s = \"hello\"; std::io::println(s[0]); }", "104");
    }

    @Test
    public void testStringEquality() throws Exception {
        assertOutput("def main() { string a = \"abc\"; string b = \"abc\"; std::io::println(a == b); }", "true");
    }

    @Test
    public void testStringInequality() throws Exception {
        assertOutput("def main() { string a = \"abc\"; string b = \"xyz\"; std::io::println(a == b); }", "false");
    }

    // =========================================================================
    // Boolean operators
    // =========================================================================
    @Test
    public void testBooleanAndTrue() throws Exception {
        assertOutput("def main() { std::io::println(true && true); }", "true");
    }

    @Test
    public void testBooleanAndFalse() throws Exception {
        assertOutput("def main() { std::io::println(true && false); }", "false");
    }

    @Test
    public void testBooleanOrTrue() throws Exception {
        assertOutput("def main() { std::io::println(false || true); }", "true");
    }

    @Test
    public void testBooleanOrFalse() throws Exception {
        assertOutput("def main() { std::io::println(false || false); }", "false");
    }

    @Test
    public void testBooleanNotTrue() throws Exception {
        assertOutput("def main() { std::io::println(not(false)); }", "true");
    }

    @Test
    public void testBooleanNotFalse() throws Exception {
        assertOutput("def main() { std::io::println(not(true)); }", "false");
    }

    @Test
    public void testBooleanEquality() throws Exception {
        assertOutput("def main() { std::io::println(true == true); }", "true");
    }

    @Test
    public void testBooleanInequality() throws Exception {
        assertOutput("def main() { std::io::println(true =/= false); }", "true");
    }

    // =========================================================================
    // Comparisons
    // =========================================================================
    @Test
    public void testLessThanTrue() throws Exception {
        assertOutput("def main() { std::io::println(3 < 5); }", "true");
    }

    @Test
    public void testLessThanFalse() throws Exception {
        assertOutput("def main() { std::io::println(5 < 3); }", "false");
    }

    @Test
    public void testGreaterThanTrue() throws Exception {
        assertOutput("def main() { std::io::println(5 > 3); }", "true");
    }

    @Test
    public void testGreaterThanFalse() throws Exception {
        assertOutput("def main() { std::io::println(3 > 5); }", "false");
    }

    @Test
    public void testLessThanOrEqualTrue() throws Exception {
        assertOutput("def main() { std::io::println(5 <= 5); }", "true");
    }

    @Test
    public void testLessThanOrEqualFalse() throws Exception {
        assertOutput("def main() { std::io::println(6 <= 5); }", "false");
    }

    @Test
    public void testGreaterThanOrEqualTrue() throws Exception {
        assertOutput("def main() { std::io::println(5 >= 5); }", "true");
    }

    @Test
    public void testGreaterThanOrEqualFalse() throws Exception {
        assertOutput("def main() { std::io::println(4 >= 5); }", "false");
    }

    @Test
    public void testIntEquality() throws Exception {
        assertOutput("def main() { std::io::println(42 == 42); }", "true");
    }

    @Test
    public void testIntInequality() throws Exception {
        assertOutput("def main() { std::io::println(42 =/= 43); }", "true");
    }

    // =========================================================================
    // Variables
    // =========================================================================
    @Test
    public void testLocalVariableDeclarationAndUse() throws Exception {
        assertOutput("def main() { int x = 10; int y = 20; std::io::println(x + y); }", "30");
    }

    @Test
    public void testLocalVariableReassignment() throws Exception {
        assertOutput("def main() { int x = 1; x = 42; std::io::println(x); }", "42");
    }

    @Test
    public void testGlobalVariable() throws Exception {
        assertOutput("int g = 100;\ndef main() { std::io::println(g); }", "100");
    }

    @Test
    public void testGlobalVariableReassignment() throws Exception {
        assertOutput("int g = 10;\ndef main() { g = 99; std::io::println(g); }", "99");
    }

    @Test
    public void testConstant() throws Exception {
        assertOutput("final int c = 42;\ndef main() { std::io::println(c); }", "42");
    }

    @Test
    public void testMultipleConstants() throws Exception {
        assertOutput(
                "final int a = 3;\nfinal int b = 7;\nfinal int c = a + b;\ndef main() { std::io::println(c); }",
                "10"
        );
    }

    @Test
    public void testGlobalInitFromFunction() throws Exception {
        String source = """
                def int compute() {
                    return 42;
                }

                int g = compute();
                def main() {
                    std::io::println(g);
                }
                """;

        assertOutput(source, "42");
    }

    // =========================================================================
    // Control flow — if / else
    // =========================================================================
    @Test
    public void testIfTaken() throws Exception {
        assertOutput("def main() { if (true) { std::io::println(1); } }", "1");
    }

    @Test
    public void testIfNotTaken() throws Exception {
        assertOutput("def main() { if (false) { std::io::println(1); } std::io::println(2); }", "2");
    }

    @Test
    public void testIfElseTrueBranch() throws Exception {
        assertOutput("def main() { if (true) { std::io::println(1); } else { std::io::println(2); } }", "1");
    }

    @Test
    public void testIfElseFalseBranch() throws Exception {
        assertOutput("def main() { if (false) { std::io::println(1); } else { std::io::println(2); } }", "2");
    }

    @Test
    public void testNestedIfElse() throws Exception {
        assertOutput(
                "def main() { int x = 5; if (x > 10) { std::io::println(3); } else { if (x > 3) { std::io::println(2); } else { std::io::println(1); } } }",
                "2"
        );
    }

    @Test
    public void testIfWithComparison() throws Exception {
        assertOutput("def main() { int x = 7; if (x == 7) { std::io::println(1); } else { std::io::println(0); } }", "1");
    }

    // =========================================================================
    // Control flow — while
    // =========================================================================
    @Test
    public void testWhileLoopSum() throws Exception {
        assertOutput(
                "def main() { int i = 0; int sum = 0; while (i < 5) { sum = sum + i; i = i + 1; } std::io::println(sum); }",
                "10"
        );
    }

    @Test
    public void testWhileLoopNotEntered() throws Exception {
        assertOutput(
                "def main() { int i = 10; int x = 0; while (i < 5) { x = 1; } std::io::println(x); }",
                "0"
        );
    }

    @Test
    public void testWhileLoopCountdown() throws Exception {
        assertOutput(
                "def main() { int n = 3; while (n > 0) { n = n - 1; } std::io::println(n); }",
                "0"
        );
    }

    // =========================================================================
    // Control flow — for
    // =========================================================================
    @Test
    public void testForLoopSum() throws Exception {
        assertOutput(
                "def main() { int sum = 0; int i = 0; for (i; 1 -> 6; i+1) { sum = sum + i; } std::io::println(sum); }",
                "15"
        );
    }

    @Test
    public void testForLoopEmptyRange() throws Exception {
        // range 5 -> 5 is empty (start == end)
        assertOutput(
                "def main() { int x = 99; int i = 0; for (i; 5 -> 5; i+1) { x = 0; } std::io::println(x); }",
                "99"
        );
    }

    @Test
    public void testForLoopCounter() throws Exception {
        assertOutput(
                "def main() { int count = 0; int i = 0; for (i; 0 -> 10; i+1) { count = count + 1; } std::io::println(count); }",
                "10"
        );
    }

    // =========================================================================
    // Functions
    // =========================================================================
    @Test
    public void testUserDefinedFunction() throws Exception {
        assertOutput(
                "def int square(int v) { return v * v; }\ndef main() { std::io::println(square(7)); }",
                "49"
        );
    }

    @Test
    public void testFunctionWithMultipleParams() throws Exception {
        assertOutput(
                "def int add(int a, int b) { return a + b; }\ndef main() { std::io::println(add(3, 4)); }",
                "7"
        );
    }

    @Test
    public void testRecursiveFactorial() throws Exception {
        assertOutput(
                "def int fact(int n) { if (n <= 1) { return 1; } return n * fact(n - 1); }\ndef main() { std::io::println(fact(5)); }",
                "120"
        );
    }

    @Test
    public void testRecursiveFibonacci() throws Exception {
        assertOutput(
                "def int fib(int n) { if (n <= 1) { return n; } return fib(n-1) + fib(n-2); }\ndef main() { std::io::println(fib(8)); }",
                "21"
        );
    }

    @Test
    public void testVoidFunctionWithReturn() throws Exception {
        String source = """
            def printTwice(int x) {
                std::io::println(x);
                std::io::println(x);
                return;
            }

            def main() {
                printTwice(5);
            }
        """;

        String output = "5\n5";

        assertOutput(source, output);
    }

    @Test
    public void testFunctionForwardReference() throws Exception {
        // main is declared after add, but the compiler should support forward references
        assertOutput(
                "def main() { std::io::println(add(10, 32)); }\ndef int add(int a, int b) { return a + b; }",
                "42"
        );
    }

    @Test
    public void testParameterShadowsGlobal() throws Exception {
        assertOutput(
                "int x = 100;\ndef int get(int x) { return x; }\ndef main() { std::io::println(get(5)); }",
                "5"
        );
    }

    // =========================================================================
    // Arrays
    // =========================================================================
    @Test
    public void testIntArrayCreateAndRead() throws Exception {
        assertOutput(
                "def main() { int[] arr = int[](5); arr[2] = 42; std::io::println(arr[2]); }",
                "42"
        );
    }

    @Test
    public void testArrayLength() throws Exception {
        assertOutput(
                "def main() { int[] arr = int[](7); std::io::println(length(arr)); }",
                "7"
        );
    }

    @Test
    public void testArraySumElements() throws Exception {
        assertOutput(
                "def main() { int[] arr = int[](3); arr[0] = 10; arr[1] = 20; arr[2] = 30; std::io::println(arr[0] + arr[1] + arr[2]); }",
                "60"
        );
    }

    @Test
    public void testArrayPassedByReference() throws Exception {
        assertOutput(
                "def fill(int[] arr) { arr[0] = 99; return; }\ndef main() { int[] a = int[](3); fill(a); std::io::println(a[0]); }",
                "99"
        );
    }

    @Test
    public void testBoolArray() throws Exception {
        String source = """
        def main() {
            bool[] flags = bool[](3);
            flags[0] = true;
            flags[1] = false;
            std::io::println(flags[0]);
            std::io::println(flags[1]);
        }
        """;

        assertOutput(source, "true\nfalse");
    }

    @Test
    public void testStringArray() throws Exception {
        assertOutput(
                "def main() { string[] words = string[](2); words[0] = \"hello\"; words[1] = \"world\"; std::io::println(words[0] + \" \" + words[1]); }",
                "hello world"
        );
    }

    @Test
    public void testArrayLengthWithBuiltin() throws Exception {
        assertOutput(
                "def main() { float[] arr = float[](4); std::io::println(length(arr)); }",
                "4"
        );
    }

    // =========================================================================
    // Collections
    // =========================================================================
    @Test
    public void testCollectionCreateAndFieldAccess() throws Exception {
        assertOutput(
                "coll Point { int x; int y; }\ndef main() { Point p = Point(3, 4); std::io::println(p.x + p.y); }",
                "7"
        );
    }

    @Test
    public void testCollectionFieldAssignment() throws Exception {
        assertOutput(
                "coll Point { int x; int y; }\ndef main() { Point p = Point(3, 4); p.x = 10; std::io::println(p.x); }",
                "10"
        );
    }

    @Test
    public void testCollectionPassedByReference() throws Exception {
        assertOutput(
                "coll Point { int x; int y; }\n"
                + "def move(Point p, int dx) { p.x = p.x + dx; return; }\n"
                + "def main() { Point p = Point(3, 4); move(p, 10); std::io::println(p.x); }",
                "13"
        );
    }

    @Test
    public void testCollectionWithStringField() throws Exception {
        assertOutput(
                "coll Person { string name; int age; }\n"
                + "def main() { Person p = Person(\"Alice\", 30); std::io::println(p.name); std::io::println(p.age); }",
                "Alice\n30"
        );
    }

    @Test
    public void testCollectionWithArrayField() throws Exception {
        assertOutput(
                "coll Data { int[] values; int n; }\n"
                + "def main() { int[] arr = int[](3); arr[0] = 5; Data d = Data(arr, 3); std::io::println(d.values[0]); }",
                "5"
        );
    }

    @Test
    public void testStructuralTypingConversion() throws Exception {
        assertOutput(
                "coll A { int x; }\ncoll B { int x; }\ndef main() { A a = A(7); B b = a; std::io::println(b.x); }",
                "7"
        );
    }

    // =========================================================================
    // Impl methods
    // =========================================================================
    @Test
    public void testImplInstanceMethod() throws Exception {
        assertOutput(
                "coll Vec { int x; int y; }\n"
                + "impl Vec { def int len2(self) { return self.x * self.x + self.y * self.y; } }\n"
                + "def main() { Vec v = Vec(3, 4); std::io::println(v.len2()); }",
                "25"
        );
    }

    @Test
    public void testImplStaticMethod() throws Exception {
        assertOutput(
                "coll Point { int x; int y; }\n"
                + "impl Point { def Point origin() { return Point(0, 0); } }\n"
                + "def main() { Point p = Point::origin(); std::io::println(p.x + p.y); }",
                "0"
        );
    }

    @Test
    public void testImplMethodChain() throws Exception {
        String source = """
        coll Foo { int a; int b; }
        impl Foo {
          def int add(self) { return self.a + self.b; }
          def Foo make(int a, int b) { return Foo(a, b); }
        }

        def main() {
            int r = Foo::make(3, 4).add();
            std::io::println(r);
        }
        """;

        assertOutput(source, "7");
    }

    @Test
    public void testImplStaticAndTopLevelFunctionCoexist() throws Exception {
        String source = """
            coll Foo { int a; int b; }
            impl Foo {
                def int add(self) { return self.a + self.b; }
                def Foo create(int a, int b) { return Foo(a, b); }
            }
            def Foo create(int a, int b) { return Foo(a, b); }
            def main() {
                Foo foo = Foo(10, 20);
                int result = Foo::create(4, 4).add() + foo.add() + Foo::create(1, 2).add();
                std::io::println(result);
            }
        """;
        assertOutput(source, "41");
    }

    // =========================================================================
    // Built-in functions
    // =========================================================================
    @Test
    public void testBuiltinFloor() throws Exception {
        assertOutput("def main() { std::io::println(floor(3.9)); }", "3");
    }

    @Test
    public void testBuiltinFloorNegative() throws Exception {
        assertOutput("def main() { std::io::println(floor(-1.1)); }", "-2");
    }

    @Test
    public void testBuiltinCeil() throws Exception {
        assertOutput("def main() { std::io::println(ceil(3.1)); }", "4");
    }

    @Test
    public void testBuiltinCeilExact() throws Exception {
        assertOutput("def main() { std::io::println(ceil(3.0)); }", "3");
    }

    @Test
    public void testBuiltinStr() throws Exception {
        assertOutput("def main() { std::io::println(str(42)); }", "42");
    }

    @Test
    public void testBuiltinLengthString() throws Exception {
        assertOutput("def main() { std::io::println(length(\"abcde\")); }", "5");
    }

    @Test
    public void testBuiltinLengthArray() throws Exception {
        assertOutput("def main() { int[] arr = int[](10); std::io::println(length(arr)); }", "10");
    }

    // =========================================================================
    // any type & type tests
    // =========================================================================
    @Test
    public void testAnyHoldingInt() throws Exception {
        assertOutput(
                "def main() { any a = 42; std::io::println(a); }",
                "42"
        );
    }

    // @Test
    // public void testTypeTestMatchingCollection() throws Exception {
    //     assertOutput(
    //             "coll Foo { int v; }\n"
    //             + "def main() {\n"
    //             + "  any a = Foo(42);\n"
    //             + "  if (a is Foo f) { std::io::println(f.v); }\n"
    //             + "  return;\n"
    //             + "}",
    //             "42"
    //     );
    // }
    // @Test
    // public void testTypeTestNonMatching() throws Exception {
    //     assertOutput(
    //             "coll Foo { int v; }\n"
    //             + "coll Bar { int v; }\n"
    //             + "def main() {\n"
    //             + "  any a = Foo(1);\n"
    //             + "  if (a is Bar b) { std::io::println(1); } else { std::io::println(0); }\n"
    //             + "  return;\n"
    //             + "}",
    //             "0"
    //     );
    // }
    // @Test
    // public void testTypeTestInConditionPattern() throws Exception {
    //     assertOutput(
    //             "coll Vector2 { int x; int y; }\n"
    //             + "def main() {\n"
    //             + "  any a = Vector2(1, 2);\n"
    //             + "  any b = Vector2(3, 4);\n"
    //             + "  if (a is Vector2 vx && b is Vector2 vy) {\n"
    //             + "    std::io::println(vx.x + vy.y);\n"
    //             + "  }\n"
    //             + "  return;\n"
    //             + "}",
    //             "5"
    //     );
    // }
    // =========================================================================
    // Compound / integration tests
    // =========================================================================
    @Test
    public void testSumArrayWithForLoop() throws Exception {
        assertOutput(
                "def int sumArr(int[] arr, int n) {\n"
                + "  int s = 0;\n"
                + "  int i = 0;\n"
                + "  for (i; 0 -> n; i+1) { s = s + arr[i]; }\n"
                + "  return s;\n"
                + "}\n"
                + "def main() {\n"
                + "  int[] a = int[](5);\n"
                + "  a[0] = 1; a[1] = 2; a[2] = 3; a[3] = 4; a[4] = 5;\n"
                + "  std::io::println(sumArr(a, 5));\n"
                + "}",
                "15"
        );
    }

    @Test
    public void testStringBuilding() throws Exception {
        assertOutput(
                "def main() {\n"
                + "  string result = \"\";\n"
                + "  int i = 0;\n"
                + "  while (i < 3) {\n"
                + "    result = result + str(i);\n"
                + "    i = i + 1;\n"
                + "  }\n"
                + "  std::io::println(result);\n"
                + "}",
                "012"
        );
    }

    @Test
    public void testConstantsUsedInExpressions() throws Exception {
        assertOutput(
                "final int base = 10;\nfinal int mult = 3;\ndef main() { std::io::println(base * mult + base); }",
                "40"
        );
    }

    @Test
    public void testComplexCollectionUsage() throws Exception {
        assertOutput(
                "coll Point { int x; int y; }\n"
                + "def int dist2(Point a, Point b) {\n"
                + "  int dx = a.x - b.x;\n"
                + "  int dy = a.y - b.y;\n"
                + "  return dx*dx + dy*dy;\n"
                + "}\n"
                + "def main() {\n"
                + "  Point p1 = Point(0, 0);\n"
                + "  Point p2 = Point(3, 4);\n"
                + "  std::io::println(dist2(p1, p2));\n"
                + "}",
                "25"
        );
    }

    @Test
    public void testNestedCollections() throws Exception {
        String source = """
        coll Inner { int v; }
        coll Outer { Inner inner; int w; }
        def main() {
            Inner i = Inner(10);
            Outer o = Outer(i, 5);
            std::io::println(o.inner.v + o.w);
        }
        """;

        assertOutput(source, "15");
    }

    @Test
    public void testLocalVariableShadowing() throws Exception {
        assertOutput(
                "int x = 1;\n"
                + "def main() {\n"
                + "  int x = 2;\n"
                + "  std::io::println(x);\n"
                + "}",
                "2"
        );
    }

    @Test
    public void testMultiplePrintlnLines() throws Exception {
        assertOutput(
                "def main() { std::io::println(1); std::io::println(2); std::io::println(3); }",
                "1\n2\n3"
        );
    }
}
