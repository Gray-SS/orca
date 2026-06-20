package com.orca.compiler.core.phases;

import java.io.IOException;
import java.util.List;

import com.orca.compiler.core.CompilationContext;
import com.orca.compiler.core.CompilerException;
import com.orca.compiler.core.lexer.Token;
import com.orca.compiler.core.text.TextSource;

/**
 * Interface pour la phase de lexical analysis (lexing).
 */
public interface LexingPhase {

    /**
     * Retourne une instance par défaut de la phase de lexing.
     */
    public static LexingPhase defaultInstance() {
        return new DefaultLexingPhase();
    }

    /**
     * Effectue l'analyse lexicale sur une source de texte.
     *
     * @param context le contexte de compilation
     * @param source la source de texte à analyser
     * @return la liste des tokens
     * @throws CompilerException si une erreur lexicale se produit
     * @throws IOException si une erreur d'entrée/sortie se produit
     */
    List<Token> lex(CompilationContext context, TextSource source) throws CompilerException, IOException;
}
