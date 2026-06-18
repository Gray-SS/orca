package com.orca.compiler.core;

import java.nio.file.Files;
import java.util.List;
import java.util.Set;

import com.orca.compiler.core.boundtree.BoundExpression;
import com.orca.compiler.core.boundtree.BoundMethod;
import com.orca.compiler.core.boundtree.BoundNamespace;
import com.orca.compiler.core.boundtree.BoundProgram;
import com.orca.compiler.core.boundtree.BoundType;
import com.orca.compiler.core.boundtree.expressions.BoundMethodCallExpr;
import com.orca.compiler.core.boundtree.expressions.BoundReferenceExpr;
import com.orca.compiler.core.boundtree.statements.BoundBlockStmt;
import com.orca.compiler.core.boundtree.statements.BoundExpressionStmt;
import com.orca.compiler.core.diagnostics.DiagnosticCollector;
import com.orca.compiler.core.diagnostics.DiagnosticFactory;
import com.orca.compiler.core.externals.ClassPath;
import com.orca.compiler.core.externals.ExternalSymbolResolver;
import com.orca.compiler.core.io.EnhancedSyntaxTreePrinter;
import com.orca.compiler.core.semantics.SemanticModel;
import com.orca.compiler.core.symbols.NamespaceOrTypeSymbol;
import com.orca.compiler.core.symbols.NamespaceSymbol;
import com.orca.compiler.core.symbols.Symbol;
import com.orca.compiler.core.symbols.SymbolModifiers;
import com.orca.compiler.core.symbols.sources.SourceExtensionSymbol;
import com.orca.compiler.core.symbols.sources.SourceMethodSymbol;
import com.orca.compiler.core.symbols.sources.SourceNamespaceSymbol;
import com.orca.compiler.core.symbols.sources.SourceVariableSymbol;
import com.orca.compiler.core.symbols.synthesized.GlobalNamespaceSymbol;
import com.orca.compiler.core.symbols.synthesized.SynthesizedEntryClassSymbol;
import com.orca.compiler.core.symbols.synthesized.SynthesizedMethodSymbol;
import com.orca.compiler.core.syntax.SyntaxNode;
import com.orca.compiler.core.syntax.SyntaxTree;
import com.orca.compiler.core.syntax.members.MethodDeclarationSyntax;
import com.orca.compiler.core.syntax.nodes.IdentifierSyntax;
import com.orca.compiler.core.syntax.nodes.QualifiedIdentifierSyntax;
import com.orca.compiler.core.syntax.nodes.SimpleIdentifierSyntax;
import com.orca.compiler.core.syntax.nodes.SpecialTypeIdentifierSyntax;
import com.orca.compiler.core.syntax.nodes.VariableDeclaratorSyntax;
import com.orca.compiler.core.text.FileSource;
import com.orca.compiler.core.text.TextSource;
import com.orca.compiler.core.typesystem.ArrayType;
import com.orca.compiler.core.typesystem.LangType;

public final class Compilation {

    public static final String ORCA_ENTRY_CLASS_NAME = "OrcaEntry";
    public static final String ORCA_ENTRY_METHOD_NAME = "main";

    private final GlobalNamespaceSymbol globalNamespace;

    private final DiagnosticCollector diagnostics;
    private final List<SemanticModel> semanticModels;

    private final CompilationContext context;
    private final ExternalSymbolResolver resolver;
    private final TypeRegistry typeRegistry = new TypeRegistry(this);

    private BoundProgram boundProgram;

    private boolean declarationsComplete = false;
    private boolean boundProgramConstructed = false;

    public Compilation(CompilationContext context) {
        this.diagnostics = context.diagnostics();
        this.context = context;
        this.globalNamespace = new GlobalNamespaceSymbol(this);
        this.semanticModels = initializeSemanticModels(context.arguments());
        this.resolver = initializeExternalSymbolResolver(context.arguments());
    }

    private static ExternalSymbolResolver initializeExternalSymbolResolver(CompilerArguments args) {
        var classPathIndices = getClassPathIndices(args);
        var includeJdk = true; // TODO: Make this configurable via compiler arguments if needed.

        return new ExternalSymbolResolver(classPathIndices, includeJdk);
    }

    private static Set<ClassPath> getClassPathIndices(CompilerArguments args) {
        return args.getClassPaths()
                .stream()
                .map(path -> ClassPath.of(path))
                .collect(java.util.stream.Collectors.toSet());
    }

    public List<SyntaxTree> getSyntaxTrees() {
        return semanticModels.stream().map(SemanticModel::getSyntaxTree).toList();
    }

    public SemanticModel getSemanticModel(TextSource source) {
        for (var semanticModel : semanticModels) {
            if (semanticModel.getSyntaxTree().source() == source) {
                return semanticModel;
            }
        }

        throw new IllegalArgumentException("No semantic model found for source: " + source);
    }

    public SemanticModel getSemanticModel(SyntaxTree syntaxTree) {
        for (var semanticModel : semanticModels) {
            if (semanticModel.getSyntaxTree() == syntaxTree) {
                return semanticModel;
            }
        }

        throw new IllegalArgumentException("No semantic model found for syntax tree: " + syntaxTree);
    }

    public BoundProgram getBoundProgram() {
        if (boundProgramConstructed) {
            return boundProgram;
        }

        ensureDeclarationsComplete();

        var boundNamespace = new BoundNamespace(globalNamespace);
        for (SemanticModel semanticModel : semanticModels) {
            semanticModel.bind(boundNamespace);
        }

        for (var extension : collectExtensions()) {
            bindExtension(boundNamespace, extension);
        }

        if (!context.arguments().hasFlag(CompilerFlag.LIBRARY_MODE)) {
            var entryClassSymbol = getEntryClassSymbol();
            var boundEntryClass = bindEntryClassSymbol(boundNamespace, entryClassSymbol);
            boundNamespace.addType(boundEntryClass);
        }

        boundProgram = new BoundProgram(boundNamespace);
        boundProgramConstructed = true;
        return boundProgram;
    }

    private BoundType bindEntryClassSymbol(BoundNamespace boundNamespace, SynthesizedEntryClassSymbol entryClassSymbol) {
        var boundEntryClass = new BoundType(boundNamespace, entryClassSymbol);
        var mainMethodSymbol = entryClassSymbol.getMainMethodSymbol();

        var entryMainMethodSymbol = new SynthesizedMethodSymbol(entryClassSymbol, ORCA_ENTRY_METHOD_NAME, LangType.Void, List.of(LangType.arrayOf(LangType.String)), SymbolModifiers.STATIC);
        var argsParameter = entryMainMethodSymbol.getParameter(0);

        // Only forward String[] args if the user's main actually accepts them.
        var userMainParams = mainMethodSymbol.parameters();
        var callArgs = userMainParams.isEmpty()
                ? List.<BoundExpression>of()
                : List.<BoundExpression>of(BoundReferenceExpr.of(argsParameter));

        var entryMainBody = new BoundBlockStmt(
                new BoundExpressionStmt(
                        new BoundMethodCallExpr(
                                BoundReferenceExpr.of(mainMethodSymbol),
                                callArgs
                        )
                )
        );

        var boundEntryMainMethod = new BoundMethod(boundEntryClass, entryMainMethodSymbol, entryMainBody);
        boundEntryClass.addMethod(boundEntryMainMethod);

        return boundEntryClass;
    }

    private void bindExtension(BoundNamespace boundNamespace, SourceExtensionSymbol extension) {
        var boundType = new BoundType(boundNamespace, extension);
        boundNamespace.addType(boundType);

        for (var members : extension.getMembers()) {
            switch (members) {
                case SourceMethodSymbol methodSymbol -> {
                    var methodDecl = (MethodDeclarationSyntax) methodSymbol.declaringSyntax();
                    var semanticModel = getSemanticModelForSyntaxNode(methodDecl);

                    semanticModel.bindMethodDecl(boundType, methodDecl);
                }
                case SourceVariableSymbol variableSymbol -> {
                    var variableDecl = (VariableDeclaratorSyntax) variableSymbol.declaringSyntax();
                    var semanticModel = getSemanticModelForSyntaxNode(variableDecl);

                    semanticModel.bindVariableDecl(boundType, variableDecl);
                }
                default ->
                    throw new IllegalStateException("Unsupported member in extension: " + members.getClass().getName());
            }
        }
    }

    private SemanticModel getSemanticModelForSyntaxNode(SyntaxNode node) {
        for (var semanticModel : semanticModels) {
            if (semanticModel.getSyntaxTree().source() == node.source()) {
                return semanticModel;
            }
        }

        throw new IllegalStateException("No semantic model found for syntax node: " + node.getClass().getName());
    }

    private List<SourceExtensionSymbol> collectExtensions() {
        var extensions = new java.util.ArrayList<SourceExtensionSymbol>();
        for (var member : globalNamespace.getMembers()) {
            if (member instanceof SourceExtensionSymbol extension) {
                extensions.add(extension);
            }
        }

        return extensions;
    }

    private SynthesizedEntryClassSymbol getEntryClassSymbol() {
        var mainCandidates = findMainFunctionCandidates();
        if (mainCandidates.isEmpty()) {
            throw CompilerException.wrap(DiagnosticFactory.missingMainFunction());
        } else if (mainCandidates.size() > 1) {
            throw CompilerException.wrap(DiagnosticFactory.ambiguousMainFunction(mainCandidates));
        }

        var mainFunction = mainCandidates.get(0);
        mainFunction.markAsEntryPoint();

        return new SynthesizedEntryClassSymbol(ORCA_ENTRY_CLASS_NAME, this, mainFunction);
    }

    private List<SourceMethodSymbol> findMainFunctionCandidates() {
        var candidates = new java.util.ArrayList<SourceMethodSymbol>();

        for (var member : globalNamespace.getMembersWithChildren()) {
            if (!member.name().equals("main")) {
                continue;
            }

            if (member instanceof SourceMethodSymbol methodSymbol && isMainFunctionCandidate(methodSymbol)) {
                candidates.add(methodSymbol);
            }
        }

        return candidates;
    }

    private boolean isMainFunctionCandidate(SourceMethodSymbol symbol) {
        if (!symbol.name().equals("main")) {
            return false;
        }

        var parameters = symbol.parameters();
        if (parameters.size() > 1) {
            return false;
        } else if (parameters.size() == 1) {
            var paramType = parameters.get(0).type();
            if (!(paramType instanceof ArrayType arrayType) || (arrayType.getElementType() != LangType.String)) {
                return false;
            }
        }

        return symbol.returnType().isVoid();
    }

    public List<String> suggestSymbols(IdentifierSyntax identifier) {
        var suggestions = new java.util.ArrayList<String>();

        // for (var source : context.arguments().getJarSources()) {
        //     // suggestions.addAll(source.suggestExternalPackageNamespaces(identifier.text()));
        //     // suggestions.addAll(source.suggestExternalClasses(identifier.text()));
        // }
        return suggestions;
    }

    public Symbol resolveSymbolFromGlobalNamespace(String fullName) {
        var nameParts = fullName.split("\\.");
        Symbol current = globalNamespace;
        for (var part : nameParts) {
            if (current instanceof NamespaceOrTypeSymbol ns) {
                current = ns.getMember(part);
                if (current == null) {
                    return null;
                }
            } else {
                return null;
            }
        }

        return current;
    }

    public Symbol resolveExternalSymbol(String name) {
        if (resolver.exists(name)) {
            return resolver.getOrCreateSymbol(name);
        }

        return null;
    }

    public NamespaceSymbol getOrCreatePackageNamespace(IdentifierSyntax identifier) {
        return getOrCreatePackageNamespace(globalNamespace, identifier);
    }

    public TypeRegistry getTypeRegistry() {
        return typeRegistry;
    }

    public CompilationContext getContext() {
        return context;
    }

    public GlobalNamespaceSymbol getGlobalNamespace() {
        return globalNamespace;
    }

    private List<SemanticModel> initializeSemanticModels(CompilerArguments args) {
        var result = new java.util.ArrayList<SemanticModel>();
        var seenSourcesPaths = new java.util.HashSet<String>();

        var allSources = new java.util.ArrayList<TextSource>();
        allSources.addAll(args.getSources());

        for (var source : allSources) {
            if (source instanceof FileSource fileSource) {
                if (!seenSourcesPaths.add(fileSource.getPath().toAbsolutePath().normalize().toString())) {
                    Debug.warning("Duplicate source file ignored: " + fileSource.getPathString());
                    continue; // Skip duplicate source
                }

                if (!Files.exists(fileSource.getPath())) {
                    throw CompilerException.wrap(DiagnosticFactory.inputFileNotFound(fileSource.getPath()));
                }

                if (!Files.isRegularFile(fileSource.getPath())) {
                    throw CompilerException.wrap(DiagnosticFactory.inputFileNotFound(fileSource.getPath()));
                }
            }

            source.validate();

            var syntaxTree = SyntaxTree.parse(diagnostics, source);
            var semanticModel = new SemanticModel(this, syntaxTree);

            result.add(semanticModel);
        }

        if (args.hasFlag(CompilerFlag.PRINT_AST)) {
            for (var semanticModel : result) {
                var syntaxTree = semanticModel.getSyntaxTree();
                EnhancedSyntaxTreePrinter.print(syntaxTree, args.getIndentSize());
            }
        }

        return result;
    }

    private NamespaceSymbol getOrCreatePackageNamespace(NamespaceSymbol owner, IdentifierSyntax identifier) {
        return switch (identifier) {
            case SpecialTypeIdentifierSyntax special ->
                throw new IllegalStateException("Special type identifiers cannot be used as namespaces: " + special.text());
            case SimpleIdentifierSyntax simple -> {
                var member = owner.getMember(simple.text());
                if (member == null) {
                    var newNamespace = new SourceNamespaceSymbol(owner, simple.text(), identifier);
                    owner.addMember(newNamespace);

                    yield newNamespace;
                }

                if (member instanceof NamespaceSymbol ns) {
                    yield ns;
                }

                throw new IllegalStateException("Name conflict: " + simple.text() + " in " + owner.getFullName() + " is not a namespace");
            }
            case QualifiedIdentifierSyntax qualified -> {
                var namespace = getOrCreatePackageNamespace(owner, qualified.left());
                yield getOrCreatePackageNamespace(namespace, qualified.right());
            }
        };
    }

    public void ensureDeclarationsComplete() {
        if (declarationsComplete) {
            return;
        }

        declarationsComplete = true;

        // 1. Register lazy imports
        for (var semanticModel : semanticModels) {
            semanticModel.resolveImports();
        }

        // 2. Declare all types
        for (var semanticModel : semanticModels) {
            semanticModel.declareTypes();
        }

        // 3. Declare methods (after all types are resolved to allow method signatures to reference other types)
        for (var semanticModel : semanticModels) {
            semanticModel.declareMethods();
        }

        // 4. Declare remaining symbols
        for (var semanticModel : semanticModels) {
            semanticModel.declareRemainingSymbols();
        }
    }
}
