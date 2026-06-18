package com.orca.compiler.core.semantics;

import java.util.List;

import com.orca.compiler.core.Debug;

/**
 * Represents the result of a symbol lookup operation.
 */
public class LookupResult<T> {

    private final Kind kind;
    private final List<T> candidates;

    private LookupResult(Kind kind, List<T> candidates) {
        this.kind = kind;
        this.candidates = candidates;
    }

    /**
     * Gets the kind of the lookup result, indicating whether it represents:
     * <br/>
     * - A successful single match, <br/>
     * - An ambiguous match with multiple candidates, <br/>
     * - No match at all. <br/>
     *
     * @return The kind of the lookup result.
     */
    public Kind kind() {
        return kind;
    }

    /**
     * Gets the single symbol from the lookup result. This should only be called
     * if the lookup result is of kind SINGLE_MATCH.
     *
     * @return The single symbol that was matched.
     */
    public T getSingle() {
        Debug.assertTrue(kind == Kind.SINGLE_MATCH, "Lookup result does not contain a single match.");
        return candidates.get(0);
    }

    /**
     * Gets the list of candidate symbols from the lookup result. This should
     * only be called if the lookup result is of kind AMBIGUOUS.
     *
     * @return The list of candidate symbols that were matched.
     */
    public List<T> getCandidates() {
        Debug.assertTrue(kind == Kind.AMBIGUOUS, "Lookup result does not contain multiple candidates.");
        return candidates;
    }

    /**
     * Checks if the lookup result represents a successful single match.
     *
     * @return True if the lookup result is a single match, false otherwise.
     */
    public boolean isSingleMatch() {
        return kind == Kind.SINGLE_MATCH;
    }

    /**
     * Checks if the lookup result represents an ambiguous match with multiple
     * candidates.
     *
     * @return True if the lookup result is ambiguous, false otherwise.
     */
    public boolean isAmbiguous() {
        return kind == Kind.AMBIGUOUS;
    }

    /**
     * Checks if the lookup result represents no match found.
     *
     * @return True if the lookup result is no match, false otherwise.
     */
    public boolean isNoMatch() {
        return kind == Kind.NO_MATCH;
    }

    /**
     * Creates a lookup result representing a successful single match with the
     * given symbol.
     *
     * @param symbol The symbol that was successfully matched. Must not be null.
     * @return A lookup result representing a successful single match.
     */
    public static <T> LookupResult<T> singleMatch(T symbol) {
        Debug.requireNotNull(symbol, "Resolved symbol cannot be null for a resolved lookup result.");
        return new LookupResult<>(Kind.SINGLE_MATCH, List.of(symbol));
    }

    /**
     * Creates a lookup result representing an ambiguous match with the given
     * list of candidate symbols. The list must contain at least two symbols to
     * be considered ambiguous.
     *
     * @param candidateSymbols The list of candidate symbols that were matched.
     * Must have at least two candidates.
     * @return A lookup result representing an ambiguous match.
     */
    public static <T> LookupResult<T> ambiguousMatch(List<T> candidateSymbols) {
        Debug.requireNotNull(candidateSymbols, "Candidate symbols list cannot be null for an ambiguous lookup result.");
        Debug.assertTrue(candidateSymbols.size() >= 2, "Candidate symbols list must have at least two candidates for an ambiguous lookup result.");

        return new LookupResult<>(Kind.AMBIGUOUS, candidateSymbols);
    }

    /**
     * Creates a lookup result representing no match found. This indicates that
     * the lookup process did not find any symbols matching the criteria.
     *
     * @return A lookup result representing no match found.
     */
    public static <T> LookupResult<T> noMatch() {
        return new LookupResult<>(Kind.NO_MATCH, null);
    }

    /**
     * Represents the kind of a lookup result, indicating whether it is a
     * successful single match, an ambiguous match with multiple candidates, or
     * no match at all.
     */
    public enum Kind {
        /**
         * No match found
         */
        NO_MATCH,
        /**
         * A single match was found
         */
        SINGLE_MATCH,
        /**
         * An ambiguous match was found (i.e., multiple candidates)
         */
        AMBIGUOUS
    }
}
