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

import com.orca.compiler.core.CompilationContext;
import com.orca.compiler.core.CompilationPipeline;
import com.orca.compiler.core.CompilerArguments;
import com.orca.compiler.core.diagnostics.DiagnosticSeverity;
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
        var args = new CompilerArguments();

        getSources().getFiles().stream()
                .filter(f -> f.getName().endsWith(OrcaPlugin.ORCA_FILE_EXTENSION) && f.exists())
                .forEach(f -> args.addSource(new FileSource(f.getAbsolutePath())));

        getClasspath().getFiles().stream()
                .filter(f -> f.getName().endsWith(".jar") && f.exists())
                .forEach(f -> args.addClassPath(f.getAbsolutePath()));

        File output = getOutputFile().get().getAsFile();
        output.getParentFile().mkdirs();
        args.setOutputFile(output.getAbsolutePath());

        var context = new CompilationContext(args);
        var pipeline = new CompilationPipeline(context);
        boolean success = pipeline.compile();

        if (!success) {
            var diagnostics = context.diagnostics();
            for (var diag : diagnostics) {
                String msg = "[" + diag.code() + "] " + diag.message();
                if (diag.severity() == DiagnosticSeverity.ERROR) {
                    getLogger().error(msg);
                } else {
                    getLogger().warn(msg);
                }
            }
            throw new GradleException("Orca compilation failed with "
                    + diagnostics.countErrors() + " error(s).");
        }
    }
}
