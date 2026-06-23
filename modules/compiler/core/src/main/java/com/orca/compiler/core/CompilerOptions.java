package com.orca.compiler.core;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.google.common.base.Preconditions;
import com.orca.compiler.core.externals.ClassPath;
import com.orca.compiler.core.text.FileSource;
import com.orca.compiler.core.text.TextSource;

/**
 * Represent the configuration options for the compiler, including input
 * sources, output path, and various flags that control the compilation process.
 */
public final class CompilerOptions {

    private final Path outputPath;
    private final OutputKind outputKind;
    private final OutputFormat outputFormat;
    private final Set<ClassPath> classPaths;
    private final Set<TextSource> sources;
    private final boolean includeJdk;

    private CompilerOptions(Path outputPath, OutputKind outputKind, OutputFormat outputFormat, Set<ClassPath> classPaths, Set<TextSource> sources, boolean includeJdk) {
        Preconditions.checkNotNull(outputPath, "Output path cannot be null");
        Preconditions.checkNotNull(outputKind, "Output kind cannot be null");
        Preconditions.checkNotNull(outputFormat, "Output format cannot be null");
        Preconditions.checkNotNull(classPaths, "Classpath entries cannot be null");
        Preconditions.checkNotNull(sources, "Source files cannot be null");

        this.sources = Set.copyOf(sources);
        this.classPaths = Set.copyOf(classPaths);
        this.outputPath = outputPath;
        this.outputKind = outputKind;
        this.outputFormat = outputFormat;
        this.includeJdk = includeJdk;
    }

    /**
     * Returns a new {@link Builder} instance for constructing
     * {@link CompilerOptions}.
     *
     * @return a new {@link Builder} instance
     */
    public static Builder builder() {
        return new Builder();
    }

    /**
     * Checks if the compiler is configured to include the JDK in the classpath.
     *
     * @return true if the JDK should be included, false otherwise
     */
    public boolean includeJdk() {
        return includeJdk;
    }

    /**
     * Returns the output kind configured for the compiler.
     *
     * @return the output kind configured for the compiler
     */
    public OutputKind getOutputKind() {
        return outputKind;
    }

    /**
     * Returns the output format configured for the compiler.
     *
     * @return the output format configured for the compiler
     */
    public OutputFormat getOutputFormat() {
        return outputFormat;
    }

    /**
     * Returns the output path configured for the compiler. This is where the
     * compiler will write the compiled class files or jar file. If the user has
     * not specified an output path, this will return the default output path.
     *
     * @return the output path for the compiled program
     */
    public Path getOutputPath() {
        return outputPath;
    }

    /**
     * Returns the set of classpath entries configured for the compiler.
     *
     * @return the set of classpath entries
     */
    public Set<ClassPath> getClassPaths() {
        return Set.copyOf(classPaths);
    }

    /**
     * Returns the set of source files configured for the compiler.
     *
     * @return the set of source files
     */
    public Set<TextSource> getSources() {
        return Set.copyOf(sources);
    }

    /**
     * Returns the number of source files configured for the compiler.
     *
     * @return the number of source files
     */
    public int sourcesCount() {
        return sources.size();
    }

    /**
     * Returns the number of classpath entries configured for the compiler.
     *
     * @return the number of classpath entries
     */
    public int classPathsCount() {
        return classPaths.size();
    }

    /**
     * A builder for creating instances of {@link CompilerOptions}.
     */
    public static final class Builder {

        private Path outputPath;
        private OutputKind outputKind;
        private OutputFormat outputFormat;
        private boolean includeJdk = true; // Default to including the JDK in the classpath

        private final Set<ClassPath> classPaths = new HashSet<>();
        private final Set<TextSource> sources = new HashSet<>();

        private Builder() {
        }

        /**
         * Configures whether the compiler should include the JDK in the
         * classpath when resolving external symbols. By default, the compiler
         * includes the JDK in the classpath.
         *
         * @param includeJdk true to include the JDK, false to exclude it
         * @return this builder instance for chaining
         */
        public Builder setIncludeJdk(boolean includeJdk) {
            this.includeJdk = includeJdk;
            return this;
        }

        /**
         * Sets the output path for the compiled program. This is where the
         * compiler will write the compiled class files or jar file. If not set,
         * the compiler will use the default output path.
         *
         * @param outputPath the output path to set
         * @return this builder instance for chaining
         */
        public Builder setOutputPath(Path outputPath) {
            this.outputPath = outputPath;
            return this;
        }

        /**
         * Configures the output kind for the compiler. See {@link OutputKind}
         * enum for details on the available output kinds.
         *
         * @param outputKind the output kind to set
         * @return this builder instance for chaining
         */
        public Builder setOutputKind(OutputKind outputKind) {
            this.outputKind = outputKind;
            return this;
        }

        /**
         * Configures the output format for the compiler. See
         * {@link OutputFormat} enum for details on the available output
         * formats.
         *
         * @param outputFormat the output format to set
         * @return this builder instance for chaining
         */
        public Builder setOutputFormat(OutputFormat outputFormat) {
            this.outputFormat = outputFormat;
            return this;
        }

        /**
         * Adds a source file to the compiler options.
         *
         * @param source the source to add
         * @return this builder instance for chaining
         */
        public Builder addSource(TextSource source) {
            Preconditions.checkNotNull(source, "Source cannot be null");

            this.sources.add(source);
            return this;
        }

        /**
         * Adds a source file to the compiler options from a file path. The file
         * must exist, otherwise a
         * {@link CompilerValidationException.InvalidSourceException} will be
         * thrown.
         *
         * @param filePath the path of the source file to add
         * @return this builder instance for chaining
         * @throws CompilerValidationException.InvalidSourceException if the
         * specified file does not exist
         */
        public Builder addSourceFile(Path filePath) throws CompilerValidationException.InvalidSourceException {
            Preconditions.checkNotNull(filePath, "Source file path cannot be null");

            if (!Files.exists(filePath)) {
                throw new CompilerValidationException.InvalidSourceException(filePath, "Source file not found: " + filePath);
            }

            if (Files.isDirectory(filePath)) {
                throw new CompilerValidationException.InvalidSourceException(filePath, "Expected a file but found a directory: " + filePath);
            }

            this.sources.add(new FileSource(filePath.toString()));
            return this;
        }

        /**
         * Adds all source files from the specified directory to the compiler
         * options. The directory must exist, otherwise a
         * {@link CompilerValidationException.InvalidSourceException} will be
         * thrown. Only files with the
         * {@link OrcaConstants.SOURCE_FILE_EXTENSION} will be added as sources.
         *
         * @param directoryPath the path of the directory containing source
         * files to add
         * @return this builder instance for chaining
         * @throws CompilerValidationException.InvalidSourceException if the
         * specified directory does not exist
         */
        public Builder addSourceDirectory(Path directoryPath) throws CompilerValidationException.InvalidSourceException {
            Preconditions.checkNotNull(directoryPath, "Source directory path cannot be null");

            if (!Files.exists(directoryPath)) {
                throw new CompilerValidationException.InvalidSourceException(directoryPath, "Source directory not found: " + directoryPath);
            }

            if (!Files.isDirectory(directoryPath)) {
                throw new CompilerValidationException.InvalidSourceException(directoryPath, "Expected a directory but found a file: " + directoryPath);
            }

            try (var files = Files.walk(directoryPath)) {
                files.filter((f) -> Files.isRegularFile(f) && f.toString().endsWith(".orca"))
                        .forEach(f -> this.sources.add(new FileSource(f.toString())));
            } catch (Exception e) {
                throw new RuntimeException("Failed to read source directory: " + directoryPath, e);
            }

            return this;
        }

        /**
         * Adds multiple source files to the compiler options.
         *
         * @param sources the list of sources to add
         * @return this builder instance for chaining
         */
        public Builder addSources(List<? extends TextSource> sources) {
            for (TextSource source : sources) {
                addSource(source);
            }

            return this;
        }

        /**
         * Adds a classpath entry to the compiler options.
         *
         * @param entry the classpath entry to add
         * @return this builder instance for chaining
         */
        public Builder addClassPath(ClassPath entry) {
            Preconditions.checkNotNull(entry, "ClassPath entry cannot be null");

            this.classPaths.add(entry);
            return this;
        }

        /**
         * Adds multiple classpath entries to the compiler options.
         *
         * @param entries the list of classpath entries to add
         * @return this builder instance for chaining
         */
        public Builder addClassPaths(List<? extends ClassPath> entries) {
            for (ClassPath entry : entries) {
                addClassPath(entry);
            }
            return this;
        }

        /**
         * Builds a {@link CompilerOptions} instance based on the configuration
         * provided to this builder. If certain options were not explicitly set,
         * default values will be used.
         *
         * @return a new {@link CompilerOptions} instance with the configured
         * settings
         */
        public CompilerOptions build() {
            setDefaults();
            return new CompilerOptions(
                    outputPath,
                    outputKind,
                    outputFormat,
                    classPaths,
                    sources,
                    includeJdk
            );
        }

        private void setDefaults() {
            if (outputKind == null) {
                outputKind = OutputKind.APPLICATION;
            }

            if (outputFormat == null) {
                outputFormat = OutputFormat.JAR;
            }

            if (outputPath == null) {
                outputPath = getDefaultOutputPathFromOutputFormat(outputFormat);
            }
        }

        private static Path getDefaultOutputPathFromOutputFormat(OutputFormat outputFormat) {
            return switch (outputFormat) {
                case JAR ->
                    Path.of("output.jar");
                case CLASS_DIRECTORY ->
                    Path.of("output");
            };
        }
    }
}
