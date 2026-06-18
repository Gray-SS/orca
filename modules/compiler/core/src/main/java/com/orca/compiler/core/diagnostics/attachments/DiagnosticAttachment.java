package com.orca.compiler.core.diagnostics.attachments;

public sealed interface DiagnosticAttachment
        permits CodeSnippetAttachment,
        HelpAttachment,
        NoteAttachment,
        RelatedAttachment,
        SuggestionAttachment,
        SourceAttachment {
}
