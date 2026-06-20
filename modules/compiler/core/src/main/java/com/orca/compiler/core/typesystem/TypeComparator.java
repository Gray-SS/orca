package com.orca.compiler.core.typesystem;

public final class TypeComparator {

    public static boolean areStructurallyEquivalent(LangType a, LangType b) {
        a = a.unwrap();
        b = b.unwrap();

        if (a.kind() != b.kind()) {
            // Types of different kinds are not structurally equivalent.
            return false;
        }

        if (a.isAny()) {
            return true;
        }

        if (a.isPrimitive()) {
            return a.equals(b);
        }

        if (a instanceof CollectionType collectionA && b instanceof CollectionType collectionB) {
            return areStructurallyEquivalent(collectionA, collectionB);
        }

        if (a instanceof ArrayType arrayA && b instanceof ArrayType arrayB) {
            return areStructurallyEquivalent(arrayA, arrayB);
        }

        // Other types are not structurally compatible.
        return false;
    }

    public static boolean areStructurallyEquivalent(CollectionType a, CollectionType b) {
        var fa = a.getFields();
        var fb = b.getFields();

        if (fa.size() != fb.size()) {
            // Collections with different number of fields are not structurally equivalent.
            return false;
        }

        for (var entry : fa.entrySet()) {
            var fieldName = entry.getKey();
            var fieldTypeA = entry.getValue();
            var fieldTypeB = fb.get(fieldName);

            if (fieldTypeB == null || !areStructurallyEquivalent(fieldTypeA, fieldTypeB)) {
                return false;
            }
        }

        return true;
    }

    public static boolean areStructurallyEquivalent(ArrayType a, ArrayType b) {
        return areStructurallyEquivalent(a.getElementType(), b.getElementType());
    }
}
