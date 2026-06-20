package com.orca.compiler.core.symbols.sources;

import java.util.List;

import com.orca.compiler.core.symbols.MethodSymbol;
import com.orca.compiler.core.symbols.NamespaceOrTypeSymbol;
import com.orca.compiler.core.symbols.ParameterSymbol;
import com.orca.compiler.core.symbols.SymbolModifiers;
import com.orca.compiler.core.syntax.SyntaxNode;
import com.orca.compiler.core.typesystem.LangType;

public class SourceMethodSymbol extends MethodSymbol implements SourceSymbol {

    private int modifiers;
    private final String name;
    private final SyntaxNode declaringSyntax;
    private final LangType returnType;
    private final List<SourceParameterSymbol> parameters;

    public SourceMethodSymbol(String name, SyntaxNode declaringSyntax, NamespaceOrTypeSymbol ownerType, LangType returnType, List<SourceParameterSymbol> parameters, boolean isStatic) {
        super(ownerType, name, isStatic ? SymbolModifiers.STATIC : SymbolModifiers.INSTANCE);
        this.name = name;
        this.declaringSyntax = declaringSyntax;
        this.returnType = returnType;
        this.parameters = parameters;
    }

    @Override
    public String name() {
        return name;
    }

    @Override
    public SyntaxNode declaringSyntax() {
        return declaringSyntax;
    }

    /**
     * Marks this method as the entry point of the program. This is used to
     * identify the main function during code generation.
     */
    public void markAsEntryPoint() {
        modifiers |= SymbolModifiers.ENTRY_POINT;
    }

    @Override
    public int getModifiers() {
        return super.getModifiers()
                | SourceSymbol.super.getModifiers()
                | modifiers;
    }

    @Override
    protected LangType getReturnTypeInternal() {
        return returnType;
    }

    @Override
    protected List<? extends ParameterSymbol> getParametersInternal() {
        return parameters;
    }

    @Override
    public String toString() {
        return "SourceMethodSymbol{name='" + name + "',, modifiers=" + getModifiers() + ", parameters=" + parameters + ", return-type=" + returnType + "}";
    }
}
