package com.orca.compiler.core.bindings;

import com.orca.compiler.core.symbols.NamespaceSymbol;

public class NamespaceBinder extends MemberBinder {

    private final NamespaceSymbol namespace;

    public NamespaceBinder(Binder parent, NamespaceSymbol namespace) {
        super(parent, namespace);

        this.namespace = namespace;
    }

    @Override
    public NamespaceSymbol getOwnerSymbol() {
        return namespace;
    }
}
