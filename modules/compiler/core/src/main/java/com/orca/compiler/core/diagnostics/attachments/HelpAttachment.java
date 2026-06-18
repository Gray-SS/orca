package com.orca.compiler.core.diagnostics.attachments;

import com.orca.compiler.core.Debug;

public record HelpAttachment(String message) implements DiagnosticAttachment {
    public HelpAttachment {
        Debug.requireNotNullOrEmpty(message, "Message cannot be null or empty for HelpAttachment");
    }
}
