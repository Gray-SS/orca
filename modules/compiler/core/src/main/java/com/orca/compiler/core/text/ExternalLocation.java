package com.orca.compiler.core.text;

/**
 * A lightweight location for entities that do not originate from user source
 * files or JARs. Used for reporting diagnostics related to the Java runtime /
 * module system.
 */
public final class ExternalLocation implements Location {

    private final String description;

    public ExternalLocation(String description) {
        if (description == null || description.isBlank()) {
            throw new IllegalArgumentException("Description cannot be null or blank");
        }

        this.description = description;
    }

    @Override
    public Source source() {
        return null;
    }

    @Override
    public String describe() {
        return description;
    }

    @Override
    public String toString() {
        return describe();
    }
}
