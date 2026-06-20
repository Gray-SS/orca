package com.orca.compiler.core.text;

import com.orca.compiler.core.symbols.synthesized.SynthesizedSymbol;

public final class SynthesizedLocation implements Location {

    private final SynthesizedSymbol symbol;

    public SynthesizedLocation(SynthesizedSymbol symbol) {
        this.symbol = symbol;
    }

    @Override
    public Source source() {
        return null;
    }

    @Override
    public String describe() {
        return symbol.displayName();
    }

    @Override
    public String toString() {
        return describe();
    }
}
