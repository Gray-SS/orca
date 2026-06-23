package com.orca.cli;

import com.orca.compiler.core.Compilation;
import com.orca.compiler.core.CompilerConstants;
import com.orca.compiler.core.CompilerValidationException;
import com.orca.compiler.core.diagnostics.Diagnostic;
import com.orca.compiler.core.io.AnsiColor;
import com.orca.compiler.core.io.AnsiConsole;
import com.orca.compiler.core.io.DiagnosticPrinter;

public class Main {

    public static void main(String[] args) {
        Exception exception = null;
        var options = parseCommandLineArguments(args);

        try {
            int exitCode = execute(options);
            System.exit(exitCode);
        } catch (ProgramExecutionException e) {
            handleFailure("program execution failed", e.getMessage());
            exception = e;
        } catch (Exception e) {
            handleFailure("unexpected error", e.getMessage());
            exception = e;
        } finally {
            if (exception != null && options.shouldShowStackTrace()) {
                exception.printStackTrace();
            }
        }
    }

    private static CommandLineOptions parseCommandLineArguments(String[] args) {
        try {
            return CommandLineParser.parse(args);
        } catch (IllegalArgumentException e) {
            handleFailure("invalid arguments", e.getMessage());
        } catch (CompilerValidationException.InvalidSourceException e) {
            handleFailure("invalid source", e.getMessage());
        }

        // Needed to satisfy the compiler, but this line should never be reached due to System.exit calls in handleFailure.
        return null;
    }

    private static int execute(CommandLineOptions options) throws Exception, ProgramExecutionException {
        var compilerOptions = options.getCompilerOptions();
        if (options.shouldShowHelp()) {
            printUsage();
            return 0;
        }

        if (options.shouldShowVersion()) {
            System.out.println(CompilerConstants.LANGUAGE_NAME + " v" + CompilerConstants.LANGUAGE_VERSION);
            return 0;
        }

        if (compilerOptions.sourcesCount() == 0) {
            handleFailure("invalid arguments", "No input file provided.");
            return 1;
        }

        var compilation = new Compilation(compilerOptions);
        var compilationResult = compilation.compile();

        var diagnostics = compilationResult.diagnostics();
        for (Diagnostic diagnostic : diagnostics) {
            DiagnosticPrinter.printDiagnostic(diagnostic, options.getIndentSize());
        }

        int errorsCount = diagnostics.countErrors();
        int warningsCount = diagnostics.countWarnings();

        if (errorsCount > 0) {
            var sb = new StringBuilder("Compilation failed with ")
                    .append(errorsCount).append(" error(s)");
            if (warningsCount > 0) {
                sb.append(" and ").append(warningsCount).append(" warning(s)");
            }
            handleFailure(sb.append(".").toString(), 1);
        }

        if (options.shouldExecuteCompiledProgram()) {
            return ProgramRunner.run(compilerOptions);
        }

        var sb = new StringBuilder("Compilation completed successfully");
        if (warningsCount > 0) {
            sb.append(" with ").append(warningsCount).append(" warning(s)");
        }

        handleSuccess(sb.append(".").toString());
        return 0;
    }

    private static void printUsage() {
        System.out.println(getUsage());
    }

    public static String getUsage() {
        return """
        Usage: %s [options] <input>

        <input> can be a single .orca file or a directory (compiled recursively).

        Output:
            -o, --output <path>           Output path (default: output.jar for jar, output/ for class)
            --output-kind <app|lib>       Kind of artifact to produce (default: app)
                                            app  — executable with entry point, emits Main-Class in manifest
                                            lib  — library, no entry point required
            --output-format <jar|class>   How to write the compiled output (default: jar)
                                            jar  — single JAR file
                                            class — directory of loose .class files

        Compilation:
            --cp <path>                   Add a classpath entry for external JVM symbols.
                                          Can be used multiple times or separated by '%s'.
            --execute                     Execute the compiled program after compilation.

        Inspection:
            --emit-tokens                 Run only the lexer and print tokens, then stop.
            --emit-ast                    Run through parsing and print the AST, then stop.
            --emit-ir                     Run through binding and print the IR, then stop.
            --emit-symbols                Run through binding and print the symbol hierarchy, then stop.
            --emit-bytecode               Compile fully and print the emitted bytecode, then stop.
            --indent-size <n>             Indentation width for --emit-* output (default: 4).
            --stack-trace                 Show stack trace for errors.

        General:
            -h, --help                    Show this message and exit.
            -v, --version                 Show the compiler version and exit.
        """.formatted(CompilerConstants.LANGUAGE_NAME, CommandLineParser.CLASS_PATH_SEPARATOR);
    }

    private static void handleFailure(String message, int exitCode) {
        AnsiConsole.pushColor(System.err, AnsiColor.RED, AnsiColor.BOLD);
        System.err.println(message);
        AnsiConsole.popColor(System.err, 2);

        System.exit(exitCode);
    }

    private static void handleFailure(String reason, String message) {
        AnsiConsole.pushColor(System.err, AnsiColor.RED, AnsiColor.BOLD);
        System.err.print(reason + ": ");
        AnsiConsole.popColor(System.err, 2);

        if (message != null && !message.isEmpty()) {
            System.err.println(message);
        }

        System.exit(1);
    }

    private static void handleSuccess(String message) {
        AnsiConsole.pushColor(AnsiColor.GREEN, AnsiColor.BOLD);
        System.out.println(message);
        AnsiConsole.popColor(2);

        System.exit(0);
    }
}
