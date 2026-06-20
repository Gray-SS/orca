package com.orca.compiler.core.syntax.expressions;

import com.orca.compiler.core.lexer.TokenKind;

public enum UnaryOperatorKind {
    Identity,
    Negation,
    LogicalNot;

    public static boolean isUnaryOperator(TokenKind kind) {
        return switch (kind) {
            case Plus, Minus, Bang ->
                true;
            default ->
                false;
        };
    }

    public String getOperatorText() {
        return switch (this) {
            case Identity ->
                "+";
            case Negation ->
                "-";
            case LogicalNot ->
                "not";
        };
    }
}
