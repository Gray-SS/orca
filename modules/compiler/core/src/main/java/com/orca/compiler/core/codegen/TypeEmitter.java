package com.orca.compiler.core.codegen;

import org.objectweb.asm.ClassWriter;

import com.orca.compiler.core.JvmTypeMapper;
import com.orca.compiler.core.boundtree.BoundExpression;
import com.orca.compiler.core.boundtree.BoundField;
import com.orca.compiler.core.boundtree.BoundMethod;
import com.orca.compiler.core.boundtree.BoundStatement;
import com.orca.compiler.core.boundtree.BoundType;
import com.orca.compiler.core.boundtree.BoundVariable;
import com.orca.compiler.core.boundtree.expressions.BoundAssignmentExpr;
import com.orca.compiler.core.boundtree.expressions.BoundDefaultValueExpr;
import com.orca.compiler.core.boundtree.expressions.BoundReferenceExpr;
import com.orca.compiler.core.boundtree.statements.BoundBlockStmt;
import com.orca.compiler.core.boundtree.statements.BoundExpressionStmt;
import com.orca.compiler.core.symbols.NamespaceSymbol;
import com.orca.compiler.core.symbols.SymbolModifiers;
import com.orca.compiler.core.symbols.synthesized.SynthesizedMethodSymbol;
import com.orca.compiler.core.typesystem.LangType;

import static org.objectweb.asm.Opcodes.*;

import java.util.List;

public final class TypeEmitter {

    private final Emitter emitter;
    private final BoundType boundType;
    private final boolean isPackageClass;
    private final AsmClassWriter classWriter;

    private static final int JAVA_CLASS_VERSION = V21;

    public TypeEmitter(Emitter emitter, BoundType boundType, boolean isPackageClass) {
        this.emitter = emitter;
        this.boundType = boundType;
        this.isPackageClass = isPackageClass;
        this.classWriter = new AsmClassWriter(ClassWriter.COMPUTE_FRAMES | ClassWriter.COMPUTE_MAXS);
    }

    public void write() {
        var bytes = emit();
        emitter.writeClass(JvmUtils.getInternalName(boundType.getSymbol()), bytes);
    }

    public byte[] emit() {
        classWriter.visit(JAVA_CLASS_VERSION, ACC_PUBLIC | ACC_SUPER, JvmUtils.getInternalName(boundType.getSymbol()), null, Emitter.JAVA_OBJECT_INTERNAL_NAME, null);

        if (isPackageClass) {
            var namespace = (NamespaceSymbol) boundType.getSymbol().owner();
            AnnotationEmitter.writeNamespace(classWriter, namespace.getFullName());
        }

        emitConstructors();
        emitMethods();
        emitVariables();
        emitFields();

        classWriter.visitEnd();
        return classWriter.toByteArray();
    }

    private BoundMethod createSynthesizedClinitMethod() {
        var clinitSymbol = new SynthesizedMethodSymbol(boundType.getSymbol(), Emitter.JAVA_CLASS_INIT_NAME, LangType.Void, List.of(), SymbolModifiers.STATIC);
        var clinitStmts = new java.util.ArrayList<BoundStatement>();
        for (BoundVariable boundVariable : boundType.getVariables()) {
            BoundExpression initializer = boundVariable.initializer();
            if (initializer == null) {
                initializer = new BoundDefaultValueExpr(boundVariable.getSymbol().type());
            }

            var assignment = BoundAssignmentExpr.simpleAssignment(BoundReferenceExpr.of(boundVariable.getSymbol()), initializer);
            var expressionStmt = new BoundExpressionStmt(assignment);
            clinitStmts.add(expressionStmt);
        }

        var clinitBody = new BoundBlockStmt(clinitStmts);
        return new BoundMethod(boundType, clinitSymbol, clinitBody);
    }

    private void emitFields() {
        for (BoundField boundField : boundType.getFields()) {
            var access = ACC_PUBLIC;
            if (boundField.getSymbol().isCompileTimeConstant()) {
                access |= ACC_FINAL;
            }

            var fieldVisitor = classWriter.visitField(access, boundField.getName(), JvmTypeMapper.descriptor(boundField.getSymbol().type()), null, null);
            fieldVisitor.visitEnd();
        }
    }

    private void emitMethods() {
        for (BoundMethod boundMethod : boundType.getMethods()) {
            var methodEmitter = new MethodEmitter(classWriter, boundMethod);
            methodEmitter.emit();
        }

        var clinitMethod = createSynthesizedClinitMethod();
        var methodEmitter = new MethodEmitter(classWriter, clinitMethod);
        methodEmitter.emit();
    }

    private void emitVariables() {
        for (BoundVariable boundVariable : boundType.getVariables()) {
            var symbol = boundVariable.getSymbol();
            var access = ACC_PUBLIC | ACC_STATIC;
            if (symbol.isCompileTimeConstant()) {
                access |= ACC_FINAL;
            }

            var fieldVisitor = classWriter.visitField(access, boundVariable.getName(), JvmTypeMapper.descriptor(symbol.type()), null, null);

            if (isPackageClass) {
                var isMutable = !symbol.isCompileTimeConstant();
                var modifiers = symbol.getAttributes().stream()
                        .map(a -> a.getDisplayName())
                        .toList();
                AnnotationEmitter.writeVariable(fieldVisitor, symbol.type().displayName(), isMutable, modifiers);
            }

            fieldVisitor.visitEnd();
        }
    }

    private void emitConstructors() {
        var constructors = boundType.getConstructors();
        if (constructors.isEmpty()) {
            // If no constructors are defined, emit a default constructor
            emitDefaultConstructor();
            return;
        }

        for (var constructor : constructors) {
            var constructorEmitter = new MethodEmitter(classWriter, constructor);
            constructorEmitter.emit();
        }
    }

    private void emitDefaultConstructor() {
        var mv = classWriter.visitMethod(ACC_PUBLIC, Emitter.JAVA_CTOR_NAME, "()V", null, null);
        mv.visitCode();
        mv.visitVarInsn(ALOAD, 0);
        mv.visitMethodInsn(INVOKESPECIAL, Emitter.JAVA_OBJECT_INTERNAL_NAME, Emitter.JAVA_CTOR_NAME, "()V", false);
        mv.visitInsn(RETURN);
        mv.visitMaxs(0, 0); // COMPUTE_MAXS will calculate this
        mv.visitEnd();
    }
}
