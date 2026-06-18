package com.orca.lsp;

import org.eclipse.lsp4j.Location;
import org.eclipse.lsp4j.Position;
import org.eclipse.lsp4j.Range;

import com.orca.compiler.core.diagnostics.Diagnostic;
import com.orca.compiler.core.diagnostics.attachments.CodeSnippetAttachment;
import com.orca.compiler.core.diagnostics.attachments.SourceAttachment;
import com.orca.compiler.core.text.IHaveSpan;
import com.orca.compiler.core.text.SourceLocation;
import com.orca.compiler.core.text.SourceSpan;

public final class LspUtils {

    /**
     * Convert an LSP Position (line and character) to our internal
     * SourceLocation, which is 1-based.
     *
     * @param source The source file this position is in
     * @param position The LSP position to convert
     * @return A SourceLocation corresponding to the given LSP position
     */
    public static SourceLocation toOrcaPosition(LspSource source, Position position) {
        // LSP positions are 0-based, but our SourceLocation is 1-based, so we need to add 1 to both line and character.
        return new SourceLocation(source, position.getCharacter() + 1, position.getLine() + 1);
    }

    /**
     * Convert an LSP Range (start and end positions) to our internal
     * SourceSpan.
     *
     * @param source The source file this range is in
     * @param range The LSP range to convert
     * @return A SourceSpan corresponding to the given LSP range
     */
    public static SourceSpan toOrcaRange(LspSource source, Range range) {
        var start = source.getOffset(toOrcaPosition(source, range.getStart()));
        var end = source.getOffset(toOrcaPosition(source, range.getEnd()));
        return SourceSpan.fromBounds(source, start, end);
    }

    /**
     * Convert our internal SourceSpan to an LSP Location, which includes the
     * URI of the source file and the range of the span.
     *
     * @param source The source file this span is in
     * @param span The SourceSpan to convert
     * @return An LSP Location corresponding to the given SourceSpan
     */
    public static Location toLspLocation(SourceSpan span) {
        var source = getLspSourceFromSpanProvider(span);
        return new Location(source.uri(), toLspRange(span));
    }

    /**
     * Convert our internal SourceSpan to an LSP Range (start and end
     * positions).
     *
     * @param span The SourceSpan to convert
     * @return An LSP Range corresponding to the given SourceSpan
     */
    public static Range toLspRange(SourceSpan span) {
        var start = toLspPosition(span.loc());
        var end = toLspPosition(span.endLoc());
        return new Range(start, end);
    }

    /**
     * Convert our internal SourceLocation to an LSP Position (line and
     * character), which is 0-based.
     *
     * @param location The SourceLocation to convert
     * @return An LSP Position corresponding to the given SourceLocation
     */
    public static Position toLspPosition(SourceLocation location) {
        // Convert back to 0-based for LSP
        return new Position(location.line() - 1, location.col() - 1);
    }

    /**
     * Convert our internal Diagnostic to an LSP Diagnostic.
     *
     * @param source The LspSource associated with the diagnostic
     * @param diagnostic The internal Diagnostic to convert
     * @return An LSP Diagnostic corresponding to the given internal Diagnostic
     */
    public static org.eclipse.lsp4j.Diagnostic toLspDiagnostic(Diagnostic diagnostic) {
        var code = diagnostic.code().toString();
        var message = diagnostic.message();
        var severity = switch (diagnostic.severity()) {
            case ERROR ->
                org.eclipse.lsp4j.DiagnosticSeverity.Error;
            case WARNING ->
                org.eclipse.lsp4j.DiagnosticSeverity.Warning;
            case INFO ->
                org.eclipse.lsp4j.DiagnosticSeverity.Information;
        };

        Range range;
        LspSource source = null;
        var codeSnippetAttachment = diagnostic.getFirstAttachment(CodeSnippetAttachment.class);
        if (codeSnippetAttachment != null) {
            var spanProvider = codeSnippetAttachment.span();
            range = toLspRange(spanProvider.span());
            source = getLspSourceFromSpanProvider(spanProvider);
            message += ": " + codeSnippetAttachment.label();
        } else {
            // If no span is provided, use a default range at the start of the file
            range = new Range(new Position(0, 0), new Position(0, 0));
        }

        var sourceAttachment = diagnostic.getFirstAttachment(SourceAttachment.class);
        if (sourceAttachment != null) {
            var attachedSource = sourceAttachment.source();
            if (!(attachedSource instanceof LspSource lspSource)) {
                System.err.println("Warning: Expected source attachment to be an LspSource, but got " + attachedSource.getClass().getName());
            } else {
                source = lspSource;
            }
        }

        var uri = (source != null) ? source.uri() : null;

        return new org.eclipse.lsp4j.Diagnostic(
                range,
                message,
                severity,
                uri,
                code
        );
    }

    private static LspSource getLspSourceFromSpanProvider(IHaveSpan spanProvider) {
        var actualSpan = spanProvider.span();
        var source = actualSpan.source();
        return switch (source) {
            case LspSource lspSource ->
                lspSource;
            case null -> {
                System.err.println("Warning: Span provider provided a null source");
                yield null;
            }
            default -> {
                System.err.println("Warning: Expected source to be an LspSource, but got " + source.getClass().getName());
                yield null;
            }
        };
    }
}
