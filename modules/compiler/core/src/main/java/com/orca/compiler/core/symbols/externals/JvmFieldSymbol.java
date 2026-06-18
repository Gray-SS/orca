package com.orca.compiler.core.symbols.externals;

import java.lang.reflect.Field;

import com.orca.compiler.core.JvmTypeMapper;
import com.orca.compiler.core.symbols.FieldSymbol;
import com.orca.compiler.core.typesystem.LangType;

public final class JvmFieldSymbol implements JvmSymbol, FieldSymbol {

    private final int index;
    private final Field field;
    private final JvmClassSymbol owner;

    private LangType type;

    public JvmFieldSymbol(JvmClassSymbol owner, int index, Field field) {
        this.owner = owner;
        this.index = index;
        this.field = field;
    }

    @Override
    public String name() {
        return field.getName();
    }

    @Override
    public boolean isStatic() {
        return java.lang.reflect.Modifier.isStatic(field.getModifiers());
    }

    @Override
    public int getModifiers() {
        return JvmSymbol.super.getModifiers()
                | FieldSymbol.super.getModifiers();
    }

    @Override
    public int index() {
        return index;
    }

    @Override
    public LangType type() {
        if (type == null) {
            type = JvmTypeMapper.adaptType(field.getType());
        }

        return type;
    }

    @Override
    public JvmClassSymbol owner() {
        return owner;
    }
}
