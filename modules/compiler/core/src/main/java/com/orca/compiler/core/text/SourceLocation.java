package com.orca.compiler.core.text;

public record SourceLocation(TextSource source, int col, int line) implements Location {

    public SourceLocation {
        if (source == null) {
            throw new IllegalArgumentException("Source cannot be null");
        }
        if (col < 0) {
            throw new IllegalStateException("Column must be non-negative");
        }
        if (line < 0) {
            throw new IllegalStateException("Line must be non-negative");
        }
    }

    public String describe() {
        return toString();
    }

    @Override
    public String toString() {
        return source.formatLocation(this);
    }
}
