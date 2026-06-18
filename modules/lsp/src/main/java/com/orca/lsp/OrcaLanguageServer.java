package com.orca.lsp;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

import org.eclipse.lsp4j.CompletionOptions;
import org.eclipse.lsp4j.ConfigurationItem;
import org.eclipse.lsp4j.ConfigurationParams;
import org.eclipse.lsp4j.DidChangeWatchedFilesRegistrationOptions;
import org.eclipse.lsp4j.FileSystemWatcher;
import org.eclipse.lsp4j.InitializeParams;
import org.eclipse.lsp4j.InitializeResult;
import org.eclipse.lsp4j.InitializedParams;
import org.eclipse.lsp4j.Registration;
import org.eclipse.lsp4j.RegistrationParams;
import org.eclipse.lsp4j.SemanticTokensWithRegistrationOptions;
import org.eclipse.lsp4j.ServerCapabilities;
import org.eclipse.lsp4j.TextDocumentSyncKind;
import org.eclipse.lsp4j.jsonrpc.messages.Either;
import org.eclipse.lsp4j.services.LanguageClient;
import org.eclipse.lsp4j.services.LanguageClientAware;
import org.eclipse.lsp4j.services.LanguageServer;
import org.eclipse.lsp4j.services.WorkspaceService;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;

public class OrcaLanguageServer implements LanguageServer, LanguageClientAware {

    private final OrcaTextDocumentService textDocumentService;
    private final OrcaWorkspaceService workspaceService;
    private LanguageClient client;

    // Classpath entries from the orca.classpath VSCode setting (applies to all projects).
    private final List<String> manualClasspath = new CopyOnWriteArrayList<>();

    // Known projects, keyed by their root directory (the directory containing build.gradle.kts).
    private final Map<Path, OrcaProject> projects = new ConcurrentHashMap<>();

    // Fallback project for files that don't belong to any discovered project.
    // Compiles files in isolation with no extra sources or classpath.
    private final OrcaProject defaultProject = new OrcaProject(null, List.of(), List.of());

    private Path workspaceRoot;

    public OrcaLanguageServer() {
        this.textDocumentService = new OrcaTextDocumentService(this);
        this.workspaceService = new OrcaWorkspaceService(this);
    }

    @Override
    public CompletableFuture<InitializeResult> initialize(InitializeParams params) {
        if (params.getWorkspaceFolders() != null && !params.getWorkspaceFolders().isEmpty()) {
            try {
                workspaceRoot = Path.of(new URI(params.getWorkspaceFolders().get(0).getUri()));
            } catch (URISyntaxException e) {
                System.err.println("Invalid workspace root URI: " + e.getMessage());
            }
        }

        var capabilities = new ServerCapabilities();
        capabilities.setTextDocumentSync(TextDocumentSyncKind.Full);

        var completionOptions = new CompletionOptions();
        completionOptions.setTriggerCharacters(List.of(".", ":"));

        capabilities.setCompletionProvider(completionOptions);
        capabilities.setHoverProvider(true);
        capabilities.setDefinitionProvider(true);

        var semanticTokensOptions = new SemanticTokensWithRegistrationOptions();
        semanticTokensOptions.setLegend(OrcaSemanticTokensLegend.LEGEND);
        semanticTokensOptions.setFull(true);
        capabilities.setSemanticTokensProvider(semanticTokensOptions);

        return CompletableFuture.completedFuture(new InitializeResult(capabilities));
    }

    @Override
    public void initialized(InitializedParams params) {
        var watcher = new FileSystemWatcher(Either.forLeft("**/build/orca-project-model.json"));
        var registration = new Registration(
                UUID.randomUUID().toString(),
                "workspace/didChangeWatchedFiles",
                new DidChangeWatchedFilesRegistrationOptions(List.of(watcher))
        );
        client.registerCapability(new RegistrationParams(List.of(registration)));

        loadAllProjects();
        fetchClasspath();
    }

    @Override
    public void setTrace(org.eclipse.lsp4j.SetTraceParams params) {}

    @Override
    public CompletableFuture<Object> shutdown() {
        System.out.println("Shutting down Orca Language Server");
        return CompletableFuture.completedFuture(null);
    }

    @Override
    public void exit() {
        System.out.println("Exiting Orca Language Server");
        System.exit(0);
    }

    @Override
    public OrcaTextDocumentService getTextDocumentService() {
        return textDocumentService;
    }

    @Override
    public WorkspaceService getWorkspaceService() {
        return workspaceService;
    }

    @Override
    public void connect(LanguageClient client) {
        this.client = client;
    }

    public LanguageClient getClient() {
        return client;
    }

    public List<String> getManualClasspath() {
        return manualClasspath;
    }

    // -------------------------------------------------------------------------
    // Project discovery
    // -------------------------------------------------------------------------

    /**
     * Scans the workspace root (up to 5 levels deep) for orca-project-model.json files
     * and loads each discovered project.
     */
    public void loadAllProjects() {
        if (workspaceRoot == null) return;
        System.out.println("Scanning for Orca projects under: " + workspaceRoot);
        try (var stream = Files.walk(workspaceRoot, 5)) {
            stream
                .filter(p -> p.getFileName().toString().equals("orca-project-model.json"))
                .filter(p -> p.getParent() != null && p.getParent().getFileName().toString().equals("build"))
                .forEach(modelFile -> loadOrReloadProjectAt(modelFile.getParent().getParent()));
        } catch (IOException e) {
            System.err.println("Failed to scan for Orca projects: " + e.getMessage());
        }
    }

    /**
     * Loads or reloads the project rooted at {@code projectRoot}.
     * If the project already exists, its model is refreshed and its cache is invalidated.
     * Called both during the initial scan and when the file watcher fires.
     */
    public void loadOrReloadProjectAt(Path projectRoot) {
        try {
            Path modelFile = projectRoot.resolve("build/orca-project-model.json");
            if (!Files.exists(modelFile)) {
                projects.remove(projectRoot);
                System.out.println("Project removed (model file gone): " + projectRoot);
                return;
            }

            var json = new Gson().fromJson(Files.readString(modelFile), JsonObject.class);
            var sources = new ArrayList<String>();
            var classpath = new ArrayList<String>();
            json.getAsJsonArray("sources").forEach(e -> sources.add(e.getAsString()));
            json.getAsJsonArray("classpath").forEach(e -> classpath.add(e.getAsString()));

            var existing = projects.get(projectRoot);
            if (existing != null) {
                existing.reload(sources, classpath);
            } else {
                projects.put(projectRoot, new OrcaProject(projectRoot, sources, classpath));
                System.out.println("Loaded project: " + projectRoot
                    + " (" + sources.size() + " sources, " + classpath.size() + " classpath entries)");
            }
        } catch (JsonSyntaxException | IOException e) {
            System.err.println("Failed to load project at " + projectRoot + ": " + e.getMessage());
        }
    }

    // -------------------------------------------------------------------------
    // Project routing
    // -------------------------------------------------------------------------

    /**
     * Finds the project that owns the given file URI by walking up its directory tree.
     * Lazily discovers model files that appeared after the initial scan.
     * Falls back to the default (isolation) project if no project is found.
     */
    public OrcaProject findProjectForFile(String fileUri) {
        try {
            Path filePath = Path.of(new URI(fileUri));
            Path candidate = filePath.getParent();
            while (candidate != null) {
                var project = projects.get(candidate);
                if (project != null) return project;

                // Lazily pick up a project whose model appeared after the initial scan.
                if (Files.exists(candidate.resolve("build/orca-project-model.json"))) {
                    loadOrReloadProjectAt(candidate);
                    var loaded = projects.get(candidate);
                    if (loaded != null) return loaded;
                }

                if (candidate.equals(workspaceRoot)) break;
                candidate = candidate.getParent();
            }
        } catch (URISyntaxException e) {
            System.err.println("Invalid file URI: " + fileUri);
        }
        return defaultProject;
    }

    // -------------------------------------------------------------------------
    // Manual classpath (from VSCode settings)
    // -------------------------------------------------------------------------

    void fetchClasspath() {
        var item = new ConfigurationItem();
        item.setSection("orca.classpath");
        client.configuration(new ConfigurationParams(List.of(item))).thenAccept(result -> {
            manualClasspath.clear();
            if (result != null && !result.isEmpty() && result.get(0) instanceof JsonArray array) {
                for (JsonElement el : array) {
                    String p = el.getAsString().trim();
                    if (!p.isEmpty()) manualClasspath.add(p);
                }
            }
        });
    }
}
