package com.orca.compiler.core.boundtree;

import java.util.List;

import com.orca.compiler.core.symbols.NamespaceSymbol;

public final class BoundNamespace extends BoundNode {

    private BoundType packageClass;

    private final NamespaceSymbol symbol;
    @Child private final List<BoundType> types = new java.util.ArrayList<>();
    @Child private final List<BoundMethod> methods = new java.util.ArrayList<>();
    @Child private final List<BoundVariable> variables = new java.util.ArrayList<>();
    @Child private final List<BoundNamespace> namespaces = new java.util.ArrayList<>();

    public BoundNamespace(NamespaceSymbol symbol) {
        this.symbol = symbol;
    }

    public NamespaceSymbol getSymbol() {
        return symbol;
    }

    public BoundType getBoundPackageClass() {
        if (packageClass != null) {
            return packageClass;
        }

        return packageClass = bindPackageClassSymbol();
    }

    private BoundType bindPackageClassSymbol() {
        var packageClassSymbol = symbol.createSynthesizedClassSymbol();
        var boundType = new BoundType(this, packageClassSymbol);
        for (var s : this.getVariables()) {
            boundType.addVariable(s);
        }

        for (var m : this.getMethods()) {
            boundType.addMethod(m);
        }

        return boundType;
    }

    public void addType(BoundType type) {
        types.add(type);
    }

    public List<BoundType> getTypes() {
        return List.copyOf(types);
    }

    public void addMethod(BoundMethod method) {
        methods.add(method);
    }

    public List<BoundMethod> getMethods() {
        return List.copyOf(methods);
    }

    public void addVariable(BoundVariable variable) {
        variables.add(variable);
    }

    public List<BoundVariable> getVariables() {
        return List.copyOf(variables);
    }

    public void addNamespace(BoundNamespace namespace) {
        namespaces.add(namespace);
    }

    public List<BoundNamespace> getNamespaces() {
        return List.copyOf(namespaces);
    }

    @Override
    public BoundNodeKind kind() {
        return BoundNodeKind.NAMESPACE;
    }

    @Override
    public <R> R accept(BoundVisitor<R> visitor) {
        return visitor.visitNamespace(this);
    }
}
