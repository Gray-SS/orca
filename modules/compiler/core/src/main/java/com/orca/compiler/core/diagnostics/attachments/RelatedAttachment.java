package com.orca.compiler.core.diagnostics.attachments;

import com.orca.compiler.core.Debug;
import com.orca.compiler.core.text.IHaveLocation;
import com.orca.compiler.core.text.IHaveSpan;

public final class RelatedAttachment implements DiagnosticAttachment {
    private final IHaveLocation location;
    private final IHaveSpan span;
    private final String label;

    public RelatedAttachment(IHaveSpan span, String label) {
        Debug.requireNotNull(span, "Span cannot be null for RelatedAttachment");
        Debug.requireNotNullOrEmpty(label, "Label cannot be null or empty for RelatedAttachment");

        this.location = span;
        this.span = span;
        this.label = label;
    }

    public RelatedAttachment(IHaveLocation location, String label) {
        Debug.requireNotNull(location, "Location cannot be null for RelatedAttachment");
        Debug.requireNotNullOrEmpty(label, "Label cannot be null or empty for RelatedAttachment");

        this.location = location;
        this.label = label;
        this.span = null;
    }

    public boolean hasSpan() {
        return span != null;
    }

    public IHaveSpan span() {
        return span;
    }

    public IHaveLocation location() {
        return location;
    }

    public String label() {
        return label;
    }
}
