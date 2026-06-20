package com.orca.compiler.core.boundtree;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nullable;

import com.orca.compiler.core.syntax.SyntaxNode;
import com.orca.compiler.core.text.IHaveSpan;
import com.orca.compiler.core.text.SourceLocation;
import com.orca.compiler.core.text.SourceSpan;
import com.orca.compiler.core.text.TextSource;

public abstract class BoundNode implements IHaveSpan {

    private @Nullable
    SyntaxNode syntax;
    private @Nullable
    SourceSpan synthesizedSpan;

    public abstract BoundNodeKind kind();

    public abstract <R> R accept(BoundVisitor<R> visitor);

    public List<BoundNode> children() {
        List<BoundNode> result = new ArrayList<>();
        Class<?> cls = getClass();
        while (cls != null && cls != BoundNode.class) {
            for (Field field : cls.getDeclaredFields()) {
                if (!field.isAnnotationPresent(Child.class)) {
                    continue;
                }
                field.setAccessible(true);
                try {
                    Object value = field.get(this);
                    switch (value) {
                        case null -> {
                        }

                        case BoundNode node ->
                            result.add(node);
                        case List<?> list -> {
                            for (Object item : list) {
                                if (item instanceof BoundNode node) {
                                    result.add(node);
                                }
                            }
                        }
                        default -> {
                        }
                    }
                } catch (IllegalAccessException ignored) {
                }
            }
            cls = cls.getSuperclass();
        }
        return result;
    }

    public @Nullable
    SyntaxNode syntax() {
        return syntax;
    }

    @Override
    public SourceSpan span() {
        if (synthesizedSpan != null) {
            return synthesizedSpan;
        }

        if (syntax != null) {
            return syntax.span();
        }

        return new SourceSpan(new TextSource("<unknown>") {
            @Override
            public String content() {
                return "";
            }

            @Override
            public String formatSpan(SourceSpan span) {
                return "<unknown>";
            }

            @Override
            public String formatLocation(SourceLocation location) {
                return "<unknown>";
            }
        }, 0, 0);
    }

    public boolean isSynthesized() {
        return synthesizedSpan != null;
    }

    public void setSyntax(SyntaxNode syntax) {
        this.syntax = syntax;
    }

    public void setSynthesizedSpan(SourceSpan span) {
        this.synthesizedSpan = span;
    }
}
