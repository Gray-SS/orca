package com.orca.compiler.core.text;

import java.io.Reader;
import java.util.List;

public abstract class TextSource extends Source {

    private int[] _cachedLineStarts;

    public TextSource(String name) {
        super(name);
    }

    public abstract String formatSpan(SourceSpan span);

    public abstract String formatLocation(SourceLocation location);

    public void validate() {
        // Default implementation does nothing. Subclasses can override to perform specific validation.
    }

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
        int[] lineStarts = lineStarts();
        if (lineNumber < 1 || lineNumber > lineStarts.length) {
            throw new IllegalArgumentException("Line number out of range");
        }

        int lineStart = lineStarts[lineNumber - 1];
        int lineEnd = (lineNumber < lineStarts.length) ? lineStarts[lineNumber] : content().length();

        return content().substring(lineStart, lineEnd).replaceAll("\\r?\\n", "");
    }

    public int lineCount() {
        return lineStarts().length;
    }

    public SourceLocation locAt(int position) {
        if (position < 0 || position > content().length()) {
            throw new IllegalArgumentException("Position must be within the bounds of the file content");
        }

        int line = 0;
        int[] lineStarts = lineStarts();
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

        int[] lineStarts = lineStarts();
        if (line < 0 || line >= lineStarts.length) {
            throw new IllegalArgumentException("Line number out of range");
        }

        int position = lineStarts[line] + col;
        if (position < 0 || position > content().length()) {
            throw new IllegalArgumentException("Calculated position is out of bounds of the file content");
        }

        return position;
    }

    private int[] lineStarts() {
        computeLineStarts();
        return _cachedLineStarts;
    }

    private void computeLineStarts() {
        if (_cachedLineStarts != null) {
            return;
        }

        String content = content();
        if (content == null) {
            throw new IllegalStateException("Content cannot be null when computing line starts");
        }

        if (content.isEmpty()) {
            this._cachedLineStarts = new int[]{0}; // Single line starting at index 0 for empty content
            return;
        }

        List<Integer> computed = new java.util.ArrayList<>();
        computed.add(0); // First line starts at index 0

        for (int i = 0; i < content.length(); i++) {
            char c = content.charAt(i);
            if (c == '\n') {
                computed.add(i + 1);
            }
        }

        this._cachedLineStarts = computed.stream().mapToInt(Integer::intValue).toArray();
    }
}
