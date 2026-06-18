package com.orca.compiler.core.symbols;

import com.orca.compiler.core.symbols.synthesized.SynthesizedSymbol;
import com.orca.compiler.core.text.Location;
import com.orca.compiler.core.text.SynthesizedLocation;

public final class MissingSymbol implements SynthesizedSymbol {

    private final String name;

    public MissingSymbol(String name) {
        this.name = name;
    }

    @Override
    public String name() {
        return this.name;
    }

    @Override
    public String toString() {
        return "<MISSING SYMBOL: " + name + ">";
    }

    @Override
    public Location location() {
        return new SynthesizedLocation(this);
    }

    @Override
    public SymbolKind kind() {
        return SymbolKind.MISSING;
    }

    @Override
    public NamespaceOrTypeSymbol owner() {
        return null;
    }

}
