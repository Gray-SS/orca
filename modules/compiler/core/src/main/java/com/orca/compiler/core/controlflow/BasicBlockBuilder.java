package com.orca.compiler.core.controlflow;

import java.util.List;

import com.orca.compiler.core.boundtree.BoundStatement;
import com.orca.compiler.core.boundtree.statements.BoundBlockStmt;

public final class BasicBlockBuilder {
    private final List<BasicBlock> blocks = new java.util.ArrayList<>();
    private final List<BoundStatement> statements = new java.util.ArrayList<>();

    public List<BasicBlock> build(BoundBlockStmt block) {
        for (BoundStatement stmt : block.statements()) {
            switch (stmt.kind()) {
                case LABEL_STMT:
                    flush();
                    statements.add(stmt);
                    break;

                case CONDITIONAL_GOTO_STMT:
                case RETURN_STMT:
                case GOTO_STMT:
                    statements.add(stmt);
                    flush();
                    break;

                default:
                    statements.add(stmt);
                    break;
            }
        }

        flush();
        return blocks;
    }

    private void flush() {
        if (statements.isEmpty()) return;

        BasicBlock block = new BasicBlock(List.copyOf(statements));
        blocks.add(block);
        statements.clear();
    }
}
