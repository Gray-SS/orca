package com.orca.compiler.core.diagnostics.attachments;

import com.orca.compiler.core.Debug;
import com.orca.compiler.core.text.IHaveSpan;

public record CodeSnippetAttachment(IHaveSpan span, String label) implements DiagnosticAttachment {

    public CodeSnippetAttachment {
        Debug.requireNotNull(span, "Span cannot be null for CodeSnippetAttachment");
        Debug.requireNotNullOrEmpty(label, "Label cannot be null or empty for CodeSnippetAttachment");
    }
}
