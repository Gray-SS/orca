package com.orca.compiler.core.phases;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashMap;

import com.orca.compiler.core.CompilationContext;
import com.orca.compiler.core.boundtree.BoundProgram;
import com.orca.compiler.core.codegen.ClassDirWriter;
import com.orca.compiler.core.codegen.CompilationMetadata;
import com.orca.compiler.core.codegen.Emitter;
import com.orca.compiler.core.codegen.JarPackager;

public class DefaultCodeGenPhase implements CodeGenPhase {

    @Override
    public void generate(CompilationContext context, BoundProgram program) throws Exception {
        context.logger().debug("Starting CodeGen phase (JVM bytecode)...");

        String outputStr = context.arguments().getOutputFile();
        Path outputPath = Path.of(outputStr);

        if (outputStr.endsWith(".jar")) {
            emitJar(context, program, outputPath);
        } else {
            emitClasses(context, program, outputPath);
        }
    }

    private void emitJar(CompilationContext context, BoundProgram program, Path outputPath) throws Exception {
        var classes = new LinkedHashMap<String, byte[]>();
        new Emitter(program, classes::put).emit();

        var metadata = CompilationMetadata.of(context, new ArrayList<>(classes.keySet()));
        JarPackager.pack(outputPath, metadata, classes);

        context.logger().info("CodeGen completed. Output jar: " + outputPath.toAbsolutePath());
    }

    private void emitClasses(CompilationContext context, BoundProgram program, Path outputPath) throws Exception {
        Path outDir = outputPath.getParent() != null ? outputPath.getParent() : Path.of(".");
        new Emitter(program, new ClassDirWriter(outDir)).emit();
        context.logger().info("CodeGen completed. Output dir: " + outDir.toAbsolutePath());
    }
}
