package com.orca.compiler.core.text;

import java.io.Reader;

public abstract class TextSource extends Source {

    private int[] cachedLineStarts;
    private boolean lineStartsComputed = false;

    public TextSource(String name) {
        super(name);
    }

    public abstract String formatSpan(SourceSpan span);

    public abstract String formatLocation(SourceLocation location);

    public abstract String content();

    public int length() {
        return content().length();
    }

    public Reader reader() {
        return new java.io.StringReader(content());
    }

    public String getText(SourceSpan span) {
        if (span.source() != this) {
            throw new IllegalArgumentException("Span does not belong to this source");
        }

        int start = span.start();
        int length = span.length();

        if (start < 0 || length < 0 || start + length > content().length()) {
            throw new IllegalArgumentException("Span is out of bounds of the source content");
        }

        return content().substring(start, start + length);
    }

    public SourceSpan spanStart() {
        return new SourceSpan(this, 0, 0);
    }

    public SourceSpan spanEnd() {
        return new SourceSpan(this, content().length(), 0);
    }

    public SourceSpan spanAt(int start, int length) {
        return new SourceSpan(this, start, length);
    }

    public String getLine(int lineNumber) throws IllegalArgumentException {
        int[] lineStarts = getOrComputeLineStarts();
        if (lineNumber < 1 || lineNumber > lineStarts.length) {
            throw new IllegalArgumentException("Line number out of range");
        }

        int lineStart = lineStarts[lineNumber - 1];
        int lineEnd = (lineNumber < lineStarts.length) ? lineStarts[lineNumber] : content().length();

        return content().substring(lineStart, lineEnd).replaceAll("\\r?\\n", "");
    }

    public int lineCount() {
        return getOrComputeLineStarts().length;
    }

    public SourceLocation locAt(int position) {
        if (position < 0 || position > content().length()) {
            throw new IllegalArgumentException("Position must be within the bounds of the file content");
        }

        int line = 0;
        int[] lineStarts = getOrComputeLineStarts();
        while (line < lineStarts.length && lineStarts[line] <= position) {
            line++;
        }
        line--; // Step back to the correct line

        int col = position - lineStarts[line];
        return new SourceLocation(this, col + 1, line + 1);
    }

    public int getOffset(SourceLocation loc) {
        if (loc.source() != this) {
            throw new IllegalArgumentException("Source location does not belong to this source");
        }

        int line = loc.line() - 1; // Convert to 0-based index
        int col = loc.col() - 1;   // Convert to 0-based index

        int[] lineStarts = getOrComputeLineStarts();
        if (line < 0 || line >= lineStarts.length) {
            throw new IllegalArgumentException("Line number out of range");
        }

        int position = lineStarts[line] + col;
        if (position < 0 || position > content().length()) {
            throw new IllegalArgumentException("Calculated position is out of bounds of the file content");
        }

        return position;
    }

    private int[] getOrComputeLineStarts() {
        if (!lineStartsComputed) {
            cachedLineStarts = computeLineStarts();
            lineStartsComputed = true;
        }

        return cachedLineStarts;
    }

    private int[] computeLineStarts() {
        String content = content();
        if (content == null) {
            throw new IllegalStateException("Content cannot be null when computing line starts");
        }

        if (content.isEmpty()) {
            return new int[]{0}; // Single line starting at index 0 for empty content
        }

        int initialCapacity = Math.max(16, content.length() / 80); // Estimate initial capacity based on average line length
        int lineCount = 0;
        var lineStarts = new int[initialCapacity];
        lineStarts[lineCount++] = 0; // First line starts at index 0

        for (int i = 0; i < content.length(); i++) {
            char c = content.charAt(i);
            if (c == '\n') {
                if (lineCount >= lineStarts.length) {
                    // Resize the array if needed
                    int newCapacity = lineStarts.length * 2;
                    var newLineStarts = new int[newCapacity];
                    System.arraycopy(lineStarts, 0, newLineStarts, 0, lineStarts.length);
                    lineStarts = newLineStarts;
                }
                lineStarts[lineCount++] = i + 1; // Next line starts after the newline character
            }
        }

        // Trim the array to the actual number of lines
        var result = new int[lineCount];
        System.arraycopy(lineStarts, 0, result, 0, lineCount);

        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof TextSource o)) {
            return false;
        }

        return this.name().equals(o.name());
    }

    @Override
    public int hashCode() {
        return name().hashCode();
    }
}
