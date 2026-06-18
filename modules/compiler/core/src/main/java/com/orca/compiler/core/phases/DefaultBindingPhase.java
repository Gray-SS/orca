package com.orca.compiler.core.phases;

import com.orca.compiler.core.Compilation;
import com.orca.compiler.core.CompilationContext;
import com.orca.compiler.core.CompilerException;
import com.orca.compiler.core.CompilerFlag;
import com.orca.compiler.core.boundtree.BoundProgram;
import com.orca.compiler.core.io.LoweredBoundTreePrinter;
import com.orca.compiler.core.io.SymbolHierarchyRenderer;

/**
 * Implémentation par défaut de la phase de binding.
 */
public class DefaultBindingPhase implements BindingPhase {

    @Override
    public BoundProgram bind(CompilationContext context) throws CompilerException {
        context.logger().debug("Starting binding phase...");
        var compilation = new Compilation(context);

        try {
            var boundProgram = compilation.getBoundProgram();

            if (context.hasFlag(CompilerFlag.PRINT_LOWERED_BOUND_TREE)) {
                LoweredBoundTreePrinter.print(boundProgram, context.getIndentSize());
            }

            return boundProgram;
        } finally {
            if (context.hasFlag(CompilerFlag.PRINT_SYMBOL_HIERARCHY)) {
                SymbolHierarchyRenderer.render(compilation.getGlobalNamespace(), context.getIndentSize());
            }
        }
    }
}
