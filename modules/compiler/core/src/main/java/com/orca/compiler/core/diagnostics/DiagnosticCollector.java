package com.orca.compiler.core.diagnostics;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Stream;

import com.orca.compiler.core.diagnostics.attachments.DiagnosticAttachment;
import com.orca.compiler.core.text.IHaveSpan;

/**
 * Collecte centralisée des diagnostics (erreurs, avertissements, infos).
 * Remplace les méthodes statiques de reporting dans Compiler.
 */
public class DiagnosticCollector implements Iterable<Diagnostic> {

    private final List<Diagnostic> diagnostics = new ArrayList<>();

    public DiagnosticBag freeze() {
        return new DiagnosticBag(List.copyOf(diagnostics));
    }

    public static DiagnosticCollector copyOf(DiagnosticCollector other) {
        var copy = new DiagnosticCollector();
        copy.diagnostics.addAll(other.diagnostics);
        return copy;
    }

    public static DiagnosticCollector merge(DiagnosticCollector first, DiagnosticCollector second) {
        var merged = new DiagnosticCollector();
        merged.diagnostics.addAll(first.diagnostics);
        merged.diagnostics.addAll(second.diagnostics);
        return merged;
    }

    public static DiagnosticCollector merge(DiagnosticCollector... collectors) {
        var merged = new DiagnosticCollector();
        for (DiagnosticCollector collector : collectors) {
            merged.diagnostics.addAll(collector.diagnostics);
        }
        return merged;
    }

    public boolean hasAny() {
        return !diagnostics.isEmpty();
    }

    public Stream<Diagnostic> stream() {
        return diagnostics.stream();
    }

    public List<Diagnostic> getErrors() {
        return diagnostics.stream()
                .filter(d -> d.severity() == DiagnosticSeverity.ERROR)
                .toList();
    }

    public Diagnostic getFirstError() {
        return diagnostics.stream()
                .filter(d -> d.severity() == DiagnosticSeverity.ERROR)
                .findFirst()
                .orElse(null);
    }

    public void mergeFrom(DiagnosticCollector other) {
        diagnostics.addAll(other.diagnostics);
    }

    public void mergeFrom(DiagnosticBag bag) {
        diagnostics.addAll(bag.toList());
    }

    public void mergeFrom(DiagnosticBag... bags) {
        for (DiagnosticBag bag : bags) {
            diagnostics.addAll(bag.toList());
        }
    }

    /**
     * Ajoute un diagnostic.
     */
    public void report(Diagnostic diagnostic) {
        diagnostics.add(diagnostic);
    }

    /**
     * Ajoute un diagnostic avec message, location et severity.
     */
    public void report(DiagnosticCode code, String message, DiagnosticSeverity severity, List<DiagnosticAttachment> attachments) {
        diagnostics.add(new Diagnostic(code, message, severity, attachments));
    }

    public void reportError(DiagnosticCode code, IHaveSpan span, String message) {
        diagnostics.add(
                DiagnosticBuilder.from(code)
                        .withCodeSnippet(span, message)
                        .build()
        );
    }

    /**
     * Vérifie s'il y a un diagnostic avec le code spécifié.
     */
    public boolean hasDiagnostic(DiagnosticCode code) {
        return diagnostics.stream().anyMatch(d -> d.code() == code);
    }

    public int countErrors() {
        return (int) diagnostics.stream().filter(d -> d.severity() == DiagnosticSeverity.ERROR).count();
    }

    public int countWarnings() {
        return (int) diagnostics.stream().filter(d -> d.severity() == DiagnosticSeverity.WARNING).count();
    }

    /**
     * Retourne la liste des diagnostics.
     */
    public List<Diagnostic> diagnostics() {
        return Collections.unmodifiableList(diagnostics);
    }

    /**
     * Vérifie s'il y a des erreurs.
     */
    public boolean hasErrors() {
        return diagnostics.stream()
                .anyMatch(d -> d.severity() == DiagnosticSeverity.ERROR);
    }

    /**
     * Efface tous les diagnostics. Utile pour les tests.
     */
    public void clear() {
        diagnostics.clear();
    }

    /**
     * Retourne le nombre de diagnostics.
     */
    public int count() {
        return diagnostics.size();
    }

    @Override
    public Iterator<Diagnostic> iterator() {
        return diagnostics.iterator();
    }
}
