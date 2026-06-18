package com.orca.compiler.core.symbols.externals;

import java.util.List;
import java.util.Set;

import com.orca.compiler.core.externals.ExternalSymbolResolver;
import com.orca.compiler.core.symbols.NamespaceSymbol;
import com.orca.compiler.core.symbols.Symbol;

public final class JvmNamespaceSymbol implements JvmSymbol, NamespaceSymbol {

    private final String name;
    private final JvmNamespaceSymbol owner;

    private final ExternalSymbolResolver resolver;
    private final Set<String> indexedMembers;

    private JvmClassSymbol packageClassSymbol;
    private boolean packageClassChecked = false;

    public JvmNamespaceSymbol(ExternalSymbolResolver resolver, String qualifiedName) {
        if (!resolver.packageExists(qualifiedName)) {
            throw new IllegalArgumentException("Package does not exist: " + qualifiedName);
        }

        this.resolver = resolver;
        this.name = qualifiedName.substring(qualifiedName.lastIndexOf('.') + 1);
        this.owner = getParentPackageSymbol(resolver, qualifiedName);
        this.indexedMembers = resolver.indexDirectMembersOf(qualifiedName);
    }

    private JvmClassSymbol getPackageClassSymbol() {
        if (packageClassChecked) {
            return packageClassSymbol;
        }

        var packageClassName = getFullName() + ".Package";
        if (!resolver.classExists(packageClassName)) {
            packageClassSymbol = null;
        } else {
            packageClassSymbol = resolver.getOrCreateClassSymbol(packageClassName);
        }

        packageClassChecked = true;
        return packageClassSymbol;
    }

    private static JvmNamespaceSymbol getParentPackageSymbol(ExternalSymbolResolver resolver, String qualifiedName) {
        var parentPackageName = getParentPackageName(qualifiedName);
        if (parentPackageName.isBlank()) {
            return null;
        }

        return resolver.getOrCreatePackageSymbol(parentPackageName);
    }

    private static String getParentPackageName(String qualifiedName) {
        var lastDotIndex = qualifiedName.lastIndexOf('.');
        if (lastDotIndex == -1) {
            return "";
        }

        return qualifiedName.substring(0, lastDotIndex);
    }

    @Override
    public String name() {
        return name;
    }

    public List<String> getIndexedMemberNames() {
        return List.copyOf(indexedMembers);
    }

    @Override
    public JvmNamespaceSymbol owner() {
        return owner;
    }

    @Override
    public int getModifiers() {
        return JvmSymbol.super.getModifiers()
                | NamespaceSymbol.super.getModifiers();
    }

    @Override
    public void addMember(Symbol member) {
        throw new UnsupportedOperationException("Cannot add members to an external package symbol");
    }

    @Override
    public Symbol getMember(String name) {
        if (!hasMember(name)) {
            return null;
        }

        var fullName = getMemberName(name);
        if (resolver.exists(fullName)) {
            return resolver.getOrCreateSymbol(fullName);
        }

        var pkgClassSymbol = getPackageClassSymbol();
        if (pkgClassSymbol != null && pkgClassSymbol.hasMember(name)) {
            return pkgClassSymbol.getMember(name);
        }

        throw new IllegalStateException("Member should exist but was not found: " + fullName);
    }

    @Override
    public List<Symbol> getMembers(String name) {
        if (!hasMember(name)) {
            return List.of();
        }

        var members = new java.util.ArrayList<Symbol>();
        var pkgClassSymbol = getPackageClassSymbol();
        if (pkgClassSymbol != null && pkgClassSymbol.hasMember(name)) {
            members.addAll(pkgClassSymbol.getMembers(name));
        }

        var member = getMember(name);
        if (member != null) {
            members.add(member);
        }

        return members;
    }

    @Override
    public List<Symbol> getMembers() {
        var members = new java.util.ArrayList<Symbol>();
        for (var memberName : indexedMembers) {
            var member = getMember(memberName);
            if (member != null) {
                members.add(member);
            }
        }

        var pkgClassSymbol = getPackageClassSymbol();
        if (pkgClassSymbol != null) {
            members.addAll(pkgClassSymbol.getMembers());
        }

        return members;
    }

    @Override
    public boolean hasMember(String name) {
        var pkgClassSymbol = getPackageClassSymbol();
        if (pkgClassSymbol != null && pkgClassSymbol.hasMember(name)) {
            return true;
        }

        return resolver.exists(getMemberName(name));
    }

    private String getMemberName(String className) {
        return getFullName() + "." + className;
    }
}
