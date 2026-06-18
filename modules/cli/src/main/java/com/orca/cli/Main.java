package com.orca.cli;

import com.orca.compiler.core.*;
import com.orca.compiler.core.io.*;
import com.orca.compiler.core.diagnostics.Diagnostic;

public class Main {
    private static CompilerArguments compilerArgs;
    private static CompilationContext compilationContext;

    public static void main(String[] args) {
        try {
            compilerArgs = CompilerArguments.fromArgs(args);
            compilationContext = new CompilationContext(compilerArgs);

            if (compilerArgs.sourcesCount() == 0) {
                printUsage();
                handleFailure("invalid arguments", "No input file provided.");
            }

            CompilationPipeline pipeline = new CompilationPipeline(compilationContext);

            int exitCode = compilerArgs.hasFlag(CompilerFlag.EXECUTE_COMPILED_PROGRAM)
                ? pipeline.compileAndRun()
                : (pipeline.compile() ? 0 : 1);

            if (exitCode != 0) {
                // Print collected diagnostics
                var diagnostics = compilationContext.diagnostics();
                int errorsCount = diagnostics.countErrors();
                int warningsCount = diagnostics.countWarnings();

                for (Diagnostic diagnostic : diagnostics) {
                    DiagnosticPrinter.printDiagnostic(diagnostic, compilerArgs.getIndentSize());
                }

                handleFailure("Compilation failed with " + errorsCount + " error(s)" + (warningsCount > 0 ? " and " + warningsCount + " warning(s)" : ""), exitCode);
            }

            handleSuccess();
        }
        catch (Exception e) {
            e.printStackTrace();
            handleFailure("unexpected error", e.getMessage());
        }
    }

    public static CompilerArguments args() {
        return compilerArgs;
    }

    private static void printUsage() {
        System.out.println(CompilerArguments.getUsage());
    }

    public static void printDebug(String message) {
        if (compilerArgs != null && compilerArgs.hasFlag(CompilerFlag.DEBUG)) {
            System.out.println("[DEBUG] " + message);
        }
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

    private static void handleSuccess() {
        handleSuccess(null);
    }

    private static void handleSuccess(String message) {
        if (compilationContext.isSilentMode()) {
            System.exit(0);
            return;
        }

        AnsiConsole.pushColor(AnsiColor.GREEN, AnsiColor.BOLD);
        if (message != null && !message.isEmpty()) {
            System.out.println(message);
        } else {
            if (compilationContext.isExecuteMode()) {
                System.out.println("Program executed successfully.");
            } else {
                System.out.println("Compilation completed successfully.");
            }
        }
        AnsiConsole.popColor(2);

        System.exit(0);
    }
}
