package com.orca.compiler.core.syntax.expressions;

public enum BinaryOperatorKind {
    StringConcatenation,
    Addition,
    Subtraction,
    Multiplication,
    Division,
    Modulo,
    Equal,
    NotEqual,
    LessThan,
    LessThanOrEqual,
    GreaterThan,
    GreaterThanOrEqual,
    LogicalAnd,
    LogicalOr;

    public String getOperatorText() {
        return switch (this) {
            case StringConcatenation ->
                "+";
            case Addition ->
                "+";
            case Subtraction ->
                "-";
            case Multiplication ->
                "*";
            case Division ->
                "/";
            case Modulo ->
                "%";
            case Equal ->
                "==";
            case NotEqual ->
                "=/=";
            case LessThan ->
                "<";
            case LessThanOrEqual ->
                "<=";
            case GreaterThan ->
                ">";
            case GreaterThanOrEqual ->
                ">=";
            case LogicalAnd ->
                "&&";
            case LogicalOr ->
                "||";
        };
    }
}
