package com.orca.compiler.core.symbols.constructed;

import com.orca.compiler.core.symbols.TypeSymbol;
import com.orca.compiler.core.typesystem.LangType;

public final class ArrayTypeSymbol extends ConstructedTypeSymbol {

    private final TypeSymbol elementType;

    public ArrayTypeSymbol(TypeSymbol elementType) {
        super(LangType.arrayOf(elementType.type()));
        this.elementType = elementType;
    }

    public TypeSymbol getElementType() {
        return elementType;
    }
}
