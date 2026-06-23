package com.orca.cli;

import com.orca.compiler.core.CompilerOptions;

/**
 * Represents the parsed command-line options for the Orca compiler. This class
 * encapsulates the various flags and settings that can be specified by the user
 * when invoking the compiler from the command line.
 */
public final class CommandLineOptions {

    private final int indentSize;
    private final boolean executeCompiledProgram;
    private final boolean showHelp;
    private final boolean showVersion;
    private final boolean showStackTrace;

    private final EmitKind emitKind;
    private final CompilerOptions compilerOptions;

    private CommandLineOptions(CompilerOptions compilerOptions, int indentSize, EmitKind emitKind, boolean executeCompiledProgram, boolean showHelp, boolean showVersion, boolean showStackTrace) {
        this.indentSize = indentSize;
        this.emitKind = emitKind;
        this.compilerOptions = compilerOptions;
        this.executeCompiledProgram = executeCompiledProgram;
        this.showHelp = showHelp;
        this.showVersion = showVersion;
        this.showStackTrace = showStackTrace;
    }

    /**
     * Returns the number of spaces to use for indentation when printing.
     *
     * @return the number of spaces to use for indentation
     */
    public int getIndentSize() {
        return indentSize;
    }

    /**
     * Returns whether the help message should be displayed.
     *
     * @return true if the help message should be displayed, false otherwise
     */
    public boolean shouldShowHelp() {
        return showHelp;
    }

    /**
     * Returns whether the stack trace should be displayed when an error occurs.
     *
     * @return true if the stack trace should be displayed, false otherwise
     */
    public boolean shouldShowStackTrace() {
        return showStackTrace;
    }

    /**
     * Returns whether the version information should be displayed.
     *
     * @return true if the version information should be displayed, false
     * otherwise
     */
    public boolean shouldShowVersion() {
        return showVersion;
    }

    /**
     * Returns whether the compiled program should be executed.
     *
     * @return true if the compiled program should be executed, false otherwise
     */
    public boolean shouldExecuteCompiledProgram() {
        return executeCompiledProgram;
    }

    /**
     * Returns the kind of output to emit. See {@link EmitKind} for more details
     * on the available options.
     *
     * @return the kind of output to emit
     */
    public EmitKind getEmitKind() {
        return emitKind;
    }

    /**
     * Returns the compiler options that were parsed from the command-line
     * arguments. These options include the input sources, output path, and
     * various flags that control the compilation process.
     *
     * @return the compiler options parsed from the command-line arguments
     */
    public CompilerOptions getCompilerOptions() {
        return compilerOptions;
    }

    /**
     * Builder class for constructing instances of {@link CommandLineOptions}.
     * This class provides a fluent API for setting the various options and
     * flags that can be specified when invoking the Orca compiler from the
     * command line.
     */
    public static final class Builder {

        private int indentSize = 4;
        private EmitKind emitKind;
        private boolean executeCompiledProgram = false;

        private boolean showHelp = false;
        private boolean showVersion = false;
        private boolean showStackTrace = false;

        private Builder() {
        }

        /**
         * Creates a new instance of the {@link Builder} class for constructing
         * instances of {@link CommandLineOptions}.
         *
         * @return a new instance of the {@link Builder} class
         */
        public static Builder builder() {
            return new Builder();
        }

        /**
         * Sets whether the help message should be displayed when the compiler
         * is invoked. If this flag is set to true, the compiler will print the
         * usage information and exit without performing any compilation.
         *
         * @param showHelp true to display the help message, false otherwise
         * @return the current instance of the {@link Builder} class for method
         * chaining
         */
        public Builder setShowHelp(boolean showHelp) {
            this.showHelp = showHelp;
            return this;
        }

        /**
         * Sets whether the stack trace should be displayed when an error occurs
         * during compilation. If this flag is set to true, the compiler will
         * print the full stack trace of any exceptions that are thrown, which
         * can be useful for debugging purposes.
         *
         * @param showStackTrace true to display the stack trace, false
         * otherwise
         * @return the current instance of the {@link Builder} class for method
         * chaining
         */
        public Builder setShowStackTrace(boolean showStackTrace) {
            this.showStackTrace = showStackTrace;
            return this;
        }

        /**
         * Sets whether the version information should be displayed when the
         * compiler is invoked. If this flag is set to true, the compiler will
         * print the version information and exit without performing any
         * compilation.
         *
         * @param showVersion true to display the version information, false
         * otherwise
         * @return the current instance of the {@link Builder} class for method
         * chaining
         */
        public Builder setShowVersion(boolean showVersion) {
            this.showVersion = showVersion;
            return this;
        }

        /**
         * Sets the number of spaces to use for indentation when printing output
         * such as the AST or tokens. This value is used by the compiler when
         * generating formatted output for display to the user.
         *
         * @param indentSize the number of spaces to use for indentation
         * @return the current instance of the {@link Builder} class for method
         * chaining
         */
        public Builder setIndentSize(int indentSize) {
            this.indentSize = indentSize;
            return this;
        }

        /**
         * Sets the kind of output to emit when the compiler is invoked. The
         * available options are defined in the {@link EmitKind} enum. This
         * value determines what kind of output the compiler will produce, such
         * as tokens, AST, IR, symbols, or bytecode.
         *
         * @param emitKind the kind of output to emit
         * @return the current instance of the {@link Builder} class for method
         * chaining
         */
        public Builder setEmitKind(EmitKind emitKind) {
            this.emitKind = emitKind;
            return this;
        }

        /**
         * Sets whether the compiled program should be executed after
         * compilation. If this flag is set to true, the compiler will attempt
         * to run the compiled program after successfully compiling the input
         * sources.
         *
         * @param executeCompiledProgram true to execute the compiled program,
         * false otherwise
         * @return the current instance of the {@link Builder} class for method
         * chaining
         */
        public Builder setExecuteCompiledProgram(boolean executeCompiledProgram) {
            this.executeCompiledProgram = executeCompiledProgram;
            return this;
        }

        /**
         * Builds and returns a new instance of {@link CommandLineOptions} based
         * on the current state of the builder. This method constructs a new
         * {@link CommandLineOptions} object with the specified options and
         * flags that have been set on the builder.
         *
         * @param compilerOptions the compiler options to associate with the
         * {@link CommandLineOptions} instance
         * @return a new instance of {@link CommandLineOptions} with the
         * specified options and flags
         */
        public CommandLineOptions build(CompilerOptions compilerOptions) {
            return new CommandLineOptions(
                    compilerOptions,
                    indentSize,
                    emitKind,
                    executeCompiledProgram,
                    showHelp,
                    showVersion,
                    showStackTrace
            );
        }
    }
}
