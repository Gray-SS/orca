package com.orca.compiler.core.codegen;

import java.io.IOException;
import java.io.OutputStream;
import java.time.Instant;
import java.util.List;
import java.util.Properties;

import com.google.common.base.Optional;
import com.orca.compiler.core.CompilationContext;
import com.orca.compiler.core.OrcaVersion;
import com.orca.compiler.core.text.TextSource;

/**
 * Immutable snapshot of a compilation run, embedded in every emitted JAR.
 * Readable at runtime via {@code META-INF/orca/compilation.properties}.
 *
 * Design note: to extend the metadata (e.g. dependency graph, type exports),
 * add fields here and update {@link #serialize} — no other layer needs
 * touching.
 */
public record CompilationMetadata(
        String compilerVersion,
        String languageVersion,
        Instant compiledAt,
        Optional<String> mainClass,
        List<String> sources,
        List<String> emittedTypes
        ) {

    public static final String RESOURCE_PATH = "META-INF/orca/compilation.properties";

    @SuppressWarnings("null")
    public static CompilationMetadata of(CompilationContext context, List<String> emittedTypes) {
        List<String> sourceNames = context.arguments().getSources().stream()
                .map(TextSource::name)
                .toList();

        return new CompilationMetadata(
                OrcaVersion.COMPILER_VERSION,
                OrcaVersion.LANGUAGE_VERSION,
                Instant.now(),
                Optional.fromNullable(context.getMainClassName()),
                sourceNames,
                emittedTypes
        );
    }

    public void serialize(OutputStream out) throws IOException {
        var props = new Properties();
        props.setProperty("compiler.version", compilerVersion);
        props.setProperty("language.version", languageVersion);
        props.setProperty("compiled.at", compiledAt.toString());
        if (mainClass.isPresent()) {
            props.setProperty("main.class", mainClass.get());
        }
        props.setProperty("sources", String.join(",", sources));
        props.setProperty("emitted.types", String.join(",", emittedTypes));
        props.store(out, "Orca Compilation Metadata");
    }
}
