package com.orca.compiler.core.symbols.metadata;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marks a {@code public static} method in a {@code Package.class} as an Orca
 * namespace-level function.
 *
 * <p>
 * Parameter names are preserved here because the JVM strips them from bytecode
 * unless debug info is present.
 *
 * <p>
 * Schema version history:
 * <ul>
 * <li>v1 — initial: {@code paramNames}, {@code paramTypes}, {@code returnType},
 * {@code modifiers}
 * </ul>
 */
@Retention(RetentionPolicy.CLASS)
@Target(ElementType.METHOD)
public @interface OrcaMethod {

    public static final int SCHEMA_VERSION = 1;

    /**
     * Orca parameter names in declaration order (excludes the implicit
     * {@code self} parameter).
     */
    String[] paramNames() default {};

    /**
     * Orca type names for each parameter, parallel to {@code paramNames}.
     */
    String[] paramTypes() default {};

    /**
     * Fully qualified Orca return type, e.g. {@code "Int"} or {@code "Void"}.
     */
    String returnType() default "Void";

    /**
     * Stable string modifier tags, e.g.
     * {@code "static"}, {@code "entry_point"}.
     */
    String[] modifiers() default {};

    /**
     * Schema version — increment when fields are added or semantics change.
     */
    int version() default SCHEMA_VERSION;
}
