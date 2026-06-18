package com.orca.lsp;

import com.orca.compiler.core.text.SourceLocation;
import com.orca.compiler.core.text.SourceSpan;
import com.orca.compiler.core.text.TextSource;

/**
 * TextSource backed by in-memory content for LSP use. The name is the document
 * URI; validate() is a no-op.
 */
public class LspSource extends TextSource {

    private final String uri;
    private final String content;

    public LspSource(String uri, String content) {
        super(uri);
        this.uri = uri;
        this.content = content;
    }

    public String uri() {
        return uri;
    }

    @Override
    public String content() {
        return content;
    }

    @Override
    public String formatLocation(SourceLocation location) {
        return String.format("%s:%d:%d", name(), location.line(), location.col());
    }

    @Override
    public String formatSpan(SourceSpan span) {
        return String.format("%s:%d:%d - %d:%d",
                name(),
                span.loc().line(), span.loc().col(),
                span.endLoc().line(), span.endLoc().col());
    }

    @Override
    public void validate() {
        // No-op: content is provided in-memory by the editor.
    }
}
