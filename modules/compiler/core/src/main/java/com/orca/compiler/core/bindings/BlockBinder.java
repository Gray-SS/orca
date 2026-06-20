package com.orca.compiler.core.bindings;

import com.orca.compiler.core.CompilerException;
import com.orca.compiler.core.boundtree.statements.BoundBlockStmt;
import com.orca.compiler.core.syntax.statements.BlockStmt;

public final class BlockBinder extends LocalBinder {

    public BlockBinder(Binder parent) {
        super(parent);
    }

    public static BoundBlockStmt bindBlock(Binder parent, BlockStmt block) throws CompilerException {
        var binder = new BlockBinder(parent);
        return (BoundBlockStmt) binder.bindStatement(block);
    }
}
