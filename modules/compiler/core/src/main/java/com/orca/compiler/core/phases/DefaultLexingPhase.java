package com.orca.compiler.core.phases;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import com.orca.compiler.core.CompilationContext;
import com.orca.compiler.core.CompilerException;
import com.orca.compiler.core.CompilerFlag;
import com.orca.compiler.core.lexer.Lexer;
import com.orca.compiler.core.lexer.Token;
import com.orca.compiler.core.text.TextSource;
import com.orca.compiler.core.io.TokenPrinter;

/**
 * Implémentation par défaut de la phase de lexing.
 */
public class DefaultLexingPhase implements LexingPhase {

    @Override
    public List<Token> lex(CompilationContext context, TextSource source) throws CompilerException, IOException {
        context.logger().debug("Starting lexer phase...");

        var symbols = new ArrayList<Token>();
        try (var lexer = new Lexer(source, context.diagnostics())) {
            Token symbol;
            do {
                symbol = lexer.getNextSymbol();
                symbols.add(symbol);
            } while (!symbol.isEOF());
        }

        context.logger().info("Lexer phase completed. Total tokens: " + symbols.size());

        if (context.hasFlag(CompilerFlag.PRINT_TOKENS)) {
            TokenPrinter.printTokens(symbols);
        }

        return symbols;
    }
}
