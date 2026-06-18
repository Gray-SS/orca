package com.orca.compiler.core.phases;

import com.orca.compiler.core.CompilationContext;
import com.orca.compiler.core.CompilerException;
import com.orca.compiler.core.boundtree.BoundProgram;

/**
 * Interface pour la phase de semantic analysis et binding.
 */
public interface BindingPhase {
    /**
     * Retourne une instance par défaut de la phase de binding.
     */
    public static BindingPhase defaultInstance() {
        return new DefaultBindingPhase();
    }

    /**
     * Effectue la liaision (binding) et analyse sémantique sur l'arbre syntaxique.
     * 
     * @param context le contexte de compilation
     * @param syntaxTree l'arbre syntaxique
     * @return le programme lié
     * @throws CompilerException si une erreur sémantique se produit
     */
    BoundProgram bind(CompilationContext context) throws CompilerException;
}
