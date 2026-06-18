package com.orca.compiler.core.boundtree;

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
