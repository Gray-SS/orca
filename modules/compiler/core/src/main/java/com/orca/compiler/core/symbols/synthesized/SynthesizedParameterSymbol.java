package com.orca.compiler.core.symbols.synthesized;

import com.orca.compiler.core.symbols.ParameterSymbol;
import com.orca.compiler.core.typesystem.LangType;

public final class SynthesizedParameterSymbol implements SynthesizedSymbol, ParameterSymbol {

    private final int index;
    private final String name;
    private final LangType type;

    public SynthesizedParameterSymbol(int index, LangType type) {
        this("param" + index, index, type);
    }

    public SynthesizedParameterSymbol(String name, int index, LangType type) {
        this.name = name;
        this.index = index;
        this.type = type;
    }

    @Override
    public String name() {
        return name;
    }

    @Override
    public boolean isVarargs() {
        return false;
    }

    @Override
    public int getModifiers() {
        return SynthesizedSymbol.super.getModifiers()
                | ParameterSymbol.super.getModifiers();
    }

    @Override
    public int index() {
        return index;
    }

    @Override
    public ParameterSymbol withIndex(int newIndex) {
        return new SynthesizedParameterSymbol(name, newIndex, type);
    }

    @Override
    public LangType type() {
        return type;
    }
}
