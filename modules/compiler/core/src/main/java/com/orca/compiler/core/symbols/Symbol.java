package com.orca.compiler.core.symbols;

import java.util.EnumSet;

import org.checkerframework.checker.nullness.qual.NonNull;

import com.orca.compiler.core.Compilation;
import com.orca.compiler.core.symbols.synthesized.GlobalNamespaceSymbol;
import com.orca.compiler.core.text.IHaveLocation;

/**
 * An interface for symbols in the compiler.
 */
public interface Symbol extends IHaveLocation {

    /**
     * Gets the name of this symbol. For example, for a method symbol named
     * "foo", this would return "foo".
     *
     * @return The name of this symbol.
     */
    String name();

    /**
     * Gets the kind of this symbol, which indicates what type of entity it
     * represents (e.g., function, variable, collection, etc.).
     *
     * @return The kind of this symbol.
     */
    SymbolKind kind();

    /**
     * Gets the owner of this symbol, which is the namespace or type that
     * contains this symbol.
     *
     * @return The owner of this symbol.
     * @implNote This method might return null for the global namespace symbol
     * itself, as it does not have an owner.
     * @implNote This method MAY NOT be applicable to local symbols, as they are
     * not owned by a namespace or type. If this method is called on a local
     * symbol, it should throw an {@link UnsupportedOperationException}.
     */
    NamespaceOrTypeSymbol owner();

    /**
     * Gets the modifiers of this symbol, which indicate additional properties
     * of the symbol such as whether it is static, synthesized, etc.
     *
     * @return The modifiers of this symbol.
     */
    default int getModifiers() {
        return SymbolModifiers.NONE;
    }

    /**
     * Determines whether this symbol has the specified modifier.
     *
     * @param modifier The modifier to check for.
     * @return true if this symbol has the specified modifier, false otherwise.
     */
    default boolean hasModifier(int modifier) {
        return SymbolModifiers.hasModifier(getModifiers(), modifier);
    }

    default EnumSet<BindingAttribute> getAttributes() {
        return BindingAttribute.getAttributesFromModifiers(getModifiers());
    }

    /**
     * A user-friendly name for this symbol, used in error messages and other
     * diagnostics. For example, for a method symbol named "foo", this might
     * return "method 'foo'".
     *
     * @return A user-friendly name for this symbol.
     */
    default String displayName() {
        return kind().getDisplayName() + " '" + getFullName() + "'";
    }

    /**
     * A user-friendly name for this symbol that includes its binding
     * attributes, used in error messages and other diagnostics. For example,
     * for a static method symbol named "foo", this might return "static method
     * 'foo'".
     *
     * @return A user-friendly name for this symbol that includes its binding
     * attributes.
     */
    default String displayNameWithAttributes() {
        var attributes = getAttributes();
        if (attributes.isEmpty()) {
            return displayName();
        }

        String attributesString = attributes.stream()
                .map(BindingAttribute::getDisplayName)
                .filter(a -> !a.isEmpty())
                .reduce((a, b) -> a + " " + b)
                .orElse("");

        return attributesString + " " + displayName();
    }

    /**
     * Gets the fully qualified name of this symbol, which includes any relevant
     * namespaces or parent entities. For example, for a method symbol named
     * "foo" in a collection named "Bar", this might return "Bar.foo".
     *
     * @return The fully qualified name of this symbol.
     */
    default String getFullName() {
        var owner = owner();
        var name = name();

        if (owner == null || owner.isGlobalNamespace()) {
            return name;
        }

        return owner.getFullName() + "." + name;
    }

    /**
     * Gets the compilation that this symbol belongs to by traversing up the
     * owner chain until it finds the global namespace symbol, which holds a
     * reference to the compilation.
     *
     * @return The compilation that this symbol belongs to.
     */
    default Compilation getCompilation() {
        var current = this;
        while (current != null) {
            if (current instanceof GlobalNamespaceSymbol globalNamespace) {
                return globalNamespace.getCompilation();
            }

            current = current.owner();
        }

        throw new IllegalStateException("Could not find compilation for symbol: " + name());
    }

    // ─── Identity check utilities ──────────────────────────────────────────────────────────────────────────────────────────
    // Respond to questions about what this symbol is, such as "Is this symbol a method?", "Is this symbol a namespace?", etc.
    /**
     * Determines whether this symbol represents a namespace.
     *
     * @return true if this symbol is a namespace, false otherwise.
     */
    default boolean isNamespace() {
        return kind() == SymbolKind.NAMESPACE;
    }

    /**
     * Determines whether this symbol represents a type.
     *
     * @return true if this symbol is a type, false otherwise.
     */
    default boolean isType() {
        return kind() == SymbolKind.TYPE;
    }

    /**
     * Checks if this namespace symbol represents the global namespace, which is
     * identified by having no owner namespace.
     *
     * @return true if this is the global namespace, false otherwise.
     */
    default boolean isGlobalNamespace() {
        return kind() == SymbolKind.GLOBAL_NAMESPACE;
    }

    /**
     * Determines whether this symbol represents a method.
     *
     * @return true if this symbol is a method, false otherwise.
     */
    default boolean isMethod() {
        return kind() == SymbolKind.METHOD;
    }

    /**
     * Determines whether this symbol represents a constructor.
     *
     * @return true if this symbol is a constructor, false otherwise.
     */
    default boolean isConstructor() {
        return kind() == SymbolKind.CONSTRUCTOR;
    }

    /**
     * Determines whether this symbol is part of the global namespace.
     *
     * @return true if this symbol is part of the global namespace, false
     * otherwise.
     */
    default boolean isGlobal() {
        return owner() != null && owner().isGlobalNamespace();
    }

    /**
     * Determines whether this symbol represents a constant variable.
     *
     * @return true if this symbol is a constant variable, false otherwise.
     */
    default boolean isConstantVariable() {
        return kind() == SymbolKind.VARIABLE && hasModifier(SymbolModifiers.CONSTANT);
    }

    /**
     * Determines whether this symbol represents a global variable.
     *
     * @return true if this symbol is a global variable, false otherwise.
     */
    default boolean isGlobalVariable() {
        return isVariable() && isGlobal();
    }

    /**
     * Determines whether this symbol represents a local variable.
     *
     * @return true if this symbol is a local variable, false otherwise.
     */
    default boolean isLocalVariable() {
        return isVariable() && owner() == null;
    }

    /**
     * Determines whether this symbol represents any kind of variable (constant,
     * global, or local).
     *
     * @return true if this symbol is any kind of variable, false otherwise.
     */
    default boolean isVariable() {
        return kind() == SymbolKind.VARIABLE;
    }

    /**
     * Determines whether this symbol represents a parameter.
     *
     * @return true if this symbol is a parameter, false otherwise.
     */
    default boolean isParameter() {
        return kind() == SymbolKind.PARAMETER;
    }

    /**
     * Determines whether this symbol represents a field (i.e., a member
     * variable of a collection).
     *
     * @return true if this symbol is a field, false otherwise.
     */
    default boolean isField() {
        return kind() == SymbolKind.FIELD;
    }

    /**
     * Determines whether this symbol is a missing symbol, which represents an
     * entity that could not be resolved during compilation. Missing symbols are
     * used to allow the compiler to continue processing code even when some
     * symbols cannot be resolved, and they typically result in error
     * diagnostics being reported to the user.
     *
     * @return true if this symbol is a missing symbol, false otherwise.
     */
    default boolean isMissing() {
        return kind() == SymbolKind.MISSING;
    }

    // ─── Binding Check Utilities ───────────────────────────────────────────
    // Respond to questions about how this symbol is bound, such as "Can I use this symbol in a static context?", "Is this symbol compile-time evaluable?", etc.
    /**
     * Determines whether this symbol is an intrinsic symbol, which is a symbol
     * that is handled specially by the compiler.
     *
     * @return true if this symbol is an intrinsic symbol, false otherwise.
     */
    default boolean isIntrinsic() {
        int modifiers = getModifiers();
        SymbolModifiers.assertValidModifierCombination(modifiers);
        return SymbolModifiers.isIntrinsic(modifiers);
    }

    /**
     * Determines whether this symbol is static-bound, which means it is
     * associated with the namespace or type itself rather than an instance of
     * the type.
     *
     * @return true if this symbol is static-bound, false otherwise.
     */
    default boolean isStatic() {
        int modifiers = getModifiers();
        SymbolModifiers.assertValidModifierCombination(modifiers);
        return SymbolModifiers.isStatic(modifiers);
    }

    /**
     * Determines whether this symbol is instance-only, which means it is
     * associated with an instance of a type and can only be accessed through an
     * instance of that type. This is a special case for symbols that represent
     * members of Java types, as they are technically instance members but
     * cannot be accessed through the collection type itself in Orca code.
     *
     * @return true if this symbol is instance-only, false otherwise.
     */
    default boolean isInstanceOnly() {
        int modifiers = getModifiers();
        SymbolModifiers.assertValidModifierCombination(modifiers);
        return SymbolModifiers.isInstanceOnly(modifiers);
    }

    /**
     * Determines whether this symbol can be accessed through an instance of the
     * owning type. This includes symbols that are instance-bound (i.e., they
     * must be accessed through an instance) as well as symbols that are Java
     * instance-bound (i.e., they represent members of Java types and can only
     * be accessed through instances of those types).
     *
     * @return true if this symbol can be accessed through an instance, false
     * otherwise.
     */
    default boolean canBeAccessedThroughInstance() {
        return (isInstance() || isInstanceOnly()) && !isConstructor();
    }

    /**
     * Determines whether this symbol can be accessed through the type itself.
     * This includes symbols that are static-bound (i.e., they are associated
     * with the type itself and can be accessed through the type) as well as
     * symbols that are Java static-bound (i.e., they represent static members
     * of Java types and can be accessed through the type itself).
     *
     * @return true if this symbol can be accessed through the type itself,
     * false otherwise.
     */
    default boolean canBeAccessedThroughStatic() {
        return isStatic() || isInstance();
    }

    // /**
    //  * Determines whether this symbol is static-bound but can also be accessed through an instance of the owning type. This is a special case that applies to certain symbols (e.g., extension methods) that are technically static but can be accessed through instances for convenience.
    //  * @return true if this symbol is static-bound but can also be accessed through an instance, false otherwise.
    //  */
    // default boolean isStaticInstance() {
    //     int modifiers = getModifiers();
    //     SymbolModifiers.assertValidModifierCombination(modifiers);
    //     return SymbolModifiers.isStaticInstance(modifiers);
    // }
    /**
     * Determines whether this symbol is instance-bound. This means that this
     * symbols MUST ALWAYS be accessed through an instance of the owning type,
     * and cannot be accessed through the type itself. For example, if a field
     * symbol is instance-bound, it can only be accessed through an instance of
     * the collection that owns it, and not through the collection type itself.
     * {@code collectionInstance.field } is valid, while
     * {@code CollectionType.field} is invalid and should result in a
     * compile-time error.
     *
     * @return true if this symbol is instance-bound, false otherwise.
     */
    default boolean isInstance() {
        int modifiers = getModifiers();
        SymbolModifiers.assertValidModifierCombination(modifiers);
        return SymbolModifiers.isInstance(modifiers);
    }

    /**
     * Determines whether this symbol is synthesized (i.e., not declared in the
     * source code but generated by the compiler).
     *
     * @return true if the symbol is synthesized, false otherwise.
     */
    default boolean isSynthesized() {
        int modifiers = getModifiers();
        SymbolModifiers.assertValidModifierCombination(modifiers);

        return SymbolModifiers.isSynthesized(modifiers);
    }

    /**
     * Determines whether this symbol is a user defined symbol (i.e., declared
     * in the source code).
     *
     * @return true if the symbol is a user defined symbol, false otherwise.
     */
    default boolean isSource() {
        int modifiers = getModifiers();
        SymbolModifiers.assertValidModifierCombination(modifiers);

        return SymbolModifiers.isSource(modifiers);
    }

    /**
     * Determines whether this symbol originates from an external JVM class
     * (e.g., a JAR on the classpath).
     *
     * @return true if this is a JVM-external symbol, false otherwise.
     */
    default boolean isJvmExternal() {
        int modifiers = getModifiers();
        SymbolModifiers.assertValidModifierCombination(modifiers);

        return SymbolModifiers.isJvmExternal(modifiers);
    }

    @NonNull
    public static MissingSymbol missing(String name) {
        return new MissingSymbol(name);
    }
}
