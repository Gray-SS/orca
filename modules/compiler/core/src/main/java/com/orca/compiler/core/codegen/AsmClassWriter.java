package com.orca.compiler.core.codegen;

import org.objectweb.asm.ClassWriter;

/**
 * ASM ClassWriter with a conservative common-super resolution that avoids
 * loading generated classes through the application ClassLoader.
 */
final class AsmClassWriter extends ClassWriter {
    AsmClassWriter(int flags) {
        super(flags);
    }

    @Override
    protected String getCommonSuperClass(String type1, String type2) {
        if (type1 == null || type2 == null) return "java/lang/Object";
        if (type1.equals(type2)) return type1;

        // Arrays: common super is Object unless they share exact element type.
        if (type1.startsWith("[") || type2.startsWith("[")) {
            if (type1.equals(type2)) return type1;
            return "java/lang/Object";
        }

        // All generated classes are simple (extend Object, no interfaces).
        // Returning Object is safe and avoids classloading.
        return "java/lang/Object";
    }
}
