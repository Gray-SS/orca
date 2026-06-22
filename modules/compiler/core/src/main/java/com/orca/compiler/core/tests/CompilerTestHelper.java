package com.orca.compiler.core.tests;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;

import com.orca.compiler.core.Compilation;
import com.orca.compiler.core.CompilationContext;
import com.orca.compiler.core.CompilationPipeline;
import com.orca.compiler.core.CompilerArguments;
import com.orca.compiler.core.CompilerException;
import com.orca.compiler.core.CompilerFlag;
import com.orca.compiler.core.boundtree.BoundProgram;
import com.orca.compiler.core.diagnostics.DiagnosticCode;
import com.orca.compiler.core.diagnostics.DiagnosticCollector;
import com.orca.compiler.core.diagnostics.DiagnosticSeverity;
import com.orca.compiler.core.diagnostics.attachments.CodeSnippetAttachment;
import com.orca.compiler.core.diagnostics.attachments.HelpAttachment;
import com.orca.compiler.core.diagnostics.attachments.NoteAttachment;
import com.orca.compiler.core.diagnostics.attachments.RelatedAttachment;
import com.orca.compiler.core.diagnostics.attachments.SourceAttachment;
import com.orca.compiler.core.diagnostics.attachments.SuggestionAttachment;
import com.orca.compiler.core.syntax.CompilationUnit;
import com.orca.compiler.core.syntax.SyntaxTree;
import com.orca.compiler.core.text.StringSource;

public class CompilerTestHelper {

    public static CompilerArguments createTestArgs(String... args) {
        return CompilerArguments.fromArgs(args);
    }

    public static CompilationContext createTestContext(String... sources) {
        CompilerArguments args = createTestArgs();
        for (String source : sources) {
            args.addSource(new StringSource(source));
        }

        return new CompilationContext(args);
    }

    public static String wrapInMain(String source) {
        return String.format("""
            package test;

            def main() {
                %s
            }
        """, source);
    }

    public static String appendEmptyMain(String source) {
        return String.format("""
            package test;

            %s

            def main() { }
        """, source);
    }

    public static String formatSource(String source) {
        return String.format("""
            package test;

            %s
        """, source);
    }

    public static SyntaxTree parseSyntaxTree(String source) throws Exception {
        var collector = new DiagnosticCollector();
        return SyntaxTree.parse(collector, new StringSource(source));
    }

    public static CompilationUnit parseUnit(String source) throws Exception {
        var syntaxTree = parseSyntaxTree(source);
        return (CompilationUnit) syntaxTree.root();
    }

    /**
     * Parses the source string and binds it to produce a BoundProgram. The
     * source needs to omit the 'main' function.
     */
    public static BoundProgram parseAndBind(String source) throws Exception {
        if (!source.contains("def main()")) {
            assert false : "Source code must contain an explicit 'def main()' function for parseAndBind. Use parseAndBindStrict if you want to allow implicit main functions.";
        }

        var context = createTestContext(source);
        var compilation = new Compilation(context);
        return compilation.getBoundProgram();
    }

    public static BoundProgram parseAndBind(String... sources) throws Exception {
        var context = createTestContext(sources);
        var compilation = new Compilation(context);
        return compilation.getBoundProgram();
    }

    /**
     * Like parseAndBind but requires an explicit 'main' function. Use this to
     * test SEM_MISSING_MAIN_FUNCTION and other strict-mode errors.
     */
    public static BoundProgram parseAndBindStrict(String source) throws Exception {
        var context = createTestContext(source);
        var compilation = new Compilation(context);
        return compilation.getBoundProgram();
    }

    public static DiagnosticCollector getDiagnostics(CompilationContext context) {
        return context.diagnostics();
    }

    public static boolean hasErrors(DiagnosticCollector diagnostics) {
        return diagnostics.hasErrors();
    }

    public static boolean hasErrorWithCode(DiagnosticCollector diagnostics, DiagnosticCode code) {
        return diagnostics.diagnostics().stream()
                .filter(d -> d.severity() == DiagnosticSeverity.ERROR && d.code() == code)
                .findFirst()
                .isPresent();
    }

    /**
     * Compiles the source string to JVM bytecode, runs it with the JVM, and
     * returns the trimmed stdout output. The source must contain an explicit
     * 'def main()' function.
     */
    public static String compileAndRun(String source) throws Exception {
        return compileAndRun(source, null);
    }

    /**
     * Compiles the source string, runs it, feeding stdinInput to the process's
     * stdin, and returns the trimmed stdout output.
     */
    public static String compileAndRun(String source, String stdinInput) throws Exception {
        Path tempDir = Files.createTempDirectory("compiler-codegentest-");
        try {
            CompilerArguments args = createTestArgs();
            args.enableFlag(CompilerFlag.DEBUG);
            args.addSource(new StringSource(source));
            args.setOutputFile(tempDir.resolve("Program.class").toString());

            CompilationContext context = new CompilationContext(args);
            CompilationPipeline pipeline = new CompilationPipeline(context);

            boolean success = pipeline.compile();
            if (!success) {
                throwReadableDiagnosticException(context.diagnostics());
            }

            ProcessBuilder pb = new ProcessBuilder("java", "-cp", tempDir.toString(), context.getMainClassName());
            pb.redirectErrorStream(true);

            Process process = pb.start();

            if (stdinInput != null) {
                try (var out = process.getOutputStream()) {
                    out.write(stdinInput.getBytes());
                }
            } else {
                process.getOutputStream().close();
            }

            String stdout = new String(process.getInputStream().readAllBytes());
            process.waitFor();
            return stdout.trim();
        } catch (IOException e) {
            throw new RuntimeException("Failed to run compiled program: " + e.getMessage(), e);
        } finally {
            deleteDirRecursive(tempDir);
        }
    }

    public static void throwReadableDiagnosticException(CompilerException e) {
        var collector = new DiagnosticCollector();
        collector.report(e.diagnostic());

        throwReadableDiagnosticException(collector);
    }

    public static void throwReadableDiagnosticException(DiagnosticCollector context) {
        StringBuilder sb = new StringBuilder("Compilation failed:\n");
        for (var d : context.diagnostics()) {
            sb.append("  [").append(d.code()).append("] ").append(d.message()).append("\n");
            for (var a : d.attachments()) {
                switch (a) {
                    case CodeSnippetAttachment csa -> {
                        sb.append("label: ").append(csa.label()).append("\n");
                        var span = csa.span().span();
                        sb.append("at:");
                        sb.append(span);
                        sb.append(": ");
                        sb.append("\n");
                        sb.append(span.text());
                        sb.append("\n");
                    }
                    case RelatedAttachment related -> {
                        sb.append("related: ").append(related.label()).append("\n");
                        sb.append("at:");
                        if (related.hasSpan()) {
                            var span = related.span().span();
                            sb.append(span);
                        } else {
                            sb.append(related.location().location().describe());
                        }
                        sb.append(": ");
                        sb.append("\n");
                        if (related.hasSpan()) {
                            var span = related.span().span();
                            sb.append(span.text());
                        }
                        sb.append("\n");
                    }
                    case HelpAttachment help -> {
                        sb.append("help: ").append(help.message()).append("\n");
                    }
                    case NoteAttachment note -> {
                        sb.append("note: ").append(note.message()).append("\n");
                    }
                    case SuggestionAttachment suggestion -> {
                        sb.append("suggestion: ").append(suggestion.getLabel()).append("\n");
                    }
                    case SourceAttachment source -> {
                        sb.append("source: ").append(source.source().name()).append("\n");
                        sb.append(source.label()).append("\n");
                    }
                }
                sb.append("");
            }
        }

        throw new RuntimeException(sb.toString());
    }

    private static void deleteDirRecursive(Path dir) {
        try {
            Files.walk(dir)
                    .sorted(Comparator.reverseOrder())
                    .forEach(p -> {
                        try {
                            Files.delete(p);
                        } catch (IOException ignored) {
                        }
                    });
        } catch (IOException ignored) {
        }
    }
}
