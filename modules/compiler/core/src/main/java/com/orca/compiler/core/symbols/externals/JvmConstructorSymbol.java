package com.orca.compiler.core.symbols.externals;

import java.lang.reflect.Constructor;
import java.util.List;

import com.orca.compiler.core.symbols.ConstructorSymbol;
import com.orca.compiler.core.symbols.ParameterSymbol;
import com.orca.compiler.core.symbols.SymbolModifiers;

public final class JvmConstructorSymbol extends ConstructorSymbol implements JvmSymbol {

    private final Constructor<?> constructor;
    private final JvmClassSymbol owner;

    private List<JvmParameterSymbol> parameters;

    public JvmConstructorSymbol(JvmClassSymbol owner, Constructor<?> constructor) {
        super(owner);
        this.owner = owner;
        this.constructor = constructor;
    }

    @Override
    public JvmClassSymbol owner() {
        return owner;
    }

    @Override
    public List<? extends ParameterSymbol> getParametersInternal() {
        if (parameters == null) {
            var params = constructor.getParameters();
            parameters = new java.util.ArrayList<>(params.length);
            for (int i = 0; i < params.length; i++) {
                var param = params[i];
                var paramSymbol = new JvmParameterSymbol(this, param, i);
                parameters.add(paramSymbol);
            }
        }

        return parameters;
    }

    @Override
    public int getModifiers() {
        return super.getModifiers()
                | JvmSymbol.super.getModifiers()
                | SymbolModifiers.fromJavaModifiers(constructor.getModifiers());
    }
}
