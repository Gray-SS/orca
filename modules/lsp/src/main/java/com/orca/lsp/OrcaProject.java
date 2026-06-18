package com.orca.lsp;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.orca.compiler.core.Compilation;
import com.orca.compiler.core.CompilationContext;
import com.orca.compiler.core.CompilerArguments;
import com.orca.compiler.core.CompilerFlag;

/**
 * Active entity representing one Orca project (one build.gradle /
 * orca-project-model.json). Owns all open documents and their compilation state
 * for this project.
 *
 * The semantic model is stored directly on each {@link OrcaDocument} — the
 * document IS the cache entry. A null semantic model means stale; a non-null
 * one means ready to use.
 */
public class OrcaProject {

    private final Path root;
    private volatile List<String> sources;
    private volatile List<String> classpath;

    private final Map<String, OrcaDocument> openDocuments = new ConcurrentHashMap<>();

    OrcaProject(Path root, List<String> sources, List<String> classpath) {
        this.root = root;
        this.sources = List.copyOf(sources);
        this.classpath = List.copyOf(classpath);
    }

    public Path root() {
        return root;
    }

    // -------------------------------------------------------------------------
    // Document lifecycle
    // -------------------------------------------------------------------------
    public OrcaDocument openDocument(String uri, String content) {
        var doc = new OrcaDocument(uri, content, this);
        openDocuments.put(uri, doc);
        return doc;
    }

    public OrcaDocument changeDocument(String uri, String content) {
        var doc = openDocuments.computeIfAbsent(uri, u -> new OrcaDocument(u, content, this));
        doc.updateContent(content);
        return doc;
    }

    public void closeDocument(String uri) {
        openDocuments.remove(uri);
    }

    // -------------------------------------------------------------------------
    // Model reload
    // -------------------------------------------------------------------------
    /**
     * Called when build/orca-project-model.json is updated on disk.
     */
    public void reload(List<String> newSources, List<String> newClasspath) {
        this.sources = List.copyOf(newSources);
        this.classpath = List.copyOf(newClasspath);
        invalidateAll();
        System.out.println("Reloaded project " + root
                + " (" + newSources.size() + " sources, " + newClasspath.size() + " classpath entries)");
    }

    /**
     * Adds a source file that appeared on disk after the last model reload.
     */
    public void addSource(String sourcePath) {
        if (!sources.contains(sourcePath)) {
            var updated = new java.util.ArrayList<>(sources);
            updated.add(sourcePath);
            this.sources = List.copyOf(updated);
            invalidateAll();
        }
    }

    /**
     * Removes a source file that was deleted from disk.
     */
    public void removeSource(String sourcePath) {
        var updated = new java.util.ArrayList<>(sources);
        if (updated.remove(sourcePath)) {
            this.sources = List.copyOf(updated);
            invalidateAll();
        }
    }

    // -------------------------------------------------------------------------
    // Compilation cache (semantic model stored on OrcaDocument)
    // -------------------------------------------------------------------------
    /**
     * Marks the document stale so it is recompiled on next access.
     */
    public void invalidate(String uri) {
        var doc = openDocuments.get(uri);
        if (doc != null) {
            doc.setSemanticModel(null);
        }
    }

    /**
     * Marks all open documents stale.
     */
    public void invalidateAll() {
        System.out.println("Invalidating all compilations in project: " + root);
        openDocuments.values().forEach(doc -> doc.setSemanticModel(null));
    }

    /**
     * Returns the {@link OrcaDocument} for {@code uri} with its {@link
     * com.orca.compiler.core.semantics.SemanticModel} populated, or
     * {@code null} if the document is not open.
     *
     * <p>
     * The semantic model is computed lazily: it is built the first time this
     * method is called after opening or invalidating the document. All project
     * sources are fed into the compilation so cross-file resolution works
     * correctly.
     *
     * @param uri the document URI being requested
     * @param manualClasspath additional classpath entries from VSCode settings
     */
    public OrcaDocument getOrComputeDocument(String uri, List<String> manualClasspath) {
        var doc = openDocuments.get(uri);
        if (doc == null) {
            System.out.println("No open document for: " + uri);
            return null;
        }

        if (doc.semanticModel() != null) {
            System.out.println("Semantic model cache hit: " + uri);
            return doc;
        }

        var lspSource = new LspSource(uri, doc.content());
        var args = new CompilerArguments();
        args.addSource(lspSource);

        for (String sourcePath : sources) {
            String sourceUri = Path.of(sourcePath).toUri().toString();
            if (sourceUri.equals(uri)) {
                continue;
            }

            var openDoc = openDocuments.get(sourceUri);
            if (openDoc != null) {
                args.addSource(new LspSource(sourceUri, openDoc.content()));
            } else {
                try {
                    args.addSource(new LspSource(sourceUri, Files.readString(Path.of(sourcePath))));
                } catch (IOException e) {
                    System.out.println("Could not read project source: " + sourcePath);
                }
            }
        }

        args.enableFlag(CompilerFlag.LIBRARY_MODE);
        args.enableFlag(CompilerFlag.SILENT_MODE);

        for (String cp : classpath) {
            try {
                args.addClassPath(cp);
            } catch (IllegalArgumentException ignored) {
            }
        }
        for (String cp : manualClasspath) {
            try {
                args.addClassPath(cp);
            } catch (IllegalArgumentException ignored) {
            }
        }

        System.out.println("Building compilation for " + uri
                + " in project " + root
                + " (sources: " + (sources.size() + 1) + ", classpath: " + classpath.size() + ")");

        var context = new CompilationContext(args);
        var compilation = new Compilation(context);
        doc.setSemanticModel(compilation.getSemanticModel(lspSource));

        return doc;
    }
}
