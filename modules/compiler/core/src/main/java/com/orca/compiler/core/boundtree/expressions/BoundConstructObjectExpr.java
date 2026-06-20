package com.orca.compiler.core.boundtree.expressions;

import java.util.List;

import com.orca.compiler.core.boundtree.BoundExpression;
import com.orca.compiler.core.boundtree.BoundNodeKind;
import com.orca.compiler.core.boundtree.BoundVisitor;
import com.orca.compiler.core.boundtree.Child;
import com.orca.compiler.core.symbols.ConstructorSymbol;
import com.orca.compiler.core.symbols.TypeSymbol;
import com.orca.compiler.core.typesystem.LangType;

public final class BoundConstructObjectExpr extends BoundExpression {

    private final ConstructorSymbol constructor;
    @Child private final List<BoundExpression> arguments;

    public BoundConstructObjectExpr(ConstructorSymbol constructor, List<BoundExpression> arguments) {
        this.constructor = constructor;
        this.arguments = arguments;
    }

    /**
     * Gets the constructor symbol being invoked by this object construction
     * expression.
     *
     * @return The constructor symbol being invoked by this object construction
     * expression.
     */
    public ConstructorSymbol getConstructor() {
        return constructor;
    }

    /**
     * Gets the type symbol of the object being constructed by this object
     * construction expression.
     *
     * @return The type symbol of the object being constructed by this object
     * construction expression.
     */
    public TypeSymbol getConstructedType() {
        return constructor.owner();
    }

    /**
     * Gets the list of bound expressions representing the arguments passed to
     * the constructor in this object construction expression.
     *
     * @return The list of bound expressions representing the arguments passed
     * to the constructor in this object construction expression.
     */
    public List<BoundExpression> getArguments() {
        return arguments;
    }

    @Override
    public BoundNodeKind kind() {
        return BoundNodeKind.CONSTRUCT_OBJECT_EXPR;
    }

    @Override
    public <R> R accept(BoundVisitor<R> visitor) {
        return visitor.visitConstructObjectExpr(this);
    }

    @Override
    public LangType type() {
        return getConstructedType().type();
    }
}
