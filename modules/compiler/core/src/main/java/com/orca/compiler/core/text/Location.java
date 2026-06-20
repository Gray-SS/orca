package com.orca.compiler.core.text;

/**
 * Represents a location that can be used to identify an entity or a point in
 * the source code.
 */
public interface Location extends IHaveLocation {

    /**
     * Gets the source associated with this location.
     *
     * @return the source associated with this location
     */
    Source source();

    /**
     * Gets a human-readable description of this location, typically including
     * the source name and line/column information.
     *
     * @return a human-readable description of this location
     */
    String describe();

    /**
     * By default, the location of an entity that implements Location is itself.
     * This method can be overridden by classes that implement both Location and
     * IHaveLocation to provide a different location if needed.
     *
     * @return the location of this entity, which by default is itself
     */
    default Location location() {
        return this;
    }
}
