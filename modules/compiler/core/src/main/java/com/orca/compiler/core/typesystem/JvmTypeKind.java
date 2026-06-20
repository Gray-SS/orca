package com.orca.compiler.core.typesystem;

public enum JvmTypeKind {
    OBJECT("java.lang.Object"),
    VOID("void"),
    PRIMITIVE("primitive"),
    STRING("java.lang.String"),
    ARRAY("array"),
    ENUM("enum"),
    INTERFACE("interface"),
    ANNOTATION("annotation"),
    RECORD("record"),
    SYNTHETIC("synthetic"),
    ANONYMOUS("anonymous"),
    LOCAL("local"),
    MEMBER("member"),
    UNNAMED("unnamed"),
    CLASS("class");

    private final String displayName;

    JvmTypeKind(String displayName) {
        this.displayName = displayName;
    }

    public String displayName() {
        return displayName;
    }

    public static JvmTypeKind fromClass(Class<?> clazz) {
        if (clazz == void.class) {
            return VOID;
        }

        if (clazz == Object.class) {
            return OBJECT;
        }

        if (clazz == String.class) {
            return STRING;
        }

        if (clazz.isPrimitive()) {
            return PRIMITIVE;
        }

        if (clazz.isArray()) {
            return ARRAY;
        }

        if (clazz.isEnum()) {
            return ENUM;
        }

        if (clazz.isInterface()) {
            return INTERFACE;
        }

        if (clazz.isAnnotation()) {
            return ANNOTATION;
        }

        if (clazz.isRecord()) {
            return RECORD;
        }

        if (clazz.isSynthetic()) {
            return SYNTHETIC;
        }

        if (clazz.isAnonymousClass()) {
            return ANONYMOUS;
        }

        if (clazz.isLocalClass()) {
            return LOCAL;
        }

        if (clazz.isMemberClass()) {
            return MEMBER;
        }

        return CLASS;
    }
}
