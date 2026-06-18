package com.orca.compiler.core.bindings;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.orca.compiler.core.CompilerException;
import com.orca.compiler.core.Debug;
import com.orca.compiler.core.boundtree.BoundExpression;
import com.orca.compiler.core.boundtree.BoundIfClause;
import com.orca.compiler.core.boundtree.BoundStatement;
import com.orca.compiler.core.boundtree.statements.BoundBlockStmt;
import com.orca.compiler.core.boundtree.statements.BoundExpressionStmt;
import com.orca.compiler.core.boundtree.statements.BoundForStmt;
import com.orca.compiler.core.boundtree.statements.BoundIfStmt;
import com.orca.compiler.core.boundtree.statements.BoundReturnStmt;
import com.orca.compiler.core.boundtree.statements.BoundVariableDeclStmt;
import com.orca.compiler.core.boundtree.statements.BoundWhileStmt;
import com.orca.compiler.core.semantics.SemanticErrors;
import com.orca.compiler.core.symbols.CallableSymbol;
import com.orca.compiler.core.symbols.Symbol;
import com.orca.compiler.core.symbols.sources.SourceVariableSymbol;
import com.orca.compiler.core.syntax.StatementSyntax;
import com.orca.compiler.core.syntax.SyntaxNode;
import com.orca.compiler.core.syntax.SyntaxToken;
import com.orca.compiler.core.syntax.nodes.IfClauseSyntax;
import com.orca.compiler.core.syntax.statements.BlockStmt;
import com.orca.compiler.core.syntax.statements.ExpressionStmt;
import com.orca.compiler.core.syntax.statements.ForStmt;
import com.orca.compiler.core.syntax.statements.IfStmt;
import com.orca.compiler.core.syntax.statements.ReturnStmt;
import com.orca.compiler.core.syntax.statements.VariableDeclarationStatement;
import com.orca.compiler.core.syntax.statements.WhileStmt;
import com.orca.compiler.core.typesystem.LangType;

public abstract class LocalBinder extends Binder {

    private final CallableSymbol currentFunction;
    private final Map<String, SourceVariableSymbol> localSymbols = new HashMap<>();

    public LocalBinder(Binder parent) {
        super(parent);
        this.currentFunction = retrieveCurrentFunctionFromParent();
    }

    public List<SourceVariableSymbol> locals() {
        return new ArrayList<>(localSymbols.values());
    }

    public CallableSymbol getCurrentFunction() {
        return currentFunction;
    }

    private CallableSymbol retrieveCurrentFunctionFromParent() {
        Binder currentBinder = this;
        while (currentBinder != null) {
            if (currentBinder instanceof FunctionBodyBinder functionBodyBinder) {
                return functionBodyBinder.getCallableSymbol();
            }

            currentBinder = currentBinder.getParent();
        }

        Debug.shouldNotReachHere(getClass().getSimpleName() + " should always be within a FunctionBodyBinder or a nested BlockBinder");
        return null;
    }

    @Override
    public Symbol lookupSymbol(String name) {
        Debug.requireNotNullOrEmpty(name, "Symbol name cannot be null or empty");

        var symbol = localSymbols.get(name);
        if (symbol != null) {
            return symbol;
        }

        // If not found in local symbols, delegate to parent binder
        return super.lookupSymbol(name);
    }

    @Override
    public List<Symbol> lookupSymbols(String name) {
        Debug.requireNotNullOrEmpty(name, "Symbol name cannot be null or empty");

        var symbols = super.lookupSymbols(name);

        var localSymbol = localSymbols.get(name);
        if (localSymbol != null) {
            symbols.add(localSymbol);
        }

        return symbols;
    }

    @Override
    protected BoundStatement bindStatementInternal(StatementSyntax stmt) throws CompilerException {
        return switch (stmt) {
            case VariableDeclarationStatement d ->
                bindLocalVariableDecl(d);
            case BlockStmt s ->
                bindBlock(s);
            case ExpressionStmt s ->
                bindExpressionStmt(s);
            case IfStmt s ->
                bindIfStmt(s);
            case WhileStmt s ->
                bindWhileStmt(s);
            case ForStmt s ->
                bindForStmt(s);
            case ReturnStmt s ->
                bindReturnStmt(s);

            default ->
                throw SemanticErrors.unexpectedError(stmt, "Unsupported statement node: " + stmt);
        };
    }

    private BoundVariableDeclStmt bindLocalVariableDecl(VariableDeclarationStatement decl) throws CompilerException {
        var declarator = decl.variableDeclarator();
        var boundDeclarator = bindVariableDeclarator(declarator, true);
        if (!(boundDeclarator.variable() instanceof SourceVariableSymbol variable)) {
            throw SemanticErrors.unexpectedError(declarator, "Expected a SourceVariableSymbol for a local variable declarator");
        }

        declareLocal(variable);
        return new BoundVariableDeclStmt(boundDeclarator);
    }

    private BoundBlockStmt bindBlock(BlockStmt block) throws CompilerException {
        var blockBinder = getBinderFor(block);
        var boundStatements = new ArrayList<BoundStatement>();
        for (var stmt : block.statements()) {
            boundStatements.add(blockBinder.bindStatement(stmt));
        }

        return new BoundBlockStmt(boundStatements);
    }

    private BoundExpressionStmt bindExpressionStmt(ExpressionStmt stmt) throws CompilerException {
        // Expression statements can evaluate to VOID (e.g., println(...)).
        var boundExpression = bindExpr(stmt.expression());
        return new BoundExpressionStmt(boundExpression);
    }

    private BoundIfStmt bindIfStmt(IfStmt stmt) throws CompilerException {
        BoundIfClause ifClause = bindIfClause(stmt.ifClause());
        List<BoundIfClause> elseIfClauses = new ArrayList<>();
        for (var clause : stmt.elseIfClauses()) {
            elseIfClauses.add(bindIfClause(clause));
        }
        BoundStatement elseClause = null;
        if (stmt.elseClause() != null) {
            elseClause = bindBlock(stmt.elseClause().body());
        }

        return new BoundIfStmt(ifClause, elseIfClauses, elseClause);
    }

    private BoundIfClause bindIfClause(IfClauseSyntax clause) throws CompilerException {
        var boundCondition = bindConditionExpr(clause, clause.condition());
        BoundBlockStmt boundBlock = bindBlock(clause.body());
        return new BoundIfClause(boundCondition, boundBlock);
    }

    private BoundWhileStmt bindWhileStmt(WhileStmt stmt) throws CompilerException {
        var boundCondition = bindConditionExpr(stmt, stmt.condition());
        BoundBlockStmt boundBlock = bindBlock(stmt.body());
        return new BoundWhileStmt(boundCondition, boundBlock);
    }

    private BoundForStmt bindForStmt(ForStmt stmt) throws CompilerException {
        var binder = (ForBinder) getBinderFor(stmt);
        return binder.bind(stmt);
    }

    private BoundReturnStmt bindReturnStmt(ReturnStmt stmt) throws CompilerException {
        if (currentFunction == null) {
            throw SemanticErrors.unexpectedError(stmt.returnToken(), "Return statement not within a function");
        }

        boolean mustReturnValue = !currentFunction.returnType().isVoid();
        boolean hasReturnValue = stmt.expression() != null;

        if (!mustReturnValue && hasReturnValue) {
            throw SemanticErrors.returnValueInVoidFunction(stmt.expression(), currentFunction);
        }

        if (mustReturnValue && !hasReturnValue) {
            throw SemanticErrors.missingReturnValue(stmt.returnToken(), currentFunction);
        }

        BoundExpression boundExpr = null;
        if (hasReturnValue) {
            boundExpr = bindExpectedExpr(stmt.expression(), currentFunction.returnType());
        }

        return new BoundReturnStmt(boundExpr);
    }

    protected SourceVariableSymbol declareLocal(SourceVariableSymbol variable) throws CompilerException {
        var name = variable.name();
        var existing = localSymbols.get(name);
        if (existing != null) {
            throw SemanticErrors.symbolRedeclared(existing, variable);
        }

        localSymbols.put(variable.name(), variable);
        return variable;
    }

    protected SourceVariableSymbol declareLocal(SyntaxToken nameToken, SyntaxNode declaringSyntax, LangType type, boolean isImmutable) throws CompilerException {
        return declareLocal(SourceVariableSymbol.createLocal(nameToken.text(), declaringSyntax, type, isImmutable));
    }
}
