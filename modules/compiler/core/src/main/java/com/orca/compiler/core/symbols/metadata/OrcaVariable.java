package com.orca.compiler.core.symbols.metadata;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marks a {@code public static} field in a {@code Package.class} as an Orca
 * namespace-level variable.
 *
 * <p>
 * Schema version history:
 * <ul>
 * <li>v1 — initial: {@code orcaType}, {@code mutable}, {@code modifiers}
 * </ul>
 */
@Retention(RetentionPolicy.CLASS)
@Target(ElementType.FIELD)
public @interface OrcaVariable {

    public static final int SCHEMA_VERSION = 1;

    /**
     * Fully qualified Orca type name, e.g. {@code "Int"} or
     * {@code "math::Vector"}.
     */
    String orcaType();

    /**
     * {@code true} if declared as a mutable variable ({@code var}),
     * {@code false} for a constant ({@code val}).
     */
    boolean mutable() default false;

    /**
     * Schema version — increment when fields are added or semantics change.
     */
    int version() default SCHEMA_VERSION;

    /**
     * Stable string modifier tags, e.g.
     * {@code "static"}, {@code "synthesized"}. Using strings (not a bitmask)
     * means adding a new modifier never corrupts older readers that simply
     * ignore unknown tags.
     */
    String[] modifiers() default {};
}
