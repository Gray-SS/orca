package com.orca.compiler.core.symbols.synthesized;

import com.orca.compiler.core.symbols.Symbol;
import com.orca.compiler.core.symbols.SymbolModifiers;
import com.orca.compiler.core.text.Location;
import com.orca.compiler.core.text.SynthesizedLocation;

public interface SynthesizedSymbol extends Symbol {

    @Override
    default Location location() {
        return new SynthesizedLocation(this);
    }

    @Override
    default int getModifiers() {
        return Symbol.super.getModifiers() | SymbolModifiers.SYNTHESIZED;
    }
}
