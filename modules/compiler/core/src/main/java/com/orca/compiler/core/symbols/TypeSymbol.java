package com.orca.compiler.core.symbols;

import java.util.List;

import com.orca.compiler.core.typesystem.TypeKind;

public interface TypeSymbol extends TypedSymbol, NamespaceOrTypeSymbol {

    @Override
    default SymbolKind kind() {
        return SymbolKind.TYPE;
    }

    @Override
    default String displayName() {
        return getTypeKind().displayName() + " '" + name() + "'";
    }

    /**
     * Gets the kind of this type symbol (e.g., collection, int, float, etc.).
     *
     * @return The type kind of this type symbol.
     */
    default TypeKind getTypeKind() {
        return type().kind();
    }

    default boolean isCollectionType() {
        return getTypeKind() == TypeKind.COLLECTION;
    }

    default boolean isPrimitiveType() {
        return getTypeKind() == TypeKind.Primitive;
    }

    default boolean isExtensionType() {
        return false;
    }

    /**
     * Adds an extension symbol to this type symbol. This method is used to
     * associate extension symbols with the type symbol they extend.
     *
     * @param extension The extension symbol to add.
     */
    void addExtension(ExtensionSymbol extension);

    /**
     * Gets the list of extension symbols that extend this type symbol.
     *
     * @return The list of extension symbols that extend this type symbol.
     */
    List<ExtensionSymbol> getExtensions();

    /**
     * Gets the list of member symbols of this type symbol, including members
     * declared in this type symbol and members added through extensions.
     *
     * @return The list of member symbols of this type symbol, including members
     * declared in this type symbol and members added through extensions.
     */
    default List<Symbol> getMembersWithExtensions() {
        var members = getMembers();
        for (var extension : getExtensions()) {
            members = new java.util.ArrayList<>(members);
            members.addAll(extension.getMembers());
        }
        return List.copyOf(members);
    }

    /**
     * Gets the list of member symbols with the specified name from this type
     * symbol, including members declared in this type symbol and members added
     * through extensions.
     *
     * @param name The name of the member symbols to retrieve.
     * @return The list of member symbols with the specified name from this type
     * symbol, including members declared in this type symbol and members added
     * through extensions.
     */
    default List<Symbol> getMembersWithExtensions(String name) {
        var members = getMembers(name);
        for (var extension : getExtensions()) {
            members = new java.util.ArrayList<>(members);
            members.addAll(extension.getMembers(name));
        }
        return List.copyOf(members);
    }

    /**
     * Gets a member symbol with the specified name from this type symbol,
     * including members declared in this type symbol and members added through
     * extensions. If multiple members with the same name exist, the member
     * declared in this type symbol is returned. If no member with the specified
     * name exists, null is returned.
     *
     * @param name The name of the member symbol to retrieve.
     * @return A member symbol with the specified name from this type symbol,
     * including members declared in this type symbol and members added through
     * extensions, or null if no such member exists.
     */
    default Symbol getMemberWithExtensions(String name) {
        var member = getMember(name);
        if (member != null) {
            return member;
        }

        for (var extension : getExtensions()) {
            member = extension.getMember(name);
            if (member != null) {
                return member;
            }
        }

        return null;
    }

    /**
     * Checks if a member symbol with the specified name exists in this type
     * symbol, including members declared in this type symbol and members added
     * through extensions.
     *
     * @param name The name of the member symbol to check for existence.
     * @return True if a member symbol with the specified name exists in this
     * type symbol, including members declared in this type symbol and members
     * added through extensions, false otherwise.
     */
    default boolean hasMemberWithExtensions(String name) {
        if (hasMember(name)) {
            return true;
        }

        for (var extension : getExtensions()) {
            if (extension.hasMember(name)) {
                return true;
            }
        }

        return false;
    }

    @Override
    default int getModifiers() {
        return TypedSymbol.super.getModifiers() | NamespaceOrTypeSymbol.super.getModifiers();
    }

    default void addConstructor(ConstructorSymbol constructor) {
        if (constructor == null) {
            throw new IllegalArgumentException("Constructor cannot be null");
        }
        if (!constructor.name().equals(ConstructorSymbol.DEFAULT_NAME)) {
            throw new IllegalArgumentException("Constructor name must be '" + ConstructorSymbol.DEFAULT_NAME + "'");
        }
        if (!constructor.owner().equals(this)) {
            throw new IllegalArgumentException("Constructor owner must be this type symbol");
        }

        addMember(constructor);
    }

    default List<ConstructorSymbol> getConstructors() {
        return getMembers(ConstructorSymbol.DEFAULT_NAME)
                .stream()
                .filter(member -> member instanceof ConstructorSymbol)
                .map(member -> (ConstructorSymbol) member)
                .toList();
    }

    default FieldSymbol getField(String name) {
        var symbol = getMember(name);
        if (symbol instanceof FieldSymbol field) {
            return field;
        }

        return null;
    }
}
