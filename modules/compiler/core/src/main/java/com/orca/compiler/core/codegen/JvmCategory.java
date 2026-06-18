package com.orca.compiler.core.codegen;

import com.orca.compiler.core.typesystem.LangType;

public enum JvmCategory {
    INT_LIKE,
    LONG,
    FLOAT,
    DOUBLE,
    REFERENCE;

    public static int sizeOf(LangType type) {
        return switch (of(type)) {
            case LONG, DOUBLE ->
                2;
            default ->
                1;
        };
    }

    public int size() {
        return switch (this) {
            case LONG, DOUBLE ->
                2;
            default ->
                1;
        };
    }

    public static JvmCategory of(LangType type) {
        if (type.isByte() || type.isShort() || type.isInt() || type.isChar() || type.isBool()) {
            return INT_LIKE;
        } else if (type.isLong()) {
            return LONG;
        } else if (type.isFloat()) {
            return FLOAT;
        } else if (type.isDouble()) {
            return DOUBLE;
        } else {
            return REFERENCE;
        }
    }
}
