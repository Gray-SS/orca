package com.orca.compiler.core.syntax;

import java.util.List;

import com.orca.compiler.core.lexer.Token;
import com.orca.compiler.core.lexer.TokenKind;
import com.orca.compiler.core.text.SourceSpan;

public class SyntaxToken extends SyntaxNode {
    public final Token token;

    public SyntaxToken(Token token) {
        this.token = token;
    }

    public Object value() {
        return token.value();
    }

    public boolean isMissing() {
        return false;
    }

    public TokenKind kind() {
        return token.kind();
    }

    public String text() {
        return token.lexeme();
    }

    @Override
    public SourceSpan span() {
        return token.span();
    }

    @Override
    public List<SyntaxNode> children() {
        return List.of();
    }

    @Override
    public void accept(SyntaxVisitor visitor) {
        visitor.visitToken(this);
    }

    @Override
    public String toString() {
        return "SyntaxToken(kind=" + token.kind() + ", lexeme=" + token.lexeme() + ")";
    }

}
