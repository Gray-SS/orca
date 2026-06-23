package com.orca.cli;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.jar.JarFile;
import java.util.stream.Collectors;

import com.orca.compiler.core.CompilerOptions;
import com.orca.compiler.core.externals.ClassPath;

public final class ProgramRunner {

    private ProgramRunner() {}

    public static int run(CompilerOptions options) throws ProgramExecutionException {
        List<String> command = buildCommand(options);

        try {
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
            throw new ProgramExecutionException("Failed to launch program: " + e.getMessage(), e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new ProgramExecutionException("Program execution was interrupted", e);
        }
    }

    private static List<String> buildCommand(CompilerOptions options) throws ProgramExecutionException {
        Path outputPath = options.getOutputPath();
        Set<ClassPath> classPaths = options.getClassPaths();

        return switch (options.getOutputFormat()) {
            case JAR -> buildJarCommand(outputPath, classPaths);
            case CLASS_DIRECTORY -> throw new ProgramExecutionException(
                "Cannot execute directly from a class directory '" + outputPath + "'. " +
                "Use --output-format=jar to produce a runnable JAR."
            );
        };
    }

    private static List<String> buildJarCommand(Path jarPath, Set<ClassPath> classPaths) throws ProgramExecutionException {
        String mainClass = readMainClassFromJar(jarPath);
        if (mainClass == null) {
            throw new ProgramExecutionException(
                "No Main-Class attribute found in JAR manifest: " + jarPath
            );
        }

        var orderedPaths = new ArrayList<Path>();
        orderedPaths.add(jarPath);
        classPaths.stream().map(ClassPath::getPath).forEach(orderedPaths::add);

        var cmd = new ArrayList<String>();
        cmd.add("java");
        cmd.addAll(makeClassPathArgs(orderedPaths));
        cmd.add(mainClass);
        return cmd;
    }

    private static String readMainClassFromJar(Path jarPath) {
        try (var jar = new JarFile(jarPath.toFile())) {
            return jar.getManifest().getMainAttributes().getValue("Main-Class");
        } catch (IOException e) {
            return null;
        }
    }

    private static List<String> makeClassPathArgs(List<Path> paths) {
        if (paths.isEmpty()) {
            return List.of();
        }

        String joined = paths.stream()
                .map(Path::toString)
                .collect(Collectors.joining(System.getProperty("path.separator")));
        return List.of("-cp", joined);
    }
}
