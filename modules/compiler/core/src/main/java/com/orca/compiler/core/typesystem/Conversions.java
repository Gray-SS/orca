package com.orca.compiler.core.typesystem;

import java.util.List;

import com.google.common.base.Preconditions;

public final class Conversions {

    public static final int INCOMPATIBLE_COST = Integer.MAX_VALUE;
    public static final int BOXING_COST = 100;
    public static final int IDENTITY_COST = 0;

    private Conversions() {
    }

    public static ConversionKind classify(LangType from, LangType to) {
        if (from.isError() || to.isError()) {
            // Propagate error types without causing cascading errors
            return ConversionKind.IMPLICIT;
        }

        // VOID is never assignable from or to any type, including itself.
        if (from.isVoid() || to.isVoid()) {
            return ConversionKind.NONE;
        }

        if (from.equals(to)) {
            return ConversionKind.IDENTITY;
        }

        // Structural equivalence allows implicit conversion.
        if (TypeComparator.areStructurallyEquivalent(from, to)) {
            return ConversionKind.IMPLICIT;
        }

        if (isWiddeningNumericConversion(from, to)) {
            return ConversionKind.IMPLICIT;
        }

        if (isNarrowingNumericConversion(from, to)) {
            return ConversionKind.EXPLICIT;
        }

        // Any type can be implicitly converted to ANY, except void.
        if (to.isAny() && !from.isVoid()) {
            return ConversionKind.IMPLICIT;
        }

        // T[] is implicitly convertible to ANY[] for any element type T.
        if (from.isArray() && to instanceof ArrayType toArr && toArr.getElementType().isAny()) {
            return ConversionKind.IMPLICIT;
        }

        return ConversionKind.NONE;
    }

    public static int cost(List<LangType> targetTypes, List<LangType> sourceTypes) {
        if (targetTypes.size() != sourceTypes.size()) {
            return INCOMPATIBLE_COST;
        }

        int totalCost = 0;
        for (int i = 0; i < targetTypes.size(); i++) {
            int cost = cost(targetTypes.get(i), sourceTypes.get(i));
            Preconditions.checkState(cost >= 0, "Cost should never be negative");

            if (cost == INCOMPATIBLE_COST) {
                return INCOMPATIBLE_COST;
            }

            totalCost += cost;
        }

        return totalCost;
    }

    public static int cost(LangType target, LangType source) {
        ConversionKind conversion = classify(source, target);
        return switch (conversion) {
            case IDENTITY: {
                // No cost for identity conversion
                yield IDENTITY_COST;
            }
            case IMPLICIT: {
                // Prefer closer numeric widening over distant widening, and prefer
                // any numeric widening over boxing (primitive → Any).
                if (target.isAny() && source.isPrimitive()) {
                    yield BOXING_COST;
                } else {
                    int dist = wideningDistance(source, target);
                    yield (dist > 0) ? dist : 1;
                }
            }
            case NONE, EXPLICIT: {
                // Incompatible types, return maximum score
                yield INCOMPATIBLE_COST;
            }
        };
    }

    private static boolean isWiddeningNumericConversion(LangType from, LangType to) {
        if (from.isChar()) {
            return to.isInt() || to.isLong() || to.isFloat() || to.isDouble();
        }
        int fromRank = getNumericRank(from);
        int toRank = getNumericRank(to);
        return fromRank != -1 && toRank != -1 && fromRank < toRank;
    }

    private static boolean isNarrowingNumericConversion(LangType from, LangType to) {
        // Conversions involving char as source or target (except char widens) are narrowing
        if (from.isChar()) {
            return to.isByte() || to.isShort();
        }
        if (to.isChar()) {
            return true;
        }
        int fromRank = getNumericRank(from);
        int toRank = getNumericRank(to);
        return fromRank != -1 && toRank != -1 && fromRank > toRank;
    }

    /**
     * Returns the widening distance from {@code from} to {@code to}, or -1 if
     * not a widening. Used by the overload resolver to prefer closer numeric
     * matches.
     */
    private static int wideningDistance(LangType from, LangType to) {
        if (!isWiddeningNumericConversion(from, to)) {
            return -1;
        }
        if (from.isChar()) {
            // char → int=1, long=2, float=3, double=4
            if (to.isInt()) {
                return 1;
            }
            if (to.isLong()) {
                return 2;
            }
            if (to.isFloat()) {
                return 3;
            }
            if (to.isDouble()) {
                return 4;
            }
        }
        int fromRank = getNumericRank(from);
        int toRank = getNumericRank(to);
        return toRank - fromRank;
    }

    private static int getNumericRank(LangType type) {
        if (type.isByte()) {
            return 1;
        }
        if (type.isShort()) {
            return 2;
        }
        if (type.isInt()) {
            return 3;
        }
        if (type.isLong()) {
            return 4;
        }
        if (type.isFloat()) {
            return 5;
        }
        if (type.isDouble()) {
            return 6;
        }
        return -1;
    }
}
