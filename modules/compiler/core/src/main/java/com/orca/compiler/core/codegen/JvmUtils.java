package com.orca.compiler.core.codegen;

import com.orca.compiler.core.symbols.NamespaceOrTypeSymbol;
import com.orca.compiler.core.symbols.NamespaceSymbol;
import com.orca.compiler.core.symbols.Symbol;
import com.orca.compiler.core.symbols.TypeSymbol;
import com.orca.compiler.core.symbols.externals.JvmClassSymbol;

public final class JvmUtils {

    public static String getOwnerInternalName(Symbol symbol) {
        return getInternalName(symbol.owner());
    }

    public static String getInternalName(NamespaceOrTypeSymbol symbol) {
        if (symbol == null) {
            return "";
        }

        if (symbol instanceof NamespaceSymbol ns) {
            if (ns.isGlobalNamespace()) {
                return "Package";
            }
            return getNamespaceDirectoryPath(ns) + "/Package";
        } else if (symbol instanceof JvmClassSymbol jvmClass) {
            return jvmClass.internalName();
        } else if (symbol instanceof TypeSymbol ts) {
            var ownerPath = getNamespaceDirectoryPath((NamespaceSymbol) ts.owner());
            return ownerPath.isEmpty() ? ts.name() : ownerPath + "/" + ts.name();
        } else {
            throw new IllegalArgumentException("Unsupported symbol type: " + symbol.getClass().getName());
        }
    }

    // Returns the JVM directory path for a namespace, without a /Package suffix.
    // e.g. global → "", foo → "foo", std::io → "std/io"
    private static String getNamespaceDirectoryPath(NamespaceSymbol ns) {
        if (ns == null || ns.isGlobalNamespace()) {
            return "";
        }
        var parentPath = getNamespaceDirectoryPath((NamespaceSymbol) ns.owner());
        return parentPath.isEmpty() ? ns.name() : parentPath + "/" + ns.name();
    }
}
