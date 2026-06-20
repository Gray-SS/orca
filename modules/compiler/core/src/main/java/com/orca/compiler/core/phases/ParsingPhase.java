package com.orca.compiler.core.phases;

import com.orca.compiler.core.CompilationContext;
import com.orca.compiler.core.CompilerException;
import com.orca.compiler.core.UnsupportedFeatureException;
import com.orca.compiler.core.lexer.Lexer;
import com.orca.compiler.core.syntax.SyntaxTree;

/**
 * Interface pour la phase de parsing (syntactic analysis).
 */
public interface ParsingPhase {

    /**
     * Retourne une instance par défaut de la phase de parsing.
     */
    public static ParsingPhase defaultInstance() {
        return new DefaultParsingPhase();
    }

    /**
     * Effectue l'analyse syntaxique avec le lexer fourni.
     *
     * @param context le contexte de compilation
     * @param lexer le lexer positionné à la source
     * @return l'arbre syntaxique
     * @throws CompilerException si une erreur syntaxique se produit
     * @throws UnsupportedFeatureException si une fonctionnalité non supportée
     * est détectée
     */
    SyntaxTree parse(CompilationContext context, Lexer lexer) throws CompilerException, UnsupportedFeatureException;
}
