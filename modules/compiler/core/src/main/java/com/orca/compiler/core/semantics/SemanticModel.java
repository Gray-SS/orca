package com.orca.compiler.core.semantics;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import com.google.common.base.Preconditions;
import com.orca.compiler.core.Compilation;
import com.orca.compiler.core.CompilationResult;
import com.orca.compiler.core.CompilerException;
import com.orca.compiler.core.Debug;
import com.orca.compiler.core.bindings.Binder;
import com.orca.compiler.core.bindings.BlockBinder;
import com.orca.compiler.core.bindings.ForBinder;
import com.orca.compiler.core.bindings.FunctionBodyBinder;
import com.orca.compiler.core.bindings.GlobalBinder;
import com.orca.compiler.core.bindings.MemberBinder;
import com.orca.compiler.core.bindings.NamespaceBinder;
import com.orca.compiler.core.boundtree.BoundExpression;
import com.orca.compiler.core.boundtree.BoundMethod;
import com.orca.compiler.core.boundtree.BoundNamespace;
import com.orca.compiler.core.boundtree.BoundNode;
import com.orca.compiler.core.boundtree.BoundNodeForm;
import com.orca.compiler.core.boundtree.BoundType;
import com.orca.compiler.core.boundtree.BoundVariable;
import com.orca.compiler.core.boundtree.statements.BoundBlockStmt;
import com.orca.compiler.core.controlflow.ControlFlowGraph;
import com.orca.compiler.core.diagnostics.DiagnosticBag;
import com.orca.compiler.core.diagnostics.DiagnosticCollector;
import com.orca.compiler.core.diagnostics.DiagnosticFactory;
import com.orca.compiler.core.symbols.CallableSymbol;
import com.orca.compiler.core.symbols.NamespaceOrTypeSymbol;
import com.orca.compiler.core.symbols.NamespaceSymbol;
import com.orca.compiler.core.symbols.Symbol;
import com.orca.compiler.core.symbols.TypeSymbol;
import com.orca.compiler.core.symbols.sources.SourceFieldSymbol;
import com.orca.compiler.core.symbols.sources.SourceMethodSymbol;
import com.orca.compiler.core.symbols.sources.SourceNominalTypeSymbol;
import com.orca.compiler.core.symbols.sources.SourceSymbol;
import com.orca.compiler.core.symbols.sources.SourceVariableSymbol;
import com.orca.compiler.core.syntax.CompilationUnit;
import com.orca.compiler.core.syntax.SyntaxNode;
import com.orca.compiler.core.syntax.SyntaxTree;
import com.orca.compiler.core.syntax.members.CollectionDeclarationSyntax;
import com.orca.compiler.core.syntax.members.ImplBlockSyntax;
import com.orca.compiler.core.syntax.members.MethodDeclarationSyntax;
import com.orca.compiler.core.syntax.members.VariableDeclarationSyntax;
import com.orca.compiler.core.syntax.nodes.IdentifierSyntax;
import com.orca.compiler.core.syntax.nodes.VariableDeclaratorSyntax;
import com.orca.compiler.core.syntax.statements.BlockStmt;
import com.orca.compiler.core.syntax.statements.ForStmt;
import com.orca.compiler.core.text.SourceLocation;
import com.orca.compiler.core.text.TextSource;
import com.orca.compiler.core.typesystem.LangType;

public final class SemanticModel {

    final Map<SyntaxNode, Symbol> symbolCache = new java.util.HashMap<>();

    private final Map<SyntaxNode, Binder> bindersCache = new java.util.HashMap<>();
    private final Map<SyntaxNode, BoundNode> boundNodesCache = new java.util.HashMap<>();

    private final SymbolDeclarator symbolDeclarator = new SymbolDeclarator(this);

    private final Compilation compilation;
    private final TextSource source;

    GlobalBinder globalBinder;

    private boolean fullSemanticModelBound = false;

    private SyntaxTree cachedSyntaxTree;
    private CompilationResult<BoundNamespace> cachedBoundSemanticModelResult;

    public SemanticModel(Compilation compilation, TextSource source) {
        Preconditions.checkNotNull(compilation, "Compilation cannot be null");
        Preconditions.checkNotNull(source, "TextSource cannot be null");

        this.source = source;
        this.compilation = compilation;
    }

    public SyntaxTree getSyntaxTree() {
        if (cachedSyntaxTree == null) {
            cachedSyntaxTree = SyntaxTree.parse(source);
        }

        return cachedSyntaxTree;
    }

    public TextSource getSource() {
        return source;
    }

    public Compilation getCompilation() {
        return compilation;
    }

    public DiagnosticBag getDiagnostics() {
        var boundDiagnostics = bindFullSemanticModel().diagnostics();
        var syntaxTree = getSyntaxTree();
        return DiagnosticBag.merge(syntaxTree.getDiagnostics(), boundDiagnostics);
    }

    public CompilationUnit getCompilationUnit() {
        var root = getSyntaxTree().root();
        if (root instanceof CompilationUnit unit) {
            return unit;
        }

        throw new IllegalStateException("Root syntax node of the syntax tree should always be a CompilationUnitSyntax");
    }

    public Optional<Symbol> getDeclaredSymbol(SyntaxNode syntax) {
        Preconditions.checkArgument(syntax != null, "Syntax node cannot be null");
        return Optional.ofNullable(symbolCache.get(syntax));
    }

    public Symbol getDeclaredSymbolRequired(SyntaxNode syntax) {
        Preconditions.checkArgument(syntax != null, "Syntax node cannot be null");

        var symbol = symbolCache.get(syntax);
        Preconditions.checkState(symbol != null, "No symbol is associated with the given syntax node. Ensure that the symbol has been declared and cached before calling getDeclaredSymbol().");

        return symbol;
    }

    public boolean isNodeCached(SyntaxNode syntax) {
        return boundNodesCache.containsKey(syntax);
    }

    public BoundNode getCachedBoundNode(SyntaxNode syntax) {
        var cachedNode = boundNodesCache.get(syntax);
        Preconditions.checkState(cachedNode != null, "Bound node for the given syntax node is not cached. Use isNodeCached() to check before calling getCachedBoundNode().");

        return cachedNode;
    }

    public void cacheBoundNode(SyntaxNode syntax, BoundNode boundNode) {
        Preconditions.checkArgument(syntax != null, "Syntax node cannot be null");
        Preconditions.checkArgument(boundNode != null, "Bound node cannot be null");
        Preconditions.checkState(!boundNodesCache.containsKey(syntax), "Bound node for the given syntax node is already cached");

        boundNodesCache.put(syntax, boundNode);
    }

    public BoundNode bind(SyntaxNode syntax) throws CompilerException {
        return bind(syntax, BoundNodeForm.NORMAL);
    }

    public BoundNode bind(SyntaxNode syntax, BoundNodeForm form) throws CompilerException {
        Preconditions.checkArgument(syntax != null, "Syntax node cannot be null");

        var binder = getBinder(syntax);
        BoundNode boundNode = binder.bind(syntax);
        Preconditions.checkState(boundNode != null, "Binder returned null for syntax node: " + syntax.getClass().getSimpleName());

        if (form == BoundNodeForm.LOWERED) {
            boundNode = Lowerer.lower(boundNode);
            Preconditions.checkState(boundNode != null, "Lowerer returned null for bound node: " + boundNode.getClass().getSimpleName());
        }

        return boundNode;
    }

    public TypeSymbol resolveTypeSymbol(LangType type) {
        return compilation.getTypeRegistry().bindType(type);
    }

    /**
     * Query the symbol information for the given identifier syntax node.
     *
     * @param identifier The identifier syntax node to query symbol information
     * @return The symbol information for the given identifier syntax node.
     *
     * @implNote This method doesn't throw/report any diagnostic to allow
     * callers to handle failures gracefully (e.g., for features like code
     * completion).
     */
    public SymbolInfo getSymbolInfo(IdentifierSyntax identifier) {
        compilation.ensureDeclarationsComplete();

        var binder = getBinder(identifier);
        return binder.getSymbolInfo(identifier);
    }

    public SyntaxNode getSyntaxAtLocation(SourceLocation location) {
        Preconditions.checkArgument(location != null, "Source location cannot be null");

        if (location.source() != source) {
            throw new IllegalArgumentException("Source location does not belong to the syntax tree's source");
        }

        int position = source.getOffset(location);
        var syntaxTree = getSyntaxTree();
        return syntaxTree.getSyntaxAtPositionForCompletion(position);
    }

    public List<Symbol> suggestMembers(IdentifierSyntax identifierSyntax) throws CompilerException {
        Preconditions.checkArgument(identifierSyntax != null, "Identifier syntax cannot be null");

        var info = getSymbolInfo(identifierSyntax);
        if (info.isFailure()) {
            return List.of();
        }

        if (!(info.symbol() instanceof NamespaceOrTypeSymbol parentNamespaceOrType)) {
            return List.of();
        }

        return parentNamespaceOrType.getMembers();
    }

    public Binder getBinder(SyntaxNode syntax) throws CompilerException {
        Preconditions.checkArgument(syntax != null, "Syntax node cannot be null");

        var cached = bindersCache.get(syntax);
        if (cached != null) {
            return cached;
        }

        var binder = getBinderInternal(syntax);
        bindersCache.put(syntax, binder);

        return binder;
    }

    public void runDeclarationPhase(SymbolDeclarator.Phase phase) {
        symbolDeclarator.runPhase(phase);
    }

    public void ensureDeclarationsComplete() {
        compilation.ensureDeclarationsComplete();
    }

    public CompilationResult<BoundNamespace> bindFullSemanticModel() {
        if (fullSemanticModelBound) {
            return cachedBoundSemanticModelResult;
        }

        ensureDeclarationsComplete();

        if (symbolDeclarator.getDiagnostics().anyError()) {
            cachedBoundSemanticModelResult = CompilationResult.failure(symbolDeclarator.getDiagnostics());
            fullSemanticModelBound = true;
            return cachedBoundSemanticModelResult;
        }

        var bindingDiagnostics = new DiagnosticCollector();

        var unit = getCompilationUnit();
        var topLevelStatements = unit.topLevelStatements();

        if (!topLevelStatements.isEmpty()) {
            bindingDiagnostics.report(DiagnosticFactory.topLevelStatementsNotAllowed(topLevelStatements));
        }

        var boundGlobalNamespace = new BoundNamespace(compilation.getGlobalNamespace());
        var boundNamespace = boundGlobalNamespace;

        if (unit.packageDirectiveSyntax().isPresent()) {
            var identifier = unit.packageDirectiveSyntax().get().packageIdentifier();
            var packageNamespace = compilation.getOrCreatePackageNamespace(identifier);
            boundNamespace = getOrCreateBoundNamespace(boundGlobalNamespace, packageNamespace);
        } else {
            bindingDiagnostics.report(DiagnosticFactory.missingPackageDirective(source));
        }

        for (var stmt : unit.members()) {
            try {
                switch (stmt) {
                    case CollectionDeclarationSyntax collectionDecl ->
                        bindTypeDecl(boundNamespace, collectionDecl);
                    case MethodDeclarationSyntax methodDecl ->
                        bindMethodDecl(boundNamespace, methodDecl);
                    case VariableDeclarationSyntax constantVariableDecl ->
                        bindVariableDecl(boundNamespace, constantVariableDecl.variableDeclarator());
                    default -> {
                    }
                }
            } catch (CompilerException e) {
                bindingDiagnostics.report(e.diagnostic());
            }
        }

        var diagnostics = DiagnosticBag.merge(bindingDiagnostics.freeze(), symbolDeclarator.getDiagnostics());
        if (diagnostics.anyError()) {
            cachedBoundSemanticModelResult = CompilationResult.failure(diagnostics);
        } else {
            cachedBoundSemanticModelResult = CompilationResult.success(diagnostics, boundNamespace);
        }
        fullSemanticModelBound = true;

        return cachedBoundSemanticModelResult;
    }

    private BoundNamespace getOrCreateBoundNamespace(BoundNamespace globalBound, NamespaceSymbol ns) {
        if (ns.isGlobalNamespace()) {
            return globalBound;
        }

        var ownerBound = getOrCreateBoundNamespace(globalBound, (NamespaceSymbol) ns.owner());
        for (var existing : ownerBound.getNamespaces()) {
            if (existing.getSymbol() == ns) {
                return existing;
            }
        }

        var newBound = new BoundNamespace(ns);
        ownerBound.addNamespace(newBound);
        return newBound;
    }

    private void bindTypeDecl(BoundNamespace owner, CollectionDeclarationSyntax decl) {
        var symbol = (SourceNominalTypeSymbol) symbolCache.get(decl);
        Debug.requireNotNull(symbol, "Nominal type symbol should have been created and linked to the declaration in the declaration phase");

        symbol.forceUnderlyingTypeResolution();

        var boundType = new BoundType(owner, symbol);
        owner.addType(boundType);

        boundType.addConstructor(symbol.bindDefaultConstructor());

        for (var fieldDecl : decl.fields()) {
            var fieldSymbol = (SourceFieldSymbol) symbolCache.get(fieldDecl);
            Debug.requireNotNull(fieldSymbol, "Field symbol should have been created and linked to the declaration in the declaration phase");

            boundType.addField(fieldSymbol);
        }

        for (var member : symbol.getMembers()) {
            if (!(member instanceof SourceSymbol sourceSymbol)) {
                continue;
            }

            var declaration = sourceSymbol.declaringSyntax();
            if (declaration.source() != source) {
                // This member will be handled by another SemanticModel.
                continue;
            }

            if (member instanceof SourceMethodSymbol) {
                var methodDecl = (MethodDeclarationSyntax) declaration;
                bindMethodDecl(boundType, methodDecl);
            } else if (member instanceof SourceVariableSymbol) {
                var variableDecl = (VariableDeclaratorSyntax) declaration;
                bindVariableDecl(boundType, variableDecl);
            }
        }
    }

    public void bindMethodDecl(BoundNode owner, MethodDeclarationSyntax methodDecl) throws CompilerException {
        var symbol = (SourceMethodSymbol) symbolCache.get(methodDecl);
        Debug.requireNotNull(symbol, "Method symbol should have been created and linked to the declaration in the declaration phase");

        var boundBlock = (BoundBlockStmt) bind(methodDecl.body(), BoundNodeForm.LOWERED);
        var boundMethod = new BoundMethod(owner, symbol, boundBlock);
        DefiniteAssignment.check(boundMethod);

        if (!boundMethod.returnType().isVoid() && !ControlFlowGraph.allPathReturns(boundMethod.getBody())) {
            throw SemanticErrors.incompleteReturnsPath(methodDecl, boundMethod.getSymbol());
        }

        switch (owner) {
            case BoundNamespace boundNamespace ->
                boundNamespace.addMethod(boundMethod);
            case BoundType boundType ->
                boundType.addMethod(boundMethod);
            default ->
                throw new IllegalStateException("Owner of a method should always be either a BoundNamespace or a BoundType");
        }
    }

    public void bindVariableDecl(BoundNode owner, VariableDeclaratorSyntax declarator) throws CompilerException {
        var symbol = (SourceVariableSymbol) symbolCache.get(declarator);
        Debug.requireNotNull(symbol, "Variable symbol should have been created and linked to the declaration in the declaration phase");

        BoundExpression boundInitializer = null;
        if (declarator.initializer() != null) {
            boundInitializer = (BoundExpression) bind(declarator.initializer(), BoundNodeForm.LOWERED);
        }

        var variable = new BoundVariable(owner, symbol, boundInitializer);

        switch (owner) {
            case BoundNamespace boundNamespace ->
                boundNamespace.addVariable(variable);
            case BoundType boundType ->
                boundType.addVariable(variable);
            default ->
                throw new IllegalStateException("Owner of a variable should always be either a BoundNamespace or a BoundType");
        }
    }

    private Binder getBinderInternal(SyntaxNode syntax) throws CompilerException {
        Preconditions.checkArgument(syntax != null, "Syntax node cannot be null");

        if (syntax instanceof CompilationUnit unit) {
            // Special case for the root compilation unit to initialize the global binder with all top-level declarations
            return initializeGlobalBinder(unit);
        }

        var parentBinder = getBinder(syntax.parent());
        return switch (syntax) {
            case MethodDeclarationSyntax functionDecl ->
                new FunctionBodyBinder(parentBinder, (CallableSymbol) symbolCache.get(functionDecl));
            case BlockStmt blockStmt ->
                new BlockBinder(parentBinder);
            case ForStmt forStmt ->
                new ForBinder(parentBinder, forStmt);
            case ImplBlockSyntax implBlock ->
                new MemberBinder(parentBinder, (NamespaceOrTypeSymbol) symbolCache.get(implBlock));

            default ->
                parentBinder;
        };
    }

    /**
     * Initializes the global binder with the declarations from the compilation
     * unit
     */
    private MemberBinder initializeGlobalBinder(CompilationUnit unit) throws CompilerException {
        if (getCompilationUnit() != unit) {
            throw new IllegalStateException("initializeGlobalBinder should only be called with the compilation unit passed to the SemanticModel constructor");
        }

        globalBinder = new GlobalBinder(this);

        MemberBinder binder = globalBinder;
        if (unit.packageDirectiveSyntax().isPresent()) {
            var packageDirective = unit.packageDirectiveSyntax().get();
            var identifier = packageDirective.packageIdentifier();
            var packageNamespace = compilation.getOrCreatePackageNamespace(identifier);

            binder = new NamespaceBinder(binder, packageNamespace);
        }

        return binder;
    }
}
