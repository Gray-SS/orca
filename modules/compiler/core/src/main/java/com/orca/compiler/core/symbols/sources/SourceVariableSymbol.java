package com.orca.compiler.core.symbols.sources;

import javax.annotation.Nullable;

import com.orca.compiler.core.Debug;
import com.orca.compiler.core.boundtree.constants.BoundConstant;
import com.orca.compiler.core.symbols.NamespaceOrTypeSymbol;
import com.orca.compiler.core.symbols.SymbolKind;
import com.orca.compiler.core.symbols.SymbolModifiers;
import com.orca.compiler.core.symbols.VariableSymbol;
import com.orca.compiler.core.syntax.SyntaxNode;
import com.orca.compiler.core.typesystem.LangType;

public final class SourceVariableSymbol implements SourceSymbol, VariableSymbol {

    private final String name;
    private final SyntaxNode declaringSyntax;
    private final LangType type;
    private final SymbolKind kind;
    private final int additionalModifiers;

    private final NamespaceOrTypeSymbol owner;

    @Nullable
    private BoundConstant constant;

    private SourceVariableSymbol(NamespaceOrTypeSymbol owner, String name, SyntaxNode declaringSyntax, LangType type, SymbolKind kind, BoundConstant constant, int additionalModifiers) {
        Debug.requireNotNullOrEmpty(name, "name cannot be null");
        Debug.requireNotNull(declaringSyntax, "declaringSyntax cannot be null");
        Debug.requireNotNull(type, "type cannot be null");
        Debug.requireNotNull(kind, "kind cannot be null");

        this.owner = owner;
        this.name = name;
        this.declaringSyntax = declaringSyntax;
        this.type = type;
        this.kind = kind;
        this.constant = constant;
        this.additionalModifiers = additionalModifiers;
    }

    public static SourceVariableSymbol createLocal(String name, SyntaxNode declaringSyntax, LangType type, boolean isImmutable) {
        int modifiers = isImmutable ? SymbolModifiers.IMMUTABLE : SymbolModifiers.NONE;
        return new SourceVariableSymbol(null, name, declaringSyntax, type, SymbolKind.VARIABLE, null, modifiers);
    }

    public static SourceVariableSymbol createConstant(NamespaceOrTypeSymbol owner, String name, SyntaxNode declaringSyntax, BoundConstant constant, boolean isLocal) {
        Debug.requireNotNull(constant, "constantValue cannot be null for a constant variable");
        int modifiers = SymbolModifiers.NONE;
        if (!isLocal) {
            modifiers |= SymbolModifiers.STATIC;
        }

        return new SourceVariableSymbol(owner, name, declaringSyntax, constant.type(), SymbolKind.VARIABLE, constant, modifiers);
    }

    public static SourceVariableSymbol createAssociated(NamespaceOrTypeSymbol owner, String name, SyntaxNode declaringSyntax, LangType type, boolean isImmutable) {
        int modifiers = SymbolModifiers.STATIC;
        if (isImmutable) {
            modifiers |= SymbolModifiers.IMMUTABLE;
        }
        return new SourceVariableSymbol(owner, name, declaringSyntax, type, SymbolKind.VARIABLE, null, modifiers);
    }

    @Override
    public String name() {
        return name;
    }

    @Override
    public SymbolKind kind() {
        return kind;
    }

    @Override
    public NamespaceOrTypeSymbol owner() {
        return owner;
    }

    @Override
    public int getModifiers() {
        return SourceSymbol.super.getModifiers()
                | VariableSymbol.super.getModifiers()
                | additionalModifiers;
    }

    @Override
    public SyntaxNode declaringSyntax() {
        return declaringSyntax;
    }

    @Override
    public LangType type() {
        return type;
    }

    @Override
    public BoundConstant getConstant() {
        return constant;
    }

    public void resolveConstantValue(BoundConstant constant) {
        Debug.assertTrue(isConstantVariable(), "Only constant variables can have a constant value");
        Debug.requireNotNull(constant, "Constant value cannot be null");

        // We allow resolving the constant value multiple times as long as it's the same value (to handle cases where the value is not known at the time of symbol creation)
        if (this.constant != null) {
            Debug.assertEquals(this.constant, constant, "Constant value is already set to a different value");
        }

        this.constant = constant;
    }
}
