package com.orca.compiler.core.codegen;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import static org.objectweb.asm.Opcodes.*;

import com.orca.compiler.core.JvmTypeMapper;
import com.orca.compiler.core.boundtree.BoundConstructor;
import com.orca.compiler.core.boundtree.BoundExpression;
import com.orca.compiler.core.boundtree.BoundLabel;
import com.orca.compiler.core.boundtree.BoundMethod;
import com.orca.compiler.core.boundtree.BoundStatement;
import com.orca.compiler.core.boundtree.constants.BoundBoolConstant;
import com.orca.compiler.core.boundtree.constants.BoundByteConstant;
import com.orca.compiler.core.boundtree.constants.BoundConstant;
import com.orca.compiler.core.boundtree.constants.BoundDoubleConstant;
import com.orca.compiler.core.boundtree.constants.BoundFloatConstant;
import com.orca.compiler.core.boundtree.constants.BoundIntConstant;
import com.orca.compiler.core.boundtree.constants.BoundLongConstant;
import com.orca.compiler.core.boundtree.constants.BoundNullConstant;
import com.orca.compiler.core.boundtree.constants.BoundShortConstant;
import com.orca.compiler.core.boundtree.constants.BoundStringConstant;
import com.orca.compiler.core.boundtree.expressions.BoundArrayAccessExpr;
import com.orca.compiler.core.boundtree.expressions.BoundArrayLiteralExpr;
import com.orca.compiler.core.boundtree.expressions.BoundAssignmentExpr;
import com.orca.compiler.core.boundtree.expressions.BoundBinaryExpr;
import com.orca.compiler.core.boundtree.expressions.BoundConstructObjectExpr;
import com.orca.compiler.core.boundtree.expressions.BoundConversionExpr;
import com.orca.compiler.core.boundtree.expressions.BoundDefaultValueExpr;
import com.orca.compiler.core.boundtree.expressions.BoundLiteralExpr;
import com.orca.compiler.core.boundtree.expressions.BoundMethodCallExpr;
import com.orca.compiler.core.boundtree.expressions.BoundReferenceExpr;
import com.orca.compiler.core.boundtree.expressions.BoundSequenceExpr;
import com.orca.compiler.core.boundtree.expressions.BoundTypeTestExpr;
import com.orca.compiler.core.boundtree.expressions.BoundUnaryExpr;
import com.orca.compiler.core.boundtree.statements.BoundBlockStmt;
import com.orca.compiler.core.boundtree.statements.BoundConditionalGotoStmt;
import com.orca.compiler.core.boundtree.statements.BoundExpressionStmt;
import com.orca.compiler.core.boundtree.statements.BoundGotoStmt;
import com.orca.compiler.core.boundtree.statements.BoundLabelStmt;
import com.orca.compiler.core.boundtree.statements.BoundReturnStmt;
import com.orca.compiler.core.boundtree.statements.BoundVariableDeclStmt;
import com.orca.compiler.core.symbols.CallableSymbol;
import com.orca.compiler.core.symbols.ParameterSymbol;
import com.orca.compiler.core.symbols.ValueSymbol;
import com.orca.compiler.core.symbols.VariableSymbol;
import com.orca.compiler.core.symbols.externals.JvmMethodSymbol;
import com.orca.compiler.core.symbols.intrinsics.IntrinsicMethodSymbol;
import com.orca.compiler.core.syntax.expressions.BinaryOperatorKind;
import com.orca.compiler.core.syntax.expressions.UnaryOperatorKind;
import com.orca.compiler.core.typesystem.ArrayType;
import com.orca.compiler.core.typesystem.CollectionType;
import com.orca.compiler.core.typesystem.LangType;
import com.orca.compiler.core.typesystem.NominalType;

public final class MethodEmitter {

    private final BoundBlockStmt body;
    private final CallableSymbol callableSymbol;
    private final ClassWriter classWriter;

    private final Map<ValueSymbol, Integer> slots;
    private final Map<BoundLabel, Label> labelMap = new HashMap<>();

    private MethodVisitor mv;

    public MethodEmitter(ClassWriter classWriter, BoundConstructor constructor) {
        this(classWriter, constructor.getSymbol(), constructor.getBody());
    }

    public MethodEmitter(ClassWriter classWriter, BoundMethod method) {
        this(classWriter, method.getSymbol(), method.getBody());
    }

    private MethodEmitter(ClassWriter classWriter, CallableSymbol callableSymbol, BoundBlockStmt body) {
        this.classWriter = classWriter;
        this.callableSymbol = callableSymbol;
        this.body = body;
        this.slots = createSlots();
    }

    private Label getOrCreateLabel(BoundLabel boundLabel) {
        return labelMap.computeIfAbsent(boundLabel, _k -> new Label());
    }

    private Map<ValueSymbol, Integer> createSlots() {
        var result = new HashMap<ValueSymbol, Integer>();
        var parameters = callableSymbol.parameters();
        // JVM constructors have `this` at slot 0 implicitly (not in parameters list),
        // so explicit parameters start at slot 1.
        int firstSlot = callableSymbol.isConstructor() ? 1 : 0;
        for (int i = 0; i < parameters.size(); i++) {
            result.put(parameters.get(i), firstSlot + i);
        }

        return result;
    }

    private int ensureLocal(VariableSymbol v, int nextSlot) {
        return slots.computeIfAbsent(v, _k -> nextSlot);
    }

    private int nextLocalSlot() {
        int max = 0;
        for (var e : slots.entrySet()) {
            int s = e.getValue();
            int size = JvmCategory.sizeOf(e.getKey().type());
            max = Math.max(max, s + size);
        }
        return max;
    }

    public void emit() {
        String name = callableSymbol.name();

        String descriptor = JvmTypeMapper.getCallableDescriptor(callableSymbol);

        int javaModifiers = ACC_PUBLIC;
        if (callableSymbol.isStatic()) {
            javaModifiers |= ACC_STATIC;
        } else if (callableSymbol.isInstance()) {
            javaModifiers |= ACC_STATIC; // Instance methods are emitted as static methods with an extra "this" parameter.
        }
        mv = classWriter.visitMethod(javaModifiers, name, descriptor, null, null);

        if (!callableSymbol.isSynthesized() && !callableSymbol.isConstructor()) {
            var paramNames = callableSymbol.parameters().stream()
                    .map(ParameterSymbol::name)
                    .toList();
            var paramTypes = callableSymbol.parameters().stream()
                    .map(p -> p.type().displayName())
                    .toList();
            var returnType = callableSymbol.returnType().displayName();
            var modifiers = callableSymbol.getAttributes().stream()
                    .map(a -> a.getDisplayName())
                    .toList();
            AnnotationEmitter.writeMethod(mv, paramNames, paramTypes, returnType, modifiers);
        }

        mv.visitCode();

        if (callableSymbol.isConstructor()) {
            mv.visitVarInsn(ALOAD, 0);
            mv.visitMethodInsn(INVOKESPECIAL, Emitter.JAVA_OBJECT_INTERNAL_NAME, Emitter.JAVA_CTOR_NAME, "()V", false);
        }

        emitStmt(mv, body);

        if (callableSymbol.returnType().isVoid()) {
            mv.visitInsn(RETURN);
        } else {
            emitDefaultValue(mv, callableSymbol.returnType());
            emitReturn(mv, callableSymbol.returnType());
        }

        mv.visitMaxs(0, 0);
        mv.visitEnd();
    }

    private void emitStmt(MethodVisitor mv, BoundStatement stmt) {
        switch (stmt) {
            case BoundBlockStmt b -> {
                for (var s : b.statements()) {
                    emitStmt(mv, s);
                }
            }
            case BoundLabelStmt l ->
                mv.visitLabel(getOrCreateLabel(l.label));
            case BoundGotoStmt g ->
                mv.visitJumpInsn(GOTO, getOrCreateLabel(g.label));
            case BoundConditionalGotoStmt cg -> {
                emitExpr(mv, cg.condition);
                int opcode = cg.inverseCondition ? IFEQ : IFNE;
                mv.visitJumpInsn(opcode, getOrCreateLabel(cg.label));
            }
            case BoundVariableDeclStmt vd -> {
                // Ensure a local slot exists.
                int slot = ensureLocal(vd.variable(), nextLocalSlot());
                if (vd.initializer() != null) {
                    emitExpr(mv, vd.initializer());
                } else {
                    emitDefaultValue(mv, vd.variable().type());
                }
                emitStoreToSlot(mv, vd.variable().type(), slot);
            }
            case BoundExpressionStmt es -> {
                emitExpr(mv, es.expression());
                // Pop expression result if any
                if (!es.expression().type().isVoid()) {
                    popValue(mv, es.expression().type());
                }
            }
            case BoundReturnStmt r -> {
                if (r.expression == null) {
                    mv.visitInsn(RETURN);
                } else {
                    emitExpr(mv, r.expression);
                    emitReturn(mv, callableSymbol.returnType());
                }
            }
            default ->
                throw new IllegalStateException("Unsupported statement in lowered form: " + stmt.kind());
        }
    }

    private void emitAssignment(MethodVisitor mv, BoundAssignmentExpr as) {
        var target = as.targetExpr();
        var init = as.valueExpr();

        if (target instanceof BoundArrayAccessExpr aa) {
            emitExpr(mv, aa.arrayExpr());
            emitExpr(mv, aa.indexExpr());
            emitExpr(mv, init);

            // Stack is [array, index, value]. Duplicate value under array+index so x[i]=v leaves [v].
            dupValueUnderTwo(mv, init.type());
            emitArrayStore(mv, aa.type());
            return;
        }

        if (target instanceof BoundReferenceExpr.MemberAccessRef referenceExpr) {
            emitExpr(mv, referenceExpr.getReceiver());

            if (referenceExpr.getMemberExpr() instanceof BoundReferenceExpr.FieldRef fieldExpr) {
                emitExpr(mv, init);

                // Stack is [instance, value]. Duplicate value under instance so PUTFIELD leaves [value].
                dupValueUnderOne(mv, init.type());

                var field = fieldExpr.getReferencedSymbol();
                int modifier = field.isStatic() ? PUTSTATIC : PUTFIELD;
                mv.visitFieldInsn(modifier, JvmUtils.getOwnerInternalName(field), field.name(), JvmTypeMapper.descriptor(field.type()));
                return;
            }

            throw new IllegalStateException("Unsupported member access reference: " + referenceExpr.getMemberExpr());
        }

        if (target instanceof BoundReferenceExpr.ParameterRef pr) {
            emitExpr(mv, init);

            // Assignments are expressions: leave the assigned value on the stack.
            dupValue(mv, init.type());

            var parameter = pr.getReferencedSymbol();
            var slot = slots.get(parameter);
            if (slot == null) {
                throw new IllegalStateException("Unbound parameter slot for: " + pr.getReferencedSymbol().name());
            }

            emitStoreToSlot(mv, pr.type(), slot);
            return;
        }

        if (target instanceof BoundReferenceExpr.VariableRef vr) {
            emitExpr(mv, init);

            // Assignments are expressions: leave the assigned value on the stack.
            dupValue(mv, init.type());

            var variable = vr.getReferencedSymbol();
            if (!variable.isLocalVariable()) {
                mv.visitFieldInsn(PUTSTATIC, JvmUtils.getOwnerInternalName(variable), variable.name(), JvmTypeMapper.descriptor(variable.type()));
                return;
            }

            int slot = ensureLocal(variable, nextLocalSlot());
            emitStoreToSlot(mv, variable.type(), slot);
            return;
        }

        throw new IllegalStateException("Unsupported assignment target: " + target.kind());
    }

    private void dupValue(MethodVisitor mv, LangType type) {
        if (type.isDouble() || type.isLong()) {
            mv.visitInsn(DUP2);
        } else {
            mv.visitInsn(DUP);
        }
    }

    // Duplicate the top stack value and insert it under one category-1 value.
    // Used for member assignments: [obj, value] -> [value, obj, value]
    private void dupValueUnderOne(MethodVisitor mv, LangType type) {
        if (type.isDouble() || type.isLong()) {
            mv.visitInsn(DUP2_X1);
        } else {
            mv.visitInsn(DUP_X1);
        }
    }

    // Duplicate the top stack value and insert it under two category-1 values.
    // Used for array assignments: [arr, idx, value] -> [value, arr, idx, value]
    private void dupValueUnderTwo(MethodVisitor mv, LangType type) {
        if (type.isDouble() || type.isLong()) {
            mv.visitInsn(DUP2_X2);
        } else {
            mv.visitInsn(DUP_X2);
        }
    }

    private void emitExpr(MethodVisitor mv, BoundExpression expr) {
        switch (expr) {
            case BoundLiteralExpr lit ->
                emitLiteral(mv, lit);
            case BoundUnaryExpr un ->
                emitUnary(mv, un);
            case BoundBinaryExpr bin ->
                emitBinary(mv, bin);
            case BoundMethodCallExpr call ->
                emitCall(mv, call);
            case BoundArrayLiteralExpr al ->
                emitArrayLiteral(mv, al);
            case BoundArrayAccessExpr aa ->
                emitArrayAccess(mv, aa);
            case BoundConstructObjectExpr ctorCall ->
                emitConstructorCallExpr(mv, ctorCall);
            case BoundReferenceExpr pr ->
                emitReferenceExpr(mv, pr);
            case BoundConversionExpr conv ->
                emitConversion(mv, conv);
            case BoundAssignmentExpr as ->
                emitAssignment(mv, as);
            case BoundTypeTestExpr tt ->
                emitTypeTest(mv, tt);
            case BoundDefaultValueExpr def ->
                emitDefaultValue(mv, def.type());
            case BoundSequenceExpr seq ->
                emitSequenceExpr(mv, seq);
            default ->
                throw new IllegalStateException("Unsupported expression: " + expr.kind() + " of type " + expr.getClass().getSimpleName());
        }
    }

    private void emitSequenceExpr(MethodVisitor mv, BoundSequenceExpr seq) {
        var sideEffects = seq.sideEffects();
        for (var sideEffect : sideEffects) {
            emitStmt(mv, sideEffect);
        }

        emitExpr(mv, seq.value());
    }

    private void emitReferenceExpr(MethodVisitor mv, BoundReferenceExpr referenceExpr) {
        switch (referenceExpr) {
            case BoundReferenceExpr.SelfRef selfRef ->
                emitSelfRef(mv, selfRef);
            case BoundReferenceExpr.ParameterRef pr ->
                emitParameterLoad(mv, pr);
            case BoundReferenceExpr.VariableRef vr ->
                emitVariableLoad(mv, vr);
            case BoundReferenceExpr.FieldRef fa ->
                emitFieldAccess(mv, fa);
            case BoundReferenceExpr.MemberAccessRef ma ->
                emitMemberAccess(mv, ma);
            case BoundReferenceExpr.MissingRef missingRef ->
                throw new IllegalStateException("Missing references are not supported in expressions: " + referenceExpr);
            case BoundReferenceExpr.MethodGroupRef mg ->
                throw new IllegalStateException("Method group references are not supported in expressions: " + referenceExpr);
            case BoundReferenceExpr.MethodRef mr ->
                throw new IllegalStateException("Method references are not supported in expressions: " + referenceExpr);
            case BoundReferenceExpr.TypeRef tr ->
                throw new IllegalStateException("Type references are not supported in expressions: " + referenceExpr);
            case BoundReferenceExpr.ConstructorRef cr ->
                throw new IllegalStateException("Constructor references are not supported in expressions: " + referenceExpr);
        }
    }

    private void emitVariableLoad(MethodVisitor mv, BoundReferenceExpr.VariableRef vr) {
        var var = vr.getReferencedSymbol();
        if (var.isLocalVariable()) {
            int slot = ensureLocal(var, nextLocalSlot());
            emitLoadFromSlot(mv, var.type(), slot);
            return;
        }

        mv.visitFieldInsn(GETSTATIC, JvmUtils.getOwnerInternalName(var), var.name(), JvmTypeMapper.descriptor(var.type()));
    }

    private void emitSelfRef(MethodVisitor mv, BoundReferenceExpr.SelfRef selfRef) {
        // "self" is passed as an implicit parameter in instance methods, so it's always in slot 0.
        emitLoadFromSlot(mv, selfRef.getReferencedSymbol().type(), 0);
    }

    private void emitParameterLoad(MethodVisitor mv, BoundReferenceExpr.ParameterRef pr) {
        var parameter = (ParameterSymbol) pr.getReferencedSymbol();
        var slot = slots.get(parameter);
        if (slot == null) {
            throw new IllegalStateException("Unbound parameter slot for: " + pr.getReferencedSymbol().name());
        }

        emitLoadFromSlot(mv, pr.type(), slot);
    }

    private void emitMemberAccess(MethodVisitor mv, BoundReferenceExpr.MemberAccessRef ma) {
        emitExpr(mv, ma.getReceiver());
        emitExpr(mv, ma.getMemberExpr());
    }

    private void emitTypeTest(MethodVisitor mv, BoundTypeTestExpr tt) {
        emitExpr(mv, tt.operand());

        var target = tt.targetType();
        if (target.isAny()) {
            // `x is ANY` is always true (there is no null literal in the language).
            mv.visitInsn(POP);
            mv.visitInsn(ICONST_1);
            return;
        }

        String instanceofType = JvmUtils.getInternalName(tt.targetTypeSymbol());
        mv.visitTypeInsn(INSTANCEOF, instanceofType);
    }

    private void emitConversion(MethodVisitor mv, BoundConversionExpr conv) {
        emitExpr(mv, conv.operand());

        var from = conv.operand().type();
        var to = conv.type();

        if (to.isAny()) {
            emitBoxing(mv, from);
            return;
        }

        if (from.isAny()) {
            emitUnboxing(mv, to);
            return;
        }

        if (from.isNominal() && to.isNominal()) {
            // Might need to lower Foo foo = bar;
            // to -> Foo foo = new Foo(bar.field1, bar.field2, ...);
            var fromNominal = from.as(NominalType.class);
            var toNominal = to.as(NominalType.class);
            var fromUnderlying = fromNominal.unwrap();
            var toUnderlying = toNominal.unwrap();

            if (fromUnderlying.isCollection() && toUnderlying.isCollection()) {
                emitStructurallyEquivalentCollectionConversion(
                        mv,
                        fromNominal, fromUnderlying.as(CollectionType.class),
                        toNominal, toUnderlying.as(CollectionType.class)
                );
                return;
            }
        }

        if (from.isArray() && to.isArray() && to.as(ArrayType.class).getElementType().isAny()) {
            // T[] → ANY[] : no-op on JVM, all arrays are Objects
            return;
        }

        var fromCat = JvmCategory.of(from);
        var toCat = JvmCategory.of(to);

        int opcode = switch (fromCat) {
            case INT_LIKE ->
                switch (toCat) {
                    case INT_LIKE ->
                        NOP;   // no-op (byte/short/char/int are all int-width)
                    case LONG ->
                        I2L;
                    case FLOAT ->
                        I2F;
                    case DOUBLE ->
                        I2D;
                    default ->
                        throw new IllegalStateException("Unsupported conversion from " + from + " to " + to);
                };
            case LONG ->
                switch (toCat) {
                    case FLOAT ->
                        L2F;
                    case DOUBLE ->
                        L2D;
                    default ->
                        throw new IllegalStateException("Unsupported conversion from " + from + " to " + to);
                };
            case FLOAT ->
                switch (toCat) {
                    case DOUBLE ->
                        F2D;
                    default ->
                        throw new IllegalStateException("Unsupported conversion from " + from + " to " + to);
                };
            default ->
                throw new IllegalStateException("Unsupported conversion from " + from + " to " + to);
        };

        if (opcode != -1) {
            mv.visitInsn(opcode);
        }
    }

    private void emitBoxing(MethodVisitor mv, LangType from) {
        switch (JvmCategory.of(from)) {
            case INT_LIKE -> {
                if (from.isBool()) {
                    mv.visitMethodInsn(INVOKESTATIC, "java/lang/Boolean", "valueOf", "(Z)Ljava/lang/Boolean;", false);
                } else {
                    mv.visitMethodInsn(INVOKESTATIC, "java/lang/Integer", "valueOf", "(I)Ljava/lang/Integer;", false);
                }
            }
            case FLOAT ->
                mv.visitMethodInsn(INVOKESTATIC, "java/lang/Float", "valueOf", "(F)Ljava/lang/Float;", false);
            case LONG ->
                mv.visitMethodInsn(INVOKESTATIC, "java/lang/Long", "valueOf", "(J)Ljava/lang/Long;", false);
            case DOUBLE ->
                mv.visitMethodInsn(INVOKESTATIC, "java/lang/Double", "valueOf", "(D)Ljava/lang/Double;", false);
            case REFERENCE -> {
                /* Already a reference type */
            }
        }
    }

    private void emitUnboxing(MethodVisitor mv, LangType to) {
        switch (JvmCategory.of(to)) {
            case INT_LIKE -> {
                if (to.isBool()) {
                    mv.visitTypeInsn(CHECKCAST, "java/lang/Boolean");
                    mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/Boolean", "booleanValue", "()Z", false);
                } else {
                    mv.visitTypeInsn(CHECKCAST, "java/lang/Integer");
                    mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/Integer", "intValue", "()I", false);
                }
            }
            case FLOAT -> {
                mv.visitTypeInsn(CHECKCAST, "java/lang/Float");
                mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/Float", "floatValue", "()F", false);
            }
            case LONG -> {
                mv.visitTypeInsn(CHECKCAST, "java/lang/Long");
                mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/Long", "longValue", "()J", false);
            }
            case DOUBLE -> {
                mv.visitTypeInsn(CHECKCAST, "java/lang/Double");
                mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/Double", "doubleValue", "()D", false);
            }
            case REFERENCE -> {
                if (to.isString()) {
                    mv.visitTypeInsn(CHECKCAST, "java/lang/String");
                } else if (to.isArray()) {
                    mv.visitTypeInsn(CHECKCAST, JvmTypeMapper.descriptor(to));
                } else if (to.isNominal()) {
                    mv.visitTypeInsn(CHECKCAST, JvmTypeMapper.internalName(to.as(NominalType.class)));
                } else {
                    throw new IllegalStateException("Unsupported unboxing from ANY to " + to);
                }
            }
        }
    }

    private void emitStructurallyEquivalentCollectionConversion(MethodVisitor mv, NominalType from, CollectionType fromUnderlying, NominalType to, CollectionType toUnderlying) {
        // This happens when we have a collection of type A being converted to a collection of type B where A and B are structurally compatible but have different nominal types.
        // We need to emit a new instance of the target collection type and copy fields over.
        if (fromUnderlying.orderedFields().size() != toUnderlying.orderedFields().size()) {
            throw new IllegalStateException("Incompatible collection conversion from " + from + " to " + to);
        }

        String fromInternalName = JvmTypeMapper.internalName(from);
        String toInternalName = JvmTypeMapper.internalName(to);

        // Save source object in a temporary local to avoid invalid stack juggling with NEW/uninitialized refs.
        int sourceSlot = nextLocalSlot();
        mv.visitVarInsn(ASTORE, sourceSlot);

        mv.visitTypeInsn(NEW, toInternalName);
        mv.visitInsn(DUP);

        for (var f : fromUnderlying.orderedFields()) {
            var fieldName = f.getKey();
            var fieldType = f.getValue();

            // Load source object, then read field value to feed target constructor argument list.
            mv.visitVarInsn(ALOAD, sourceSlot);
            mv.visitFieldInsn(GETFIELD, fromInternalName, fieldName, JvmTypeMapper.descriptor(fieldType));
        }

        // Call constructor with all field values on stack.
        String ctorDesc = "(" + fromUnderlying.orderedFields().stream().map(e -> JvmTypeMapper.descriptor(e.getValue())).reduce("", String::concat) + ")V";
        mv.visitMethodInsn(INVOKESPECIAL, toInternalName, "<init>", ctorDesc, false);
    }

    private void emitLiteral(MethodVisitor mv, BoundLiteralExpr lit) {
        BoundConstant constant = lit.getConstant();

        switch (constant) {
            case BoundByteConstant b -> {
                mv.visitLdcInsn((int) b.value());
            }
            case BoundShortConstant s -> {
                mv.visitLdcInsn((int) s.value());
            }
            case BoundIntConstant i -> {
                mv.visitLdcInsn(i.value());
            }
            case BoundLongConstant l -> {
                mv.visitLdcInsn(l.value());
            }
            case BoundFloatConstant f -> {
                mv.visitLdcInsn((Float) f.value());
            }
            case BoundDoubleConstant d -> {
                mv.visitLdcInsn(d.value());
            }
            case BoundBoolConstant b -> {
                mv.visitInsn(b.value() ? ICONST_1 : ICONST_0);
            }
            case BoundStringConstant s -> {
                mv.visitLdcInsn(s.value());
            }
            case BoundNullConstant n -> {
                mv.visitInsn(ACONST_NULL);
            }
        }
    }

    private void emitUnary(MethodVisitor mv, BoundUnaryExpr un) {
        switch (un.operator.kind()) {
            case Negation, Increment, Decrement -> {
                emitExpr(mv, un.operand);
                mv.visitInsn(unaryArithmeticOpcode(un.operator.kind(), JvmCategory.of(un.operand.type())));
            }
            case LogicalNot -> {
                emitExpr(mv, un.operand);
                mv.visitInsn(ICONST_1);
                mv.visitInsn(IXOR);
            }
        }
    }

    private void emitBinary(MethodVisitor mv, BoundBinaryExpr bin) {
        switch (bin.operator.kind()) {
            case Addition, Subtraction, Multiplication, Division, Modulo -> {
                int opcode = binaryArithmeticOpcode(bin.operator.kind(), JvmCategory.of(bin.left.type()));
                emitExpr(mv, bin.left);
                emitExpr(mv, bin.right);
                mv.visitInsn(opcode);
            }
            case Equal, NotEqual, LessThan, LessThanOrEqual, GreaterThan, GreaterThanOrEqual -> {
                emitComparison(mv, bin.operator.kind(), bin.left, bin.right);
            }
            case StringConcatenation -> {
                emitStringConcat(mv, bin.left, bin.right);
            }
            case LogicalAnd, LogicalOr -> {
                throw new IllegalStateException("Logical AND/OR should have been desugared into conditional gotos in the lowering phase and should not appear in codegen input: " + bin);
            }
        }
    }

    private static int unaryArithmeticOpcode(UnaryOperatorKind op, JvmCategory category) {
        return switch (op) {
            case Negation ->
                switch (category) {
                    case FLOAT ->
                        FNEG;
                    case DOUBLE ->
                        DNEG;
                    case LONG ->
                        LNEG;
                    case INT_LIKE ->
                        INEG;
                    case REFERENCE ->
                        throw new IllegalStateException("Reference types should be handled differently");
                };
            case Increment, Decrement ->
                throw new IllegalStateException("Increment/decrement should have been desugared into addition/subtraction in the lowering phase and should not appear in codegen input: " + op);
            default ->
                throw new IllegalStateException("Unexpected unary arithmetic operator: " + op);
        };
    }

    private static int binaryArithmeticOpcode(BinaryOperatorKind op, JvmCategory category) {
        return switch (op) {
            case Addition ->
                switch (category) {
                    case FLOAT ->
                        FADD;
                    case DOUBLE ->
                        DADD;
                    case LONG ->
                        LADD;
                    case INT_LIKE ->
                        IADD;
                    case REFERENCE ->
                        throw new IllegalStateException("Reference types should be handled differently");
                };
            case Subtraction ->
                switch (category) {
                    case FLOAT ->
                        FSUB;
                    case DOUBLE ->
                        DSUB;
                    case LONG ->
                        LSUB;
                    case INT_LIKE ->
                        ISUB;
                    case REFERENCE ->
                        throw new IllegalStateException("Reference types should be handled differently");
                };
            case Multiplication ->
                switch (category) {
                    case FLOAT ->
                        FMUL;
                    case DOUBLE ->
                        DMUL;
                    case LONG ->
                        LMUL;
                    case INT_LIKE ->
                        IMUL;
                    case REFERENCE ->
                        throw new IllegalStateException("Reference types should be handled differently");
                };
            case Division ->
                switch (category) {
                    case FLOAT ->
                        FDIV;
                    case DOUBLE ->
                        DDIV;
                    case LONG ->
                        LDIV;
                    case INT_LIKE ->
                        IDIV;
                    case REFERENCE ->
                        throw new IllegalStateException("Reference types should be handled differently");
                };
            case Modulo ->
                switch (category) {
                    case FLOAT ->
                        FREM;
                    case DOUBLE ->
                        DREM;
                    case LONG ->
                        LREM;
                    case INT_LIKE ->
                        IREM;
                    case REFERENCE ->
                        throw new IllegalStateException("Reference types should be handled differently");
                };
            default ->
                throw new IllegalStateException("Unexpected binary arithmetic operator: " + op);
        };
    }

    private void emitStringConcat(MethodVisitor mv, BoundExpression left, BoundExpression right) {
        mv.visitTypeInsn(NEW, "java/lang/StringBuilder");
        mv.visitInsn(DUP);
        mv.visitMethodInsn(INVOKESPECIAL, "java/lang/StringBuilder", "<init>", "()V", false);

        emitExpr(mv, left);
        mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/StringBuilder", "append", "(Ljava/lang/String;)Ljava/lang/StringBuilder;", false);

        emitExpr(mv, right);
        mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/StringBuilder", "append", "(Ljava/lang/String;)Ljava/lang/StringBuilder;", false);

        mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/StringBuilder", "toString", "()Ljava/lang/String;", false);
    }

    private void emitComparison(MethodVisitor mv, BinaryOperatorKind op, BoundExpression left, BoundExpression right) {
        if (op == BinaryOperatorKind.Equal || op == BinaryOperatorKind.NotEqual) {
            emitEqualsOrNot(mv, op == BinaryOperatorKind.NotEqual, left, right);
            return;
        }

        emitExpr(mv, left);
        emitExpr(mv, right);

        var trueLabel = new Label();
        var endLabel = new Label();

        switch (JvmCategory.of(left.type())) {
            case INT_LIKE -> {
                int opcode = switch (op) {
                    case Equal ->
                        IF_ICMPEQ;
                    case NotEqual ->
                        IF_ICMPNE;
                    case LessThan ->
                        IF_ICMPLT;
                    case LessThanOrEqual ->
                        IF_ICMPLE;
                    case GreaterThan ->
                        IF_ICMPGT;
                    case GreaterThanOrEqual ->
                        IF_ICMPGE;
                    default ->
                        throw new IllegalStateException("Unexpected integer comparison operator: " + op);
                };
                mv.visitJumpInsn(opcode, trueLabel);
            }
            case LONG -> {
                mv.visitInsn(LCMP);
                emitCmpBranch(mv, op, trueLabel);
            }
            case FLOAT -> {
                var isLessThan = op == BinaryOperatorKind.LessThan || op == BinaryOperatorKind.LessThanOrEqual;
                int cmpOpcode = isLessThan ? FCMPG : FCMPL;
                mv.visitInsn(cmpOpcode);
                emitCmpBranch(mv, op, trueLabel);
            }
            case DOUBLE -> {
                var isLessThan = op == BinaryOperatorKind.LessThan || op == BinaryOperatorKind.LessThanOrEqual;
                int cmpOpcode = isLessThan ? DCMPG : DCMPL;
                mv.visitInsn(cmpOpcode);
                emitCmpBranch(mv, op, trueLabel);
            }
            case REFERENCE -> {
                throw new IllegalStateException("Reference types should be handled differently for equality/inequality and are not supported for other comparisons: " + left.type());
            }
        }

        // False path
        mv.visitInsn(ICONST_0);
        mv.visitJumpInsn(GOTO, endLabel);

        // True path
        mv.visitLabel(trueLabel);
        mv.visitInsn(ICONST_1);
        mv.visitLabel(endLabel);
    }

    private void emitCmpBranch(MethodVisitor mv, BinaryOperatorKind op, Label trueLabel) {
        int opcode = switch (op) {
            case Equal ->
                IFEQ;
            case NotEqual ->
                IFNE;
            case LessThan ->
                IFLT;
            case LessThanOrEqual ->
                IFLE;
            case GreaterThan ->
                IFGT;
            case GreaterThanOrEqual ->
                IFGE;
            default ->
                throw new IllegalStateException("Not a comparison: " + op);
        };
        mv.visitJumpInsn(opcode, trueLabel);
    }

    private void emitEqualsOrNot(MethodVisitor mv, boolean negate, BoundExpression left, BoundExpression right) {
        emitExpr(mv, left);
        emitExpr(mv, right);

        var trueLabel = new Label();
        var endLabel = new Label();

        switch (JvmCategory.of(left.type())) {
            case REFERENCE -> {
                if (left.type().isString() && right.type().isString()) {
                    mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/String", "equals", "(Ljava/lang/Object;)Z", false);
                    if (negate) {
                        mv.visitInsn(ICONST_1);
                        mv.visitInsn(IXOR);
                    }

                    return;
                }
                mv.visitJumpInsn(negate ? IF_ACMPNE : IF_ACMPEQ, trueLabel);
            }
            case INT_LIKE ->
                mv.visitJumpInsn(IF_ICMPEQ, trueLabel);
            case LONG -> {
                mv.visitInsn(LCMP);
                mv.visitJumpInsn(IFEQ, trueLabel);
            }
            case FLOAT -> {
                mv.visitInsn(FCMPL);
                mv.visitJumpInsn(IFEQ, trueLabel);
            }
            case DOUBLE -> {
                mv.visitInsn(DCMPL);
                mv.visitJumpInsn(IFEQ, trueLabel);
            }
        }

        mv.visitInsn(negate ? ICONST_1 : ICONST_0);
        mv.visitJumpInsn(GOTO, endLabel);
        mv.visitLabel(trueLabel);
        mv.visitInsn(negate ? ICONST_0 : ICONST_1);
        mv.visitLabel(endLabel);
    }

    private void emitCall(MethodVisitor mv, BoundMethodCallExpr call) {
        CallableSymbol methodSymbol = call.methodSymbol;
        if (methodSymbol instanceof IntrinsicMethodSymbol intrinsicMethod) {
            emitIntrinsicCall(mv, intrinsicMethod, call.arguments);
            return;
        }

        if (methodSymbol.isInstanceOnly()) {
            var memberAccessExpr = (BoundReferenceExpr.MemberAccessRef) call.methodRef;
            emitExpr(mv, memberAccessExpr.getReceiver());
        }

        for (int i = 0; i < call.arguments.size(); i++) {
            var arg = call.arguments.get(i);
            emitExpr(mv, arg);
        }

        boolean isInterface = methodSymbol instanceof JvmMethodSymbol jvmMethod
                && jvmMethod.owner().getClazz().isInterface();
        int opcode = isInterface && methodSymbol.isInstanceOnly() ? INVOKEINTERFACE
                : methodSymbol.isInstanceOnly() ? INVOKEVIRTUAL
                : INVOKESTATIC;
        mv.visitMethodInsn(opcode, JvmUtils.getOwnerInternalName(methodSymbol), methodSymbol.name(), JvmTypeMapper.getCallableDescriptor(methodSymbol), isInterface);
    }

    private void emitIntrinsicCall(MethodVisitor mv, IntrinsicMethodSymbol intrinsicMethod, List<BoundExpression> args) {
        switch (intrinsicMethod.intrinsicKind()) {
            case NONE ->
                throw new IllegalStateException("Unexpected non-intrinsic method symbol: " + intrinsicMethod);
            case METHOD_NOT -> {
                emitExpr(mv, args.get(0));
                mv.visitInsn(ICONST_1);
                mv.visitInsn(IXOR);
            }
            case METHOD_STR_0BYTE, METHOD_STR_0SHORT, METHOD_STR_0INT -> {
                emitExpr(mv, args.get(0));
                mv.visitMethodInsn(INVOKESTATIC, "java/lang/String", "valueOf", "(I)Ljava/lang/String;", false);
            }
            case METHOD_STR_0LONG -> {
                emitExpr(mv, args.get(0));
                mv.visitMethodInsn(INVOKESTATIC, "java/lang/String", "valueOf", "(J)Ljava/lang/String;", false);
            }
            case METHOD_STR_0FLOAT -> {
                emitExpr(mv, args.get(0));
                mv.visitMethodInsn(INVOKESTATIC, "java/lang/String", "valueOf", "(F)Ljava/lang/String;", false);
            }
            case METHOD_STR_0DOUBLE -> {
                emitExpr(mv, args.get(0));
                mv.visitMethodInsn(INVOKESTATIC, "java/lang/String", "valueOf", "(D)Ljava/lang/String;", false);
            }
            case METHOD_STR_0BOOL -> {
                emitExpr(mv, args.get(0));
                mv.visitMethodInsn(INVOKESTATIC, "java/lang/String", "valueOf", "(Z)Ljava/lang/String;", false);
            }
            case METHOD_STR_0CHAR -> {
                emitExpr(mv, args.get(0));
                mv.visitMethodInsn(INVOKESTATIC, "java/lang/String", "valueOf", "(C)Ljava/lang/String;", false);
            }
            case METHOD_STR_0STRING -> {
                emitExpr(mv, args.get(0));
                mv.visitMethodInsn(INVOKESTATIC, "java/lang/String", "valueOf", "(Ljava/lang/Object;)Ljava/lang/String;", false);
            }
            case METHOD_FLOOR -> {
                emitExpr(mv, args.get(0));
                if (args.get(0).type().isFloat()) {
                    mv.visitInsn(F2D);
                }
                mv.visitMethodInsn(INVOKESTATIC, "java/lang/Math", "floor", "(D)D", false);
                mv.visitInsn(D2I);
            }
            case METHOD_CEIL -> {
                emitExpr(mv, args.get(0));
                if (args.get(0).type().isFloat()) {
                    mv.visitInsn(F2D);
                }
                mv.visitMethodInsn(INVOKESTATIC, "java/lang/Math", "ceil", "(D)D", false);
                mv.visitInsn(D2I);
            }
            case METHOD_LENGTH_0STRING -> {
                var arg = args.get(0);
                emitExpr(mv, arg);
                mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/String", "length", "()I", false);
            }
            case METHOD_LENGTH_0ARRAY -> {
                var arg = args.get(0);
                emitExpr(mv, arg);
                mv.visitMethodInsn(INVOKESTATIC, "java/lang/reflect/Array", "getLength", "(Ljava/lang/Object;)I", false);
            }
        }
    }

    private void emitArrayLiteral(MethodVisitor mv, BoundArrayLiteralExpr al) {
        emitExpr(mv, al.lengthExpr);

        var elementType = al.elementType;
        switch (elementType.kind()) {
            case Primitive -> {
                switch (elementType.getPrimitiveKind()) {
                    case Byte ->
                        mv.visitIntInsn(NEWARRAY, T_BYTE);
                    case Short ->
                        mv.visitIntInsn(NEWARRAY, T_SHORT);
                    case Int ->
                        mv.visitIntInsn(NEWARRAY, T_INT);
                    case Long ->
                        mv.visitIntInsn(NEWARRAY, T_LONG);
                    case Float ->
                        mv.visitIntInsn(NEWARRAY, T_FLOAT);
                    case Double ->
                        mv.visitIntInsn(NEWARRAY, T_DOUBLE);
                    case Char ->
                        mv.visitIntInsn(NEWARRAY, T_CHAR);
                    case Bool ->
                        mv.visitIntInsn(NEWARRAY, T_BOOLEAN);
                    case String ->
                        mv.visitTypeInsn(ANEWARRAY, "java/lang/String");
                    case None ->
                        throw new IllegalStateException("Array of NONE type is not supported");
                }
            }
            case ARRAY ->
                mv.visitTypeInsn(ANEWARRAY, JvmTypeMapper.descriptor(elementType));
            case NOMINAL ->
                mv.visitTypeInsn(ANEWARRAY, JvmTypeMapper.internalName(elementType.as(NominalType.class)));
            case COLLECTION, VOID, UNKNOWN, FUNCTION, OPAQUE, ANY ->
                throw new IllegalStateException("Invalid array element type: " + elementType);
        }
    }

    private void emitArrayAccess(MethodVisitor mv, BoundArrayAccessExpr aa) {
        emitExpr(mv, aa.arrayExpr());
        emitExpr(mv, aa.indexExpr());

        if (aa.arrayExpr().type().isString()) {
            mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/String", "charAt", "(I)C", false);
            // char -> int
            return;
        }

        emitArrayLoad(mv, aa.type());
    }

    private void emitConstructorCallExpr(MethodVisitor mv, BoundConstructObjectExpr ctor) {
        String internalName = JvmUtils.getInternalName(ctor.getConstructedType());

        mv.visitTypeInsn(NEW, internalName);
        mv.visitInsn(DUP);

        var arguments = ctor.getArguments();
        for (int i = 0; i < arguments.size(); i++) {
            var arg = arguments.get(i);
            emitExpr(mv, arg);
        }

        mv.visitMethodInsn(INVOKESPECIAL, internalName, Emitter.JAVA_CTOR_NAME, JvmTypeMapper.getCallableDescriptor(ctor.getConstructor()), false);
    }

    private void emitFieldAccess(MethodVisitor mv, BoundReferenceExpr.FieldRef fa) {
        var field = fa.getReferencedSymbol();
        String owner = JvmUtils.getOwnerInternalName(field);

        int modifiers = field.isStatic() ? GETSTATIC : GETFIELD;
        mv.visitFieldInsn(modifiers, owner, field.name(), JvmTypeMapper.descriptor(fa.type()));
    }

    private void emitDefaultValue(MethodVisitor mv, LangType type) {
        switch (JvmCategory.of(type)) {
            case INT_LIKE -> {
                mv.visitInsn(ICONST_0);
            }
            case LONG -> {
                mv.visitLdcInsn(0L);
            }
            case FLOAT -> {
                mv.visitLdcInsn(0.0f);
            }
            case DOUBLE -> {
                mv.visitLdcInsn(0.0);
            }
            case REFERENCE -> {
                mv.visitInsn(ACONST_NULL);
            }
        }
    }

    private void emitReturn(MethodVisitor mv, LangType type) {
        if (type.isVoid()) {
            mv.visitInsn(RETURN);
            return;
        }

        switch (JvmCategory.of(type)) {
            case INT_LIKE -> {
                mv.visitInsn(IRETURN);
            }
            case LONG -> {
                mv.visitInsn(LRETURN);
            }
            case FLOAT -> {
                mv.visitInsn(FRETURN);
            }
            case DOUBLE -> {
                mv.visitInsn(DRETURN);
            }
            case REFERENCE -> {
                mv.visitInsn(ARETURN);
            }
        }
    }

    private void popValue(MethodVisitor mv, LangType type) {
        if (type.isVoid()) {
            return;
        }

        switch (JvmCategory.of(type).size()) {
            case 1 ->
                mv.visitInsn(POP);
            case 2 ->
                mv.visitInsn(POP2);
            default ->
                throw new IllegalStateException("Invalid JVM category size for type: " + type.displayName());
        }
    }

    private void emitLoadFromSlot(MethodVisitor mv, LangType type, int slot) {
        switch (JvmCategory.of(type)) {
            case INT_LIKE -> {
                mv.visitVarInsn(ILOAD, slot);
            }
            case LONG -> {
                mv.visitVarInsn(LLOAD, slot);
            }
            case FLOAT -> {
                mv.visitVarInsn(FLOAD, slot);
            }
            case DOUBLE -> {
                mv.visitVarInsn(DLOAD, slot);
            }
            case REFERENCE -> {
                mv.visitVarInsn(ALOAD, slot);
            }
        }
    }

    private void emitStoreToSlot(MethodVisitor mv, LangType type, int slot) {
        switch (JvmCategory.of(type)) {
            case INT_LIKE -> {
                mv.visitVarInsn(ISTORE, slot);
            }
            case LONG -> {
                mv.visitVarInsn(LSTORE, slot);
            }
            case FLOAT -> {
                mv.visitVarInsn(FSTORE, slot);
            }
            case DOUBLE -> {
                mv.visitVarInsn(DSTORE, slot);
            }
            case REFERENCE -> {
                mv.visitVarInsn(ASTORE, slot);
            }
        }
    }

    private void emitArrayLoad(MethodVisitor mv, LangType elementType) {
        switch (JvmCategory.of(elementType)) {
            case INT_LIKE -> {
                mv.visitInsn(IALOAD);
            }
            case FLOAT -> {
                mv.visitInsn(FALOAD);
            }
            case LONG -> {
                mv.visitInsn(LALOAD);
            }
            case DOUBLE -> {
                mv.visitInsn(DALOAD);
            }
            case REFERENCE -> {
                mv.visitInsn(AALOAD);
            }
        }
    }

    private void emitArrayStore(MethodVisitor mv, LangType elementType) {
        switch (JvmCategory.of(elementType)) {
            case LONG -> {
                mv.visitInsn(LASTORE);
            }
            case DOUBLE -> {
                mv.visitInsn(DASTORE);
            }
            case INT_LIKE -> {
                mv.visitInsn(IASTORE);
            }
            case FLOAT -> {
                mv.visitInsn(FASTORE);
            }
            case REFERENCE -> {
                mv.visitInsn(AASTORE);
            }
        }
    }
}
