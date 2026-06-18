package com.orca.compiler.core.typesystem;

import com.orca.compiler.core.symbols.Lazy;

public final class NominalType extends BaseType {

    private final String fullName;
    private final Lazy<LangType> lazyUnderlingType;

    public NominalType(String fullName, Lazy<LangType> lazyUnderlyingType) {
        this.fullName = fullName;
        this.lazyUnderlingType = lazyUnderlyingType;
    }

    public NominalType(String fullName, LangType underlyingType) {
        this(fullName, new Lazy<>(() -> underlyingType));
    }

    @Override
    public TypeKind kind() {
        return TypeKind.NOMINAL;
    }

    public String name() {
        return fullName;
    }

    public LangType underlyingType() {
        return lazyUnderlingType.resolve();
    }

    @Override
    public String displayName() {
        return fullName.replace('$', '.');
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof NominalType o)) {
            return false;
        }

        return this.fullName.equals(o.fullName);
    }

    @Override
    public int hashCode() {
        return fullName.hashCode();
    }
}
