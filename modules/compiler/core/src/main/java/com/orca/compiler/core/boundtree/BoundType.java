package com.orca.compiler.core.boundtree;

import java.util.List;

import com.orca.compiler.core.symbols.FieldSymbol;
import com.orca.compiler.core.symbols.TypeSymbol;

public final class BoundType extends BoundNode {
    private final TypeSymbol symbol;
    private final BoundNamespace owner;
    private final List<BoundMethod> methods = new java.util.ArrayList<>();
    private final List<BoundVariable> variables = new java.util.ArrayList<>();
    private final List<BoundField> fields = new java.util.ArrayList<>();
    private final List<BoundConstructor> constructors = new java.util.ArrayList<>();

    public BoundType(BoundNamespace owner, TypeSymbol symbol) {
        this.owner = owner;
        this.symbol = symbol;
    }

    public BoundNamespace getOwner() {
        return owner;
    }

    public TypeSymbol getSymbol() {
        return symbol;
    }

    @Override
    public BoundNodeKind kind() {
        return BoundNodeKind.TYPE;
    }

    public void addField(FieldSymbol fieldSymbol) {
        fields.add(new BoundField(this, fieldSymbol));
    }

    public List<BoundField> getFields() {
        return List.copyOf(fields);
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

    public void addConstructor(BoundConstructor constructor) {
        constructors.add(constructor);
    }

    public List<BoundConstructor> getConstructors() {
        return List.copyOf(constructors);
    }
}
