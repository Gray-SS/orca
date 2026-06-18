package com.orca.lsp;

import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Path;

import org.eclipse.lsp4j.DidChangeConfigurationParams;
import org.eclipse.lsp4j.DidChangeWatchedFilesParams;
import org.eclipse.lsp4j.FileChangeType;
import org.eclipse.lsp4j.services.WorkspaceService;

public class OrcaWorkspaceService implements WorkspaceService {

    private final OrcaLanguageServer server;

    public OrcaWorkspaceService(OrcaLanguageServer server) {
        this.server = server;
    }

    @Override
    public void didChangeConfiguration(DidChangeConfigurationParams params) {
        server.fetchClasspath();
    }

    @Override
    public void didChangeWatchedFiles(DidChangeWatchedFilesParams params) {
        for (var change : params.getChanges()) {
            String uri = change.getUri();
            if (uri.endsWith("orca-project-model.json")) {
                try {
                    Path projectRoot = Path.of(new URI(uri)).getParent().getParent();
                    server.loadOrReloadProjectAt(projectRoot);
                } catch (URISyntaxException e) {
                    System.err.println("Invalid URI in file watcher event: " + uri);
                }
            } else if (uri.endsWith(".orca")) {
                try {
                    String sourcePath = Path.of(new URI(uri)).toAbsolutePath().toString();
                    OrcaProject project = server.findProjectForFile(uri);
                    switch (change.getType()) {
                        case FileChangeType.Created -> project.addSource(sourcePath);
                        case FileChangeType.Deleted -> project.removeSource(sourcePath);
                        default -> {} // Changed: handled via didSave when the file is open
                    }
                } catch (URISyntaxException e) {
                    System.err.println("Invalid URI in file watcher event: " + uri);
                }
            }
        }
    }
}