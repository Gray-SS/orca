package com.orca.compiler.core.codegen;

import java.util.List;

import org.objectweb.asm.AnnotationVisitor;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.FieldVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Type;

import com.orca.compiler.core.symbols.metadata.OrcaMethod;
import com.orca.compiler.core.symbols.metadata.OrcaNamespace;
import com.orca.compiler.core.symbols.metadata.OrcaVariable;

/**
 * Writes Orca metadata annotations into JVM class files via ASM.
 *
 * <p>
 * All methods are pure side effects on their visitor arguments. Converting
 * {@code LangType} or {@code Symbol} to the string representations expected
 * here is the caller's responsibility (see {@code TypeEmitter}).
 *
 * <p>
 * Descriptors are derived from the live {@code @interface} classes so a package
 * rename never silently produces a stale descriptor.
 */
public final class AnnotationEmitter {

    private static final String DESC_NAMESPACE = Type.getDescriptor(OrcaNamespace.class);
    private static final String DESC_VARIABLE = Type.getDescriptor(OrcaVariable.class);
    private static final String DESC_METHOD = Type.getDescriptor(OrcaMethod.class);

    private AnnotationEmitter() {
    }

    // -------------------------------------------------------------------------
    // Namespace
    // -------------------------------------------------------------------------
    /**
     * Writes {@code @OrcaNamespace} on a {@code Package.class}.
     *
     * @param cv class visitor for the Package class being emitted
     * @param namespaceName fully qualified Orca namespace path, e.g.
     * {@code "std::io"}
     */
    public static void writeNamespace(ClassVisitor cv, String namespaceName) {
        AnnotationVisitor av = cv.visitAnnotation(DESC_NAMESPACE, false);
        av.visit("name", namespaceName);
        av.visit("version", OrcaNamespace.SCHEMA_VERSION);
        av.visitEnd();
    }

    // -------------------------------------------------------------------------
    // Variable (static field)
    // -------------------------------------------------------------------------
    /**
     * Writes {@code @OrcaVariable} on a namespace-level field.
     *
     * @param fv field visitor for the field being emitted
     * @param orcaType fully qualified Orca type name, e.g. {@code "Int"}
     * @param mutable {@code true} if declared with {@code var}
     * @param modifiers stable modifier tags, e.g. {@code ["static"]}
     */
    public static void writeVariable(FieldVisitor fv, String orcaType, boolean mutable, List<String> modifiers) {
        AnnotationVisitor av = fv.visitAnnotation(DESC_VARIABLE, false);
        av.visit("orcaType", orcaType);
        av.visit("mutable", mutable);
        av.visit("version", OrcaVariable.SCHEMA_VERSION);
        writeStringArray(av, "modifiers", modifiers);
        av.visitEnd();
    }

    // -------------------------------------------------------------------------
    // Method (static method)
    // -------------------------------------------------------------------------
    /**
     * Writes {@code @OrcaMethod} on a namespace-level function.
     *
     * <p>
     * The implicit {@code self} parameter must be excluded from
     * {@code paramNames}/{@code paramTypes} before calling this.
     *
     * @param mv method visitor for the method being emitted
     * @param paramNames Orca parameter names in declaration order
     * @param paramTypes Orca type name for each parameter, parallel to
     * {@code paramNames}
     * @param returnType fully qualified Orca return type, e.g. {@code "Void"}
     * @param modifiers stable modifier tags
     */
    public static void writeMethod(
            MethodVisitor mv,
            List<String> paramNames,
            List<String> paramTypes,
            String returnType,
            List<String> modifiers) {
        AnnotationVisitor av = mv.visitAnnotation(DESC_METHOD, false);
        writeStringArray(av, "paramNames", paramNames);
        writeStringArray(av, "paramTypes", paramTypes);
        av.visit("returnType", returnType);
        writeStringArray(av, "modifiers", modifiers);
        av.visit("version", OrcaMethod.SCHEMA_VERSION);
        av.visitEnd();
    }

    // -------------------------------------------------------------------------
    private static void writeStringArray(AnnotationVisitor av, String attrName, List<String> values) {
        AnnotationVisitor arr = av.visitArray(attrName);
        for (String v : values) {
            arr.visit(null, v);
        }
        arr.visitEnd();
    }
}
