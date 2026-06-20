package com.orca.compiler.core.phases;

import com.orca.compiler.core.CompilationContext;
import com.orca.compiler.core.boundtree.BoundProgram;

/**
 * Interface pour la phase de code generation.
 */
public interface CodeGenPhase {

    /**
     * Retourne une instance par défaut de la phase de code generation.
     */
    public static CodeGenPhase defaultInstance() {
        return new DefaultCodeGenPhase();
    }

    /**
     * Génère le bytecode JVM à partir d'un programme lié.
     *
     * @param context le contexte de compilation
     * @param program le programme lié
     * @throws Exception si la génération de code échoue
     */
    void generate(CompilationContext context, BoundProgram program) throws Exception;
}
