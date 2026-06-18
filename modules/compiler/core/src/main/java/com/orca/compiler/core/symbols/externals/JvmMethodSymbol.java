package com.orca.compiler.core.symbols.externals;

import java.lang.reflect.Method;
import java.util.List;

import com.orca.compiler.core.JvmTypeMapper;
import com.orca.compiler.core.symbols.MethodSymbol;
import com.orca.compiler.core.symbols.SymbolModifiers;
import com.orca.compiler.core.typesystem.LangType;

public class JvmMethodSymbol extends MethodSymbol implements JvmSymbol {

    private final Method method;
    private final JvmClassSymbol owner;

    public JvmMethodSymbol(JvmClassSymbol owner, Method method) {
        super(owner, method.getName(), SymbolModifiers.fromJavaModifiers(method.getModifiers()));
        this.method = method;
        this.owner = owner;
    }

    @Override
    public String name() {
        return method.getName();
    }

    @Override
    public JvmClassSymbol owner() {
        return owner;
    }

    @Override
    public int getModifiers() {
        return super.getModifiers()
                | JvmSymbol.super.getModifiers();
    }

    @Override
    protected LangType getReturnTypeInternal() {
        return JvmTypeMapper.adaptType(method.getReturnType());
    }

    @Override
    protected List<JvmParameterSymbol> getParametersInternal() {
        var params = method.getParameters();
        var parameters = new java.util.ArrayList<JvmParameterSymbol>(params.length);
        for (int i = 0; i < params.length; i++) {
            var param = params[i];
            var parameterSymbol = new JvmParameterSymbol(this, param, i);
            parameters.add(parameterSymbol);
        }

        return parameters;
    }

    @Override
    public String toString() {
        return String.format("JvmMethodSymbol(name=%s, owner=%s, returnType=%s, parameters=%s)", name(), owner.name(), returnType(), parameters());
    }
}
