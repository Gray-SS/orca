package com.orca.compiler.core.diagnostics.attachments;

import com.orca.compiler.core.Debug;

public record NoteAttachment(String message) implements DiagnosticAttachment {

    public NoteAttachment {
        Debug.requireNotNullOrEmpty(message, "Message cannot be null or empty for NoteAttachment");
    }
}
