package com.orca.compiler.core.symbols;

import java.lang.reflect.Modifier;

import com.google.common.base.Preconditions;

public class SymbolModifiers {

    public static final int NONE = 0;

    /**
     * Indicates that the symbol is not applicable in the current context, such
     * as an instance member being accessed in a static context. This modifier
     * is used to mark symbols that are not valid for certain operations or
     * contexts, and can be used by the compiler to provide better error
     * messages when such symbols are accessed incorrectly.
     */
    public static final int NOT_APPLICABLE = 1;

    /**
     * Indicates that the symbol is static, meaning it is associated with the
     * type or namespace itself rather than an instance of an object. Static
     * members can be accessed without creating an instance of the type.
     */
    public static final int STATIC = 1 << 1;

    /**
     * Indicates that the symbol can be accessed both as a static member and as
     * an instance member. This is used for instance methods, as they can be
     * called like `obj.method()` (instance access) or `Type::method(obj)`
     * (static access).
     */
    public static final int INSTANCE = 1 << 2;

    /**
     * Indicates that the symbol is an instance member that can only be accessed
     * through an instance of a class, and not through static access. This is
     * used for instance methods that are declared in Java, as they cannot be
     * accessed statically.
     */
    public static final int INSTANCE_ONLY = 1 << 3;

    /**
     * Indicates that the symbol is synthesized by the compiler rather than
     * declared directly in the source code.
     */
    public static final int SYNTHESIZED = 1 << 4;

    /**
     * Indicates that the symbol is a built-in symbol provided by the language
     * or runtime, rather than defined by the user.
     */
    public static final int INTRINSIC = 1 << 5;

    /**
     * Indicates that the symbol is declared in the source code, as opposed to
     * being synthesized or built-in. This is used for symbols that are directly
     * declared by the user in their code.
     *
     * @implNote This modifier is mutually exclusive with SYNTHESIZED and
     * INTRINSIC, as a symbol cannot be both declared in source and synthesized
     * or intrinsic at the same time.
     * @implNote This modifier is stating that a declaration syntax exists for
     * this symbol, and that the span of this symbol is the span of its
     * declaration syntax.
     */
    public static final int SOURCE = 1 << 6;

    /**
     * Indicates that the symbol is external to the JVM, such as a symbol
     * defined in a JAR file. This modifier is used for symbols that are not
     * defined in the current compilation unit but are available for use.
     */
    public static final int JVM_EXTERNAL = 1 << 7;

    /**
     * Indicates that the symbol is an entry point of the program, such as the
     * `main` method. This modifier is used to mark the method that serves as
     * the starting point of the program execution.
     */
    public static final int ENTRY_POINT = 1 << 8;

    /**
     * Indicates that the symbol is a constant, meaning its value is known at
     * compile time and cannot be changed. This modifier is used for variables
     * declared with `const`.
     */
    public static final int CONSTANT = 1 << 9;

    /**
     * Indicates that the symbol is immutable, meaning its value cannot be
     * changed after initialization. This modifier is used for variables
     * declared with `let`.
     */
    public static final int IMMUTABLE = 1 << 10;

    /**
     * Gets the default modifiers for a namespace or type symbol.
     */
    public static final int NAMESPACE_OR_TYPE = STATIC;

    public static boolean hasModifier(int modifiers, int modifier) {
        return (modifiers & modifier) != 0;
    }

    public static boolean isConstant(int modifiers) {
        return (modifiers & CONSTANT) != 0;
    }

    public static boolean isImmutable(int modifiers) {
        return (modifiers & IMMUTABLE) != 0;
    }

    public static boolean isStatic(int modifiers) {
        return (modifiers & STATIC) != 0;
    }

    public static boolean isInstance(int modifiers) {
        return (modifiers & INSTANCE) != 0;
    }

    public static boolean isInstanceOnly(int modifiers) {
        return (modifiers & INSTANCE_ONLY) != 0;
    }

    public static boolean isSynthesized(int modifiers) {
        return (modifiers & SYNTHESIZED) != 0;
    }

    public static boolean isIntrinsic(int modifiers) {
        return (modifiers & INTRINSIC) != 0;
    }

    public static boolean isJvmExternal(int modifiers) {
        return (modifiers & JVM_EXTERNAL) != 0;
    }

    public static boolean isSource(int modifiers) {
        return (modifiers & SOURCE) != 0;
    }

    public static boolean isNotApplicable(int modifiers) {
        return (modifiers & NOT_APPLICABLE) != 0;
    }

    public static boolean isEntryPoint(int modifiers) {
        return (modifiers & ENTRY_POINT) != 0;
    }

    public static int fromJavaModifiers(int javaModifiers) {
        int modifiers = NONE;

        if (Modifier.isStatic(javaModifiers)) {
            modifiers |= STATIC;
        } else {
            modifiers |= INSTANCE_ONLY;
        }

        return modifiers;
    }

    public static void assertValidModifierCombination(int modifiers) {
        Preconditions.checkState(modifiers >= 0, "Modifiers cannot be negative");
        Preconditions.checkState(!(isSynthesized(modifiers) && isSource(modifiers)), "A symbol cannot be both synthesized and source");
        Preconditions.checkState(!(isJvmExternal(modifiers) && isSource(modifiers)), "A symbol cannot be both JVM-external and source");
        Preconditions.checkState(!(isIntrinsic(modifiers) && isSource(modifiers)), "A symbol cannot be both intrinsic and source");
        Preconditions.checkState(!(isInstance(modifiers) && isInstanceOnly(modifiers)), "A symbol cannot be both instance and instance-only");
        Preconditions.checkState(!(isStatic(modifiers) && isInstance(modifiers)), "A symbol cannot be both static and instance");
        Preconditions.checkState(!(isStatic(modifiers) && isInstanceOnly(modifiers)), "A symbol cannot be both static and instance-only");
    }

    public static void assertValidModifierCombination(int modifiers, SymbolKind kind) {
        assertValidModifierCombination(modifiers);

        if (kind == SymbolKind.NAMESPACE || kind == SymbolKind.TYPE) {
            Preconditions.checkState((modifiers & NAMESPACE_OR_TYPE) != 0, "Namespaces and types must be static");
        }

        if (kind == SymbolKind.METHOD) {
            Preconditions.checkState(!(isEntryPoint(modifiers) && !isStatic(modifiers)), "Entry point methods must be static");
        }

        if (kind == SymbolKind.FIELD) {
            Preconditions.checkState(!isEntryPoint(modifiers), "Fields cannot be entry points");
            Preconditions.checkState(isInstance(modifiers), "Fields cannot be instance-only");
        }
    }
}
