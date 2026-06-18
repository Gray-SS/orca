package com.orca.compiler.core;

import java.nio.file.Path;

import com.orca.compiler.core.diagnostics.DiagnosticCollector;

/**
 * Contexte partagé entre les phases de compilation. Encapsule la configuration
 * et les services communs.
 */
public class CompilationContext {

    private static final String ENTRY_CLASS_NAME = "OrcaEntry";

    private final Logger logger;
    private final CompilerArguments arguments;
    private final DiagnosticCollector diagnosticCollector;

    public CompilationContext(CompilerArguments arguments) {
        this.logger = new Logger(this);
        this.arguments = arguments;
        this.diagnosticCollector = new DiagnosticCollector();
    }

    public Logger logger() {
        return logger;
    }

    /**
     * Retourne les arguments du compilateur.
     */
    public CompilerArguments arguments() {
        return arguments;
    }

    /**
     * Retourne le collecteur de diagnostics.
     */
    public DiagnosticCollector diagnostics() {
        return diagnosticCollector;
    }

    public boolean isSilentMode() {
        return hasFlag(CompilerFlag.SILENT_MODE);
    }

    public boolean isDebugMode() {
        return hasFlag(CompilerFlag.DEBUG);
    }

    public boolean isExecuteMode() {
        return hasFlag(CompilerFlag.EXECUTE_COMPILED_PROGRAM);
    }

    public String getMainClassName() {
        return ENTRY_CLASS_NAME;
    }

    public Path getOutputPath() {
        String outputFile = arguments.getOutputFile();
        if (outputFile == null || outputFile.isBlank()) {
            return Path.of(ENTRY_CLASS_NAME + ".class");
        }

        if (outputFile.endsWith(".jar")) {
            return Path.of(outputFile);
        }

        // The emitter writes multiple class files into a directory.
        // Resolve the synthesized entry class within that directory.
        Path p = Path.of(outputFile);
        Path outDir = outputFile.endsWith("/") ? p : p.getParent();
        return (outDir != null ? outDir : Path.of(".")).resolve(ENTRY_CLASS_NAME + ".class");
    }

    /**
     * Vérifie s'un flag est activé.
     */
    public boolean hasFlag(CompilerFlag flag) {
        return arguments.hasFlag(flag);
    }

    /**
     * Retourne la taille d'indentation pour le debug.
     */
    public int getIndentSize() {
        return arguments.getIndentSize();
    }

}
