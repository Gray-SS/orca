package com.orca.compiler.core.symbols.intrinsics;

import java.util.List;

import com.orca.compiler.core.symbols.ExtensionSymbol;
import com.orca.compiler.core.symbols.Symbol;
import com.orca.compiler.core.symbols.SymbolScope;
import com.orca.compiler.core.symbols.TypeSymbol;
import com.orca.compiler.core.typesystem.LangType;
import com.orca.compiler.core.typesystem.PrimitiveType;

public final class IntrinsicTypeSymbol implements IntrinsicSymbol, TypeSymbol {
    private final LangType type;
    private final SymbolScope members = new SymbolScope();
    private final List<ExtensionSymbol> extensions = new java.util.ArrayList<>();

    public static IntrinsicTypeSymbol Bool = new IntrinsicTypeSymbol(PrimitiveType.Bool);
    public static IntrinsicTypeSymbol Byte = new IntrinsicTypeSymbol(PrimitiveType.Byte);
    public static IntrinsicTypeSymbol Short = new IntrinsicTypeSymbol(PrimitiveType.Short);
    public static IntrinsicTypeSymbol Int = new IntrinsicTypeSymbol(PrimitiveType.Int);
    public static IntrinsicTypeSymbol Long = new IntrinsicTypeSymbol(PrimitiveType.Long);
    public static IntrinsicTypeSymbol Float = new IntrinsicTypeSymbol(PrimitiveType.Float);
    public static IntrinsicTypeSymbol Double = new IntrinsicTypeSymbol(PrimitiveType.Double);
    public static IntrinsicTypeSymbol Char = new IntrinsicTypeSymbol(PrimitiveType.Char);
    public static IntrinsicTypeSymbol String = new IntrinsicTypeSymbol(LangType.String);
    public static IntrinsicTypeSymbol Any = new IntrinsicTypeSymbol(LangType.Any);

    private IntrinsicTypeSymbol(LangType type) {
        this.type = type;
    }

    @Override
    public String name() {
        return type.displayName();
    }

    @Override
    public int getModifiers() {
        return IntrinsicSymbol.super.getModifiers() | TypeSymbol.super.getModifiers();
    }

    @Override
    public IntrinsicKind intrinsicKind() {
        return IntrinsicKind.NONE;
    }

    @Override
    public LangType type() {
        return type;
    }

    @Override
    public void addMember(Symbol member) {
        members.add(member);
    }

    @Override
    public Symbol getMember(String name) {
        return members.get(name);
    }

    @Override
    public List<Symbol> getMembers(String name) {
        return members.getAll(name);
    }

    @Override
    public List<Symbol> getMembers() {
        return members.getAll();
    }

    @Override
    public boolean hasMember(String name) {
        return members.has(name);
    }

    @Override
    public void addExtension(ExtensionSymbol extension) {
        extensions.add(extension);
    }

    @Override
    public List<ExtensionSymbol> getExtensions() {
        return extensions;
    }
}
