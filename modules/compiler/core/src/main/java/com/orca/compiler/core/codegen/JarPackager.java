package com.orca.compiler.core.codegen;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.jar.Attributes;
import java.util.jar.JarEntry;
import java.util.jar.JarOutputStream;
import java.util.jar.Manifest;

import com.google.common.base.Optional;

public final class JarPackager {

    private JarPackager() {
    }

    public static void pack(Path outputPath, CompilationMetadata metadata, Map<String, byte[]> classes) throws IOException {
        Path parent = outputPath.getParent();
        if (parent != null) {
            Files.createDirectories(parent);
        }

        try (var jarOut = new JarOutputStream(Files.newOutputStream(outputPath), buildManifest(metadata.mainClass()))) {
            writeClasses(jarOut, classes);
            writeMetadata(jarOut, metadata);
        }
    }

    private static Manifest buildManifest(Optional<String> mainClass) {
        var manifest = new Manifest();
        var attrs = manifest.getMainAttributes();
        attrs.put(Attributes.Name.MANIFEST_VERSION, "1.0");
        if (mainClass.isPresent()) {
            attrs.put(Attributes.Name.MAIN_CLASS, mainClass.get());
        }
        return manifest;
    }

    private static void writeClasses(JarOutputStream jarOut, Map<String, byte[]> classes) throws IOException {
        for (var entry : classes.entrySet()) {
            jarOut.putNextEntry(new JarEntry(entry.getKey() + ".class"));
            jarOut.write(entry.getValue());
            jarOut.closeEntry();
        }
    }

    private static void writeMetadata(JarOutputStream jarOut, CompilationMetadata metadata) throws IOException {
        jarOut.putNextEntry(new JarEntry(CompilationMetadata.RESOURCE_PATH));
        metadata.serialize(jarOut);
        jarOut.closeEntry();
    }
}
