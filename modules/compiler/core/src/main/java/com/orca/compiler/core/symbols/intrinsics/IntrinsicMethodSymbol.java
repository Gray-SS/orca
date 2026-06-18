package com.orca.compiler.core.symbols.intrinsics;

import java.util.ArrayList;
import java.util.List;

import com.orca.compiler.core.symbols.MethodSymbol;
import com.orca.compiler.core.symbols.SymbolModifiers;
import com.orca.compiler.core.symbols.synthesized.SynthesizedParameterSymbol;
import com.orca.compiler.core.typesystem.LangType;

public final class IntrinsicMethodSymbol extends MethodSymbol implements IntrinsicSymbol {
    private final IntrinsicKind intrinsicKind;
    private final List<LangType> parameterTypes;
    private final LangType returnType;

    public static final IntrinsicMethodSymbol NOT = new IntrinsicMethodSymbol(IntrinsicKind.METHOD_NOT, LangType.Bool, List.of(LangType.Bool));
    public static final IntrinsicMethodSymbol FLOOR = new IntrinsicMethodSymbol(IntrinsicKind.METHOD_FLOOR, LangType.Int, List.of(LangType.Float));
    public static final IntrinsicMethodSymbol CEIL = new IntrinsicMethodSymbol(IntrinsicKind.METHOD_CEIL, LangType.Int, List.of(LangType.Float));
    public static final IntrinsicMethodSymbol STR_0STRING = new IntrinsicMethodSymbol(IntrinsicKind.METHOD_STR_0STRING, LangType.String, List.of(LangType.String));
    public static final IntrinsicMethodSymbol STR_0BYTE = new IntrinsicMethodSymbol(IntrinsicKind.METHOD_STR_0BYTE, LangType.String, List.of(LangType.Byte));
    public static final IntrinsicMethodSymbol STR_0SHORT = new IntrinsicMethodSymbol(IntrinsicKind.METHOD_STR_0SHORT, LangType.String, List.of(LangType.Short));
    public static final IntrinsicMethodSymbol STR_0INT = new IntrinsicMethodSymbol(IntrinsicKind.METHOD_STR_0INT, LangType.String, List.of(LangType.Int));
    public static final IntrinsicMethodSymbol STR_0LONG = new IntrinsicMethodSymbol(IntrinsicKind.METHOD_STR_0LONG, LangType.String, List.of(LangType.Long));
    public static final IntrinsicMethodSymbol STR_0FLOAT = new IntrinsicMethodSymbol(IntrinsicKind.METHOD_STR_0FLOAT, LangType.String, List.of(LangType.Float));
    public static final IntrinsicMethodSymbol STR_0DOUBLE = new IntrinsicMethodSymbol(IntrinsicKind.METHOD_STR_0DOUBLE, LangType.String, List.of(LangType.Double));
    public static final IntrinsicMethodSymbol STR_0BOOL = new IntrinsicMethodSymbol(IntrinsicKind.METHOD_STR_0BOOL, LangType.String, List.of(LangType.Bool));
    public static final IntrinsicMethodSymbol STR_0CHAR = new IntrinsicMethodSymbol(IntrinsicKind.METHOD_STR_0CHAR, LangType.String, List.of(LangType.Char));
    public static final IntrinsicMethodSymbol LENGTH_0STRING = new IntrinsicMethodSymbol(IntrinsicKind.METHOD_LENGTH_0STRING, LangType.Int, List.of(LangType.String));
    public static final IntrinsicMethodSymbol LENGTH_0ARRAY = new IntrinsicMethodSymbol(IntrinsicKind.METHOD_LENGTH_0ARRAY, LangType.Int, List.of(LangType.arrayOf(LangType.Any)));

    public IntrinsicMethodSymbol(IntrinsicKind intrinsicKind, LangType returnType, List<LangType> parameterTypes) {
        super(null, intrinsicKind.getDisplayName(), SymbolModifiers.STATIC);
        this.intrinsicKind = intrinsicKind;
        this.returnType = returnType;
        this.parameterTypes = parameterTypes;
    }

    public static final List<IntrinsicMethodSymbol> getAllIntrinsicMethods() {
        return List.of(
            NOT,
            FLOOR,
            CEIL,
            STR_0STRING,
            STR_0BYTE,
            STR_0SHORT,
            STR_0INT,
            STR_0LONG,
            STR_0FLOAT,
            STR_0DOUBLE,
            STR_0BOOL,
            STR_0CHAR,
            LENGTH_0STRING,
            LENGTH_0ARRAY
        );
    }

    @Override
    protected LangType getReturnTypeInternal() {
        return returnType;
    }

    @Override
    protected List<SynthesizedParameterSymbol> getParametersInternal() {
        var parameters = new ArrayList<SynthesizedParameterSymbol>();
        for (int i = 0; i < parameterTypes.size(); i++) {
            parameters.add(new SynthesizedParameterSymbol(i, parameterTypes.get(i)));
        }

        return parameters;
    }

    @Override
    public IntrinsicKind intrinsicKind() {
        return intrinsicKind;
    }

    @Override
    public int getModifiers() {
        return super.getModifiers()
            | IntrinsicSymbol.super.getModifiers()
            | SymbolModifiers.STATIC;
    }
}
