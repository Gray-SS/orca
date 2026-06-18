package com.orca.compiler.core.codegen;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public final class ClassDirWriter implements ClassSink {
    private final Path outputDir;

    public ClassDirWriter(Path outputDir) {
        this.outputDir = outputDir;
    }

    @Override
    public void accept(String internalName, byte[] bytecode) {
        try {
            Path file = outputDir.resolve(internalName + ".class").normalize();
            Path parent = file.getParent();
            Files.createDirectories(parent != null ? parent : outputDir);
            Files.write(file, bytecode);
        } catch (IOException e) {
            throw new RuntimeException("Failed to write class file: " + internalName, e);
        }
    }
}
