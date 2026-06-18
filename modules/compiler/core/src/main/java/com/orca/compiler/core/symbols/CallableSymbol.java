package com.orca.compiler.core.symbols;

import java.util.List;

import com.google.common.base.Preconditions;
import com.orca.compiler.core.symbols.synthesized.SynthesizedParameterSymbol;
import com.orca.compiler.core.typesystem.CallableType;
import com.orca.compiler.core.typesystem.LangType;

public abstract class CallableSymbol implements TypedSymbol {

    private final int modifiers;
    private final String name;
    private final NamespaceOrTypeSymbol owner;

    private LangType cachedReturnType;
    private List<? extends ParameterSymbol> cachedParameters;

    public CallableSymbol(String name, NamespaceOrTypeSymbol owner, int modifiers) {
        Preconditions.checkNotNull(name, "Name cannot be null");

        this.name = name;
        this.owner = owner;
        this.modifiers = modifiers;
    }

    protected abstract LangType getReturnTypeInternal();

    protected abstract List<? extends ParameterSymbol> getParametersInternal();

    @Override
    public String name() {
        return name;
    }

    @Override
    public NamespaceOrTypeSymbol owner() {
        return owner;
    }

    public final List<? extends ParameterSymbol> parameters() {
        if (cachedParameters == null) {
            cachedParameters = computeParameters();
            Preconditions.checkNotNull(cachedParameters, "Parameters cannot be null");
        }

        return cachedParameters;
    }

    public final LangType returnType() {
        if (cachedReturnType == null) {
            cachedReturnType = getReturnTypeInternal();
            Preconditions.checkNotNull(cachedReturnType, "Return type cannot be null");
        }

        return cachedReturnType;
    }

    private List<ParameterSymbol> computeParameters() {
        var parameters = getParametersInternal();
        Preconditions.checkNotNull(parameters, "Parameters cannot be null");

        var size = parameters.size() + (this.isInstance() ? 1 : 0);
        var result = new java.util.ArrayList<ParameterSymbol>(size);
        if (this.isInstance()) {
            Preconditions.checkState(owner instanceof TypeSymbol, "Instance methods must be owned by a type");

            var ownerType = (TypeSymbol) owner;
            var selfParam = new SynthesizedParameterSymbol("self", 0, ownerType.type());
            result.add(selfParam);
        }

        for (var param : parameters) {
            if (param == null) {
                throw new IllegalStateException("Parameter cannot be null");
            }

            if (param.index() != result.size()) {
                param = param.withIndex(result.size());
            }

            result.add(param);
        }

        return result;
    }

    @Override
    public int getModifiers() {
        return modifiers
                | TypedSymbol.super.getModifiers();
    }

    public final ParameterSymbol getParameter(int index) {
        if (index < 0 || index >= parameters().size()) {
            throw new IndexOutOfBoundsException("Parameter index out of bounds: " + index);
        }

        return parameters().get(index);
    }

    @Override
    public final CallableType type() {
        return new CallableType(
                returnType(),
                parameters().stream()
                        .map(ParameterSymbol::type)
                        .toList()
        );
    }
}
