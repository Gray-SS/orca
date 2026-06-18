package com.orca.gradle;

import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.artifacts.Configuration;
import org.gradle.api.tasks.Exec;

public class OrcaPlugin implements Plugin<Project> {

    public static final String ORCA_CLASSPATH_CONFIGURATION_NAME = "orcaClasspath";
    public static final String GENERATE_PROJECT_MODEL_TASK_NAME = "generateOrcaProjectModel";
    public static final String ORCA_FILE_EXTENSION = ".orca";
    public static final String ORCA_EXTENSION_NAME = ORCA_FILE_EXTENSION.substring(1); // "orca"
    public static final String ORCA_PROJECT_MODEL_FILE_NAME = "orca-project-model.json";
    public static final String ORCA_DEFAULT_SOURCE_DIR = "src/main/orca";

    @Override
    public void apply(Project project) {
        // Create a resolvable configuration for Orca classpath dependencies
        Configuration orcaClasspath = project.getConfigurations().create(OrcaPlugin.ORCA_CLASSPATH_CONFIGURATION_NAME, c -> {
            c.setCanBeConsumed(false);
            c.setCanBeResolved(true);
            c.setDescription("Classpath for the Orca compiler (external JARs).");
        });

        // Create the orca {} DSL block
        OrcaExtension ext = project.getExtensions()
                .create(OrcaPlugin.ORCA_EXTENSION_NAME, OrcaExtension.class);

        // Extract stdlib sources bundled inside the plugin JAR into build/orca-stdlib/
        var extractStdlib = project.getTasks().register("extractOrcaStdlib", ExtractStdlibTask.class, task -> {
            task.setGroup("orca");
            task.setDescription("Extracts the Orca standard library sources bundled in the plugin.");
            task.getOutputDirectory().set(project.getLayout().getBuildDirectory().dir("orca-stdlib"));
        });

        // stdlib sources are automatically included in every project's source set
        ext.getSources().from(project.fileTree(
                extractStdlib.flatMap(ExtractStdlibTask::getOutputDirectory),
                t -> t.include("**/*" + OrcaPlugin.ORCA_FILE_EXTENSION)
        ));

        // By default, all .orca files under src/main/orca/
        ext.getSources().from(project.fileTree(OrcaPlugin.ORCA_DEFAULT_SOURCE_DIR, t -> t.include("**/*" + OrcaPlugin.ORCA_FILE_EXTENSION)));

        // Register the model generation task (for LSP)
        project.getTasks().register(OrcaPlugin.GENERATE_PROJECT_MODEL_TASK_NAME, GenerateProjectModelTask.class, modelTask -> {
            modelTask.setGroup("orca");
            modelTask.setDescription("Generates the Orca project model for LSP consumption.");
            modelTask.dependsOn(extractStdlib);
            modelTask.getSources().from(ext.getSources());
            modelTask.getClasspath().from(orcaClasspath);
            modelTask.getOutputFile().set(
                    project.getLayout().getBuildDirectory().file(OrcaPlugin.ORCA_PROJECT_MODEL_FILE_NAME)
            );
        });

        // Register the compile task — outputs a runnable JAR to build/libs/<project>.jar
        var compileOrca = project.getTasks().register("compileOrca", CompileOrcaTask.class, compileTask -> {
            compileTask.setGroup("orca");
            compileTask.setDescription("Compiles all Orca sources into a runnable JAR.");
            compileTask.dependsOn(extractStdlib);
            compileTask.getSources().from(ext.getSources());
            compileTask.getClasspath().from(orcaClasspath);
            compileTask.getOutputFile().set(
                    project.getLayout().getBuildDirectory()
                            .file("libs/" + project.getName() + ".jar")
            );
        });

        // Wire compileOrca into the standard build lifecycle
        project.getPluginManager().apply("base");
        project.getTasks().named("assemble").configure(t -> t.dependsOn(compileOrca));

        // Register the run task — executes the compiled JAR
        project.getTasks().register("run", Exec.class, runTask -> {
            runTask.setGroup("orca");
            runTask.setDescription("Compiles and runs the Orca program.");
            runTask.dependsOn(compileOrca);
            runTask.doFirst(t -> {
                String jar = compileOrca.get().getOutputFile().get().getAsFile().getAbsolutePath();
                String cp = orcaClasspath.getFiles().stream()
                        .filter(f -> f.getName().endsWith(".jar"))
                        .map(java.io.File::getAbsolutePath)
                        .reduce(jar, (a, b) -> a + java.io.File.pathSeparator + b);
                ((Exec) t).commandLine("java", "-cp", cp, "OrcaEntry");
            });
        });
    }
}
 