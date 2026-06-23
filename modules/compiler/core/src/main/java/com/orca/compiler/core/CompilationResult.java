package com.orca.compiler.core;

import com.google.common.base.Preconditions;
import com.orca.compiler.core.diagnostics.DiagnosticCollector;

/**
 * A sealed interface representing the result of a compilation, which can be
 * either a success or a failure. A successful compilation contains the
 * diagnostics and the result of the compilation, while a failed compilation
 * contains only the diagnostics produced during compilation.
 */
public sealed interface CompilationResult<T> {

    /**
     * Represents a successful compilation result, containing the diagnostics
     * and the result of the compilation.
     *
     * @param <T> the type of the value produced by the compilation
     * @param diagnostics the diagnostic collector containing any diagnostics
     * produced during compilation
     * @param value the result of the compilation
     * @return a {@link CompilationResult} representing a successful compilation
     * with the given diagnostics and result
     */
    public static record Success<T>(DiagnosticCollector diagnostics, T value) implements CompilationResult<T> {

    }

    /**
     * Represents a failed compilation result, containing the diagnostics
     * produced during compilation.
     *
     * @param <T> the type of the result
     * @param diagnostics the diagnostic collector containing any diagnostics
     * produced during compilation
     * @return a {@link CompilationResult} representing a failed compilation
     * with the given diagnostics
     */
    public static record Failure<T>(DiagnosticCollector diagnostics) implements CompilationResult<T> {

    }

    /**
     * Returns the diagnostic collector containing any diagnostics produced
     * during compilation.
     *
     * @return the {@link DiagnosticCollector} containing any diagnostics
     * produced during compilation.
     */
    DiagnosticCollector diagnostics();

    /**
     * Returns true if the compilation was successful and a result is available.
     * Returns false if the compilation failed and no result is available.
     *
     * @return true if the compilation was successful, false otherwise
     */
    default boolean isSuccess() {
        return this instanceof Success;
    }

    /**
     * Returns true if the compilation failed and no result is available.
     *
     * @return true if the compilation failed, false otherwise
     */
    default boolean isFailure() {
        return this instanceof Failure;
    }

    /**
     * Returns the result of the compilation if it was successful.
     *
     * @param <T> the type of the result
     * @param diagnostics the diagnostic collector containing any diagnostics
     * produced during compilation
     * @param result the result of the compilation
     * @return a {@link CompilationResult} representing a successful compilation
     * with the given diagnostics and result
     */
    public static <T> CompilationResult<T> success(DiagnosticCollector diagnostics, T result) {
        Preconditions.checkNotNull(result, "Result cannot be null for a successful compilation.");
        Preconditions.checkNotNull(diagnostics, "Diagnostics cannot be null for a successful compilation.");
        Preconditions.checkState(!diagnostics.hasErrors(), "Diagnostics cannot contain errors for a successful compilation.");

        return new Success<>(DiagnosticCollector.copyOf(diagnostics), result);
    }

    /**
     * Returns a {@link CompilationResult} representing a successful compilation
     * with the given diagnostics and no result.
     *
     * @param diagnostics the diagnostic collector containing any diagnostics
     * produced during compilation
     * @return a {@link CompilationResult} representing a successful compilation
     * with the given diagnostics and no result
     */
    public static CompilationResult<Void> success(DiagnosticCollector diagnostics) {
        Preconditions.checkNotNull(diagnostics, "Diagnostics cannot be null for a successful compilation.");
        Preconditions.checkState(!diagnostics.hasErrors(), "Diagnostics cannot contain errors for a successful compilation.");

        return new Success<>(DiagnosticCollector.copyOf(diagnostics), null);
    }

    /**
     * Returns a {@link CompilationResult} representing a failed compilation
     * with the given diagnostics.
     *
     * @param <T> the type of the result
     * @param diagnostics the diagnostic collector containing any diagnostics
     * produced during compilation
     * @return a {@link CompilationResult} representing a failed compilation
     * with the given diagnostics
     */
    public static <T> CompilationResult<T> failure(DiagnosticCollector diagnostics) {
        Preconditions.checkNotNull(diagnostics, "Diagnostics cannot be null for a failed compilation.");

        return new Failure<>(DiagnosticCollector.copyOf(diagnostics));
    }
}
