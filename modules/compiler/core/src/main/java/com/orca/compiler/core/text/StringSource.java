package com.orca.compiler.core.text;

/**
 *  Represents a source of text that is backed by an in-memory string. This can be useful for testing, or for representing code snippets that are not stored in files.
 *  The StringSource class extends the TextSource class, which provides common functionality for all text
 */
public class StringSource extends TextSource {
    private final String _content;

    public StringSource(String content) {
        super("string:" + content.hashCode());
        this._content = content;
    }

    @Override
    public String content() {
        return _content;
    }

    @Override
    public String formatLocation(SourceLocation location) {
        return String.format("%s:%d:%d", name(), location.line(), location.col());
    }

    @Override
    public String formatSpan(SourceSpan span) {
        return String.format("%s:%d:%d - %d:%d", name(), span.loc().line(), span.loc().col(), span.endLoc().line(), span.endLoc().col());
    }
}
