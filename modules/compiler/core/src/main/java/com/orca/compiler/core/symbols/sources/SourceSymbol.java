package com.orca.compiler.core.symbols.sources;

import com.orca.compiler.core.Debug;
import com.orca.compiler.core.symbols.Symbol;
import com.orca.compiler.core.symbols.SymbolModifiers;
import com.orca.compiler.core.syntax.SyntaxNode;
import com.orca.compiler.core.text.IHaveSpan;
import com.orca.compiler.core.text.SourceSpan;

public interface SourceSymbol extends Symbol, IHaveSpan {

    /**
     * Gets the syntax node that declares this symbol.
     *
     * @return The syntax node that declares this symbol.
     */
    SyntaxNode declaringSyntax();

    /**
     * Gets the source span of this symbol, which is the span of its declaring
     * syntax.
     *
     * @return The source span of this symbol.
     */
    @Override
    default SourceSpan span() {
        var syntax = declaringSyntax();
        Debug.requireNotNull(syntax, "Source symbols must have a declaring syntax.");

        return syntax.span();
    }

    @Override
    default int getModifiers() {
        return Symbol.super.getModifiers() | SymbolModifiers.SOURCE;
    }
}
