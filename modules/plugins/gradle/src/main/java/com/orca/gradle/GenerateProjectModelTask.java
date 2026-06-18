package com.orca.gradle;

import java.io.IOException;
import java.nio.file.Files;
import java.util.stream.Collectors;

import org.gradle.api.DefaultTask;
import org.gradle.api.file.ConfigurableFileCollection;
import org.gradle.api.file.RegularFileProperty;
import org.gradle.api.tasks.CacheableTask;
import org.gradle.api.tasks.InputFiles;
import org.gradle.api.tasks.OutputFile;
import org.gradle.api.tasks.PathSensitive;
import org.gradle.api.tasks.PathSensitivity;
import org.gradle.api.tasks.TaskAction;

@CacheableTask
public abstract class GenerateProjectModelTask extends DefaultTask {

    @InputFiles
    @PathSensitive(PathSensitivity.ABSOLUTE)
    public abstract ConfigurableFileCollection getSources();

    @InputFiles
    @PathSensitive(PathSensitivity.ABSOLUTE)
    public abstract ConfigurableFileCollection getClasspath();

    @OutputFile
    public abstract RegularFileProperty getOutputFile();

    @TaskAction
    public void generate() throws IOException {
        var sources = getSources().getFiles().stream()
                .filter(f -> f.getName().endsWith(".orca"))
                .map(f -> quote(f.getAbsolutePath()))
                .collect(Collectors.joining(",\n    "));

        var classpath = getClasspath().getFiles().stream()
                .filter(f -> f.getName().endsWith(".jar"))
                .map(f -> quote(f.getAbsolutePath()))
                .collect(Collectors.joining(",\n    "));

        var jsonSchema = """
        {
            "schemaVersion": 1,
            "sources": [
                %s
            ],
            "classpath": [
                %s
            ]
        }
        """;
        var json = String.format(jsonSchema, sources, classpath);

        var out = getOutputFile().get().getAsFile();
        out.getParentFile().mkdirs();
        Files.writeString(out.toPath(), json);
    }

    private String quote(String s) {
        return "\"" + s.replace("\\", "\\\\") + "\"";
    }
}
