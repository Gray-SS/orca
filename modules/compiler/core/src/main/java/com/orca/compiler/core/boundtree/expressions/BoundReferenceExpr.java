package com.orca.compiler.core.boundtree.expressions;

import java.util.List;

import com.google.common.base.Preconditions;
import com.orca.compiler.core.boundtree.BoundExpression;
import com.orca.compiler.core.boundtree.BoundNodeKind;
import com.orca.compiler.core.boundtree.BoundVisitor;
import com.orca.compiler.core.boundtree.Child;
import com.orca.compiler.core.symbols.ConstructorSymbol;
import com.orca.compiler.core.symbols.FieldSymbol;
import com.orca.compiler.core.symbols.MethodSymbol;
import com.orca.compiler.core.symbols.MissingSymbol;
import com.orca.compiler.core.symbols.ParameterSymbol;
import com.orca.compiler.core.symbols.Symbol;
import com.orca.compiler.core.symbols.TypeSymbol;
import com.orca.compiler.core.symbols.ValueSymbol;
import com.orca.compiler.core.symbols.VariableSymbol;
import com.orca.compiler.core.typesystem.LangType;

public sealed abstract class BoundReferenceExpr extends BoundExpression
        permits BoundReferenceExpr.FieldRef,
        BoundReferenceExpr.ParameterRef,
        BoundReferenceExpr.TypeRef,
        BoundReferenceExpr.VariableRef,
        BoundReferenceExpr.MemberAccessRef,
        BoundReferenceExpr.MethodGroupRef,
        BoundReferenceExpr.MethodRef,
        BoundReferenceExpr.SelfRef,
        BoundReferenceExpr.ConstructorRef,
        BoundReferenceExpr.MissingRef {

    private final List<? extends Symbol> symbols;

    public BoundReferenceExpr(Symbol symbol) {
        Preconditions.checkNotNull(symbol, "symbol cannot be null");

        this.symbols = List.of(symbol);
    }

    public BoundReferenceExpr(List<? extends Symbol> symbols) {
        Preconditions.checkNotNull(symbols, "symbols cannot be null");
        Preconditions.checkArgument(!symbols.isEmpty(), "symbols cannot be empty");

        this.symbols = List.copyOf(symbols);
    }

    @Override
    public <R> R accept(BoundVisitor<R> visitor) {
        return visitor.visitReferenceExpr(this);
    }

    public Symbol getFirstReferencedSymbol() {
        return symbols.get(0);
    }

    public Symbol getReferencedSymbol() {
        if (symbols.size() != 1) {
            throw new IllegalStateException("Expected exactly one symbol for this reference expression, but found: " + symbols.size());
        }

        return symbols.get(0);
    }

    public List<? extends Symbol> getReferencedSymbols() {
        return List.copyOf(symbols);
    }

    @Override
    public LangType type() {
        if (symbols.get(0) instanceof ValueSymbol valueSymbol) {
            return valueSymbol.type();
        }

        throw new UnsupportedOperationException("Only value symbols have a type");
    }

    public static SelfRef self(TypeSymbol containingType) {
        return new SelfRef(containingType);
    }

    public static BoundReferenceExpr of(Symbol symbol) {
        switch (symbol) {
            case ParameterSymbol parameter -> {
                return new ParameterRef(parameter);
            }
            case VariableSymbol variable -> {
                return new VariableRef(variable);
            }
            case FieldSymbol field -> {
                return new FieldRef(field);
            }
            case MethodSymbol method -> {
                return new MethodRef(method);
            }
            case TypeSymbol type -> {
                return new TypeRef(type);
            }
            case ConstructorSymbol ctor -> {
                return new ConstructorRef(ctor);
            }
            case MissingSymbol missing -> {
                return new MissingRef(missing);
            }
            default ->
                throw new IllegalStateException("Unsupported symbol type for reference expression: " + symbol.getClass().getSimpleName());
        }
    }

    public static FieldRef of(FieldSymbol symbol) {
        return new FieldRef(symbol);
    }

    public static ParameterRef of(ParameterSymbol symbol) {
        return new ParameterRef(symbol);
    }

    public static TypeRef of(TypeSymbol symbol) {
        return new TypeRef(symbol);
    }

    public static VariableRef of(VariableSymbol symbol) {
        return new VariableRef(symbol);
    }

    public static MethodRef of(MethodSymbol symbol) {
        return new MethodRef(symbol);
    }

    public static ConstructorRef of(ConstructorSymbol symbol) {
        return new ConstructorRef(symbol);
    }

    public static MethodGroupRef of(List<MethodSymbol> methods) {
        return new MethodGroupRef(methods);
    }

    public static MemberAccessRef of(BoundExpression receiver, Symbol memberSymbol) {
        var memberExpr = of(memberSymbol);
        return new MemberAccessRef(receiver, memberExpr);
    }

    public static MemberAccessRef of(BoundExpression receiver, List<MethodSymbol> methods) {
        var methodGroupExpr = of(methods);
        return new MemberAccessRef(receiver, methodGroupExpr);
    }

    public static MemberAccessRef of(BoundExpression receiver, BoundReferenceExpr memberExpr) {
        return new MemberAccessRef(receiver, memberExpr);
    }

    public static final class MissingRef extends BoundReferenceExpr {

        public MissingRef(MissingSymbol missingSymbol) {
            super(missingSymbol);
        }

        @Override
        public MissingSymbol getReferencedSymbol() {
            return (MissingSymbol) super.getReferencedSymbol();
        }

        @Override
        public BoundNodeKind kind() {
            return BoundNodeKind.MISSING_REF_EXPR;
        }
    }

    public static final class ConstructorRef extends BoundReferenceExpr {

        public ConstructorRef(ConstructorSymbol symbol) {
            super(symbol);
        }

        @Override
        public ConstructorSymbol getReferencedSymbol() {
            return (ConstructorSymbol) super.getReferencedSymbol();
        }

        @Override
        public BoundNodeKind kind() {
            return BoundNodeKind.CONSTRUCTOR_REF_EXPR;
        }
    }

    public static final class SelfRef extends BoundReferenceExpr {

        public SelfRef(TypeSymbol containingType) {
            super(containingType);
        }

        @Override
        public TypeSymbol getReferencedSymbol() {
            return (TypeSymbol) super.getReferencedSymbol();
        }

        @Override
        public BoundNodeKind kind() {
            return BoundNodeKind.SELF_EXPR;
        }
    }

    public static final class MethodRef extends BoundReferenceExpr {

        public MethodRef(MethodSymbol method) {
            super(method);
        }

        @Override
        public MethodSymbol getReferencedSymbol() {
            return (MethodSymbol) super.getReferencedSymbol();
        }

        @Override
        public BoundNodeKind kind() {
            return BoundNodeKind.METHOD_EXPR;
        }
    }

    public static final class MethodGroupRef extends BoundReferenceExpr {

        public MethodGroupRef(List<MethodSymbol> methods) {
            super(methods);
        }

        @Override
        public BoundNodeKind kind() {
            return BoundNodeKind.METHOD_GROUP_EXPR;
        }
    }

    public static final class MemberAccessRef extends BoundReferenceExpr {

        @Child private final BoundExpression receiver;
        @Child private final BoundReferenceExpr memberExpr;

        public MemberAccessRef(BoundExpression receiver, BoundReferenceExpr memberExpr) {
            super(memberExpr.getReferencedSymbols());
            this.receiver = receiver;
            this.memberExpr = memberExpr;
        }

        public BoundReferenceExpr getMemberExpr() {
            return memberExpr;
        }

        public BoundExpression getReceiver() {
            return receiver;
        }

        @Override
        public BoundNodeKind kind() {
            return BoundNodeKind.MEMBER_ACCESS_EXPR;
        }
    }

    public static final class FieldRef extends BoundReferenceExpr {

        public FieldRef(FieldSymbol symbol) {
            super(symbol);
        }

        @Override
        public FieldSymbol getReferencedSymbol() {
            return (FieldSymbol) super.getReferencedSymbol();
        }

        @Override
        public BoundNodeKind kind() {
            return BoundNodeKind.FIELD_REF_EXPR;
        }
    }

    public static final class ParameterRef extends BoundReferenceExpr {

        public ParameterRef(ParameterSymbol symbol) {
            super(symbol);
        }

        @Override
        public ParameterSymbol getReferencedSymbol() {
            return (ParameterSymbol) super.getReferencedSymbol();
        }

        @Override
        public BoundNodeKind kind() {
            return BoundNodeKind.PARAMETER_REF_EXPR;
        }
    }

    public static final class TypeRef extends BoundReferenceExpr {

        public TypeRef(TypeSymbol symbol) {
            super(symbol);
        }

        @Override
        public TypeSymbol getReferencedSymbol() {
            return (TypeSymbol) super.getReferencedSymbol();
        }

        @Override
        public BoundNodeKind kind() {
            return BoundNodeKind.TYPE_EXPR;
        }
    }

    public static final class VariableRef extends BoundReferenceExpr {

        public VariableRef(VariableSymbol symbol) {
            super(symbol);
        }

        @Override
        public VariableSymbol getReferencedSymbol() {
            return (VariableSymbol) super.getReferencedSymbol();
        }

        @Override
        public BoundNodeKind kind() {
            return BoundNodeKind.VARIABLE_REF_EXPR;
        }

        @Override
        public boolean isCompileTimeFoldable() {
            return getReferencedSymbol().isCompileTimeConstant();
        }
    }
}
