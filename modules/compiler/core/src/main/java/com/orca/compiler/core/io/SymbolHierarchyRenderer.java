package com.orca.compiler.core.io;

import java.util.List;

import com.orca.compiler.core.symbols.CallableSymbol;
import com.orca.compiler.core.symbols.NamespaceOrTypeSymbol;
import com.orca.compiler.core.symbols.NamespaceSymbol;
import com.orca.compiler.core.symbols.Symbol;

public final class SymbolHierarchyRenderer {

    public static void render(NamespaceSymbol globalNamespace, int indentSize) {
        render(globalNamespace, 0, indentSize);
    }

    public static void render(NamespaceOrTypeSymbol owner, int indentSize) {
        render(owner, 0, indentSize);
    }

    private static void render(Symbol symbol, int indent, int indentSize) {
        String indentStr = " ".repeat(indent * indentSize);

        System.out.print(indentStr + symbol.displayNameWithAttributes());
        if (symbol instanceof CallableSymbol callable) {
            System.out.print(" : " + callable.type().displayName());
        }

        if (symbol instanceof NamespaceOrTypeSymbol nsOrType) {
            System.out.println(":");

            List<Symbol> members = nsOrType.getResolvedMembers();
            for (var member : members) {
                render(member, indent + 1, indentSize);
            }
        } else {
            System.out.println();
        }
    }
}
