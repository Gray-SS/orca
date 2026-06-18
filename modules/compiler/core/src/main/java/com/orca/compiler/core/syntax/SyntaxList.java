package com.orca.compiler.core.syntax;

import java.util.List;

import com.orca.compiler.core.text.SourceSpan;

public class SyntaxList<T extends SyntaxNode> extends SyntaxNode implements Iterable<T> {
    private final List<T> elements;
    private final SourceSpan span;

    public SyntaxList(SourceSpan span, List<T> elements) {
        this.elements = elements;
        this.span = span;
    }

    /**
     * Gets the elements of this syntax list.
     * @return The elements of this syntax list.
     */
    public List<T> elements() {
        return elements;
    }

    /**
     * Gets the number of elements in this syntax list.
     * @return The number of elements in this syntax list.
     */
    public int size() {
        return elements.size();
    }

    /**
     * Gets the element at the specified index in this syntax list.
     * @param index The index of the element to get.
     * @return The element at the specified index in this syntax list.
     */
    public T get(int index) {
        return elements.get(index);
    }

    @Override
    public void accept(SyntaxVisitor visitor) {
        visitor.visitSyntaxList(this);
    }

    @Override
    public String toString() {
        return "SyntaxList";
    }

    @Override
    public List<SyntaxNode> children() {
        return List.copyOf(elements);
    }

    @Override
    public SourceSpan span() {
        return span;
    }

    @Override
    public java.util.Iterator<T> iterator() {
        return elements.iterator();
    }
}
