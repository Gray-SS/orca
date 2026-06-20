package com.orca.compiler.core.boundtree.expressions;

import java.util.List;

import com.orca.compiler.core.boundtree.BoundExpression;
import com.orca.compiler.core.boundtree.BoundNodeKind;
import com.orca.compiler.core.boundtree.BoundVisitor;
import com.orca.compiler.core.boundtree.Child;
import com.orca.compiler.core.symbols.TypeSymbol;
import com.orca.compiler.core.typesystem.CollectionType;
import com.orca.compiler.core.typesystem.LangType;

public final class BoundCollectionLiteralExpr extends BoundExpression {

    public final TypeSymbol symbol;
    @Child public final List<BoundExpression> arguments;

    public BoundCollectionLiteralExpr(TypeSymbol symbol, List<BoundExpression> arguments) {
        this.symbol = symbol;
        this.arguments = arguments;
    }

    @Override
    public BoundNodeKind kind() {
        return BoundNodeKind.COLLECTION_LITERAL_EXPR;
    }

    @Override
    public <R> R accept(BoundVisitor<R> visitor) {
        return visitor.visitCollectionLiteralExpr(this);
    }

    @Override
    public LangType type() {
        return symbol.type();
    }

    public CollectionType collectionType() {
        return (CollectionType) symbol.type();
    }
}
