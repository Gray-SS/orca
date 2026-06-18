package com.orca.compiler.core.text;

public record SourceSpan(TextSource source, int start, int length) implements Comparable<SourceSpan>, IHaveSpan {

    public static final SourceSpan NONE = new SourceSpan(new StringSource(""), 0, 0);

    public SourceSpan {
        // Validate the span parameters to ensure they are within reasonable bounds.
        if (source == null) {
            throw new IllegalStateException(String.format("Source cannot be null. Start: %d, Length: %d", start, length));
        }
        if (start < 0) {
            throw new IllegalStateException(String.format("Start must be non-negative. Start: %d, Length: %d", start, length));
        }
        if (length < 0) {
            throw new IllegalStateException(String.format("Length must be non-negative. Start: %d, Length: %d", start, length));
        }
        if (length > source.length()) {
            throw new IllegalStateException(String.format("Length cannot exceed source length. Start: %d, Length: %d, Source Length: %d", start, length, source.length()));
        }
        if (start + length > source.length()) {
            throw new IllegalStateException(String.format("Span cannot extend beyond end of source. Start: %d, Length: %d, Source Length: %d", start, length, source.length()));
        }
    }

    @Override
    public String text() {
        return source.getText(this);
    }

    @Override
    public SourceSpan span() {
        return this;
    }

    public boolean contains(int position) {
        return position >= start && position <= end();
    }

    public int end() {
        return start + length;
    }

    public SourceLocation loc() {
        return source.locAt(start);
    }

    public SourceLocation endLoc() {
        return source.locAt(end());
    }

    @Override
    public String toString() {
        return source.formatSpan(this);
    }

    @Override
    public int compareTo(SourceSpan arg0) {
        if (this.source != arg0.source) {
            throw new IllegalArgumentException("Cannot compare spans from different sources");
        }

        return Integer.compare(this.start, arg0.start);
    }

    public static SourceSpan fromBounds(TextSource source, int start, int end) {
        return new SourceSpan(source, start, end - start);
    }

    public static SourceSpan fromBounds(SourceSpan startSpan, SourceSpan endSpan) {
        TextSource source = startSpan.source();

        if (startSpan.source() != endSpan.source()) {
            throw new IllegalArgumentException("Start and end spans must be from the same source");
        }
        if (startSpan.start() > endSpan.end()) {
            throw new IllegalArgumentException("Start span must come before end span");
        }
        return source.spanAt(
                startSpan.start(),
                endSpan.end() - startSpan.start()
        );
    }
}
