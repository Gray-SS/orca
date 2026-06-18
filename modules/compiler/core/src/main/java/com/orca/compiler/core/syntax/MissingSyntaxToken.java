package com.orca.compiler.core.syntax;

import com.orca.compiler.core.lexer.Token;

public final class MissingSyntaxToken extends SyntaxToken {

    public MissingSyntaxToken(Token token) {
        super(token);
    }

    @Override
    public boolean isMissing() {
        return true;
    }

    @Override
    public String toString() {
        return "MissingToken(expected=" + token.kind() + ")";
    }
}
