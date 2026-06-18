package com.orca.compiler.core.symbols.externals;

import java.lang.reflect.Parameter;

import com.orca.compiler.core.JvmTypeMapper;
import com.orca.compiler.core.symbols.ParameterSymbol;
import com.orca.compiler.core.typesystem.LangType;

public final class JvmParameterSymbol implements JvmSymbol, ParameterSymbol {

    private final int index;
    private final Parameter parameter;
    private final JvmMethodSymbol method;
    private final JvmConstructorSymbol constructor;

    private LangType type;

    public JvmParameterSymbol(JvmMethodSymbol method, Parameter parameter, int index) {
        this.method = method;
        this.index = index;
        this.parameter = parameter;
        this.constructor = null;
    }

    public JvmParameterSymbol(JvmConstructorSymbol constructor, Parameter parameter, int index) {
        this.constructor = constructor;
        this.index = index;
        this.parameter = parameter;
        this.method = null;
    }

    @Override
    public String name() {
        return parameter.getName();
    }

    @Override
    public boolean isVarargs() {
        return parameter.isVarArgs();
    }

    @Override
    public ParameterSymbol withIndex(int newIndex) {
        return new JvmParameterSymbol(method, parameter, newIndex);
    }

    @Override
    public int getModifiers() {
        return JvmSymbol.super.getModifiers()
                | ParameterSymbol.super.getModifiers();
    }

    /**
     * Gets the method that this parameter belongs to, if it belongs to a
     * method.
     *
     * @return The method that this parameter belongs to, or null if it belongs
     * to a constructor.
     */
    public JvmMethodSymbol getMethod() {
        return method;
    }

    /**
     * Gets the constructor that this parameter belongs to, if it belongs to a
     * constructor.
     *
     * @return The constructor that this parameter belongs to, or null if it
     * belongs to a method.
     */
    public JvmConstructorSymbol getConstructor() {
        return constructor;
    }

    @Override
    public LangType type() {
        if (type == null) {
            type = JvmTypeMapper.adaptType(parameter.getType());
        }

        return type;
    }

    @Override
    public int index() {
        return index;
    }

    @Override
    public String toString() {
        return String.format("JvmParameterSymbol(name=%s, type=%s, index=%d)", name(), type(), index());
    }
}
