package com.orca.compiler.core.typesystem;

public class TypeSystem {

    private static final TypeSystem DEFAULT_INSTANCE = new TypeSystem();

    public static TypeSystem getInstance() {
        return DEFAULT_INSTANCE;
    }

    public boolean isAssignable(LangType target, LangType source) {
        var conversion = Conversions.classify(source, target);
        return conversion.identity() || conversion.implicit();
    }

    public boolean isConvertible(LangType target, LangType source) {
        var conversion = Conversions.classify(source, target);
        return !conversion.none();
    }

    public boolean requiresCast(LangType target, LangType source) {
        var conversion = Conversions.classify(source, target);
        return conversion.explicit();
    }
}
