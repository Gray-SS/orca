package com.orca.compiler.core;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.jar.JarFile;
import java.util.stream.Collectors;

import com.orca.compiler.core.boundtree.BoundProgram;
import com.orca.compiler.core.diagnostics.DiagnosticFactory;
import com.orca.compiler.core.phases.BindingPhase;
import com.orca.compiler.core.phases.CodeGenPhase;

/**
 * Pipeline d'orchestration de la compilation. Encapsule toutes les phases et
 * les exécute dans l'ordre.
 */
public class CompilationPipeline {

    private final CompilationContext context;
    private final BindingPhase bindingPhase;
    private final CodeGenPhase codeGenPhase;

    /**
     * Constructeur avec les phases par défaut.
     */
    public CompilationPipeline(CompilationContext context) {
        this(context,
                BindingPhase.defaultInstance(),
                CodeGenPhase.defaultInstance()
        );
    }

    /**
     * Constructeur avec injection des phases (pour les tests).
     */
    public CompilationPipeline(CompilationContext context,
            BindingPhase bindingPhase,
            CodeGenPhase codeGenPhase) {
        this.context = context;
        this.bindingPhase = bindingPhase;
        this.codeGenPhase = codeGenPhase;
    }

    /**
     * Exécute la pipeline complète de compilation.
     *
     * @return true si la compilation a réussi, false sinon
     */
    public boolean compile() {
        try {
            BoundProgram program = bindingPhase.bind(context);
            codeGenPhase.generate(context, program);

            return !context.diagnostics().hasErrors();
        } catch (FileNotFoundException e) {
            context.diagnostics().report(DiagnosticFactory.inputFileNotFound(Path.of(e.getMessage())));
            return false;
        } catch (IOException e) {
            context.diagnostics().report(DiagnosticFactory.inputIoError(e.getMessage()));
            return false;
        } catch (CompilerException e) {
            // L'exception contient déjà un diagnostic
            context.diagnostics().report(e.diagnostic());
            return false;
        } catch (UnsupportedFeatureException e) {
            context.diagnostics().report(DiagnosticFactory.unsupportedFeature(e.getMessage()));
            return false;
        } catch (Exception e) {
            if (context.hasFlag(CompilerFlag.DEBUG)) {
                e.printStackTrace();
            }

            context.diagnostics().report(
                    DiagnosticFactory.unexpectedError(e.getMessage())
            );
            return false;
        }
    }

    /**
     * Compile le programme et l'exécute immédiatement.
     *
     * @return Code de sortie du programme exécuté, ou 1 en cas d'erreur de
     * compilation ou d'exécution
     */
    public int compileAndRun() {
        if (!compile()) {
            context.logger().error("Compilation failed. Program will not be executed.");
            return 1;
        }

        try {
            Path outputPath = context.getOutputPath();
            Set<Path> classPaths = context.arguments().getClassPaths();
            List<String> command = getExecutionCommand(outputPath, classPaths);
            if (command == null) {
                // getExecutionCommand already reported the error
                return 1;
            }

            context.logger().info("Running compiled program...");
            context.logger().debug("Execution command: " + String.join(" ", command));

            ProcessBuilder pb = new ProcessBuilder(command);
            pb.redirectErrorStream(true);
            Process process = pb.start();

            try (var reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    System.out.println(line);
                }
            }

            return process.waitFor();
        } catch (IOException e) {
            context.diagnostics().report(DiagnosticFactory.execError("Failed to execute compiled program: " + e.getMessage()));
            return 1;
        } catch (InterruptedException e) {
            context.diagnostics().report(DiagnosticFactory.execInterrupted());
            Thread.currentThread().interrupt();
            return 1;
        }
    }

    private List<String> getExecutionCommand(Path outputPath, Set<Path> classPaths) {
        String outputPathStr = outputPath.toString();
        context.logger().debug("Determining execution command for output: " + outputPathStr);

        if (outputPathStr.endsWith(".jar")) {
            context.logger().debug("Output is a JAR file. Using 'java -cp' to execute.");
            String mainClass = readMainClassFromJar(outputPath);
            if (mainClass == null) {
                context.diagnostics().report(DiagnosticFactory.execError("No Main-Class attribute found in JAR manifest: " + outputPath));
                return null;
            }

            // output JAR must be first so its classes take precedence over same-named classes in dependencies
            var orderedPaths = new ArrayList<Path>();
            orderedPaths.add(outputPath);
            orderedPaths.addAll(classPaths);

            var cmd = new ArrayList<String>();
            cmd.add("java");
            cmd.addAll(makeClassPathArgs(orderedPaths));
            cmd.add(mainClass);
            return cmd;
        }

        if (outputPathStr.endsWith(".class")) {
            context.logger().debug("Output is a CLASS file. Using 'java -cp' to execute.");
            String fileName = outputPath.getFileName().toString();
            String mainClass = fileName.substring(0, fileName.length() - ".class".length());

            var orderedPaths = new ArrayList<Path>();
            orderedPaths.add(outputPath.getParent());
            orderedPaths.addAll(classPaths);

            var cmd = new ArrayList<String>();
            cmd.add("java");
            cmd.addAll(makeClassPathArgs(orderedPaths));
            cmd.add(mainClass);
            return cmd;
        }

        if (outputPath.toFile().isDirectory()) {
            context.logger().debug("Output is a directory. Cannot execute directly from a directory.");
            context.diagnostics().report(
                    DiagnosticFactory.execError("Cannot infer main class from output directory: " + outputPath)
            );
            return null;
        }

        context.diagnostics().report(
                DiagnosticFactory.execError("Unsupported output format for execution: " + outputPath)
        );
        return null;
    }

    private static String readMainClassFromJar(Path jarPath) {
        try (var jar = new JarFile(jarPath.toFile())) {
            return jar.getManifest().getMainAttributes().getValue("Main-Class");
        } catch (IOException e) {
            return null;
        }
    }

    private static List<String> makeClassPathArgs(List<Path> classPaths) {
        if (classPaths.isEmpty()) {
            return List.of();
        }

        var joined = classPaths.stream()
                .map(Path::toString)
                .collect(Collectors.joining(System.getProperty("path.separator")));
        return List.of("-cp", joined);
    }

    /**
     * Retourne le contexte de compilation.
     */
    public CompilationContext getContext() {
        return context;
    }
}
