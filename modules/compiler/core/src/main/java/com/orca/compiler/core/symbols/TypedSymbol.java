package com.orca.compiler.core.symbols;

import com.orca.compiler.core.typesystem.LangType;

public interface TypedSymbol extends Symbol {

    LangType type();

    default int getModifiers() {
        return Symbol.super.getModifiers();
    }
}
