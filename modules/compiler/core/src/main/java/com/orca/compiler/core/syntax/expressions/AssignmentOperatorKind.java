package com.orca.compiler.core.syntax.expressions;

public enum AssignmentOperatorKind {
    Simple,
    AdditionAssignment,
    SubtractionAssignment,
    MultiplicationAssignment,
    DivisionAssignment,
    ModuloAssignment;

    public String getOperatorText() {
        return switch (this) {
            case Simple ->
                "=";
            case AdditionAssignment ->
                "+=";
            case SubtractionAssignment ->
                "-=";
            case MultiplicationAssignment ->
                "*=";
            case DivisionAssignment ->
                "/=";
            case ModuloAssignment ->
                "%=";
        };
    }
}
