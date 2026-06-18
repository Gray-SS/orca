package com.orca.compiler.core;

import java.util.LinkedHashMap;
import java.util.function.BiPredicate;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import org.junit.Test;

import com.orca.compiler.core.typesystem.CollectionType;
import com.orca.compiler.core.typesystem.ConversionKind;
import com.orca.compiler.core.typesystem.Conversions;
import com.orca.compiler.core.typesystem.LangType;

public final class TestTypeSystem {

    private CollectionType collectionOf(LinkedHashMap<String, LangType> members) {
        return new CollectionType(members);
    }

    @Test
    public void testPrimitiveTypesAssignableFrom() {
        var arrayType = LangType.arrayOf(LangType.Int);
        var collectionType = collectionOf(new LinkedHashMap<>() {
            {
                put("x", LangType.Int);
            }
        });

        var assignableFromCases = new TestCase[]{
            // Int rules:
            //   - INT is assignable from INT.
            new TestCase(LangType.Int, LangType.Int, true),
            new TestCase(LangType.Int, LangType.Float, false),
            new TestCase(LangType.Int, LangType.Bool, false),
            new TestCase(LangType.Int, LangType.String, false),
            new TestCase(LangType.Int, LangType.Void, false),
            new TestCase(LangType.Int, LangType.Any, false),
            new TestCase(LangType.Int, collectionType, false),
            new TestCase(LangType.Int, arrayType, false),
            // Float rules:
            //   - FLOAT is assignable from FLOAT and INT.
            new TestCase(LangType.Float, LangType.Int, true),
            new TestCase(LangType.Float, LangType.Float, true),
            new TestCase(LangType.Float, LangType.Bool, false),
            new TestCase(LangType.Float, LangType.String, false),
            new TestCase(LangType.Float, LangType.Void, false),
            new TestCase(LangType.Float, LangType.Any, false),
            new TestCase(LangType.Float, collectionType, false),
            new TestCase(LangType.Float, arrayType, false),
            // Bool rules:
            //   - BOOL is assignable from BOOL.
            new TestCase(LangType.Bool, LangType.Int, false),
            new TestCase(LangType.Bool, LangType.Float, false),
            new TestCase(LangType.Bool, LangType.Bool, true),
            new TestCase(LangType.Bool, LangType.String, false),
            new TestCase(LangType.Bool, LangType.Void, false),
            new TestCase(LangType.Bool, LangType.Any, false),
            new TestCase(LangType.Bool, collectionType, false),
            new TestCase(LangType.Bool, arrayType, false),
            // String rules:
            //   - STRING is assignable from STRING.
            new TestCase(LangType.String, LangType.Int, false),
            new TestCase(LangType.String, LangType.Float, false),
            new TestCase(LangType.String, LangType.Bool, false),
            new TestCase(LangType.String, LangType.String, true),
            new TestCase(LangType.String, LangType.Void, false),
            new TestCase(LangType.String, LangType.Any, false),
            new TestCase(LangType.String, collectionType, false),
            new TestCase(LangType.String, arrayType, false),
            // Void rules:
            //   - VOID is not assignable from any type, including itself.
            new TestCase(LangType.Void, LangType.Int, false),
            new TestCase(LangType.Void, LangType.Float, false),
            new TestCase(LangType.Void, LangType.Bool, false),
            new TestCase(LangType.Void, LangType.String, false),
            new TestCase(LangType.Void, LangType.Void, false),
            new TestCase(LangType.Void, LangType.Any, false),
            new TestCase(LangType.Void, collectionType, false),
            new TestCase(LangType.Void, arrayType, false),
            // Any rules:
            //   - ANY is assignable from any type except VOID.
            new TestCase(LangType.Any, LangType.Int, true),
            new TestCase(LangType.Any, LangType.Float, true),
            new TestCase(LangType.Any, LangType.Bool, true),
            new TestCase(LangType.Any, LangType.String, true),
            new TestCase(LangType.Any, LangType.Void, false),
            new TestCase(LangType.Any, LangType.Any, true),
            new TestCase(LangType.Any, collectionType, true),
            new TestCase(LangType.Any, arrayType, true)
        };

        assertTestCases("assignable from", assignableFromCases, (a, b) -> a.isAssignableFrom(b));
    }

    @Test
    public void testPrimitiveTypesAssignableTo() {
        var arrayType = LangType.arrayOf(LangType.Int);
        var collectionType = collectionOf(new LinkedHashMap<>() {
            {
                put("x", LangType.Int);
            }
        });

        var assignableToCases = new TestCase[]{
            // Int rules:
            //   - INT is assignable to INT, FLOAT and ANY.
            new TestCase(LangType.Int, LangType.Int, true),
            new TestCase(LangType.Int, LangType.Float, true),
            new TestCase(LangType.Int, LangType.Bool, false),
            new TestCase(LangType.Int, LangType.String, false),
            new TestCase(LangType.Int, LangType.Void, false),
            new TestCase(LangType.Int, LangType.Any, true),
            new TestCase(LangType.Int, collectionType, false),
            new TestCase(LangType.Int, arrayType, false),
            // Float rules:
            //   - FLOAT is assignable to FLOAT and ANY.
            new TestCase(LangType.Float, LangType.Int, false),
            new TestCase(LangType.Float, LangType.Float, true),
            new TestCase(LangType.Float, LangType.Bool, false),
            new TestCase(LangType.Float, LangType.String, false),
            new TestCase(LangType.Float, LangType.Void, false),
            new TestCase(LangType.Float, LangType.Any, true),
            new TestCase(LangType.Float, collectionType, false),
            new TestCase(LangType.Float, arrayType, false),
            // Bool rules:
            //   - BOOL is assignable to BOOL and ANY.
            new TestCase(LangType.Bool, LangType.Int, false),
            new TestCase(LangType.Bool, LangType.Float, false),
            new TestCase(LangType.Bool, LangType.Bool, true),
            new TestCase(LangType.Bool, LangType.String, false),
            new TestCase(LangType.Bool, LangType.Void, false),
            new TestCase(LangType.Bool, LangType.Any, true),
            new TestCase(LangType.Bool, collectionType, false),
            new TestCase(LangType.Bool, arrayType, false),
            // String rules:
            //   - STRING is assignable to STRING and ANY.
            new TestCase(LangType.String, LangType.Int, false),
            new TestCase(LangType.String, LangType.Float, false),
            new TestCase(LangType.String, LangType.Bool, false),
            new TestCase(LangType.String, LangType.String, true),
            new TestCase(LangType.String, LangType.Void, false),
            new TestCase(LangType.String, LangType.Any, true),
            new TestCase(LangType.String, collectionType, false),
            new TestCase(LangType.String, arrayType, false),
            // Void rules:
            //   - VOID is not assignable to any type, including itself.
            new TestCase(LangType.Void, LangType.Int, false),
            new TestCase(LangType.Void, LangType.Float, false),
            new TestCase(LangType.Void, LangType.Bool, false),
            new TestCase(LangType.Void, LangType.String, false),
            new TestCase(LangType.Void, LangType.Void, false),
            new TestCase(LangType.Void, LangType.Any, false),
            new TestCase(LangType.Void, collectionType, false),
            new TestCase(LangType.Void, arrayType, false),
            // Any rules:
            //   - ANY is assignable to ANY.
            new TestCase(LangType.Any, LangType.Int, false),
            new TestCase(LangType.Any, LangType.Float, false),
            new TestCase(LangType.Any, LangType.Bool, false),
            new TestCase(LangType.Any, LangType.String, false),
            new TestCase(LangType.Any, LangType.Void, false),
            new TestCase(LangType.Any, LangType.Any, true),
            new TestCase(LangType.Any, collectionType, false),
            new TestCase(LangType.Any, arrayType, false)
        };

        assertTestCases("assignable to", assignableToCases, (a, b) -> a.isAssignableTo(b));
    }

    @Test
    public void testCollectionTypeCompatibility() {
        var typeA = collectionOf(new LinkedHashMap<>() {
            {
                put("x", LangType.Int);
            }
        });

        var typeB = collectionOf(new LinkedHashMap<>() {
            {
                put("x", LangType.Int);
            }
        });

        assertTrue(typeA.isAssignableFrom(typeB));
        assertTrue(typeB.isAssignableFrom(typeA));
    }

    @Test
    public void testCollectionTypeIncompatibility() {
        var typeA = collectionOf(new LinkedHashMap<>() {
            {
                put("x", LangType.Int);
            }
        });

        var typeB = collectionOf(new LinkedHashMap<>() {
            {
                put("x", LangType.Int);
                put("y", LangType.Int);
            }
        });

        assertTrue(Conversions.classify(typeA, typeB) == ConversionKind.NONE);
        assertTrue(Conversions.classify(typeB, typeA) == ConversionKind.NONE);
    }

    private static void assertTestCases(String testType, TestCase[] testCases, BiPredicate<LangType, LangType> predicate) {
        for (TestCase testCase : testCases) {
            boolean actual = predicate.test(testCase.a(), testCase.b());
            boolean expected = testCase.expected();
            String message = String.format("Expected %s to %s %s %s.", testCase.a(), expected ? "be" : "not be", testType, testCase.b());
            assertEquals(message, expected, actual);
        }
    }

    private final record TestCase(LangType a, LangType b, boolean expected) {

    }
}
