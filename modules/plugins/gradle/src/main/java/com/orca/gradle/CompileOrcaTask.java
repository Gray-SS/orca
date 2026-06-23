package com.orca.gradle;

import java.io.File;

import org.gradle.api.GradleException;
import org.gradle.api.file.ConfigurableFileCollection;
import org.gradle.api.file.RegularFileProperty;
import org.gradle.api.tasks.CacheableTask;
import org.gradle.api.tasks.InputFiles;
import org.gradle.api.tasks.OutputFile;
import org.gradle.api.tasks.PathSensitive;
import org.gradle.api.tasks.PathSensitivity;
import org.gradle.api.tasks.TaskAction;

import com.orca.compiler.core.Compilation;
import com.orca.compiler.core.CompilerOptions;
import com.orca.compiler.core.OutputFormat;
import com.orca.compiler.core.OutputKind;
import com.orca.compiler.core.diagnostics.DiagnosticSeverity;
import com.orca.compiler.core.externals.ClassPath;
import com.orca.compiler.core.text.FileSource;

@CacheableTask
public abstract class CompileOrcaTask extends org.gradle.api.DefaultTask {

    @InputFiles
    @PathSensitive(PathSensitivity.ABSOLUTE)
    public abstract ConfigurableFileCollection getSources();

    @InputFiles
    @PathSensitive(PathSensitivity.ABSOLUTE)
    public abstract ConfigurableFileCollection getClasspath();

    @OutputFile
    public abstract RegularFileProperty getOutputFile();

    @TaskAction
    public void compile() {
        var optionsBuilder = CompilerOptions.builder()
                .setOutputKind(OutputKind.APPLICATION)
                .setOutputFormat(OutputFormat.JAR);

        getSources().getFiles().stream()
                .filter(f -> f.getName().endsWith(OrcaPlugin.ORCA_FILE_EXTENSION) && f.exists())
                .forEach(f -> optionsBuilder.addSource(new FileSource(f.getAbsolutePath())));

        getClasspath().getFiles().stream()
                .filter(f -> f.getName().endsWith(".jar") && f.exists())
                .forEach(f -> optionsBuilder.addClassPath(ClassPath.of(f.toPath())));

        File output = getOutputFile().get().getAsFile();
        output.getParentFile().mkdirs();
        optionsBuilder.setOutputPath(output.toPath().toAbsolutePath());

        var options = optionsBuilder.build();
        var compilation = new Compilation(options);
        var compilationResult = compilation.compile();

        var diagnostics = compilationResult.diagnostics();
        for (var diag : diagnostics) {
            String msg = "[" + diag.code() + "] " + diag.message();
            if (diag.severity() == DiagnosticSeverity.ERROR) {
                getLogger().error(msg);
            } else {
                getLogger().warn(msg);
            }
        }

        if (compilationResult.isFailure()) {
            throw new GradleException("Orca compilation failed with "
                    + compilationResult.diagnostics().countErrors() + " error(s).");
        }
    }
}
