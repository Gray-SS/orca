package com.orca.compiler.core.symbols.externals;

import com.orca.compiler.core.symbols.Symbol;
import com.orca.compiler.core.symbols.SymbolModifiers;
import com.orca.compiler.core.text.ExternalLocation;
import com.orca.compiler.core.text.Location;

/**
 * Represents a symbol that can be emitted to the JVM.
 */
public interface JvmSymbol extends Symbol {

    @Override
    default Location location() {
        return new ExternalLocation("jvm symbol " + getFullName());
    }

    @Override
    default int getModifiers() {
        return Symbol.super.getModifiers() | SymbolModifiers.JVM_EXTERNAL;
    }
}
