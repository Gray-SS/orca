package com.orca.compiler.core.text;

/**
 * An interface for things that have a source span.
 * This is used for symbols, syntax nodes, and other entities that can be traced back to a location in the source code.
 */
public interface IHaveSpan extends IHaveLocation {
    /**
     * Gets the source span of this entity.
     * For symbols, this is typically the span of the syntax node that declares the symbol.
     * For syntax nodes, this is the span of the node itself.
     * @return
     */
    SourceSpan span();

    /**
     * Gets the text of the source code corresponding to this entity's span.
     * @return the text of the source code corresponding to this entity's span
     */
    default String text() {
        var span = span();
        return span.source().getText(span);
    }

    /**
     * Gets the location of this entity.
     * @return the location of this entity
     */
    default SourceLocation location() {
        return span().loc();
    }
}
