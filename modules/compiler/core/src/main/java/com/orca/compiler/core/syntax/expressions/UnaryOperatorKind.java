package com.orca.compiler.core.syntax.expressions;

import com.orca.compiler.core.lexer.TokenKind;

public enum UnaryOperatorKind {

    Increment,
    Decrement,
    Negation,
    LogicalNot;

    public static boolean isUnaryOperator(TokenKind kind) {
        return switch (kind) {
            case PlusPlus, MinusMinus, Minus ->
                true;
            default ->
                false;
        };
    }

    public String getOperatorText() {
        return switch (this) {
            case Negation ->
                "-";
            case Increment ->
                "++";
            case Decrement ->
                "--";
            case LogicalNot ->
                "not";
        };
    }
}
