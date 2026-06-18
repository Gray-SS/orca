package com.orca.compiler.core;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.orca.compiler.core.diagnostics.DiagnosticFactory;
import com.orca.compiler.core.io.AnsiColor;
import com.orca.compiler.core.io.AnsiConsole;
import com.orca.compiler.core.text.FileSource;
import com.orca.compiler.core.text.TextSource;

public class CompilerArguments {

    private String outputFile = "out/program.jar";
    private int flagsMask;
    private int indentSize = 2;
    private final List<TextSource> sources = new ArrayList<>();
    private final List<Path> classPaths = new ArrayList<>();

    public void enableFlag(CompilerFlag flag) {
        this.flagsMask |= flag.mask;
    }

    public void disableFlag(CompilerFlag flag) {
        this.flagsMask &= ~flag.mask;
    }

    public boolean hasFlag(CompilerFlag flag) {
        return (flagsMask & flag.mask) != 0;
    }

    public void setIndentSize(int indentSize) {
        this.indentSize = indentSize;
    }

    public int getIndentSize() {
        return this.indentSize;
    }

    public void addSource(TextSource source) {
        this.sources.add(source);
    }

    public void addSources(List<? extends TextSource> sources) {
        this.sources.addAll(sources);
    }

    public void addClassPath(String entry) {
        if (entry.isBlank()) {
            throw new IllegalArgumentException("Jar source cannot be blank");
        }

        var path = Path.of(entry);
        if (!Files.exists(path)) {
            throw new IllegalArgumentException("Jar source does not exist: " + entry);
        }

        try {
            this.classPaths.add(path);
        } catch (Exception e) {
            throw new IllegalArgumentException("Invalid jar source: " + entry, e);
        }
    }

    public Set<Path> getClassPaths() {
        return new HashSet<>(classPaths);
    }

    public List<TextSource> getSources() {
        return new ArrayList<>(sources);
    }

    public int sourcesCount() {
        return sources.size();
    }

    public void setOutputFile(String outputFile) {
        this.outputFile = outputFile;
    }

    public String getOutputFile() {
        return outputFile;
    }

    public void dump() {
        var indent = " ".repeat(indentSize);

        System.out.println("Compiler Arguments:");

        AnsiConsole.pushColor(AnsiColor.GRAY);
        System.out.println(indent + "Sources: " + String.join(",", sources.stream().map(TextSource::name).toList()));
        System.out.println(indent + "Output File: " + outputFile);
        System.out.println(indent + "Indent Size: " + indentSize);
        System.out.println(indent + "Flags:");

        for (CompilerFlag flag : CompilerFlag.values()) {
            System.out.print(indent.repeat(2) + flag.name() + ": ");
            if (hasFlag(flag)) {
                AnsiConsole.pushColor(AnsiColor.GREEN);
                System.out.print("enabled");
                AnsiConsole.popColor();
            } else {
                System.out.print("disabled");
            }

            System.out.println();
        }

        AnsiConsole.popColor();
    }

    public static String getUsage() {
        return """
    Usage: my-beautiful-compiler [options] <input-file>

    Options:
        -debug                  Enable debug mode (prints additional debug information during compilation)
        -lexer                  Stop after lexer phase (print tokens after lexer phase if successful)
        -parser                 Stop after parser phase (print AST after parser phase if successful)
        -print-tokens           Print tokens after lexer phase
        -print-ast              Print AST after parser phase
        -print-lbt              Print lowered bound tree after binding phase
        -indent-size <n>        Set indent size for AST printing (default: 2)
        -o <path>               Set output path (default: "out/program.jar"; use a .class path to emit loose class files)
        -execute                Execute the compiled program after compilation
        -silent                 Run in silent mode (suppress output)
        -print-symbols          Print the symbol hierarchy after binding phase
        -allow-implicit-main    Allow main function to be implicit (default: false)
        -cp <path>              Add a classpath entry for resolving external JVM symbols (can be used multiple times)
        """;
    }

    /**
     * Parses command-line arguments to create a CompilerArguments instance.
     *
     * @param args the command-line arguments
     * @return a CompilerArguments instance with the parsed values
     * @throws IllegalArgumentException if an unexpected argument is encountered
     * or if multiple input file paths are provided
     */
    public static CompilerArguments fromArgs(String[] args) throws IllegalArgumentException {
        CompilerArguments compilerArgs = new CompilerArguments();

        for (int i = 0; i < args.length; i++) {
            switch (args[i]) {
                case "-debug" ->
                    compilerArgs.enableFlag(CompilerFlag.DEBUG);
                case "-lexer" ->
                    compilerArgs.enableFlag(CompilerFlag.STOP_AFTER_LEXER);
                case "-parser" ->
                    compilerArgs.enableFlag(CompilerFlag.STOP_AFTER_PARSER);
                case "-print-tokens" ->
                    compilerArgs.enableFlag(CompilerFlag.PRINT_TOKENS);
                case "-print-ast" ->
                    compilerArgs.enableFlag(CompilerFlag.PRINT_AST);
                case "-print-lbt" ->
                    compilerArgs.enableFlag(CompilerFlag.PRINT_LOWERED_BOUND_TREE);
                case "-print-symbols" ->
                    compilerArgs.enableFlag(CompilerFlag.PRINT_SYMBOL_HIERARCHY);
                case "-indent-size" -> {
                    parseIntValue(compilerArgs, args, i);
                    i++; // skip the value since it's already parsed
                }
                case "-o" -> {
                    parseStringValue(compilerArgs, args, i);
                    i++;
                }
                case "-execute" ->
                    compilerArgs.enableFlag(CompilerFlag.EXECUTE_COMPILED_PROGRAM);
                case "-silent" ->
                    compilerArgs.enableFlag(CompilerFlag.SILENT_MODE);
                case "-library" ->
                    compilerArgs.enableFlag(CompilerFlag.LIBRARY_MODE);
                case "-allow-implicit-main" ->
                    compilerArgs.enableFlag(CompilerFlag.ALLOW_IMPLICIT_MAIN);
                case "-cp" -> {
                    parseClasspathValue(compilerArgs, args, i);
                    i++;
                }
                default -> {
                    if (args[i].startsWith("-")) {
                        throw new IllegalArgumentException("Unexpected argument: " + args[i]);
                    }

                    // positional argument for input file path
                    var sourcePath = CompilerPaths.getFromCwd(args[i]);
                    if (!Files.exists(sourcePath)) {
                        throw CompilerException.wrap(DiagnosticFactory.inputFileNotFound(sourcePath));
                    }

                    if (Files.isDirectory(sourcePath)) {
                        // Handle directory input by adding all files in the directory and subdirectories as sources
                        try (var files = Files.walk(sourcePath)) {
                            files.filter((f) -> Files.isRegularFile(f) && f.toString().endsWith(".orca"))
                                    .forEach(file -> compilerArgs.addSource(new FileSource(file.toString())));
                        } catch (IOException e) {
                            throw CompilerException.wrap(DiagnosticFactory.inputIoError("Failed to read input directory: " + e.getMessage()));
                        }
                    } else {
                        compilerArgs.addSource(new FileSource(sourcePath.toString()));
                    }
                }
            }
        }

        return compilerArgs;
    }

    private static void parseClasspathValue(CompilerArguments compilerArgs, String[] args, int index) {
        if (index + 1 >= args.length) {
            throw new IllegalArgumentException("Expected a value after " + args[index]);
        }

        String[] entries = args[index + 1].split(":");
        for (String entry : entries) {
            if (entry.isBlank()) {
                throw new IllegalArgumentException("Invalid classpath entry: '" + entry + "'");
            }

            compilerArgs.addClassPath(entry);
        }
    }

    private static void parseIntValue(CompilerArguments compilerArgs, String[] args, int index) {
        if (index + 1 >= args.length) {
            throw new IllegalArgumentException("Expected a value after " + args[index]);
        }

        try {
            int indentSize = Integer.parseInt(args[index + 1]);
            compilerArgs.setIndentSize(indentSize);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Invalid indent size: " + args[index + 1]);
        }
    }

    private static void parseStringValue(CompilerArguments compilerArgs, String[] args, int index) {
        if (index + 1 >= args.length) {
            throw new IllegalArgumentException("Expected a value after " + args[index]);
        }

        String v = args[index + 1];
        if (v.isBlank()) {
            throw new IllegalArgumentException("Invalid value for " + args[index] + ": '" + v + "'");
        }

        if ("-o".equals(args[index])) {
            compilerArgs.setOutputFile(v);
            return;
        }

        throw new IllegalArgumentException("Unexpected string option: " + args[index]);
    }
}
