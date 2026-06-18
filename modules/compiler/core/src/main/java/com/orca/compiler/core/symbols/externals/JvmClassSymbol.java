package com.orca.compiler.core.symbols.externals;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;

import com.orca.compiler.core.externals.ExternalSymbolResolver;
import com.orca.compiler.core.symbols.ExtensionSymbol;
import com.orca.compiler.core.symbols.LazySymbolScope;
import com.orca.compiler.core.symbols.NamespaceOrTypeSymbol;
import com.orca.compiler.core.symbols.Symbol;
import com.orca.compiler.core.symbols.SymbolKey;
import com.orca.compiler.core.symbols.TypeSymbol;
import com.orca.compiler.core.typesystem.LangType;
import com.orca.compiler.core.typesystem.NominalType;

public final class JvmClassSymbol implements JvmSymbol, TypeSymbol {

    private final Class<?> clazz;
    private final NominalType type;
    private final NamespaceOrTypeSymbol owner;
    private final ExternalSymbolResolver resolver;

    private final LazySymbolScope members = new LazySymbolScope();
    private final List<ExtensionSymbol> extensions = new java.util.ArrayList<>();

    private boolean membersEnumerated = false;

    public JvmClassSymbol(ExternalSymbolResolver resolver, Class<?> clazz) {
        if (!resolver.classExists(clazz.getName())) {
            throw new IllegalArgumentException("Class does not exist: " + clazz.getName());
        }

        this.resolver = resolver;
        this.clazz = clazz;
        this.owner = getOwnerSymbol(resolver, clazz);
        // Use the JVM binary name (with '$') so resolution and bytecode descriptors work correctly.
        this.type = new NominalType(clazz.getName(), LangType.Opaque);
    }

    private static NamespaceOrTypeSymbol getOwnerSymbol(ExternalSymbolResolver resolver, Class<?> clazz) {
        var declaringClass = clazz.getDeclaringClass();
        if (declaringClass != null) {
            return resolver.getOrCreateClassSymbol(declaringClass.getName());
        }

        var qualifiedName = clazz.getName();
        var lastDotIndex = qualifiedName.lastIndexOf('.');
        if (lastDotIndex == -1) {
            return null;
        }

        var packageName = qualifiedName.substring(0, lastDotIndex);
        return resolver.getOrCreatePackageSymbol(packageName);
    }

    @Override
    public int getModifiers() {
        return JvmSymbol.super.getModifiers()
                | TypeSymbol.super.getModifiers();
    }

    @Override
    public void addExtension(ExtensionSymbol extension) {
        extensions.add(extension);
    }

    @Override
    public List<ExtensionSymbol> getExtensions() {
        return extensions;
    }

    @Override
    public String name() {
        return clazz.getSimpleName();
    }

    @Override
    public NamespaceOrTypeSymbol owner() {
        return owner;
    }

    @Override
    public NominalType type() {
        return type;
    }

    public String internalName() {
        return clazz.getName().replace('.', '/');
    }

    public Class<?> getClazz() {
        return clazz;
    }

    @Override
    public boolean hasMember(String name) {
        ensureMembersEnumerated();
        return members.hasName(name);
    }

    @Override
    public void addMember(Symbol member) {
        throw new UnsupportedOperationException("Cannot add members to a JVM class symbol.");
    }

    @Override
    public Symbol getMember(String name) {
        ensureMembersEnumerated();
        var resolvedMembers = members.resolve(name);
        if (resolvedMembers.isEmpty()) {
            return null;
        }

        return resolvedMembers.get(0);
    }

    @Override
    public List<Symbol> getMembers() {
        ensureMembersEnumerated();
        members.resolveAll();

        return members.resolvedSymbols().toList();
    }

    @Override
    public List<Symbol> getMembers(String name) {
        ensureMembersEnumerated();
        return members.resolve(name);
    }

    @Override
    public List<Symbol> getResolvedMembers() {
        return members.resolvedSymbols().toList();
    }

    private void ensureMembersEnumerated() {
        if (membersEnumerated) {
            return;
        }

        enumerateMembers();
        membersEnumerated = true;
    }

    private void enumerateMembers() {
        enumerateFields();
        enumerateMethods();
        enumerateConstructors();
        enumerateNestedTypes();
    }

    private void enumerateFields() {
        var declaredFields = clazz.getDeclaredFields();
        for (int i = 0; i < declaredFields.length; i++) {
            var field = declaredFields[i];
            if (!isPublic(field.getModifiers())) {
                continue;
            }

            var index = i;
            members.register(SymbolKey.simple(field.getName()), () -> {
                return new JvmFieldSymbol(this, index, field);
            });
        }
    }

    private void enumerateMethods() {
        var declaredMethods = clazz.getDeclaredMethods();
        for (Method method : declaredMethods) {
            if (!isPublic(method.getModifiers())) {
                continue;
            }

            var parameterTypeNames = getParameterTypeNames(method.getParameterTypes());
            members.register(SymbolKey.Overloaded.of(method.getName(), parameterTypeNames), () -> {
                return new JvmMethodSymbol(this, method);
            });
        }
    }

    private void enumerateConstructors() {
        var declaredConstructors = clazz.getDeclaredConstructors();
        for (Constructor<?> ctor : declaredConstructors) {
            if (!isPublic(ctor.getModifiers())) {
                continue;
            }

            var parameterTypeNames = getParameterTypeNames(ctor.getParameterTypes());

            members.register(SymbolKey.Overloaded.of("<init>", parameterTypeNames), () -> {
                return new JvmConstructorSymbol(this, ctor);
            });
        }
    }

    private void enumerateNestedTypes() {
        var declaredClasses = clazz.getDeclaredClasses();
        for (Class<?> nestedClazz : declaredClasses) {
            if (!isPublic(nestedClazz.getModifiers())) {
                continue;
            }

            members.register(SymbolKey.simple(nestedClazz.getSimpleName()), () -> {
                return resolver.getOrCreateClassSymbol(nestedClazz.getName());
            });
        }
    }

    private static boolean isPublic(int modifiers) {
        return java.lang.reflect.Modifier.isPublic(modifiers);
    }

    private static List<String> getParameterTypeNames(Class<?>[] parameterTypes) {
        return Arrays.stream(parameterTypes)
                .map(Class::getName)
                .toList();
    }
}
