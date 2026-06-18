package com.orca.gradle;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

import org.gradle.api.DefaultTask;
import org.gradle.api.file.DirectoryProperty;
import org.gradle.api.tasks.Input;
import org.gradle.api.tasks.OutputDirectory;
import org.gradle.api.tasks.TaskAction;

public abstract class ExtractStdlibTask extends DefaultTask {

    private static final String MANIFEST_RESOURCE = "/com/orca/gradle/stdlib-manifest.txt";
    private static final String STDLIB_RESOURCE_PREFIX = "/com/orca/gradle/stdlib/";

    @OutputDirectory
    public abstract DirectoryProperty getOutputDirectory();

    @Input
    public String getStdlibManifest() {
        try (InputStream in = getClass().getResourceAsStream(MANIFEST_RESOURCE)) {
            return in != null ? new String(in.readAllBytes()) : "";
        } catch (IOException e) {
            return "";
        }
    }

    @TaskAction
    public void extract() throws IOException {
        File outputDir = getOutputDirectory().get().getAsFile();
        try (InputStream manifestStream = getClass().getResourceAsStream(MANIFEST_RESOURCE)) {
            if (manifestStream == null) {
                getLogger().warn("Orca stdlib manifest not found; no stdlib sources will be extracted.");
                return;
            }
            for (String relPath : new String(manifestStream.readAllBytes()).split("\n")) {
                relPath = relPath.strip();
                if (relPath.isEmpty()) continue;
                try (InputStream src = getClass().getResourceAsStream(STDLIB_RESOURCE_PREFIX + relPath)) {
                    if (src == null) {
                        getLogger().warn("Stdlib resource missing: {}", relPath);
                        continue;
                    }
                    Path target = outputDir.toPath().resolve(relPath);
                    Files.createDirectories(target.getParent());
                    Files.copy(src, target, StandardCopyOption.REPLACE_EXISTING);
                }
            }
        }
    }
}
