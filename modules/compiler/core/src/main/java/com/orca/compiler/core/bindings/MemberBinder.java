package com.orca.compiler.core.bindings;

import java.util.List;

import javax.annotation.Nullable;

import com.orca.compiler.core.symbols.NamespaceOrTypeSymbol;
import com.orca.compiler.core.symbols.Symbol;
import com.orca.compiler.core.symbols.TypeSymbol;

public class MemberBinder extends Binder {

    private final NamespaceOrTypeSymbol owner;

    public MemberBinder(Binder parent, NamespaceOrTypeSymbol owner) {
        super(parent);
        this.owner = owner;
    }

    /**
     * Gets the namespace or type that owns the members being bound by this
     * binder.
     *
     * @return The owning namespace or type symbol.
     */
    public NamespaceOrTypeSymbol getOwnerSymbol() {
        return owner;
    }

    @Override
    public @Nullable
    Symbol lookupSymbol(String name) {
        Symbol member;
        if (owner instanceof TypeSymbol typeSymbol) {
            member = typeSymbol.getMemberWithExtensions(name);
        } else {
            member = owner.getMember(name);
        }

        if (member != null) {
            return member;
        }

        return super.lookupSymbol(name);
    }

    @Override
    public List<Symbol> lookupSymbols(String name) {
        var symbols = super.lookupSymbols(name);

        if (owner instanceof TypeSymbol typeSymbol) {
            symbols.addAll(typeSymbol.getMembersWithExtensions(name));
        } else {
            symbols.addAll(owner.getMembers(name));
        }

        return symbols;
    }
}
