package com.orca.compiler.core.phases;

import com.orca.compiler.core.CompilationContext;
import com.orca.compiler.core.CompilerException;
import com.orca.compiler.core.CompilerFlag;
import com.orca.compiler.core.UnsupportedFeatureException;
import com.orca.compiler.core.io.EnhancedSyntaxTreePrinter;
import com.orca.compiler.core.lexer.Lexer;
import com.orca.compiler.core.syntax.SyntaxTree;

/**
 * Implémentation par défaut de la phase de parsing.
 */
public class DefaultParsingPhase implements ParsingPhase {

    @Override
    public SyntaxTree parse(CompilationContext context, Lexer lexer) throws CompilerException, UnsupportedFeatureException {
        context.logger().debug("Starting syntaxical analysis...");

        var syntaxTree = SyntaxTree.parse(context.diagnostics(), context.arguments().getSources().get(0));

        context.logger().info("Syntaxic analysis completed. Root node: " + syntaxTree.root().getClass().getSimpleName());

        if (context.hasFlag(CompilerFlag.PRINT_AST)) {
            EnhancedSyntaxTreePrinter.print(syntaxTree, context.getIndentSize());
        }

        return syntaxTree;
    }
}
