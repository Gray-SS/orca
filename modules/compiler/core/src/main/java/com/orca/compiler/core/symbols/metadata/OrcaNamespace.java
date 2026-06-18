package com.orca.compiler.core.symbols.metadata;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marks a {@code Package.class} as the root of an Orca namespace. Written by
 * the compiler; read back by {@code JarSymbolReader} to reconstruct the
 * namespace symbol tree without parsing source.
 *
 * <p>
 * Schema version history:
 * <ul>
 * <li>v1 — initial: {@code name}, {@code version}
 * </ul>
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface OrcaNamespace {

    public static final int SCHEMA_VERSION = 1;

    /**
     * Fully qualified Orca namespace path, e.g. {@code "math"} or
     * {@code "std::io"}.
     */
    String name();

    /**
     * Schema version — increment when fields are added or semantics change.
     */
    int version() default SCHEMA_VERSION;
}
