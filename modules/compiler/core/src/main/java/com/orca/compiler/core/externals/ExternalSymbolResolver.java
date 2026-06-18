package com.orca.compiler.core.externals;

import java.net.MalformedURLException;
import java.util.Map;
import java.util.Set;

import javax.annotation.Nonnull;

import org.checkerframework.checker.nullness.qual.NonNull;

import com.google.common.base.Preconditions;
import com.orca.compiler.core.Debug;
import com.orca.compiler.core.symbols.externals.JvmClassSymbol;
import com.orca.compiler.core.symbols.externals.JvmNamespaceSymbol;
import com.orca.compiler.core.symbols.externals.JvmSymbol;

public final class ExternalSymbolResolver {

    private final ClassLoader classLoader;

    private final Set<String> knownSymbols;
    private final Set<String> knownPackages;
    private final Set<String> knownClasses;

    private final Map<String, JvmClassSymbol> classCache = new java.util.HashMap<>();
    private final Map<String, JvmNamespaceSymbol> packageCache = new java.util.HashMap<>();

    public ExternalSymbolResolver(Set<ClassPath> classPaths, boolean includeJdk) {
        Preconditions.checkNotNull(classPaths, "classPathIndexes cannot be null");

        this.classLoader = createClassLoader(classPaths);
        this.knownClasses = mergeKnownClasses(classPaths, includeJdk);
        this.knownPackages = extractKnownPackages(knownClasses);
        this.knownSymbols = extractKnownSymbols(knownClasses, knownPackages);
    }

    private static ClassLoader createClassLoader(Set<ClassPath> classPaths) {
        var urls = classPaths.stream()
                .map(classPath -> {
                    try {
                        return classPath.getPath().toUri().toURL();
                    } catch (MalformedURLException e) {
                        throw new RuntimeException("Failed to convert class path to URL: " + classPath.getPath(), e);
                    }
                })
                .toArray(java.net.URL[]::new);

        return new java.net.URLClassLoader(urls, ExternalSymbolResolver.class.getClassLoader());
    }

    private static Set<String> mergeKnownClasses(Set<ClassPath> classPaths, boolean includeJdk) {
        var mergedEntries = new java.util.HashSet<String>();

        for (var index : classPaths) {
            mergedEntries.addAll(index.getEntryNames());
        }

        if (includeJdk) {
            mergedEntries.addAll(indexJdkClasses());
        }

        return mergedEntries;
    }

    private static Set<String> extractKnownPackages(Set<String> knownClasses) {
        var packages = new java.util.HashSet<String>();

        for (var qualifiedName : knownClasses) {
            int lastDotIndex = qualifiedName.lastIndexOf('.');
            while (lastDotIndex != -1) {
                var packageName = qualifiedName.substring(0, lastDotIndex);
                packages.add(packageName);

                lastDotIndex = packageName.lastIndexOf('.');
                qualifiedName = packageName;
            }
        }

        return Set.copyOf(packages);
    }

    private static Set<String> extractKnownSymbols(Set<String> mergedIndices, Set<String> knownPackages) {
        var symbols = new java.util.HashSet<String>();
        symbols.addAll(mergedIndices);
        symbols.addAll(knownPackages);

        return Set.copyOf(symbols);
    }

    private static Set<String> indexJdkClasses() {
        var classes = new java.util.HashSet<String>();
        java.lang.module.ModuleFinder.ofSystem().findAll().forEach(ref -> {
            try (var reader = ref.open()) {
                reader.list()
                        .filter(name -> name.endsWith(".class") && !name.equals("module-info.class"))
                        .map(name -> name.replace('/', '.').substring(0, name.length() - ".class".length()))
                        .forEach(classes::add);
            } catch (java.io.IOException ignored) {
                Debug.warning("Failed to read module for JDK class indexing: " + ref.descriptor().name());
            }
        });

        return classes;
    }

    public Set<JvmClassSymbol> getLoadedClasses() {
        return Set.copyOf(classCache.values());
    }

    public Set<JvmNamespaceSymbol> getLoadedPackages() {
        return Set.copyOf(packageCache.values());
    }

    public Set<String> getKnownSymbols() {
        return knownSymbols;
    }

    public boolean exists(String name) {
        Preconditions.checkNotNull(name, "name cannot be null");
        Preconditions.checkArgument(!name.isBlank(), "name cannot be blank");

        return knownSymbols.contains(name);
    }

    public boolean packageExists(String qualifiedName) {
        Preconditions.checkNotNull(qualifiedName, "qualifiedName cannot be null");
        Preconditions.checkArgument(!qualifiedName.isBlank(), "qualifiedName cannot be blank");

        return knownPackages.contains(qualifiedName);
    }

    public boolean classExists(String qualifiedName) {
        Preconditions.checkNotNull(qualifiedName, "qualifiedName cannot be null");
        Preconditions.checkArgument(!qualifiedName.isBlank(), "qualifiedName cannot be blank");

        return knownClasses.contains(qualifiedName);
    }

    public Set<String> indexDirectMembersOf(String qualifiedName) {
        Preconditions.checkNotNull(qualifiedName, "qualifiedName cannot be null");
        Preconditions.checkArgument(!qualifiedName.isBlank(), "qualifiedName cannot be blank");

        if (!exists(qualifiedName)) {
            throw new IllegalArgumentException("No class or package found for qualified name: " + qualifiedName);
        }

        var indexedMembers = new java.util.HashSet<String>();
        for (var entry : knownClasses) {
            if (entry.startsWith(qualifiedName + ".")) {
                var memberName = entry.substring(qualifiedName.length() + 1);
                if (!memberName.contains(".")) {
                    indexedMembers.add(memberName);
                }
            }
        }

        return indexedMembers;
    }

    public @Nonnull
    JvmClassSymbol getOrCreateClassSymbol(String qualifiedName) {
        Preconditions.checkNotNull(qualifiedName, "qualifiedName cannot be null");
        Preconditions.checkArgument(!qualifiedName.isBlank(), "qualifiedName cannot be blank");

        var result = getOrCreateClassSymbolPrivate(qualifiedName);
        if (result == null) {
            throw new IllegalArgumentException("Class not found: " + qualifiedName);
        }

        return result;
    }

    private JvmClassSymbol getOrCreateClassSymbolPrivate(String qualifiedName) {
        Preconditions.checkNotNull(qualifiedName, "qualifiedName cannot be null");
        Preconditions.checkArgument(!qualifiedName.isBlank(), "qualifiedName cannot be blank");

        if (!knownClasses.contains(qualifiedName)) {
            return null;
        }

        var cachedClass = classCache.get(qualifiedName);
        if (cachedClass != null) {
            return cachedClass;
        }

        var clazz = loadClass(qualifiedName);
        var symbol = new JvmClassSymbol(this, clazz);
        classCache.put(qualifiedName, symbol);
        return symbol;
    }

    public @NonNull
    JvmNamespaceSymbol getOrCreatePackageSymbol(String qualifiedName) {
        Preconditions.checkNotNull(qualifiedName, "qualifiedName cannot be null");
        Preconditions.checkArgument(!qualifiedName.isBlank(), "qualifiedName cannot be blank");

        var result = getOrCreatePackageSymbolPrivate(qualifiedName);
        if (result == null) {
            throw new IllegalArgumentException("Package not found: " + qualifiedName);
        }

        return result;
    }

    private JvmNamespaceSymbol getOrCreatePackageSymbolPrivate(String qualifiedName) {
        Preconditions.checkNotNull(qualifiedName, "qualifiedName cannot be null");
        Preconditions.checkArgument(!qualifiedName.isBlank(), "qualifiedName cannot be blank");

        if (!knownPackages.contains(qualifiedName)) {
            return null;
        }

        var cachedPackage = packageCache.get(qualifiedName);
        if (cachedPackage != null) {
            return cachedPackage;
        }

        var symbol = new JvmNamespaceSymbol(this, qualifiedName);
        packageCache.put(qualifiedName, symbol);
        return symbol;
    }

    public @Nonnull
    JvmSymbol getOrCreateSymbol(String name) {
        Preconditions.checkNotNull(name, "name cannot be null");
        Preconditions.checkArgument(!name.isBlank(), "name cannot be blank");

        if (!knownSymbols.contains(name)) {
            throw new IllegalArgumentException("Symbol not found: " + name);
        }

        var pkg = getOrCreatePackageSymbolPrivate(name);
        if (pkg != null) {
            return pkg;
        }

        var clazz = getOrCreateClassSymbolPrivate(name);
        if (clazz != null) {
            return clazz;
        }

        throw new IllegalArgumentException("Symbol was expected to be found but was not: " + name);
    }

    private Class<?> loadClass(String qualifiedName) {
        Preconditions.checkNotNull(qualifiedName, "qualifiedName cannot be null");
        Preconditions.checkArgument(!qualifiedName.isBlank(), "qualifiedName cannot be blank");

        if (!knownClasses.contains(qualifiedName)) {
            throw new IllegalArgumentException("Class not found in class path: " + qualifiedName);
        }

        try {
            return classLoader.loadClass(qualifiedName);
        } catch (ClassNotFoundException e) {
            throw new IllegalStateException("Failed to load class: " + qualifiedName, e);
        }
    }
}
