package com.orca.compiler.core.diagnostics.attachments;

import java.util.List;

import com.orca.compiler.core.Debug;

public final class SuggestionAttachment implements DiagnosticAttachment {

    private final String label;
    private final List<String> suggestions;

    public SuggestionAttachment(String label, List<String> suggestions) {
        Debug.requireNotNull(label, "Label cannot be null for SuggestionAttachment");
        Debug.requireNotNull(suggestions, "Suggestions cannot be null for SuggestionAttachment");

        this.label = label;
        this.suggestions = suggestions;
    }

    public String getLabel() {
        return label;
    }

    public List<String> getSuggestions() {
        return suggestions;
    }
}
