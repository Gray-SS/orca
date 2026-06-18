package com.orca.compiler.core.semantics;

import javax.annotation.Nonnull;

import com.orca.compiler.core.symbols.Symbol;
import com.orca.compiler.core.syntax.SyntaxNode;
import com.orca.compiler.core.syntax.nodes.IdentifierSyntax;

/**
 * Represents the result of a symbol resolution operation, including the
 * resolved symbol, the syntax node that was used for resolution, and any
 * failure reason if the resolution was unsuccessful.
 */
public record SymbolInfo(
        @Nonnull
        Symbol symbol,
        @Nonnull
        SyntaxNode syntax,
        @Nonnull
        FailureReason failureReason
        ) {

    public SymbolInfo(@Nonnull Symbol symbol, @Nonnull SyntaxNode syntax) {
        this(symbol, syntax, FailureReason.None);
    }

    /**
     * Indicates whether the symbol resolution was successful.
     *
     * @return true if the resolution was successful; false otherwise.
     */
    public boolean isSuccess() {
        return failureReason == FailureReason.None;
    }

    /**
     * Indicates whether the symbol resolution failed.
     *
     * @return true if the resolution failed; false otherwise.
     */
    public boolean isFailure() {
        return failureReason != FailureReason.None;
    }

    /**
     * Creates a SymbolInfo instance representing a failed symbol resolution due
     * to the symbol not being found.
     *
     * @param syntax The syntax node that was used for the resolution attempt.
     * @return A SymbolInfo instance indicating that the symbol was not found.
     */
    public static SymbolInfo notFound(@Nonnull IdentifierSyntax syntax) {
        Symbol missingSymbol = Symbol.missing(syntax.name());
        return new SymbolInfo(missingSymbol, syntax, FailureReason.NotFound);
    }

    /**
     * The reason for a symbol resolution failure. This enum provides specific
     * reasons for why a symbol resolution might fail, allowing for more
     * granular error handling and reporting.
     */
    public enum FailureReason {
        /**
         * Symbol resolution was successful.
         */
        None,
        /**
         * The symbol could not be found in the current context.
         */
        NotFound,
        /**
         * The symbol was found but is ambiguous.
         */
        Ambiguous,
        /**
         * The symbol is not a type or namespace.
         */
        NotATypeOrNamespace,
        /**
         * The symbol is not a value.
         */
        NotAValue,
        /**
         * Static access was attempted on an instance-only symbol.
         */
        StaticAccessOnInstanceOnly,
        /**
         * Instance access was attempted on a static-only symbol.
         */
        InstanceAccessOnStaticOnly,
    }
}
