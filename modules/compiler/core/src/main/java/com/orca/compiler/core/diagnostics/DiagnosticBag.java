package com.orca.compiler.core.diagnostics;

import java.util.Iterator;
import java.util.List;
import java.util.stream.Stream;

/**
 * Represents an immutable collection of diagnostics. This class is used to
 * store and manage diagnostics generated during the compilation process.
 */
public final class DiagnosticBag implements Iterable<Diagnostic> {

    private final List<Diagnostic> diagnostics;

    public DiagnosticBag(List<Diagnostic> diagnostics) {
        this.diagnostics = List.copyOf(diagnostics);
    }

    /**
     * Creates a new {@link DiagnosticBag} containing a single diagnostic.
     *
     * @param diagnostic The diagnostic to include in the bag.
     * @return A new {@link DiagnosticBag} containing the specified diagnostic.
     */
    public static DiagnosticBag single(Diagnostic diagnostic) {
        return new DiagnosticBag(List.of(diagnostic));
    }

    /**
     * Checks if the diagnostic bag contains any diagnostics.
     *
     * @return true if there are diagnostics, false otherwise.
     */
    public boolean any() {
        return !diagnostics.isEmpty();
    }

    /**
     * Checks if the diagnostic bag contains any diagnostics with severity level
     * ERROR.
     *
     * @return true if there are diagnostics with severity level ERROR, false
     * otherwise.
     */
    public boolean anyError() {
        return diagnostics.stream().anyMatch(d -> d.severity() == DiagnosticSeverity.ERROR);
    }

    /**
     * Checks if the diagnostic bag contains any diagnostics with severity level
     * WARNING.
     *
     * @return true if there are diagnostics with severity level WARNING, false
     * otherwise.
     */
    public boolean anyWarning() {
        return diagnostics.stream().anyMatch(d -> d.severity() == DiagnosticSeverity.WARNING);
    }

    /**
     * Returns a stream of diagnostics contained in the bag.
     *
     * @return A stream of diagnostics.
     */
    public Stream<Diagnostic> stream() {
        return diagnostics.stream();
    }

    /**
     * Returns a stream of diagnostics with severity level ERROR.
     *
     * @return A stream of diagnostics with severity level ERROR.
     */
    public Stream<Diagnostic> streamErrors() {
        return diagnostics.stream().filter(d -> d.severity() == DiagnosticSeverity.ERROR);
    }

    /**
     * Returns a stream of diagnostics with severity level WARNING.
     *
     * @return A stream of diagnostics with severity level WARNING.
     */
    public Stream<Diagnostic> streamWarnings() {
        return diagnostics.stream().filter(d -> d.severity() == DiagnosticSeverity.WARNING);
    }

    /**
     * Returns a stream of diagnostics with severity level INFO.
     *
     * @return A stream of diagnostics with severity level INFO.
     */
    public Stream<Diagnostic> streamInfos() {
        return diagnostics.stream().filter(d -> d.severity() == DiagnosticSeverity.INFO);
    }

    /**
     * Returns the diagnostics as a list.
     *
     * @return A list of diagnostics.
     */
    public List<Diagnostic> toList() {
        return diagnostics;
    }

    /**
     * Returns the number of diagnostics in the bag.
     *
     * @return The number of diagnostics in the bag.
     */
    public int count() {
        return diagnostics.size();
    }

    /**
     * Returns the number of diagnostics with severity level ERROR in the bag.
     *
     * @return The number of diagnostics with severity level ERROR in the bag.
     */
    public int countErrors() {
        return (int) streamErrors().count();
    }

    /**
     * Returns the number of diagnostics with severity level WARNING in the bag.
     *
     * @return The number of diagnostics with severity level WARNING in the bag.
     */
    public int countWarnings() {
        return (int) streamWarnings().count();
    }

    /**
     * Returns the number of diagnostics with severity level INFO in the bag.
     *
     * @return The number of diagnostics with severity level INFO in the bag.
     */
    public int countInfos() {
        return (int) streamInfos().count();
    }

    /**
     * Returns the number of diagnostics in the bag.
     *
     * @param bag1 The first {@link DiagnosticBag} to merge.
     * @param bag2 The second {@link DiagnosticBag} to merge.
     * @return The merged {@link DiagnosticBag}.
     */
    public static DiagnosticBag merge(DiagnosticBag bag1, DiagnosticBag bag2) {
        var mergedDiagnostics = new java.util.ArrayList<Diagnostic>();
        mergedDiagnostics.addAll(bag1.diagnostics);
        mergedDiagnostics.addAll(bag2.diagnostics);
        return new DiagnosticBag(mergedDiagnostics);
    }

    /**
     * Merges multiple {@link DiagnosticBag} instances into a single
     * {@link DiagnosticBag}.
     *
     * @param bags The {@link DiagnosticBag} instances to merge.
     * @return A new {@link DiagnosticBag} containing all diagnostics from the
     * provided bags.
     */
    public static DiagnosticBag merge(DiagnosticBag... bags) {
        var mergedDiagnostics = new java.util.ArrayList<Diagnostic>();
        for (var bag : bags) {
            mergedDiagnostics.addAll(bag.diagnostics);
        }
        return new DiagnosticBag(mergedDiagnostics);
    }

    @Override
    public Iterator<Diagnostic> iterator() {
        return diagnostics.iterator();
    }

}
