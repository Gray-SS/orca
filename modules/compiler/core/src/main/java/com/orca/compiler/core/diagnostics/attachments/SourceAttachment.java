package com.orca.compiler.core.diagnostics.attachments;

import com.google.common.base.Preconditions;
import com.orca.compiler.core.text.TextSource;

public record SourceAttachment(TextSource source, String label) implements DiagnosticAttachment {

    public SourceAttachment {
        Preconditions.checkNotNull(source, "source cannot be null");
        Preconditions.checkNotNull(label, "label cannot be null");
    }
}
