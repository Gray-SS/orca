package com.orca.compiler.core;

/**
 * Enum representing the format of the compiled artifact.
 */
public enum OutputFormat {
    /**
     * Represents a JAR file format for the compiled artifact.
     */
    JAR,
    /**
     * Represents a class directory format for the compiled artifact, where the
     * compiled classes are placed in a directory structure.
     */
    CLASS_DIRECTORY
}
