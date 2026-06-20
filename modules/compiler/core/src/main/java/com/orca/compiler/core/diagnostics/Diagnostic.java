package com.orca.compiler.core.diagnostics;

import java.util.List;

import javax.annotation.Nullable;

import com.orca.compiler.core.diagnostics.attachments.DiagnosticAttachment;

public class Diagnostic {

    private final String message;
    private final DiagnosticCode code;
    private final DiagnosticSeverity severity;
    private final List<DiagnosticAttachment> attachments;

    public Diagnostic(DiagnosticCode code, String message, DiagnosticSeverity severity, List<DiagnosticAttachment> attachments) {
        this.code = code;
        this.message = message;
        this.severity = severity;
        this.attachments = attachments;
    }

    public String message() {
        return message;
    }

    public DiagnosticCode code() {
        return code;
    }

    public List<DiagnosticAttachment> attachments() {
        return attachments;
    }

    public DiagnosticSeverity severity() {
        return severity;
    }

    public boolean hasAttachments() {
        return !attachments.isEmpty();
    }

    public <T extends DiagnosticAttachment> List<T> getAttachments(Class<T> type) {
        var result = new java.util.ArrayList<T>();
        for (DiagnosticAttachment attachment : attachments) {
            if (type.isInstance(attachment)) {
                result.add(type.cast(attachment));
            }
        }
        return result;
    }

    public @Nullable
    <T extends DiagnosticAttachment> T getFirstAttachment(Class<T> type) {
        for (DiagnosticAttachment attachment : attachments) {
            if (type.isInstance(attachment)) {
                return type.cast(attachment);
            }
        }
        return null;
    }
}
