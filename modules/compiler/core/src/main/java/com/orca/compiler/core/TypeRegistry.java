package com.orca.compiler.core;

import java.util.Map;

import com.orca.compiler.core.lexer.SpecialTypeKind;
import com.orca.compiler.core.symbols.ErrorTypeSymbol;
import com.orca.compiler.core.symbols.TypeSymbol;
import com.orca.compiler.core.symbols.constructed.ArrayTypeSymbol;
import com.orca.compiler.core.symbols.intrinsics.IntrinsicTypeSymbol;
import com.orca.compiler.core.typesystem.AnyType;
import com.orca.compiler.core.typesystem.ArrayType;
import com.orca.compiler.core.typesystem.CallableType;
import com.orca.compiler.core.typesystem.CollectionType;
import com.orca.compiler.core.typesystem.ErrorType;
import com.orca.compiler.core.typesystem.LangType;
import com.orca.compiler.core.typesystem.NominalType;
import com.orca.compiler.core.typesystem.OpaqueType;
import com.orca.compiler.core.typesystem.PrimitiveType;
import com.orca.compiler.core.typesystem.TypeIdentity;
import com.orca.compiler.core.typesystem.UnknownType;
import com.orca.compiler.core.typesystem.VoidType;

public final class TypeRegistry {

    private final Compilation compilation;
    private final Map<TypeIdentity, TypeSymbol> typeCache = new java.util.HashMap<>();

    public TypeRegistry(Compilation compilation) {
        this.compilation = compilation;
    }

    public TypeSymbol bindSpecialType(SpecialTypeKind kind) {
        return switch (kind) {
            case Any ->
                IntrinsicTypeSymbol.Any;
            case Bool ->
                IntrinsicTypeSymbol.Bool;
            case Byte ->
                IntrinsicTypeSymbol.Byte;
            case Short ->
                IntrinsicTypeSymbol.Short;
            case Int ->
                IntrinsicTypeSymbol.Int;
            case Long ->
                IntrinsicTypeSymbol.Long;
            case Float ->
                IntrinsicTypeSymbol.Float;
            case Double ->
                IntrinsicTypeSymbol.Double;
            case Char ->
                IntrinsicTypeSymbol.Char;
            case String ->
                IntrinsicTypeSymbol.String;
            case Void ->
                throw new IllegalStateException("Void type should not be bound to a symbol.");
        };
    }

    public TypeSymbol bindType(LangType type) {
        return switch (type) {
            case AnyType a ->
                IntrinsicTypeSymbol.Any;
            case PrimitiveType p ->
                bindPrimitiveType(p);
            case ArrayType a ->
                bindArrayType(a);
            case CollectionType c ->
                bindCollectionType(c);
            case NominalType n ->
                bindNominalType(n);
            case ErrorType e ->
                new ErrorTypeSymbol(e.displayName(), e);
            case OpaqueType o ->
                throw new IllegalStateException("Opaque types should not be bound to symbols.");
            case UnknownType u ->
                throw new IllegalStateException("Unknown types should not be bound to symbols.");
            case VoidType v ->
                throw new IllegalStateException("Void type should not be bound to a symbol.");
            case CallableType f ->
                throw new IllegalStateException("Function types should not be directly bound to symbols.");
        };
    }

    private TypeSymbol bindPrimitiveType(PrimitiveType primitiveType) {
        return switch (primitiveType.getPrimitiveKind()) {
            case Bool ->
                IntrinsicTypeSymbol.Bool;
            case Byte ->
                IntrinsicTypeSymbol.Byte;
            case Short ->
                IntrinsicTypeSymbol.Short;
            case Int ->
                IntrinsicTypeSymbol.Int;
            case Long ->
                IntrinsicTypeSymbol.Long;
            case Float ->
                IntrinsicTypeSymbol.Float;
            case Double ->
                IntrinsicTypeSymbol.Double;
            case Char ->
                IntrinsicTypeSymbol.Char;
            case String ->
                IntrinsicTypeSymbol.String;
            case None ->
                throw new IllegalStateException("None type should not be bound to a symbol.");
        };
    }

    private TypeSymbol bindNominalType(NominalType nominalType) {
        var typeName = nominalType.name();
        var foundSymbol = compilation.resolveSymbolFromGlobalNamespace(typeName);
        if (foundSymbol == null) {
            foundSymbol = compilation.resolveExternalSymbol(typeName);
        }

        if (!(foundSymbol instanceof TypeSymbol typeSymbol)) {
            throw new IllegalStateException("Symbol is not a type: " + foundSymbol.getClass().getName());
        }

        return typeSymbol;
    }

    private TypeSymbol bindArrayType(ArrayType arrayType) {
        var elementTypeSymbol = bindType(arrayType.getElementType());
        var arrayTypeIdentity = TypeIdentity.of(arrayType);
        return typeCache.computeIfAbsent(arrayTypeIdentity, id -> new ArrayTypeSymbol(elementTypeSymbol));
    }

    private TypeSymbol bindCollectionType(CollectionType collectionType) {
        throw new IllegalStateException("Anonymous collection types are currently not supported.");
        // var collectionTypeIdentity = TypeIdentity.of(collectionType);
        // return typeCache.computeIfAbsent(collectionTypeIdentity, id -> {
        //     var collectionTypeSymbol = new CollectionTypeSymbol(collectionType);

        //     for (var entry : collectionType.orderedFields()) {
        //         var fieldName = entry.getKey();
        //         var fieldType = entry.getValue();
        //         var fieldTypeSymbol = bindType(fieldType);
        //         collectionTypeSymbol.addField(fieldName, fieldTypeSymbol);
        //     }
        //     return collectionTypeSymbol;
        // });
    }
}
