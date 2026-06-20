package com.orca.compiler.core.text;

import java.util.Iterator;

import com.orca.compiler.core.syntax.nodes.IdentifierSyntax;
import com.orca.compiler.core.syntax.nodes.QualifiedIdentifierSyntax;
import com.orca.compiler.core.syntax.nodes.SimpleIdentifierSyntax;
import com.orca.compiler.core.syntax.nodes.SpecialTypeIdentifierSyntax;

public final class ScopeQualifier implements Iterable<String> {

    private final String name;
    private ScopeQualifier next;

    public ScopeQualifier(String name) {
        this.name = name;
    }

    public boolean hasNext() {
        return next != null;
    }

    public void append(String name) {
        if (next != null) {
            next.append(name);
            return;
        }

        next = new ScopeQualifier(name);
    }

    public ScopeQualifier next() {
        return next;
    }

    public String head() {
        return name;
    }

    public String getFullName() {
        if (next == null) {
            return name;
        }

        return name + "." + next.getFullName();
    }

    public int size() {
        int size = 0;
        for (var current = this; current != null; current = current.next) {
            size++;
        }

        return size;
    }

    public boolean isParentOf(ScopeQualifier other) {
        if (other == null) {
            return false;
        }

        var currentThis = this;
        var currentOther = other;

        while (currentThis != null && currentOther != null) {
            if (!currentThis.name.equals(currentOther.name)) {
                return false;
            }

            currentThis = currentThis.next;
            currentOther = currentOther.next;
        }

        return currentThis == null;
    }

    public ScopeQualifier relativeTo(ScopeQualifier parent) {
        if (parent == null) {
            return this;
        }

        var currentThis = this;
        var currentParent = parent;

        while (currentThis != null && currentParent != null) {
            if (!currentThis.name.equals(currentParent.name)) {
                return null;
            }

            currentThis = currentThis.next;
            currentParent = currentParent.next;
        }

        if (currentParent != null) {
            return null;
        }

        return currentThis;
    }

    public ScopeQualifier take(int n) {
        if (n <= 0) {
            return null;
        }

        var result = new ScopeQualifier(name);
        var currentResult = result;
        var currentThis = next;

        for (int i = 1; i < n; i++) {
            if (currentThis == null) {
                throw new IllegalArgumentException("Cannot take " + n + " segments from a qualifier with only " + i + " segments.");
            }

            currentResult.next = new ScopeQualifier(currentThis.name);
            currentResult = currentResult.next;
            currentThis = currentThis.next;
        }

        return result;
    }

    public String[] segmentsFromLeftToRight() {
        String[] segments = new String[size()];

        int index = 0;
        for (var current = this; current != null; current = current.next) {
            segments[index++] = current.name;
        }

        return segments;
    }

    public String[] segmentsFromRightToLeft() {
        String[] segments = new String[size()];

        int index = segments.length - 1;
        for (var current = this; current != null; current = current.next) {
            segments[index--] = current.name;
        }

        return segments;
    }

    @Override
    public Iterator<String> iterator() {
        return new Iterator<>() {
            private ScopeQualifier current = ScopeQualifier.this;

            @Override
            public boolean hasNext() {
                return current != null;
            }

            @Override
            public String next() {
                if (current == null) {
                    throw new java.util.NoSuchElementException();
                }

                String result = current.name;
                current = current.next;
                return result;
            }
        };
    }

    @Override
    public String toString() {
        return getFullName();
    }

    public static ScopeQualifier of(String name) {
        return of(name.split("\\."));
    }

    public static ScopeQualifier of(IdentifierSyntax identifier) {
        return switch (identifier) {
            case SpecialTypeIdentifierSyntax special ->
                new ScopeQualifier(special.text());
            case SimpleIdentifierSyntax simple -> {
                yield new ScopeQualifier(simple.name());
            }
            case QualifiedIdentifierSyntax qualified -> {
                var left = of(qualified.left());
                left.append(qualified.right().name());

                yield left;
            }
        };
    }

    public static ScopeQualifier of(String... segments) {
        if (segments.length == 0) {
            return null;
        }

        ScopeQualifier result = new ScopeQualifier(segments[0]);
        for (int i = 1; i < segments.length; i++) {
            result.append(segments[i]);
        }

        return result;
    }
}
