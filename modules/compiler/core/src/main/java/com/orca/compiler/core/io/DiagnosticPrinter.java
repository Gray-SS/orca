package com.orca.compiler.core.io;

import java.io.PrintStream;

import com.orca.compiler.core.diagnostics.Diagnostic;
import com.orca.compiler.core.diagnostics.DiagnosticSeverity;
import com.orca.compiler.core.diagnostics.attachments.CodeSnippetAttachment;
import com.orca.compiler.core.diagnostics.attachments.HelpAttachment;
import com.orca.compiler.core.diagnostics.attachments.NoteAttachment;
import com.orca.compiler.core.diagnostics.attachments.RelatedAttachment;
import com.orca.compiler.core.diagnostics.attachments.SourceAttachment;
import com.orca.compiler.core.diagnostics.attachments.SuggestionAttachment;
import com.orca.compiler.core.text.SourceLocation;
import com.orca.compiler.core.text.SourceSpan;
import com.orca.compiler.core.text.TextSource;

public class DiagnosticPrinter {

    public static void printDiagnostic(Diagnostic diagnostic, int indentSize) {
        PrintStream outputStream = switch (diagnostic.severity()) {
            case ERROR ->
                System.err;
            case WARNING, INFO ->
                System.out;
        };

        printHeader(outputStream, diagnostic);

        var primarySnippets = diagnostic.getAttachments(CodeSnippetAttachment.class);
        var relatedSnippets = diagnostic.getAttachments(RelatedAttachment.class);
        var noteMessages = diagnostic.getAttachments(NoteAttachment.class);
        var helpMessages = diagnostic.getAttachments(HelpAttachment.class);
        var suggestionsMessages = diagnostic.getAttachments(SuggestionAttachment.class);
        var sourceAttachments = diagnostic.getAttachments(SourceAttachment.class);

        boolean printedAnyBlock = false;

        for (var sourceAttachment : sourceAttachments) {
            if (printedAnyBlock) {
                outputStream.println();
            }

            System.out.println();
            AnsiConsole.pushColor(outputStream, AnsiColor.GRAY);
            AnsiConsole.print(outputStream, " --> ");
            AnsiConsole.popColor(outputStream);
            AnsiConsole.pushColor(outputStream, AnsiColor.CYAN);
            AnsiConsole.println(outputStream, sourceAttachment.source().name());
            AnsiConsole.popColor(outputStream);

            printedAnyBlock = true;
        }

        if (!primarySnippets.isEmpty()) {
            outputStream.println();
            for (int i = 0; i < primarySnippets.size(); i++) {
                var snippet = primarySnippets.get(i);
                if (snippet.span() == null) {
                    continue;
                }

                printSpanSnippet(outputStream, snippet.span().span(), snippet.label(), indentSize, severityColor(diagnostic.severity()));
                printedAnyBlock = true;
                if (i != primarySnippets.size() - 1) {
                    outputStream.println();
                }
            }
        }

        if (!relatedSnippets.isEmpty()) {
            if (printedAnyBlock) {
                outputStream.println();
            } else {
                outputStream.println();
            }

            for (int i = 0; i < relatedSnippets.size(); i++) {
                var related = relatedSnippets.get(i);

                printPrefixedMessage(outputStream, diagnostic.severity(), "note", related.label());
                outputStream.println();

                if (related.hasSpan()) {
                    printSpanSnippet(outputStream, related.span().span(), "", indentSize, AnsiColor.CYAN);
                } else {
                    AnsiConsole.pushColor(outputStream, AnsiColor.GRAY);
                    AnsiConsole.println(outputStream, "   at " + related.location().location().describe());
                    AnsiConsole.popColor();
                }

                printedAnyBlock = true;
                if (i != relatedSnippets.size() - 1) {
                    outputStream.println();
                }
            }
        }

        for (var suggestion : suggestionsMessages) {
            if (printedAnyBlock) {
                outputStream.println();
                printedAnyBlock = false;
            }

            if (suggestion.getSuggestions().isEmpty()) {
                continue;
            }

            printPrefixedMessage(outputStream, diagnostic.severity(), "help", suggestion.getLabel());
            for (String suggestionText : suggestion.getSuggestions()) {
                printPrefixedMessage(outputStream, diagnostic.severity(), "-", suggestionText);
            }
        }

        for (var note : noteMessages) {
            if (printedAnyBlock) {
                outputStream.println();
                printedAnyBlock = false;
            }
            printPrefixedMessage(outputStream, diagnostic.severity(), "note", note.message());
        }

        for (var help : helpMessages) {
            if (printedAnyBlock) {
                outputStream.println();
                printedAnyBlock = false;
            }
            printPrefixedMessage(outputStream, diagnostic.severity(), "help", help.message());
        }

        outputStream.println();
    }

    private static void printHeader(PrintStream outputStream, Diagnostic diagnostic) {
        String severityText = switch (diagnostic.severity()) {
            case ERROR ->
                "error";
            case WARNING ->
                "warning";
            case INFO ->
                "info";
        };

        AnsiColor severityColor = severityColor(diagnostic.severity());

        AnsiConsole.pushColor(outputStream, severityColor, AnsiColor.BOLD);
        AnsiConsole.print(outputStream, severityText + ": ");
        AnsiConsole.popColor(outputStream, 2);

        var keyword = diagnostic.code().keyword();
        if (keyword != null) {
            AnsiConsole.pushColor(outputStream, AnsiColor.CYAN, AnsiColor.BOLD);
            AnsiConsole.print(outputStream, keyword.text());
            AnsiConsole.popColor(outputStream, 2);
            AnsiConsole.print(outputStream, ": ");
        }

        // AnsiConsole.print(outputStream, "[");
        // AnsiConsole.pushColor(outputStream, AnsiColor.CYAN, AnsiColor.BOLD);
        // AnsiConsole.print(outputStream, diagnosticCode(diagnostic.code()));
        // AnsiConsole.popColor(outputStream, 2);
        // AnsiConsole.print(outputStream, "]: ");
        AnsiConsole.println(outputStream, diagnostic.message());
    }

    private static AnsiColor severityColor(DiagnosticSeverity severity) {
        return switch (severity) {
            case ERROR ->
                AnsiColor.RED;
            case WARNING ->
                AnsiColor.YELLOW;
            case INFO ->
                AnsiColor.BLUE;
        };
    }

    private static void printPrefixedMessage(PrintStream outputStream, DiagnosticSeverity severity, String prefix, String message) {
        if (prefix == null) {
            prefix = "";
        }
        if (message == null) {
            message = "";
        }

        AnsiColor prefixColor = switch (prefix) {
            case "help" ->
                AnsiColor.GREEN;
            case "note" ->
                AnsiColor.MAGENTA;
            case "source" ->
                AnsiColor.BLUE;
            case ">" ->
                AnsiColor.CYAN;
            case "-" -> {
                prefix = "  " + prefix; // indent suggestions
                yield AnsiColor.CYAN;
            }
            default ->
                severityColor(severity);
        };

        AnsiConsole.pushColor(outputStream, prefixColor, AnsiColor.BOLD);
        AnsiConsole.print(outputStream, prefix);
        AnsiConsole.popColor(outputStream, 2);
        AnsiConsole.println(outputStream, ": " + message);
    }

    private static String expandTabs(String text, int indentSize) {
        return text.replace("\t", " ".repeat(indentSize));
    }

    private static int computeVisualColumn(String rawLine, int rawColumn, int indentSize) {
        int clampedRawColumn = Math.max(1, rawColumn);
        int endExclusive = Math.min(clampedRawColumn - 1, rawLine.length());

        int visualColumn = 1;
        for (int i = 0; i < endExclusive; i++) {
            char c = rawLine.charAt(i);
            visualColumn += (c == '\t') ? indentSize : 1;
        }
        return visualColumn;
    }

    private static int digitCount(int value) {
        int n = Math.abs(value);
        int digits = 1;
        while (n >= 10) {
            n /= 10;
            digits++;
        }
        return digits;
    }

    private static void printSpanSnippet(PrintStream outputStream, SourceSpan span, String label, int indentSize, AnsiColor pointerColor) {
        TextSource source = span.source();
        SourceLocation start = span.loc();
        SourceLocation end = span.endLoc();

        int lineNumber = start.line();
        int startCol = start.col();

        String rawLine = source.getLine(lineNumber);
        String lineContent = expandTabs(rawLine, indentSize);

        int lineNumberWidth = digitCount(lineNumber);
        String blankGutter = " ".repeat(lineNumberWidth) + " |";

        AnsiConsole.pushColor(outputStream, AnsiColor.GRAY);
        AnsiConsole.print(outputStream, " --> ");
        AnsiConsole.popColor(outputStream);
        AnsiConsole.pushColor(outputStream, AnsiColor.CYAN);
        AnsiConsole.println(outputStream, start.toString());
        AnsiConsole.popColor(outputStream);

        AnsiConsole.pushColor(outputStream, AnsiColor.GRAY);
        AnsiConsole.println(outputStream, blankGutter);
        AnsiConsole.popColor(outputStream);

        String lineNo = String.format("%" + lineNumberWidth + "d", lineNumber);
        AnsiConsole.pushColor(outputStream, AnsiColor.GRAY);
        AnsiConsole.print(outputStream, lineNo);
        AnsiConsole.print(outputStream, " | ");
        AnsiConsole.popColor(outputStream);
        AnsiConsole.println(outputStream, lineContent);

        int endCol;
        if (end.line() != start.line()) {
            endCol = rawLine.length() + 1;
        } else {
            endCol = Math.max(startCol, end.col());
        }

        int visualStartCol = computeVisualColumn(rawLine, startCol, indentSize);
        int visualEndCol = computeVisualColumn(rawLine, endCol, indentSize);
        int underlineWidth = Math.max(1, visualEndCol - visualStartCol);

        // Pointer line (gutter + spaces + carets + optional label)
        AnsiConsole.pushColor(outputStream, AnsiColor.GRAY);
        AnsiConsole.print(outputStream, blankGutter);
        AnsiConsole.print(outputStream, " ");
        AnsiConsole.popColor(outputStream);

        AnsiConsole.print(outputStream, " ".repeat(Math.max(0, visualStartCol - 1)));
        AnsiConsole.pushColor(outputStream, pointerColor, AnsiColor.BOLD);
        AnsiConsole.print(outputStream, "^".repeat(underlineWidth));
        AnsiConsole.popColor(outputStream, 2);

        if (label != null && !label.isBlank()) {
            AnsiConsole.print(outputStream, " ");
            AnsiConsole.println(outputStream, label);
        } else {
            AnsiConsole.println(outputStream, "");
        }

        AnsiConsole.pushColor(outputStream, AnsiColor.GRAY);
        AnsiConsole.println(outputStream, blankGutter);
        AnsiConsole.popColor(outputStream);
    }

    public static void printError(String message) {
        AnsiConsole.pushColor(AnsiColor.RED);
        AnsiConsole.print(System.err, "error: ");
        AnsiConsole.popColor();

        AnsiConsole.println(System.err, message);
    }

    public static void printSuccess(String message) {
        AnsiConsole.pushColor(AnsiColor.GREEN);
        AnsiConsole.println(System.out, message);
        AnsiConsole.popColor();
    }

    public static void printWarning(String message) {
        AnsiConsole.pushColor(AnsiColor.YELLOW);
        AnsiConsole.print(System.out, "warning: ");
        AnsiConsole.popColor();

        AnsiConsole.println(System.out, message);
    }

    public static void printInfo(String message) {
        AnsiConsole.println(System.out, message);
    }
}
