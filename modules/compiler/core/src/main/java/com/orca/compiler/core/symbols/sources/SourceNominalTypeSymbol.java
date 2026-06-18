package com.orca.compiler.core.symbols.sources;

import java.util.ArrayList;
import java.util.List;

import com.orca.compiler.core.boundtree.BoundConstructor;
import com.orca.compiler.core.boundtree.BoundStatement;
import com.orca.compiler.core.boundtree.expressions.BoundAssignmentExpr;
import com.orca.compiler.core.boundtree.expressions.BoundReferenceExpr;
import com.orca.compiler.core.boundtree.statements.BoundBlockStmt;
import com.orca.compiler.core.boundtree.statements.BoundExpressionStmt;
import com.orca.compiler.core.symbols.CallableSymbol;
import com.orca.compiler.core.symbols.ConstructorSymbol;
import com.orca.compiler.core.symbols.ExtensionSymbol;
import com.orca.compiler.core.symbols.FieldSymbol;
import com.orca.compiler.core.symbols.Lazy;
import com.orca.compiler.core.symbols.NamespaceOrTypeSymbol;
import com.orca.compiler.core.symbols.Symbol;
import com.orca.compiler.core.symbols.TypeSymbol;
import com.orca.compiler.core.symbols.synthesized.SynthesizedConstructorSymbol;
import com.orca.compiler.core.symbols.synthesized.SynthesizedParameterSymbol;
import com.orca.compiler.core.syntax.SyntaxNode;
import com.orca.compiler.core.typesystem.LangType;
import com.orca.compiler.core.typesystem.NominalType;

public class SourceNominalTypeSymbol implements SourceSymbol, TypeSymbol {

    private final List<Symbol> members = new ArrayList<>();
    private final List<ExtensionSymbol> extensions = new ArrayList<>();

    private final String name;
    private final SyntaxNode declaringSyntax;

    private final NamespaceOrTypeSymbol owner;
    private final NominalType type;

    private ConstructorSymbol defaultConstructor;

    public SourceNominalTypeSymbol(NamespaceOrTypeSymbol owner, String name, SyntaxNode declaringSyntax, Lazy<LangType> lazyShape) {
        this.name = name;
        this.declaringSyntax = declaringSyntax;
        this.owner = owner;
        this.type = new NominalType(getFullName(), lazyShape);
    }

    public BoundConstructor bindDefaultConstructor() {
        ensureDefaultConstructor();
        var statements = new ArrayList<BoundStatement>();
        for (FieldSymbol field : getFields()) {
            var parameter = defaultConstructor.getParameter(field.index());
            var assignment = BoundAssignmentExpr.simpleAssignment(
                    BoundReferenceExpr.of(BoundReferenceExpr.self(this), field),
                    BoundReferenceExpr.of(parameter)
            );

            var statement = new BoundExpressionStmt(assignment);
            statements.add(statement);
        }

        var body = new BoundBlockStmt(statements);
        return new BoundConstructor(defaultConstructor, body);
    }

    public void forceUnderlyingTypeResolution() {
        type.underlyingType();
    }

    @Override
    public List<ConstructorSymbol> getConstructors() {
        ensureDefaultConstructor();
        return TypeSymbol.super.getConstructors();
    }

    private void ensureDefaultConstructor() {
        if (defaultConstructor == null) {
            defaultConstructor = createDefaultConstructor();
            addMember(defaultConstructor);
        }
    }

    private ConstructorSymbol createDefaultConstructor() {
        var fields = getFields();
        var parameters = new ArrayList<SynthesizedParameterSymbol>();
        for (int i = 0; i < fields.size(); i++) {
            var field = fields.get(i);
            parameters.add(new SynthesizedParameterSymbol(field.name(), field.index(), field.type()));
        }

        return new SynthesizedConstructorSymbol(this, parameters);
    }

    @Override
    public String name() {
        return name;
    }

    @Override
    public List<ExtensionSymbol> getExtensions() {
        return extensions;
    }

    @Override
    public void addExtension(ExtensionSymbol extension) {
        extensions.add(extension);
    }

    @Override
    public int getModifiers() {
        return SourceSymbol.super.getModifiers()
                | TypeSymbol.super.getModifiers();
    }

    @Override
    public NamespaceOrTypeSymbol owner() {
        return owner;
    }

    @Override
    public SyntaxNode declaringSyntax() {
        return declaringSyntax;
    }

    @Override
    public NominalType type() {
        return type;
    }

    @Override
    public void addMember(Symbol symbol) {
        if (!(symbol instanceof CallableSymbol) && hasNonCallableMember(symbol.name())) {
            throw new IllegalStateException("Name conflict: " + symbol.name() + " already exists in namespace " + getFullName());
        }

        members.add(symbol);
    }

    private boolean hasNonCallableMember(String name) {
        for (Symbol member : members) {
            if (member.name().equals(name) && !(member instanceof CallableSymbol)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public Symbol getMember(String name) {
        for (Symbol member : members) {
            if (member.name().equals(name)) {
                return member;
            }
        }
        return null;
    }

    public List<Symbol> getMembers(String name) {
        List<Symbol> result = new ArrayList<>();

        for (Symbol member : members) {
            if (member.name().equals(name)) {
                result.add(member);
            }
        }

        return result;
    }

    public List<FieldSymbol> getFields() {
        forceUnderlyingTypeResolution();
        return members.stream()
                .filter(member -> member instanceof FieldSymbol)
                .map(member -> (FieldSymbol) member)
                .toList();
    }

    @Override
    public List<Symbol> getMembers() {
        return new ArrayList<>(members);
    }

    @Override
    public boolean hasMember(String name) {
        for (Symbol member : members) {
            if (member.name().equals(name)) {
                return true;
            }
        }

        return false;
    }
}
