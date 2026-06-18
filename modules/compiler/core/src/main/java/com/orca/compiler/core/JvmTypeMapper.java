package com.orca.compiler.core;

import com.orca.compiler.core.symbols.CallableSymbol;
import com.orca.compiler.core.typesystem.ArrayType;
import com.orca.compiler.core.typesystem.CallableType;
import com.orca.compiler.core.typesystem.LangType;
import com.orca.compiler.core.typesystem.NominalType;
import com.orca.compiler.core.typesystem.PrimitiveType;

public final class JvmTypeMapper {

    private JvmTypeMapper() {
    }

    public static LangType adaptType(Class<?> clazz) {
        if (clazz == null) {
            throw new IllegalArgumentException("clazz is null");
        }

        if (clazz == void.class) {
            return LangType.Void;
        }
        if (clazz == Object.class) {
            return LangType.Any;
        }
        if (clazz == String.class) {
            return LangType.String;
        }

        if (clazz.isPrimitive()) {
            if (clazz == byte.class) {
                return LangType.Byte;
            }
            if (clazz == short.class) {
                return LangType.Short;
            }
            if (clazz == int.class) {
                return LangType.Int;
            }
            if (clazz == long.class) {
                return LangType.Long;
            }
            if (clazz == float.class) {
                return LangType.Float;
            }
            if (clazz == double.class) {
                return LangType.Double;
            }
            if (clazz == boolean.class) {
                return LangType.Bool;
            }
            if (clazz == char.class) {
                return LangType.Char;
            }
        }

        if (clazz.isArray()) {
            var componentType = adaptType(clazz.getComponentType());
            return new ArrayType(componentType);
        }

        // TODO: Nominal type's name should be the internal name of the class, not the JVM internal name.
        return new NominalType(clazz.getName(), LangType.Opaque);
    }

    public static String descriptor(LangType type) {
        if (type == null) {
            throw new IllegalArgumentException("type is null");
        }

        if (type instanceof PrimitiveType p) {
            return p.descriptor();
        }

        return switch (type.kind()) {
            case VOID ->
                "V";
            case ANY ->
                "Ljava/lang/Object;";
            case ARRAY -> {
                ArrayType arr = type.as(ArrayType.class);
                yield "[" + componentDescriptor(arr.getElementType());
            }
            case NOMINAL -> {
                var nominal = type.as(NominalType.class);
                yield "L" + internalName(nominal) + ";";
            }
            case FUNCTION -> {
                CallableType func = type.as(CallableType.class);
                yield getFunctionDescriptor(func);
            }
            case COLLECTION ->
                throw new IllegalStateException("Collection types should be treated as nominal types");
            case UNKNOWN ->
                throw new IllegalStateException("Cannot generate descriptor for unknown type");
            default ->
                throw new IllegalStateException("Unsupported type kind: " + type.kind());
        };
    }

    private static String componentDescriptor(LangType elementType) {
        if (elementType instanceof PrimitiveType p) {
            return p.descriptor();
        }

        return switch (elementType.kind()) {
            case ANY ->
                "Ljava/lang/Object;";
            case ARRAY ->
                descriptor(elementType);
            case NOMINAL -> {
                var nominal = elementType.as(NominalType.class);
                yield "L" + internalName(nominal) + ";";
            }
            case COLLECTION ->
                throw new IllegalStateException("Collection types should be treated as nominal types");
            case VOID ->
                throw new IllegalStateException("Array element type cannot be void");
            case FUNCTION ->
                throw new IllegalStateException("Array element type cannot be function");
            case UNKNOWN ->
                throw new IllegalStateException("Cannot generate descriptor for unknown type");
            default ->
                throw new IllegalStateException("Unsupported element type kind: " + elementType.kind());
        };
    }

    public static String getCallableDescriptor(CallableSymbol symbol) {
        var type = symbol.type();
        return getFunctionDescriptor(type.as(CallableType.class));
    }

    public static String getFunctionDescriptor(CallableType type) {
        var returnType = type.returnType();
        var paramTypes = type.parameterTypes();

        var sb = new StringBuilder();
        sb.append('(');
        for (var p : paramTypes) {
            sb.append(descriptor(p));
        }
        sb.append(')');
        sb.append(descriptor(returnType));
        return sb.toString();
    }

    public static String internalName(NominalType nominal) {
        return nominal.name().replace('.', '/');
    }
}
