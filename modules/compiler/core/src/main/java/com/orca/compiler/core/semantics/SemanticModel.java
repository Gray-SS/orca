package com.orca.compiler.core.semantics;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

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
import com.orca.compiler.core.diagnostics.DiagnosticCollector;
import com.orca.compiler.core.diagnostics.DiagnosticFactory;
import com.orca.compiler.core.lexer.TokenKind;
import com.orca.compiler.core.symbols.CallableSymbol;
import com.orca.compiler.core.symbols.Lazy;
import com.orca.compiler.core.symbols.MethodSymbol;
import com.orca.compiler.core.symbols.NamespaceOrTypeSymbol;
import com.orca.compiler.core.symbols.NamespaceSymbol;
import com.orca.compiler.core.symbols.Symbol;
import com.orca.compiler.core.symbols.TypeSymbol;
import com.orca.compiler.core.symbols.VariableSymbol;
import com.orca.compiler.core.symbols.sources.SourceExtensionSymbol;
import com.orca.compiler.core.symbols.sources.SourceFieldSymbol;
import com.orca.compiler.core.symbols.sources.SourceMethodSymbol;
import com.orca.compiler.core.symbols.sources.SourceNominalTypeSymbol;
import com.orca.compiler.core.symbols.sources.SourceParameterSymbol;
import com.orca.compiler.core.symbols.sources.SourceSymbol;
import com.orca.compiler.core.symbols.sources.SourceVariableSymbol;
import com.orca.compiler.core.syntax.CompilationUnit;
import com.orca.compiler.core.syntax.SyntaxNode;
import com.orca.compiler.core.syntax.SyntaxTree;
import com.orca.compiler.core.syntax.members.CollectionDeclarationSyntax;
import com.orca.compiler.core.syntax.members.ImplBlockSyntax;
import com.orca.compiler.core.syntax.members.MemberSyntax;
import com.orca.compiler.core.syntax.members.MethodDeclarationSyntax;
import com.orca.compiler.core.syntax.members.VariableDeclarationSyntax;
import com.orca.compiler.core.syntax.nodes.IdentifierSyntax;
import com.orca.compiler.core.syntax.nodes.ImportSyntax;
import com.orca.compiler.core.syntax.nodes.VariableDeclaratorSyntax;
import com.orca.compiler.core.syntax.statements.BlockStmt;
import com.orca.compiler.core.syntax.statements.ForStmt;
import com.orca.compiler.core.text.SourceLocation;
import com.orca.compiler.core.text.TextSource;
import com.orca.compiler.core.typesystem.CollectionType;
import com.orca.compiler.core.typesystem.LangType;

public final class SemanticModel {

    private final Map<SyntaxNode, BoundNode> boundNodesCache = new java.util.HashMap<>();
    private final Map<SyntaxNode, Binder> bindersCache = new java.util.HashMap<>();
    private final Map<SyntaxNode, Symbol> symbolCache = new java.util.HashMap<>();

    private final DiagnosticCollector declarationDiagnostics;

    private final Compilation compilation;
    private final TextSource source;

    private GlobalBinder globalBinder;

    private boolean fullSemanticModelBound = false;

    private SyntaxTree cachedSyntaxTree;
    private CompilationResult<BoundNamespace> cachedBoundSemanticModelResult;

    private boolean importsResolved = false;
    private boolean typesDeclared = false;
    private boolean methodsDeclared = false;
    private boolean remainingSymbolsDeclared = false;

    public SemanticModel(Compilation compilation, TextSource source) {
        Preconditions.checkNotNull(compilation, "Compilation cannot be null");
        Preconditions.checkNotNull(source, "TextSource cannot be null");

        this.source = source;
        this.compilation = compilation;
        this.declarationDiagnostics = new DiagnosticCollector();
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

    public DiagnosticCollector getDiagnostics() {
        var boundDiagnostics = bindFullSemanticModel().diagnostics();
        var syntaxTree = getSyntaxTree();
        return DiagnosticCollector.merge(syntaxTree.getDiagnostics(), declarationDiagnostics, boundDiagnostics);
    }

    public CompilationUnit getCompilationUnit() {
        var root = getSyntaxTree().root();
        if (root instanceof CompilationUnit unit) {
            return unit;
        }

        throw new IllegalStateException("Root syntax node of the syntax tree should always be a CompilationUnitSyntax");
    }

    public Symbol getDeclaredSymbol(SyntaxNode syntax) {
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

    public void ensureDeclarationsComplete() {
        compilation.ensureDeclarationsComplete();
    }

    public void declareTypes() throws CompilerException {
        if (typesDeclared) {
            return;
        }

        typesDeclared = true;
        var unit = getCompilationUnit();
        MemberBinder gbinder = (MemberBinder) getBinder(unit);

        for (var member : unit.members()) {
            if (member instanceof CollectionDeclarationSyntax decl) {
                declareCollection(gbinder, decl);
            }
        }
    }

    public void declareRemainingSymbols() throws CompilerException {
        if (remainingSymbolsDeclared) {
            return;
        }

        remainingSymbolsDeclared = true;
        var unit = getCompilationUnit();
        MemberBinder gbinder = (MemberBinder) getBinder(unit);

        for (var member : unit.members()) {
            if (!(member instanceof CollectionDeclarationSyntax)) {
                declareNamespaceMember(gbinder, member, false);
            }
        }
    }

    public void resolveImports() throws CompilerException {
        if (importsResolved) {
            return;
        }

        importsResolved = true;
        var unit = getCompilationUnit();
        getBinder(unit); // Force initialization of the global binder

        var seenImports = new HashSet<String>();
        for (var importSyntax : unit.imports()) {
            var identifier = importSyntax.identifier();
            var symbolName = identifier.name();

            if (!seenImports.add(symbolName)) {
                continue;
            }

            var lazyImport = new Lazy<>(() -> resolveImportedSymbol(importSyntax));
            globalBinder.registerLazyImportSymbol(symbolName, lazyImport);
        }
    }

    private Symbol resolveImportedSymbol(ImportSyntax importSyntax) {
        var importedSymbol = globalBinder.resolveSymbol(importSyntax.identifier());
        if (importedSymbol == null) {
            throw SemanticErrors.undeclaredPackage(importSyntax, importSyntax.identifier().text(), List.of());
        }

        return importedSymbol;
    }

    public CompilationResult<BoundNamespace> bindFullSemanticModel() {
        if (fullSemanticModelBound) {
            return cachedBoundSemanticModelResult;
        }

        ensureDeclarationsComplete();

        var bindingDiagnostics = DiagnosticCollector.copyOf(declarationDiagnostics);
        if (bindingDiagnostics.hasErrors()) {
            cachedBoundSemanticModelResult = CompilationResult.failure(bindingDiagnostics);
            return cachedBoundSemanticModelResult;
        }

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

        fullSemanticModelBound = true;

        if (bindingDiagnostics.hasErrors()) {
            cachedBoundSemanticModelResult = CompilationResult.failure(bindingDiagnostics);
        } else {
            cachedBoundSemanticModelResult = CompilationResult.success(bindingDiagnostics, boundNamespace);
        }

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

    public void declareMethods() throws CompilerException {
        if (methodsDeclared) {
            return;
        }

        methodsDeclared = true;

        try {
            var unit = getCompilationUnit();
            MemberBinder gbinder = (MemberBinder) getBinder(unit);

            for (var member : unit.members()) {
                switch (member) {
                    case MethodDeclarationSyntax methodDecl ->
                        declareNamespaceMethod(gbinder, methodDecl);
                    case ImplBlockSyntax implBlock ->
                        declareTypeMembers(gbinder, implBlock, true);
                    default -> {
                    }
                }
            }
        } catch (CompilerException e) {
            declarationDiagnostics.report(e.diagnostic());
        }
    }

    private void declareNamespaceMember(MemberBinder binder, MemberSyntax syntax, boolean methodsOnly) throws CompilerException {
        if (methodsOnly) {
            switch (syntax) {
                case MethodDeclarationSyntax methodDecl ->
                    declareNamespaceMethod(binder, methodDecl);
                case ImplBlockSyntax implBlock ->
                    declareTypeMembers(binder, implBlock, true);
                default -> {
                }
            }
        } else {
            switch (syntax) {
                case VariableDeclarationSyntax constantDecl ->
                    declareVariable(binder, constantDecl);
                case ImplBlockSyntax implBlock ->
                    declareTypeMembers(binder, implBlock, false);
                default -> {
                }
            }
        }
    }

    private void declareCollection(MemberBinder binder, CollectionDeclarationSyntax decl) throws CompilerException {
        Preconditions.checkArgument(decl != null, "Collection declaration cannot be null");

        var owner = binder.getOwnerSymbol();
        if (!(owner instanceof NamespaceSymbol ownerNamespace)) {
            throw new IllegalStateException("Collection declarations should only be allowed directly under namespaces");
        }

        var collectionName = decl.identifier().text();
        if (collectionName.isEmpty() || !Character.isUpperCase(collectionName.charAt(0))) {
            throw SemanticErrors.lowercaseCollectionName(decl, collectionName);
        }

        var lazyType = new Lazy<>(() -> resolveCollectionTypeShape(decl));
        SourceNominalTypeSymbol symbol = new SourceNominalTypeSymbol(ownerNamespace, collectionName, decl, lazyType);

        symbolCache.put(decl, symbol);

        var seenNames = new HashSet<String>();
        for (int i = 0; i < decl.fields().size(); i++) {
            var fieldDecl = decl.fields().get(i);
            var fieldName = fieldDecl.identifier().text();
            if (!seenNames.add(fieldName)) {
                throw SemanticErrors.fieldRedeclared(decl, fieldName, collectionName);
            }

            var fieldSymbol = new SourceFieldSymbol(fieldName, fieldDecl, symbol, i, new Lazy<>(() -> binder.resolveType(fieldDecl.type())));
            symbolCache.put(fieldDecl, fieldSymbol);
            symbol.addMember(fieldSymbol);
        }

        declare(ownerNamespace, symbol);
    }

    private void declareTypeMembers(MemberBinder parentBinder, ImplBlockSyntax decl, boolean methodsOnly) throws CompilerException {
        Preconditions.checkArgument(decl != null, "Impl block syntax cannot be null");

        var resolvedType = parentBinder.resolveType(decl.type());

        var comp = getCompilation();
        var targetTypeSymbol = comp.getTypeRegistry().bindType(resolvedType);

        TypeSymbol ownerTypeSymbol = targetTypeSymbol;
        if (!targetTypeSymbol.isSource()) {
            var globalNamespace = comp.getGlobalNamespace();
            var extensionSymbol = new SourceExtensionSymbol(globalNamespace, targetTypeSymbol, decl);
            targetTypeSymbol.addExtension(extensionSymbol);

            ownerTypeSymbol = extensionSymbol;
        }

        var binder = new MemberBinder(parentBinder, ownerTypeSymbol);

        symbolCache.put(decl, ownerTypeSymbol);

        for (var member : decl.members()) {
            declareTypeMember(binder, member, methodsOnly);
        }

    }

    private void declareTypeMember(MemberBinder binder, MemberSyntax syntax, boolean methodsOnly) throws CompilerException {
        if (methodsOnly) {
            switch (syntax) {
                case MethodDeclarationSyntax methodDecl ->
                    declareMethod(binder, methodDecl);
                default -> {
                }
            }
        } else {
            switch (syntax) {
                case VariableDeclarationSyntax constantDecl ->
                    declareVariable(binder, constantDecl);
                case ImplBlockSyntax implBlock -> {
                    throw SemanticErrors.forbiddenNestedDeclaration(syntax, "impl block");
                }
                case CollectionDeclarationSyntax collDecl -> {
                    throw SemanticErrors.forbiddenNestedDeclaration(syntax, "collection");
                }
                default -> {
                }
            }
        }
    }

    private void declareMethod(MemberBinder binder, MethodDeclarationSyntax decl) throws CompilerException {
        Preconditions.checkArgument(decl != null, "Method declaration cannot be null");

        var owner = binder.getOwnerSymbol();
        if (!(owner instanceof TypeSymbol ownerType)) {
            throw new IllegalStateException("MemberBinder's owner should always be a TypeSymbol when declaring methods");
        }

        var name = decl.identifier().text();
        var returnType = (decl.returnType() == null) ? LangType.Void : binder.resolveType(decl.returnType());

        var params = new ArrayList<SourceParameterSymbol>();
        var seenParameterNames = new HashSet<String>();

        var firstReceiverParamIdx = -1;
        for (int i = 0; i < decl.parameters().size(); i++) {
            var p = decl.parameters().get(i);
            var pname = p.identifier().text();

            if (p.isReceiver()) {
                if (firstReceiverParamIdx != -1) {
                    throw SemanticErrors.multipleReceiverParameters(p, decl.parameters().get(firstReceiverParamIdx), ownerType.name());
                }

                if (i != 0) {
                    throw SemanticErrors.receiverParameterNotFirst(p, ownerType.name());
                }

                firstReceiverParamIdx = i;
            }

            if (p.isReceiver()) {
                continue;
            }

            var parameter = new SourceParameterSymbol(pname, p, i, binder.resolveType(p.type()));
            params.add(parameter);

            if (!seenParameterNames.add(pname)) {
                var existingParam = params.stream()
                        .filter(pr -> pr.name().equals(pname))
                        .findFirst().orElseThrow(() -> new IllegalStateException("Parameter should be in the list"));

                throw SemanticErrors.symbolRedeclared(existingParam, parameter);
            }
        }

        boolean isStatic = (firstReceiverParamIdx == -1);
        var method = new SourceMethodSymbol(name, decl, owner, returnType, params, isStatic);

        declare(owner, method);
    }

    private VariableSymbol declareVariable(MemberBinder binder, VariableDeclarationSyntax decl) throws CompilerException {
        Preconditions.checkArgument(decl != null, "Variable declaration cannot be null");
        Preconditions.checkArgument(decl.variableDeclarator() != null, "Variable declarator cannot be null");

        var declarator = decl.variableDeclarator();
        final var name = declarator.identifier().text();

        final var modifierToken = declarator.modifierToken();
        final var modifierKind = modifierToken.kind();
        final var isConst = modifierKind == TokenKind.ConstKeyword;
        final var isLet = modifierKind == TokenKind.LetKeyword;
        final var requireInitializer = isConst || isLet;
        final var owner = binder.getOwnerSymbol();
        final var declaredType = declarator.type() != null ? binder.resolveType(declarator.type()) : null;

        if (declaredType != null && declaredType.isVoid()) {
            throw SemanticErrors.voidVariableType(declarator, name);
        }

        BoundExpression boundInitializer = null;
        if (declarator.initializer() != null) {
            boundInitializer = (BoundExpression) binder.bindExpr(declarator.initializer());

            if (declaredType != null && !boundInitializer.type().isAssignableTo(declaredType)) {
                throw SemanticErrors.typeMismatch(declarator, declaredType, boundInitializer.type());
            }
        }

        final var variableType = (declaredType != null) ? declaredType : (boundInitializer != null ? boundInitializer.type() : null);
        if (variableType == null) {
            throw SemanticErrors.cannotInferVariableType(declarator, name);
        }

        // Check constant type constraint before binding the initializer so the
        // correct diagnostic is reported even when the initializer itself fails.
        if (isConst && !variableType.isPrimitive()) {
            throw SemanticErrors.missingConstantBaseType(declarator, name);
        }

        if (requireInitializer && boundInitializer == null) {
            throw SemanticErrors.missingVariableInitializer(declarator, name, modifierToken.text());
        }

        // Initializer of a constant variable must be compile-time evaluable.
        SourceVariableSymbol variable;
        if (isConst) {
            if (boundInitializer == null) {
                // Already checked but makes the compiler happy
                throw SemanticErrors.missingVariableInitializer(declarator, name, modifierToken.text());
            }

            // Initializer must be compile-time evaluable.
            if (!boundInitializer.isCompileTimeFoldable()) {
                throw SemanticErrors.constantNonCompileTimeFoldableInitializer(declarator, name);
            }

            var constantValue = boundInitializer.getConstant();

            // example: const x := 20; -> 20 is an int literal but needs to be converted to the declared type (e.g., float) of the constant variable
            var convertedConstantValue = constantValue.convertTo(variableType);

            variable = SourceVariableSymbol.createConstant(owner, name, declarator, convertedConstantValue, false);
        } else {
            variable = SourceVariableSymbol.createAssociated(owner, name, declarator, variableType, isLet);
        }

        declare(owner, variable);

        if (declarator.initializer() != null && boundInitializer != null) {
            cacheBoundNode(declarator.initializer(), boundInitializer);
        }
        return variable;
    }

    private MethodSymbol declareNamespaceMethod(MemberBinder binder, MethodDeclarationSyntax decl) throws CompilerException {
        Preconditions.checkArgument(binder != null, "Binder cannot be null");
        Preconditions.checkArgument(decl != null, "Method declaration cannot be null");

        var owner = binder.getOwnerSymbol();
        if (!(owner instanceof NamespaceSymbol ownerNamespace)) {
            throw new IllegalStateException("Function declarations should only be allowed directly under namespaces");
        }

        var name = decl.identifier().text();
        var returnType = (decl.returnType() == null) ? LangType.Void : binder.resolveType(decl.returnType());

        var params = new ArrayList<SourceParameterSymbol>();
        var seenParameterNames = new HashSet<String>();

        for (int i = 0; i < decl.parameters().size(); i++) {
            var p = decl.parameters().get(i);
            if (p.isReceiver()) {
                throw SemanticErrors.receiverParameterInFunction(p, decl.identifier().text());
            }

            var paramName = p.identifier().text();
            var parameter = new SourceParameterSymbol(paramName, p, i, binder.resolveType(p.type()));
            params.add(parameter);

            if (seenParameterNames.contains(parameter.name())) {
                var existingParam = params.stream()
                        .filter(pr -> pr.name().equals(parameter.name()))
                        .findFirst().orElseThrow(() -> new IllegalStateException("Parameter should be in the list"));

                throw SemanticErrors.symbolRedeclared(existingParam, parameter);
            }

            seenParameterNames.add(paramName);
        }

        var method = new SourceMethodSymbol(name, decl, ownerNamespace, returnType, params, true);
        declare(ownerNamespace, method);

        return method;
    }

    private void declare(NamespaceOrTypeSymbol owner, SourceSymbol symbol) throws CompilerException {
        Preconditions.checkArgument(owner != null, "Owner cannot be null");
        Preconditions.checkArgument(symbol != null, "Symbol cannot be null");

        if (symbol instanceof CallableSymbol callableSymbol) {
            var members = owner.getMembers(symbol.name());
            var parameterTypes = callableSymbol.type().parameterTypes();
            for (var member : members) {
                if (member instanceof CallableSymbol existingCallable) {
                    if (existingCallable.type().parameterTypes().equals(parameterTypes)) {
                        switch (existingCallable) {
                            case MethodSymbol m ->
                                throw SemanticErrors.methodRedeclared((SourceMethodSymbol) symbol, m);
                            default ->
                                throw SemanticErrors.symbolRedeclared(existingCallable, symbol);
                        }
                    }
                } else {
                    throw SemanticErrors.symbolRedeclared(member, symbol, symbol.name());
                }
            }
        } else {
            var existing = owner.getMember(symbol.name());
            if (existing != null) {
                throw SemanticErrors.symbolRedeclared(existing, symbol);
            }
        }

        symbolCache.put(symbol.declaringSyntax(), symbol);
        owner.addMember(symbol);
    }

    private LangType resolveCollectionTypeShape(CollectionDeclarationSyntax decl) throws CompilerException {
        Preconditions.checkArgument(decl != null, "Collection declaration cannot be null");
        var symbol = (SourceNominalTypeSymbol) symbolCache.get(decl);
        Debug.requireNotNull(symbol, "Collection symbol should have been created in the declaration phase");

        var fieldTypes = new LinkedHashMap<String, LangType>();
        for (int i = 0; i < decl.fields().size(); i++) {
            var fieldDecl = decl.fields().get(i);
            var fieldSymbol = (SourceFieldSymbol) symbolCache.get(fieldDecl);

            fieldTypes.put(fieldSymbol.name(), fieldSymbol.type());
        }

        return new CollectionType(fieldTypes);
    }
}
