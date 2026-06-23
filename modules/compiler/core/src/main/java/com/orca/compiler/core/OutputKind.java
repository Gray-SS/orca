package com.orca.compiler.core;

/**
 * Represents the kind of output the compiler should produce.
 */
public enum OutputKind {
    /**
     * Compile to a standalone application (e.g., a JAR file with a main class).
     * This is the default output kind. In this mode, the compiler will require
     * a main function and will generate an entry point for the program.
     */
    APPLICATION,
    /**
     * Compile to a library (e.g., a JAR file without a main class). In this
     * mode, the compiler will not require a main function and will generate
     * only the necessary classes for use as a dependency.
     */
    LIBRARY
}
