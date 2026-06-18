package com.orca.compiler.core.typesystem;

import java.util.stream.Collectors;

public record TypeIdentity(String value) {

    public static TypeIdentity of(LangType type) {
        return new TypeIdentity(componentOf(type));
    }

    private static String componentOf(LangType type) {
        return switch (type.kind()) {
            case VOID ->
                "void";
            case ANY ->
                "any";
            case ERROR ->
                "<error>";
            case UNKNOWN ->
                "unknown";
            case OPAQUE ->
                "opaque";
            case Primitive -> {
                yield switch (type.getPrimitiveKind()) {
                    case None ->
                        throw new IllegalStateException("None is not a valid primitive type.");
                    case Bool ->
                        "bool";
                    case Byte ->
                        "byte";
                    case Short ->
                        "short";
                    case Int ->
                        "int";
                    case Long ->
                        "long";
                    case Float ->
                        "float";
                    case Double ->
                        "double";
                    case Char ->
                        "char";
                    case String ->
                        "string";
                };
            }
            case FUNCTION -> {
                var functionType = type.as(CallableType.class);
                var returnId = componentOf(functionType.returnType());
                var paramIds = functionType.parameterTypes().stream()
                        .map(TypeIdentity::componentOf)
                        .collect(Collectors.joining(";"));

                yield "fn$" + returnId + ";[" + paramIds + "]";
            }
            case ARRAY -> {
                var arrayType = type.as(ArrayType.class);
                var elementId = componentOf(arrayType.getElementType());
                yield "array$" + elementId;
            }
            case COLLECTION -> {
                var collectionType = type.as(CollectionType.class);
                var fieldIds = collectionType.orderedFields()
                        .stream()
                        .map(entry -> entry.getKey() + ":" + componentOf(entry.getValue()))
                        .collect(Collectors.joining(";"));

                yield "collection$" + fieldIds;
            }
            case NOMINAL -> {
                var nominalType = type.as(NominalType.class);
                yield "nominal$" + nominalType.name();
            }
        };
    }

    @Override
    public final boolean equals(Object other) {
        return other instanceof TypeIdentity o && this.value.equals(o.value);
    }

    @Override
    public final int hashCode() {
        return value.hashCode();
    }

    @Override
    public final String toString() {
        return value;
    }
}
