package com.orca.compiler.core.typesystem;

import java.util.List;

public final class CallableType extends BaseType {

    private final LangType returnType;
    private final List<LangType> parameterTypes;

    public CallableType(LangType returnType, List<LangType> parameterTypes) {
        this.returnType = returnType;
        this.parameterTypes = parameterTypes;
    }

    @Override
    public TypeKind kind() {
        return TypeKind.FUNCTION;
    }

    @Override
    public String displayName() {
        var sb = new StringBuilder();
        sb.append("(");
        for (int i = 0; i < parameterTypes.size(); i++) {
            if (i > 0) {
                sb.append(", ");
            }
            sb.append(parameterTypes.get(i).displayName());
        }
        sb.append(") -> ").append(returnType.displayName());
        return sb.toString();
    }

    public LangType returnType() {
        return returnType;
    }

    public List<LangType> parameterTypes() {
        return parameterTypes;
    }
}
