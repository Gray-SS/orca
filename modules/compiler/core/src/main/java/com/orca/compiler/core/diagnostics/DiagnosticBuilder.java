package com.orca.compiler.core.diagnostics;

import java.util.ArrayList;
import java.util.List;

import com.orca.compiler.core.diagnostics.attachments.CodeSnippetAttachment;
import com.orca.compiler.core.diagnostics.attachments.DiagnosticAttachment;
import com.orca.compiler.core.diagnostics.attachments.HelpAttachment;
import com.orca.compiler.core.diagnostics.attachments.NoteAttachment;
import com.orca.compiler.core.diagnostics.attachments.RelatedAttachment;
import com.orca.compiler.core.diagnostics.attachments.SourceAttachment;
import com.orca.compiler.core.diagnostics.attachments.SuggestionAttachment;
import com.orca.compiler.core.text.IHaveLocation;
import com.orca.compiler.core.text.IHaveSpan;
import com.orca.compiler.core.text.TextSource;

public final class DiagnosticBuilder {

    private String message;
    private DiagnosticSeverity severity;

    private final DiagnosticCode code;

    private final List<DiagnosticAttachment> attachments = new ArrayList<>();

    private DiagnosticBuilder(DiagnosticCode code) {
        this.code = code;
        this.message = code.message();
        this.severity = code.severity();
    }

    public static DiagnosticBuilder create() {
        return new DiagnosticBuilder(DiagnosticCode.UNCATEGORIZED);
    }

    public static DiagnosticBuilder from(DiagnosticCode code) {
        return new DiagnosticBuilder(code);
    }

    public Diagnostic build() {
        return new Diagnostic(code, message, severity, List.copyOf(attachments));
    }

    public DiagnosticBuilder withSeverity(DiagnosticSeverity severity) {
        this.severity = severity;
        return this;
    }

    public DiagnosticBuilder withSuffixMessage(String suffix) {
        return withMessage(message + ": " + suffix);
    }

    public DiagnosticBuilder withMessage(String message) {
        this.message = message;
        return this;
    }

    public DiagnosticBuilder withSource(TextSource source, String label) {
        return attach(new SourceAttachment(source, label));
    }

    public DiagnosticBuilder withCodeSnippet(IHaveSpan span, String label) {
        return attach(new CodeSnippetAttachment(span, label));
    }

    public DiagnosticBuilder withHelp(String message) {
        return attach(new HelpAttachment(message));
    }

    public DiagnosticBuilder withSuggestion(String label, List<String> suggestions) {
        return attach(new SuggestionAttachment(label, suggestions));
    }

    public DiagnosticBuilder withNote(String message) {
        return attach(new NoteAttachment(message));
    }

    public DiagnosticBuilder withRelated(IHaveLocation location, String label) {
        return attach(new RelatedAttachment(location, label));
    }

    public DiagnosticBuilder attach(DiagnosticAttachment attachment) {
        attachments.add(attachment);
        return this;
    }
}
