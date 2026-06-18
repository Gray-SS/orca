package com.orca.compiler.core.externals;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.jar.JarFile;
import java.util.stream.Stream;

import javax.annotation.Nonnull;

import com.google.common.base.Preconditions;

public final class ClassPath {

    private final Path path;
    private final Map<String, ClassEntry> entries;

    private ClassPath(Path path, Map<String, ClassEntry> classEntries) {
        this.path = path;
        this.entries = Map.copyOf(classEntries);
    }

    public Path getPath() {
        return path;
    }

    public Set<ClassEntry> getEntries() {
        return Set.copyOf(entries.values());
    }

    public Set<String> getEntryNames() {
        return Set.copyOf(entries.keySet());
    }

    public boolean hasEntry(String qualifiedName) {
        if (qualifiedName == null || qualifiedName.isBlank()) {
            return false;
        }

        return entries.containsKey(qualifiedName);
    }

    public @Nonnull
    ClassEntry getEntry(String qualifiedName) {
        Preconditions.checkNotNull(qualifiedName, "qualifiedName cannot be null");
        Preconditions.checkArgument(!qualifiedName.isBlank(), "qualifiedName cannot be blank");

        var entry = entries.get(qualifiedName);
        if (entry == null) {
            throw new IllegalArgumentException("Entry not found for qualifiedName: " + qualifiedName);
        }

        return entry;
    }

    public static ClassPath of(Path path) {
        Preconditions.checkNotNull(path, "path cannot be null");

        var result = new HashMap<String, ClassEntry>();

        try {
            if (Files.isDirectory(path)) {
                indexDirectoryClasses(result, path);
            } else if (path.toString().endsWith(".jar")) {
                indexJarClasses(result, path);
            }
        } catch (IOException ignored) {
            // Keep compilation resilient even if one classpath entry is invalid.
        }

        return new ClassPath(path, result);
    }

    private static void indexDirectoryClasses(Map<String, ClassEntry> result, Path root) throws IOException {
        try (var stream = Files.walk(root)) {
            var classNames = stream.filter(Files::isRegularFile)
                    .filter(path -> path.toString().endsWith(".class"))
                    .map(path -> root.relativize(path).toString());
            cacheClasspathEntries(result, classNames);
        }
    }

    private static void indexJarClasses(Map<String, ClassEntry> result, Path jarPath) throws IOException {
        try (var jar = new JarFile(jarPath.toFile())) {
            var classNames = jar.stream().map(entry -> entry.getName());
            cacheClasspathEntries(result, classNames);
        }
    }

    private static void cacheClasspathEntries(Map<String, ClassEntry> result, Stream<String> classNames) {
        classNames.filter(name -> name.endsWith(".class"))
                .map(name -> name.replace('/', '.').replace('\\', '.'))
                .map(name -> name.substring(0, name.length() - ".class".length()))
                .forEach(className -> result.put(className, new ClassEntry(className)));
    }
}
