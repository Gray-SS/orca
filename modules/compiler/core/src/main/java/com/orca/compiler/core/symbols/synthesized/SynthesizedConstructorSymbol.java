package com.orca.compiler.core.symbols.synthesized;

import java.util.List;

import com.google.common.base.Preconditions;
import com.orca.compiler.core.symbols.ConstructorSymbol;
import com.orca.compiler.core.symbols.ParameterSymbol;
import com.orca.compiler.core.symbols.TypeSymbol;

public final class SynthesizedConstructorSymbol extends ConstructorSymbol implements SynthesizedSymbol {
    private final List<? extends ParameterSymbol> parameters;

    public SynthesizedConstructorSymbol(TypeSymbol owner, List<? extends ParameterSymbol> parameters) {
        super(owner);
        Preconditions.checkNotNull(parameters, "Parameters cannot be null");
        this.parameters = parameters;
    }

    @Override
    protected List<? extends ParameterSymbol> getParametersInternal() {
        return parameters;
    }

    @Override
    public int getModifiers() {
        return super.getModifiers()
            | SynthesizedSymbol.super.getModifiers();
    }
}
