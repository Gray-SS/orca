package com.orca.compiler.core.symbols.synthesized;

import java.util.List;

import com.orca.compiler.core.symbols.MethodSymbol;
import com.orca.compiler.core.symbols.NamespaceOrTypeSymbol;
import com.orca.compiler.core.symbols.ParameterSymbol;
import com.orca.compiler.core.typesystem.LangType;

public class SynthesizedMethodSymbol extends MethodSymbol implements SynthesizedSymbol {

    private final int additionalModifiers;
    private final LangType returnType;
    private final List<SynthesizedParameterSymbol> parameters;

    public SynthesizedMethodSymbol(NamespaceOrTypeSymbol owner, String name, LangType returnType, List<LangType> parameters, int additionalModifiers) {
        super(owner, name, additionalModifiers);
        this.additionalModifiers = additionalModifiers;
        this.returnType = returnType;
        this.parameters = createParameters(parameters);
    }

    private static List<SynthesizedParameterSymbol> createParameters(List<LangType> parameterTypes) {
        var parameters = new java.util.ArrayList<SynthesizedParameterSymbol>();
        for (int i = 0; i < parameterTypes.size(); i++) {
            var paramType = parameterTypes.get(i);
            if (paramType == null) {
                throw new IllegalArgumentException("Parameter type cannot be null");
            }

            parameters.add(new SynthesizedParameterSymbol(i, paramType));
        }

        return parameters;
    }

    @Override
    protected LangType getReturnTypeInternal() {
        return this.returnType;
    }

    @Override
    protected List<? extends ParameterSymbol> getParametersInternal() {
        return this.parameters;
    }

    @Override
    public int getModifiers() {
        return super.getModifiers()
                | SynthesizedSymbol.super.getModifiers()
                | additionalModifiers;
    }
}
