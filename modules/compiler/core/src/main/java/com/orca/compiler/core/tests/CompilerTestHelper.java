package com.orca.compiler.core.tests;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.function.Consumer;

import com.orca.compiler.core.Compilation;
import com.orca.compiler.core.CompilationResult;
import com.orca.compiler.core.CompilerConstants;
import com.orca.compiler.core.CompilerException;
import com.orca.compiler.core.CompilerOptions;
import com.orca.compiler.core.CompilerPaths;
import com.orca.compiler.core.CompilerValidationException;
import com.orca.compiler.core.OutputFormat;
import com.orca.compiler.core.OutputKind;
import com.orca.compiler.core.boundtree.BoundProgram;
import com.orca.compiler.core.diagnostics.DiagnosticBag;
import com.orca.compiler.core.diagnostics.DiagnosticCode;
import com.orca.compiler.core.diagnostics.DiagnosticCollector;
import com.orca.compiler.core.diagnostics.DiagnosticFactory;
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

    public static CompilerOptions createOptions(Consumer<CompilerOptions.Builder> configureArgs, String... sources) {
        var builder = CompilerOptions.builder();
        for (String source : sources) {
            builder.addSource(new StringSource(source));
        }

        try {
            var stdLibPath = CompilerPaths.getFromCwd("../../stdlib/src");
            builder.addSourceDirectory(stdLibPath);
        } catch (CompilerValidationException.InvalidSourceException e) {
            throw new RuntimeException("Failed to add standard library source directory: " + e.getMessage(), e);
        }

        if (configureArgs != null) {
            configureArgs.accept(builder);
        }
        return builder.build();
    }

    public static CompilerOptions createLibraryOptions(Consumer<CompilerOptions.Builder> configureArgs, String... sources) {
        return createOptions(builder -> {
            builder.setOutputKind(OutputKind.LIBRARY);

            if (configureArgs != null) {
                configureArgs.accept(builder);
            }
        }, sources);
    }

    public static Compilation createAppCompilation(Consumer<CompilerOptions.Builder> configureArgs, String... sources) {
        var options = createOptions(configureArgs, sources);
        return new Compilation(options);
    }

    public static Compilation createLibraryCompilation(Consumer<CompilerOptions.Builder> configureArgs, String... sources) {
        var options = createLibraryOptions(configureArgs, sources);
        return new Compilation(options);
    }

    public static SyntaxTree parseSyntaxTree(String source) throws Exception {
        return SyntaxTree.parse(new StringSource(source));
    }

    public static CompilationUnit parseUnit(String source) throws Exception {
        var syntaxTree = parseSyntaxTree(source);
        return (CompilationUnit) syntaxTree.root();
    }

    /**
     * Parses the source string and binds it to produce a BoundProgram. The
     * source needs to omit the 'main' function.
     */
    public static CompilationResult<BoundProgram> parseAndBindApplication(String source) throws Exception {
        var compilation = createAppCompilation(null, source);
        return compilation.getBoundProgram();
    }

    public static CompilationResult<BoundProgram> parseAndBind(String... sources) throws Exception {
        var compilation = createAppCompilation(null, sources);
        return compilation.getBoundProgram();
    }

    public static CompilationResult<BoundProgram> parseAndBindLibrary(String source) throws Exception {
        var compilation = createLibraryCompilation(null, source);
        return compilation.getBoundProgram();
    }

    public static CompilationResult<BoundProgram> parseAndBindLibrary(String... sources) throws Exception {
        var compilation = createLibraryCompilation(null, sources);
        return compilation.getBoundProgram();
    }

    public static boolean hasErrorWithCode(DiagnosticCollector diagnostics, DiagnosticCode code) {
        return diagnostics.diagnostics()
                .stream()
                .filter(d -> d.severity() == DiagnosticSeverity.ERROR && d.code() == code)
                .findFirst()
                .isPresent();
    }

    /**
     * Compiles the source string to JVM bytecode, runs it with the JVM, and
     * returns the trimmed stdout output. The source must contain an explicit
     * 'def main()' function.
     */
    public static CompilationResult<String> compileAndRun(String source) throws Exception {
        return compileAndRun(source, null);
    }

    /**
     * Compiles the source string, runs it, feeding stdinInput to the process's
     * stdin, and returns the trimmed stdout output.
     */
    public static CompilationResult<String> compileAndRun(String source, String stdinInput) throws IOException {
        Path tempDir = Files.createTempDirectory("compiler-codegentest-");
        var compilation = createAppCompilation(builder -> {
            builder.setOutputFormat(OutputFormat.CLASS_DIRECTORY);
            builder.setOutputPath(tempDir);
        }, source);

        var result = compilation.compile();
        if (result.isFailure()) {
            return CompilationResult.failure(result.diagnostics());
        }

        try {
            ProcessBuilder pb = new ProcessBuilder("java", "-cp", tempDir.toString(), CompilerConstants.DEFAULT_ENTRY_CLASS_NAME);
            pb.redirectErrorStream(true);

            Process process = pb.start();

            if (stdinInput != null) {
                try (var out = process.getOutputStream()) {
                    out.flush();
                    out.write(stdinInput.getBytes());
                }
            } else {
                process.getOutputStream().close();
            }

            String stdout = new String(process.getInputStream().readAllBytes());
            process.waitFor();

            return CompilationResult.success(result.diagnostics(), stdout.trim());
        } catch (IOException e) {
            e.printStackTrace();
            return CompilationResult.failure(DiagnosticBag.single(DiagnosticFactory.inputIoError("Failed to run compiled program: " + e.getMessage())));
        } catch (InterruptedException e) {
            e.printStackTrace();
            return CompilationResult.failure(DiagnosticBag.single(DiagnosticFactory.inputIoError("Process was interrupted: " + e.getMessage())));
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
