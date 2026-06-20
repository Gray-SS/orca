package com.orca.compiler.core.semantics;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.orca.compiler.core.CompilerException;
import com.orca.compiler.core.boundtree.BoundExpression;
import com.orca.compiler.core.boundtree.BoundMethod;
import com.orca.compiler.core.boundtree.BoundStatement;
import com.orca.compiler.core.boundtree.BoundWalker;
import com.orca.compiler.core.boundtree.expressions.BoundAssignmentExpr;
import com.orca.compiler.core.boundtree.expressions.BoundReferenceExpr;
import com.orca.compiler.core.boundtree.statements.BoundBlockStmt;
import com.orca.compiler.core.boundtree.statements.BoundConditionalGotoStmt;
import com.orca.compiler.core.boundtree.statements.BoundExpressionStmt;
import com.orca.compiler.core.boundtree.statements.BoundGotoStmt;
import com.orca.compiler.core.boundtree.statements.BoundLabelStmt;
import com.orca.compiler.core.boundtree.statements.BoundReturnStmt;
import com.orca.compiler.core.boundtree.statements.BoundVariableDeclStmt;
import com.orca.compiler.core.controlflow.BasicBlock;
import com.orca.compiler.core.controlflow.BasicBlockBranch;
import com.orca.compiler.core.controlflow.ControlFlowGraph;
import com.orca.compiler.core.diagnostics.DiagnosticFactory;
import com.orca.compiler.core.symbols.ParameterSymbol;
import com.orca.compiler.core.symbols.ValueSymbol;

public final class DefiniteAssignment {

    public static void check(BoundMethod function) throws CompilerException {
        check(function.getBody(), function.getSymbol().parameters());
    }

    public static void check(BoundBlockStmt body, List<? extends ParameterSymbol> parameters) throws CompilerException {
        var graph = ControlFlowGraph.create(body);

        var values = new ArrayList<ValueSymbol>();
        for (var p : parameters) {
            values.add(p);
        }

        for (var block : graph.blocks()) {
            for (var stmt : block.statements()) {
                if (stmt instanceof BoundVariableDeclStmt decl) {
                    values.add(decl.variable());
                }
            }
        }

        // Deduplicate and index
        Map<ValueSymbol, Integer> index = new HashMap<>();
        int n = 0;
        for (var v : values) {
            if (!index.containsKey(v)) {
                index.put(v, n++);
            }
        }

        BitSet entryAssigned = new BitSet(n);
        for (var e : index.entrySet()) {
            var variable = e.getKey();
            int idx = e.getValue();

            if (variable.isParameter() || variable.isCompileTimeConstant()) {
                entryAssigned.set(idx);
            }
        }

        List<BitSet> in = new ArrayList<>();
        List<BitSet> out = new ArrayList<>();
        for (BasicBlock block : graph.blocks()) {
            BitSet topIn = new BitSet(n);
            topIn.set(0, n);
            in.add(topIn);

            BitSet topOut = new BitSet(n);
            topOut.set(0, n);
            out.add(topOut);
        }

        boolean changed = true;
        while (changed) {
            changed = false;

            List<BasicBlock> blocks = graph.blocks();
            for (int bi = 0; bi < blocks.size(); bi++) {
                BasicBlock b = blocks.get(bi);

                // compute in[bi]
                BitSet inSet = new BitSet(n);
                var incoming = b.incomingBranches();
                if (incoming.isEmpty()) {
                    inSet.or(entryAssigned);
                } else {
                    boolean first = true;
                    for (BasicBlockBranch br : incoming) {
                        int predIndex = blocks.indexOf(br.from());
                        if (predIndex < 0) {
                            continue;
                        }
                        if (first) {
                            inSet.or(out.get(predIndex));
                            first = false;
                        } else {
                            inSet.and(out.get(predIndex));
                        }
                    }
                    if (first) {
                        inSet.or(entryAssigned);
                    }
                }

                if (!in.get(bi).equals(inSet)) {
                    in.set(bi, (BitSet) inSet.clone());
                    changed = true;
                }

                // Simulate statements in order starting from inSet to compute out
                BitSet current = (BitSet) inSet.clone();

                for (var stmt : b.statements()) {
                    simulateStatement(stmt, current, index, false);
                }

                BitSet outSet = current;
                if (!out.get(bi).equals(outSet)) {
                    out.set(bi, (BitSet) outSet.clone());
                    changed = true;
                }
            }
        }

        // Validate variable uses once dataflow has converged.
        List<BasicBlock> blocks = graph.blocks();
        for (int bi = 0; bi < blocks.size(); bi++) {
            BitSet current = (BitSet) in.get(bi).clone();
            for (var stmt : blocks.get(bi).statements()) {
                simulateStatement(stmt, current, index, true);
            }
        }
    }

    private static void simulateStatement(
            BoundStatement stmt,
            BitSet current,
            Map<ValueSymbol, Integer> index,
            boolean validateUses
    ) throws CompilerException {
        if (stmt instanceof BoundExpressionStmt es) {
            // Always propagate assignments so the fixed-point is accurate.
            if (es.expression() instanceof BoundAssignmentExpr as && as.targetExpr() instanceof BoundReferenceExpr.VariableRef vref) {
                Integer idx = index.get(vref.getReferencedSymbol());
                if (idx != null) {
                    current.set(idx);
                }
            }
            if (validateUses) {
                checkExpression(es.expression(), current, index);
            }
        } else if (stmt instanceof BoundReturnStmt rs) {
            if (validateUses && rs.expression != null) {
                checkExpression(rs.expression, current, index);
            }
        } else if (stmt instanceof BoundConditionalGotoStmt cgs) {
            if (validateUses && cgs.condition != null) {
                checkExpression(cgs.condition, current, index);
            }
        } else if (stmt instanceof BoundVariableDeclStmt decl) {
            if (decl.initializer() != null) {
                if (validateUses) {
                    checkExpression(decl.initializer(), current, index);
                }
                Integer idx = index.get(decl.variable());
                if (idx != null) {
                    current.set(idx);
                }
            }
        } else if (stmt instanceof BoundLabelStmt || stmt instanceof BoundGotoStmt) {
            // nothing to check
        }
    }

    private static void checkExpression(BoundExpression expr, BitSet assigned, Map<ValueSymbol, Integer> index) throws CompilerException {
        if (expr == null) {
            return;
        }

        if (expr instanceof BoundAssignmentExpr as) {
            if (as.valueExpr() != null) {
                checkExpression(as.valueExpr(), assigned, index);
            }
            if (as.targetExpr() instanceof BoundReferenceExpr.VariableRef vref) {
                Integer idx = index.get(vref.getReferencedSymbol());
                if (idx != null) {
                    assigned.set(idx);
                }
            }
        }

        // Quick check for direct variable ref
        if (expr instanceof BoundReferenceExpr.VariableRef vref) {
            var var = vref.getReferencedSymbol();
            if (var.isGlobalVariable()) {
                return;
            }
            Integer idx = index.get(var);
            if (idx == null) {
                return;
            }
            if (!assigned.get(idx)) {
                throw CompilerException.wrap(DiagnosticFactory.uninitializedVariable(expr.span(), var.name()));
            }
            return;
        }

        // Use a small walker to catch any nested variable refs and report errors with CompilerException
        List<ValueSymbol> uninitializedVariables = UnitializedVariableCollector.collect(expr, assigned, index);
        if (!uninitializedVariables.isEmpty()) {
            var first = uninitializedVariables.get(0);
            throw CompilerException.wrap(DiagnosticFactory.uninitializedVariable(expr.span(), first.name()));
        }
    }

    private static final class UnitializedVariableCollector extends BoundWalker {

        private final BitSet assigned;
        private final Map<ValueSymbol, Integer> index;
        private final List<ValueSymbol> unitializedVariables = new ArrayList<>();

        private UnitializedVariableCollector(BitSet assigned, Map<ValueSymbol, Integer> index) {
            this.assigned = assigned;
            this.index = index;
        }

        public static List<ValueSymbol> collect(BoundExpression expr, BitSet assigned, Map<ValueSymbol, Integer> index) {
            var collector = new UnitializedVariableCollector(assigned, index);
            expr.accept(collector);

            return collector.unitializedVariables;
        }

        @Override
        public Void visitReferenceExpr(BoundReferenceExpr node) {
            if (!(node instanceof BoundReferenceExpr.VariableRef vref)) {
                return null;
            }

            var var = vref.getReferencedSymbol();
            if (var.isGlobalVariable()) {
                return null;
            }

            Integer idx = index.get(var);
            if (idx == null) {
                return null;
            }

            if (!assigned.get(idx)) {
                unitializedVariables.add(var);
            }

            return null;
        }
    }
}
