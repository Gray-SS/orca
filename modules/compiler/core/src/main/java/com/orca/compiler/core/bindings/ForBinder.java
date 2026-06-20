package com.orca.compiler.core.bindings;

import java.util.ArrayList;
import java.util.List;

import com.orca.compiler.core.CompilerException;
import com.orca.compiler.core.boundtree.BoundNode;
import com.orca.compiler.core.boundtree.expressions.BoundAssignmentExpr;
import com.orca.compiler.core.boundtree.statements.BoundBlockStmt;
import com.orca.compiler.core.boundtree.statements.BoundForStmt;
import com.orca.compiler.core.semantics.SemanticErrors;
import com.orca.compiler.core.symbols.Symbol;
import com.orca.compiler.core.symbols.ValueSymbol;
import com.orca.compiler.core.symbols.sources.SourceVariableSymbol;
import com.orca.compiler.core.syntax.SyntaxNode;
import com.orca.compiler.core.syntax.statements.ForStmt;

public final class ForBinder extends LocalBinder {

    private final ForStmt syntax;
    private ValueSymbol loopVariable;

    public ForBinder(Binder parent, ForStmt syntax) {
        super(parent);
        this.syntax = syntax;
    }

    @Override
    public Symbol lookupSymbol(String name) {
        // First check if the name matches the loop variable of this for statement
        if (loopVariable != null && loopVariable.name().equals(name)) {
            return loopVariable;
        }

        // If not, delegate to the parent binder
        return super.lookupSymbol(name);
    }

    @Override
    public List<Symbol> lookupSymbols(String name) {
        var symbols = new ArrayList<Symbol>();
        symbols.addAll(super.lookupSymbols(name));

        if (loopVariable != null && loopVariable.name().equals(name)) {
            symbols.add(loopVariable);
        }

        return symbols;
    }

    @Override
    public BoundNode bind(SyntaxNode syntax) throws CompilerException {
        if (syntax instanceof ForStmt forStmt) {
            return bind(forStmt);
        }

        return super.bind(syntax);
    }

    public BoundForStmt bind(ForStmt stmt) throws CompilerException {
        var loopVariableDeclarator = bindVariableDeclarator(stmt.variableDeclarator(), true);
        if (!(loopVariableDeclarator.variable() instanceof SourceVariableSymbol variable)) {
            throw SemanticErrors.unexpectedError(stmt.variableDeclarator(), "Expected a SourceVariableSymbol for a loop variable declarator");
        }

        declareLocal(variable);
        loopVariable = variable;

        var boundConditionExpr = bindConditionExpr(stmt, stmt.conditionExpr());

        // Step must be compatible with loopVarType (e.g. i++).
        var boundStepExpr = bindExpectedExpr(syntax.stepExpr(), loopVariable.type());
        if (!(boundStepExpr instanceof BoundAssignmentExpr)) {
            throw SemanticErrors.forLoopStepMustBeAssignment(syntax.stepExpr());
        }

        BoundBlockStmt boundBlock = BlockBinder.bindBlock(this, stmt.body());

        return new BoundForStmt(loopVariableDeclarator, boundConditionExpr, boundStepExpr, boundBlock);
    }
}
