package com.orca.cli;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;

import com.orca.compiler.core.CompilerException;
import com.orca.compiler.core.CompilerOptions;
import com.orca.compiler.core.CompilerPaths;
import com.orca.compiler.core.CompilerValidationException;
import com.orca.compiler.core.OutputFormat;
import com.orca.compiler.core.OutputKind;
import com.orca.compiler.core.diagnostics.DiagnosticFactory;
import com.orca.compiler.core.externals.ClassPath;

public class CommandLineParser {

    /**
     * The path separator used in the classpath, which is platform-dependent.
     */
    public static final String CLASS_PATH_SEPARATOR = File.pathSeparator;

    /**
     * Parses command-line arguments to create a CompilerOption instance.
     *
     * @param args the command-line arguments
     * @return a CompilerOption instance with the parsed values
     * @throws IllegalArgumentException if an unexpected argument is encountered
     * or if multiple input file paths are provided
     */
    public static CommandLineOptions parse(String[] args) throws IllegalArgumentException, CompilerValidationException.InvalidSourceException {
        var cliBuilder = CommandLineOptions.Builder.builder();
        var compilerBuilder = CompilerOptions.builder();

        var context = new Context(args);
        while (context.hasNext()) {
            String arg = context.next();
            switch (arg) {
                case "-h", "--help" -> {
                    cliBuilder.setShowHelp(true);
                    break;
                }
                case "-v", "--version" -> {
                    cliBuilder.setShowVersion(true);
                    break;
                }
                case "--stack-trace" ->
                    cliBuilder.setShowStackTrace(true);
                case "--emit-tokens" ->
                    cliBuilder.setEmitKind(EmitKind.TOKENS);
                case "--emit-ast" ->
                    cliBuilder.setEmitKind(EmitKind.AST);
                case "--emit-ir" ->
                    cliBuilder.setEmitKind(EmitKind.IR);
                case "--emit-symbols" ->
                    cliBuilder.setEmitKind(EmitKind.SYMBOLS);
                case "--emit-bytecode" ->
                    cliBuilder.setEmitKind(EmitKind.BYTECODE);
                case "--indent-size" -> {
                    var indentSize = context.nextInt();
                    cliBuilder.setIndentSize(indentSize);
                }
                case "-o", "--output" -> {
                    var outputPath = context.next();

                    if (outputPath.isBlank()) {
                        throw new IllegalArgumentException("Invalid output path: '" + outputPath + "'");
                    }

                    compilerBuilder.setOutputPath(CompilerPaths.getFromCwd(outputPath));
                }
                case "--output-kind" -> {
                    var outputKindStr = context.next();
                    switch (outputKindStr) {
                        case "app" ->
                            compilerBuilder.setOutputKind(OutputKind.APPLICATION);
                        case "lib" ->
                            compilerBuilder.setOutputKind(OutputKind.LIBRARY);
                        default ->
                            throw new IllegalArgumentException("Invalid output kind: '" + outputKindStr + "'");
                    }
                }
                case "--output-format" -> {
                    var outputFormat = context.next();
                    switch (outputFormat) {
                        case "jar" ->
                            compilerBuilder.setOutputFormat(OutputFormat.JAR);
                        case "class" ->
                            compilerBuilder.setOutputFormat(OutputFormat.CLASS_DIRECTORY);
                        default ->
                            throw new IllegalArgumentException("Invalid output format: '" + outputFormat + "'");
                    }
                }
                case "--execute" ->
                    cliBuilder.setExecuteCompiledProgram(true);
                case "--cp" -> {
                    var classpathEntry = context.next();
                    if (classpathEntry.isBlank()) {
                        throw new IllegalArgumentException("Invalid classpath entry: '" + classpathEntry + "'");
                    }

                    String[] entries = classpathEntry.split(File.pathSeparator);
                    for (String entry : entries) {
                        if (entry.isBlank()) {
                            throw new IllegalArgumentException("Invalid classpath entry: '" + entry + "'");
                        }

                        Path entryPath = CompilerPaths.getFromCwd(entry);
                        if (!Files.exists(entryPath)) {
                            throw new IllegalArgumentException("Classpath entry does not exist: '" + entry + "'");
                        }

                        if (Files.isDirectory(entryPath)) {
                            throw new IllegalArgumentException("Classpath entry must be a file, not a directory: '" + entry + "'");
                        }

                        if (!entry.endsWith(".jar")) {
                            throw new IllegalArgumentException("Classpath entry must be a .jar file: '" + entry + "'");
                        }

                        var classPathEntry = ClassPath.of(entryPath);
                        compilerBuilder.addClassPath(classPathEntry);
                    }
                }
                default -> {
                    if (arg.startsWith("-")) {
                        throw new IllegalArgumentException("Unexpected argument: " + arg);
                    }

                    // positional argument for input file path
                    var sourcePath = CompilerPaths.getFromCwd(arg);
                    if (!Files.exists(sourcePath)) {
                        throw CompilerException.wrap(DiagnosticFactory.inputFileNotFound(sourcePath));
                    }

                    if (Files.isDirectory(sourcePath)) {
                        compilerBuilder.addSourceDirectory(sourcePath);
                    } else {
                        compilerBuilder.addSourceFile(sourcePath);
                    }
                }
            }
        }

        var compilerOptions = compilerBuilder.build();
        return cliBuilder.build(compilerOptions);
    }

    private static class Context {

        private int index;
        private final String[] args;

        public Context(String[] args) {
            this.args = args;
            this.index = 0;
        }

        public boolean hasNext() {
            return index < args.length;
        }

        public String next() {
            if (!hasNext()) {
                throw new IllegalStateException("No more arguments available");
            }
            return args[index++];
        }

        public int nextInt() {
            if (!hasNext()) {
                throw new IllegalStateException("No more arguments available");
            }
            try {
                return Integer.parseInt(args[index++]);
            } catch (NumberFormatException e) {
                throw new IllegalArgumentException("Expected an integer value but got: " + args[index - 1]);
            }
        }
    }
}
