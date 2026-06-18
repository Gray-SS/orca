package com.orca.compiler.core.symbols.intrinsics;

import java.util.ArrayList;
import java.util.List;

import com.orca.compiler.core.symbols.NamespaceOrTypeSymbol;
import com.orca.compiler.core.symbols.SymbolModifiers;
import com.orca.compiler.core.symbols.synthesized.SynthesizedSymbol;

/**
 * Represents a symbol that is intrinsic to the language, meaning it is handled
 * specially by the compiler and does not have a direct representation in the
 * source code. These symbols needs to be handled in a special way by the
 * compiler.
 */
public interface IntrinsicSymbol extends SynthesizedSymbol {

    /**
     * Gets the kind of this intrinsic symbol, which indicates what kind of
     * intrinsic symbol this is and how it should be handled by the compiler.
     *
     * @return the kind of this intrinsic symbol.
     */
    IntrinsicKind intrinsicKind();

    public static List<IntrinsicSymbol> getAllIntrinsics() {
        var intrinsics = new ArrayList<IntrinsicSymbol>();
        intrinsics.addAll(IntrinsicMethodSymbol.getAllIntrinsicMethods());
        return intrinsics;
    }

    @Override
    default String name() {
        return intrinsicKind().getDisplayName();
    }

    @Override
    default NamespaceOrTypeSymbol owner() {
        return null;
    }

    @Override
    default int getModifiers() {
        return SynthesizedSymbol.super.getModifiers()
                | SymbolModifiers.INTRINSIC;
    }

    @Override
    default String getFullName() {
        return name();
    }
}
