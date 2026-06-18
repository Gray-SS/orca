package com.orca.compiler.core.diagnostics;

import java.nio.file.Path;
import java.util.List;
import java.util.stream.Collectors;

import com.orca.compiler.core.boundtree.expressions.BoundOperator;
import com.orca.compiler.core.semantics.SemanticErrors;
import com.orca.compiler.core.symbols.CallableSymbol;
import com.orca.compiler.core.symbols.ConstructorSymbol;
import com.orca.compiler.core.symbols.MethodSymbol;
import com.orca.compiler.core.symbols.NamespaceOrTypeSymbol;
import com.orca.compiler.core.symbols.Symbol;
import com.orca.compiler.core.symbols.TypeSymbol;
import com.orca.compiler.core.symbols.ValueSymbol;
import com.orca.compiler.core.symbols.sources.SourceMethodSymbol;
import com.orca.compiler.core.symbols.sources.SourceSymbol;
import com.orca.compiler.core.syntax.StatementSyntax;
import com.orca.compiler.core.syntax.SyntaxNode;
import com.orca.compiler.core.syntax.SyntaxToken;
import com.orca.compiler.core.syntax.nodes.IfClauseSyntax;
import com.orca.compiler.core.syntax.nodes.ParameterSyntax;
import com.orca.compiler.core.syntax.statements.ForStmt;
import com.orca.compiler.core.syntax.statements.WhileStmt;
import com.orca.compiler.core.text.IHaveSpan;
import com.orca.compiler.core.text.TextSource;
import com.orca.compiler.core.typesystem.LangType;

public final class DiagnosticFactory {

    // ─── Cross-cutting pipeline ──────────────────────────────────────────────
    // These errors don't belong to a single compilation phase.
    public static Diagnostic missingStdPath() {
        return DiagnosticBuilder.from(DiagnosticCode.MISSING_STD_PATH)
                .withHelp("The 'FLUX_STD_PATH' environment variable is not set. Please set it to the path of the standard library to start using Flux.")
                .build();
    }

    public static Diagnostic missingPackageDirective(TextSource source) {
        return DiagnosticBuilder.from(DiagnosticCode.MISSING_PACKAGE_DIRECTIVE)
                .withSource(source, "Every source file must start with a package directive that declares which package the file belongs to.")
                .withHelp("Add a package directive at the top of the file, for example: 'package my.package.name;'")
                .build();
    }

    public static Diagnostic unsupportedSyntaxTreeRoot() {
        return DiagnosticBuilder.from(DiagnosticCode.UNSUPPORTED_SYNTAX_TREE_ROOT)
                .withHelp("The parser produced a syntax tree with an invalid root node. This is likely a bug in the parser. Please report this to the developers.")
                .build();
    }

    public static Diagnostic unsupportedFeature(String featureDescription) {
        return DiagnosticBuilder.from(DiagnosticCode.UNSUPPORTED_FEATURE)
                .withHelp("The compiler does not support the following feature: " + featureDescription)
                .build();
    }

    public static Diagnostic unsupportedFeature(IHaveSpan span, String featureDescription) {
        return simpleWithCodeSnippet(DiagnosticCode.UNSUPPORTED_FEATURE, span, "Unsupported feature: " + featureDescription);
    }

    // ─── Input ────────────────────────────────────────────────────────────────
    public static Diagnostic inputSourceMissing() {
        return DiagnosticBuilder.from(DiagnosticCode.INPUT_SOURCE_MISSING)
                .withHelp("No input source was provided to the compiler. Please provide a source file or other input source to compile.")
                .build();
    }

    public static Diagnostic inputFileNotFound(Path filePath) {
        return DiagnosticBuilder.from(DiagnosticCode.INPUT_FILE_NOT_FOUND)
                .withSuffixMessage(filePath.toAbsolutePath().toString())
                .withHelp("Make sure the file path is correct and that the file exists.")
                .build();
    }

    public static Diagnostic inputIoError(String message) {
        return DiagnosticBuilder.from(DiagnosticCode.INPUT_IO_ERROR)
                .withMessage(message)
                .build();
    }

    // ─── Lexing ───────────────────────────────────────────────────────────────
    public static Diagnostic lexUnexpectedCharacter(IHaveSpan span, char character) {
        return simpleWithCodeSnippet(DiagnosticCode.LEX_UNEXPECTED_CHARACTER, span, "Unexpected character: '" + character + "'");
    }

    public static Diagnostic lexUnterminatedString(IHaveSpan span) {
        return simpleWithCodeSnippet(DiagnosticCode.LEX_UNTERMINATED_STRING, span, "Unterminated string literal");
    }

    public static Diagnostic lexInvalidEscapeSequence(IHaveSpan span, String escapeSequence) {
        return simpleWithCodeSnippet(DiagnosticCode.LEX_INVALID_ESCAPE_SEQUENCE, span, "Invalid escape sequence: '" + escapeSequence + "'");
    }

    public static Diagnostic lexMalformedNumberLiteral(IHaveSpan span, String literal) {
        return simpleWithCodeSnippet(DiagnosticCode.LEX_MALFORMED_NUMBER_LITERAL, span, "Malformed number literal: '" + literal + "'");
    }

    // ─── Parsing  ────────────────────────────────────────────────────────────
    public static Diagnostic parserInvalidExpressionStatement(IHaveSpan span) {
        return simpleWithCodeSnippet(DiagnosticCode.PARSER_FORBIDDEN_EXPRESSION_STATEMENT, span, "Invalid expression statement");
    }

    public static Diagnostic parserUnexpectedToken(IHaveSpan span, String expected, String actual) {
        return simpleWithCodeSnippet(DiagnosticCode.PARSER_UNEXPECTED_TOKEN, span, "Unexpected token: expected '" + expected + "', got '" + actual + "'");
    }

    public static Diagnostic parserExpectedIdentifierAfterDoubleColon(IHaveSpan span) {
        return simpleWithCodeSnippet(DiagnosticCode.PARSER_EXPECTED_IDENTIFIER_AFTER_DOUBLE_COLON, span, "Expected identifier after '::'");
    }

    public static Diagnostic parserMissingSemicolon(IHaveSpan span) {
        return simpleWithCodeSnippet(DiagnosticCode.PARSER_MISSING_SEMICOLON, span, "Missing semicolon");
    }

    // ─── Execution  ──────────────────────────────────────────────────────────
    public static Diagnostic execError(String message) {
        return simpleWithSuffix(DiagnosticCode.EXEC_ERROR, message);
    }

    public static Diagnostic execInterrupted() {
        return simpleDefault(DiagnosticCode.EXEC_INTERRUPTED);
    }

    // ─── Semantic - Overload resolution  ──────────────────────────────────
    public static Diagnostic noMatchingOverload(IHaveSpan span, String functionName, List<LangType> argumentTypes, List<? extends CallableSymbol> candidates) {
        String argumentTypesString = argumentTypes.stream()
                .map(LangType::toString)
                .collect(java.util.stream.Collectors.joining(", "));

        boolean isCtor = functionName.equals(ConstructorSymbol.DEFAULT_NAME);

        var builder = DiagnosticBuilder.from(DiagnosticCode.SEM_NO_MATCHING_OVERLOAD)
                .withCodeSnippet(span, "No matching overload for " + (isCtor ? "constructor" : "function '" + functionName + "'") + " with argument types (" + argumentTypesString + ")");

        List<String> suggestions = candidates.stream()
                .filter((c) -> !c.isSource())
                .map((c) -> c.displayName() + " " + c.type().displayName())
                .collect(Collectors.toList());

        builder.withSuggestion("The following overloads are valid candidates:", suggestions);

        return builder.build();
    }

    public static Diagnostic ambiguousOverload(IHaveSpan span, String functionName, List<? extends Symbol> ambiguousSymbols) {
        var builder = DiagnosticBuilder.from(DiagnosticCode.SEM_AMBIGUOUS_OVERLOAD)
                .withCodeSnippet(span, "Multiple overloads of function '" + functionName + "' match the given argument types, and the compiler cannot determine which one to call.");

        String overloadList = ambiguousSymbols.stream()
                .filter((s) -> s instanceof CallableSymbol)
                .map((s) -> ((CallableSymbol) s).type().toString())
                .collect(Collectors.joining("\n"));

        builder.withHelp("The following overloads are valid candidates: \n" + overloadList);

        return builder.build();
    }

    /**
     * The member cannot be accessed because the instance being accessed is not
     * a named type.
     */
    public static Diagnostic instanceMemberAccessOnNonNamedType(IHaveSpan span, String memberName) {
        return DiagnosticBuilder.from(DiagnosticCode.SEM_INSTANCE_MEMBER_ACCESS_ON_NON_NAMED_TYPE)
                .withCodeSnippet(span, "The member '" + memberName + "' cannot be accessed because the instance being accessed is not a named type.")
                .build();
    }

    // ─── Semantic - Scope  ────────────────────────────────────────────────
    /**
     * The referenced name resolves to multiple symbols in the current scope.
     */
    public static Diagnostic ambiguousIdentifier(IHaveSpan span, String name, List<? extends Symbol> symbols) {
        var builder = DiagnosticBuilder.from(DiagnosticCode.SEM_AMBIGUOUS_IDENTIFIER)
                .withCodeSnippet(span, "The name '" + name + "' resolves to multiple symbols, and the compiler cannot determine which one you meant.");

        for (Symbol symbol : symbols) {
            if (symbol instanceof SourceSymbol sourceSymbol) {
                builder.withRelated(sourceSymbol, "One candidate is declared here:");
            } else {
                builder.withHelp("One candidate is: " + symbol.getFullName());
            }
        }

        return builder.build();
    }

    /**
     * The referenced name was not declared in any reachable scope.
     */
    public static Diagnostic undeclaredIdentifier(IHaveSpan span, String symbolName) {
        return simpleWithCodeSnippet(DiagnosticCode.SEM_UNDECLARED_IDENTIFIER, span, "Symbol '" + symbolName + "' not found in this scope");
    }

    /**
     * The referenced type name was not declared.
     */
    public static Diagnostic undeclaredType(IHaveSpan span, String typeName) {
        return simpleWithCodeSnippet(DiagnosticCode.SEM_UNDECLARED_TYPE, span, "Unknown type '" + typeName + "'");
    }

    /**
     * A name was declared more than once in the same scope.
     */
    public static Diagnostic symbolRedeclared(Symbol existing, IHaveSpan newSpan, String name) {
        return DiagnosticBuilder
                .from(DiagnosticCode.SEM_SYMBOL_REDECLARED)
                .withCodeSnippet(newSpan,
                        "This declaration of '" + name + "' conflicts with another declaration in the same scope.")
                .withRelated(existing, "The previous declaration of '" + name + "' is here.")
                .build();
    }

    /**
     * Convenience overload when the incoming symbol is available.
     */
    public static Diagnostic symbolRedeclared(Symbol existing, SourceSymbol incoming) {
        return symbolRedeclared(existing, incoming.span(), incoming.name());
    }

    /**
     * A function was declared more than once with the same parameter types.
     */
    public static Diagnostic functionRedeclared(IHaveSpan newSpan, IHaveSpan existingSpan, String name) {
        return DiagnosticBuilder
                .from(DiagnosticCode.SEM_SYMBOL_REDECLARED)
                .withCodeSnippet(newSpan, "This declaration conflicts with another overload of '" + name + "'.")
                .withRelated(existingSpan, "The conflicting declaration is here.")
                .build();
    }

    /**
     * The name resolves to a symbol that is not a function.
     */
    public static Diagnostic symbolNotAFunction(IHaveSpan span, Symbol symbol) {
        return simpleWithCodeSnippet(DiagnosticCode.SEM_SYMBOL_NOT_A_FUNCTION, span,
                capitalize(symbol.displayName()) + " is not a function");
    }

    /**
     * A non-callable expression was used in a call position.
     */
    public static Diagnostic expressionNotCallable(IHaveSpan span) {
        return simpleWithCodeSnippet(DiagnosticCode.SEM_SYMBOL_NOT_A_FUNCTION, span,
                "Expression is not callable");
    }

    /**
     * The name resolves to a symbol that is not a variable.
     */
    public static Diagnostic symbolNotAVariable(IHaveSpan span, Symbol symbol) {
        return simpleWithCodeSnippet(DiagnosticCode.SEM_SYMBOL_NOT_A_VARIABLE, span, capitalize(symbol.displayName()) + " is not a variable");
    }

    /**
     * The name resolves to a symbol that is not a type or namespace symbol.
     */
    public static Diagnostic symbolUsedLikeATypeOrNamespace(IHaveSpan span, Symbol symbol) {
        String message = capitalize(symbol.displayName()) + " is used like a type or namespace";
        return simpleWithCodeSnippet(DiagnosticCode.SEM_INVALID_OWNER_SYMBOL, span, message);
    }

    /**
     * The name resolves to a symbol that is not a type.
     */
    public static Diagnostic symbolNotAType(IHaveSpan span, Symbol symbol) {
        return simpleWithCodeSnippet(DiagnosticCode.SEM_SYMBOL_NOT_A_TYPE, span,
                capitalize(symbol.displayName()) + " is not a type");
    }

    /**
     * The name resolves to a symbol that cannot produce a value.
     */
    public static Diagnostic symbolNotAValue(IHaveSpan span, Symbol symbol) {
        return simpleWithCodeSnippet(DiagnosticCode.SEM_SYMBOL_NOT_A_VALUE, span,
                capitalize(symbol.displayName()) + " cannot be used as a value");
    }

    public static Diagnostic undeclaredPackage(IHaveSpan span, String packageName, List<String> suggestions) {
        var builder = DiagnosticBuilder.from(DiagnosticCode.SEM_UNDECLARED_PACKAGE)
                .withCodeSnippet(span, "Package '" + packageName + "' not found");

        builder.withSuggestion("Did you mean one of the following ?", suggestions);

        return builder.build();
    }

    /**
     * A variable was read on a code path where it may not have been assigned.
     *
     * <p>
     * Non-fatal: this is collected and reported separately from fatal errors,
     * so it intentionally does not have a counterpart in
     * {@link SemanticErrors}.
     */
    public static Diagnostic uninitializedVariable(IHaveSpan span, String varName) {
        String message = "Variable '" + varName + "' might not have been initialized";
        return DiagnosticBuilder
                .from(DiagnosticCode.SEM_UNINITIALIZED_VARIABLE)
                .withCodeSnippet(span, message)
                .build();
    }

    /**
     * A declaration appeared inside a block where only top-level declarations
     * are allowed.
     */
    public static Diagnostic forbiddenNestedDeclaration(IHaveSpan span, String declType) {
        return DiagnosticBuilder
                .from(DiagnosticCode.SEM_FORBIDDEN_NESTED_DECLARATION)
                .withCodeSnippet(span, declType + " declarations cannot be nested inside other statements or declarations.")
                .build();
    }

    /**
     * No entry-point function named {@code main} was found.
     */
    public static Diagnostic missingMainFunction() {
        return DiagnosticBuilder
                .from(DiagnosticCode.SEM_MISSING_MAIN_FUNCTION)
                .withHelp("Every program must have a 'main' function with signature 'def main() { ... }' "
                        + "that serves as the entry point of the program.")
                .build();
    }

    public static Diagnostic ambiguousMainFunction(List<SourceMethodSymbol> mainCandidates) {
        var builder = DiagnosticBuilder.from(DiagnosticCode.SEM_AMBIGUOUS_MAIN_FUNCTION);

        for (SourceMethodSymbol candidate : mainCandidates) {
            assert candidate instanceof SourceMethodSymbol;
            builder.withRelated(candidate, "One candidate is declared here:");
        }

        return builder.build();
    }

    // ─── Type errors ────────────────────────────────────────────────────────
    public static Diagnostic valueNotCallable(IHaveSpan span, Symbol symbol) {
        return simpleWithCodeSnippet(DiagnosticCode.SEM_VALUE_NOT_CALLABLE, span,
                capitalize(symbol.displayName()) + " is not callable");
    }

    public static Diagnostic typeNotConstructible(IHaveSpan span, TypeSymbol type) {
        return simpleWithCodeSnippet(DiagnosticCode.SEM_TYPE_NOT_CONSTRUCTIBLE, span,
                capitalize(type.displayName()) + " cannot be constructed");
    }

    /**
     * The type of an expression did not match the single expected type.
     */
    public static Diagnostic typeMismatch(IHaveSpan span, LangType expected, LangType actual) {
        return DiagnosticBuilder
                .from(DiagnosticCode.SEM_TYPE_MISMATCH)
                .withCodeSnippet(span, "Expected type '" + expected + "', got '" + actual + "'")
                .build();
    }

    /**
     * The type of an expression did not match any of the accepted types.
     */
    public static Diagnostic typeMismatch(IHaveSpan span, List<LangType> expectedTypes, LangType actual) {
        return DiagnosticBuilder
                .from(DiagnosticCode.SEM_TYPE_MISMATCH)
                .withCodeSnippet(span, "Expected one of " + formatTypeList(expectedTypes) + ", got '" + actual + "'")
                .build();
    }

    /**
     * A member access was attempted from an instance context (i.e. an
     * expression), but requires a static context.
     */
    public static Diagnostic instanceMemberAccessOnStaticContext(IHaveSpan span, Symbol accessedSymbol) {
        return DiagnosticBuilder.from(DiagnosticCode.SEM_INSTANCE_MEMBER_ACCESS_STATICALLY)
                .withCodeSnippet(span, capitalize(accessedSymbol.displayName()) + " access requires an instance of " + accessedSymbol.owner().displayName())
                .build();
    }

    /**
     * A member access was attempted from a static context (i.e. a
     * type/namespace), but requires an instance context.
     */
    public static Diagnostic staticMemberAccessOnInstanceContext(IHaveSpan span, Symbol accessedSymbol) {
        return DiagnosticBuilder.from(DiagnosticCode.SEM_STATIC_MEMBER_ACCESS_ON_INSTANCE)
                .withCodeSnippet(span, "Cannot access " + accessedSymbol.displayName() + " from an instance of " + accessedSymbol.owner().displayName())
                .build();
    }

    /**
     * A named member was not found on the given type.
     *
     * <p>
     * Covers both collection-field access and general member lookup. Pass the
     * most specific type representation available.
     */
    public static Diagnostic memberNotFound(IHaveSpan span, String memberName, NamespaceOrTypeSymbol type) {
        return simpleWithCodeSnippet(DiagnosticCode.SEM_MEMBER_NOT_FOUND, span,
                "Type '" + type.name() + "' does not have a member named '" + memberName + "'");
    }

    public static Diagnostic memberNotFound(IHaveSpan span, String memberName, LangType type) {
        return simpleWithCodeSnippet(DiagnosticCode.SEM_MEMBER_NOT_FOUND, span,
                "Type '" + type + "' does not have a member named '" + memberName + "'");
    }

    public static Diagnostic ambiguousMemberAccess(IHaveSpan span, String memberName, NamespaceOrTypeSymbol type, List<? extends Symbol> ambiguousSymbols) {
        var builder = DiagnosticBuilder.from(DiagnosticCode.SEM_AMBIGUOUS_MEMBER_ACCESS)
                .withCodeSnippet(span, "Multiple members named '" + memberName + "' found in type '" + type.name() + "', and the compiler cannot determine which one you meant.");

        var suggestions = ambiguousSymbols.stream()
                .filter((s) -> !s.isSource())
                .map((s) -> s.getFullName())
                .collect(Collectors.toList());

        builder.withSuggestion(memberName, suggestions);
        return builder.build();
    }

    /**
     * An implicit conversion between two types is not supported; an explicit
     * cast is required.
     *
     * <p>
     * Includes a help note with the suggested cast syntax.
     */
    public static Diagnostic implicitConversionUnsupported(IHaveSpan span, LangType from, LangType to) {
        return DiagnosticBuilder
                .from(DiagnosticCode.SEM_IMPLICIT_CONVERSION_UNSUPPORTED)
                .withCodeSnippet(span,
                        "Cannot implicitly convert from '" + from + "' to '" + to + "'. An explicit cast is required.")
                .withHelp("Try adding an explicit cast: '(" + to + ") " + span.span().text() + "'")
                .build();
    }

    /**
     * A field was declared with type {@code void}, which is not allowed.
     */
    public static Diagnostic voidFieldType(IHaveSpan span, String fieldName) {
        return simpleWithCodeSnippet(DiagnosticCode.SEM_VOID_FIELD_TYPE, span,
                "Field '" + fieldName + "' cannot be of type 'void'");
    }

    /**
     * A variable was declared with type {@code void}, which is not allowed.
     */
    public static Diagnostic voidVariableType(IHaveSpan span, String varName) {
        return simpleWithCodeSnippet(DiagnosticCode.SEM_VOID_VARIABLE_TYPE, span,
                "Variable '" + varName + "' cannot be of type 'void'");
    }

    // ─── Argument errors ────────────────────────────────────────────────────
    /**
     * The number of arguments did not match the single expected arity.
     */
    public static Diagnostic argumentCountMismatch(IHaveSpan span, String calleeName, int expected, int actual) {
        return simpleWithCodeSnippet(DiagnosticCode.SEM_ARGUMENT_COUNT_MISMATCH, span,
                calleeName + " expected " + expected + " argument(s), got " + actual);
    }

    /**
     * The number of arguments did not match any of the accepted arities.
     */
    public static Diagnostic argumentCountMismatch(IHaveSpan span, String calleeName, List<Integer> expectedCounts, int actual) {
        return simpleWithCodeSnippet(DiagnosticCode.SEM_ARGUMENT_COUNT_MISMATCH, span,
                calleeName + " expected one of " + formatIntList(expectedCounts) + " argument(s), got " + actual);
    }

    /**
     * A single argument's type did not match the expected parameter type.
     */
    public static Diagnostic argumentTypeMismatch(IHaveSpan span, int position, LangType expected, LangType actual) {
        return DiagnosticBuilder
                .from(DiagnosticCode.SEM_ARGUMENT_TYPE_MISMATCH)
                .withCodeSnippet(span,
                        "Argument " + (position + 1) + ": expected '" + expected + "', got '" + actual + "'")
                .build();
    }

    /**
     * A single argument's type did not match any of the accepted parameter
     * types.
     */
    public static Diagnostic argumentTypeMismatch(IHaveSpan span, int position, List<LangType> expectedTypes, LangType actual) {
        String message = "Argument " + (position + 1) + ": expected one of " + formatTypeList(expectedTypes) + ", got '" + actual + "'";
        return DiagnosticBuilder
                .from(DiagnosticCode.SEM_ARGUMENT_TYPE_MISMATCH)
                .withCodeSnippet(span, message)
                .build();
    }

    // ─── Return / control-flow errors ───────────────────────────────────────
    /**
     * A value was returned from a function declared as {@code void}.
     */
    public static Diagnostic returnValueInVoidFunctionLike(IHaveSpan span, CallableSymbol callable) {
        return simpleWithCodeSnippet(DiagnosticCode.SEM_RETURN_VALUE_IN_VOID_FUNCTION, span,
                "Cannot return a value from void " + callable.displayName());
    }

    /**
     * A non-void function has a return path that does not produce a value.
     */
    public static Diagnostic missingReturnValue(IHaveSpan span, CallableSymbol callable) {
        return simpleWithCodeSnippet(DiagnosticCode.SEM_MISSING_RETURN_VALUE, span,
                capitalize(callable.displayName()) + " must return a value");
    }

    /**
     * The type of a returned expression does not match the function's declared
     * return type.
     */
    public static Diagnostic returnTypeMismatch(IHaveSpan span, LangType expected, LangType actual) {
        return simpleWithCodeSnippet(DiagnosticCode.SEM_RETURN_TYPE_MISMATCH, span,
                "Return type mismatch: expected '" + expected + "', got '" + actual + "'");
    }

    /**
     * Not all code paths in a non-void function reach a return statement.
     */
    public static Diagnostic incompleteReturnPaths(IHaveSpan span, CallableSymbol callable) {
        return simpleWithCodeSnippet(DiagnosticCode.SEM_INCOMPLETE_RETURN_PATHS, span,
                "Not all code paths in " + callable.displayName() + " return a value");
    }

    public static Diagnostic forLoopStepMustBeAssignment(IHaveSpan span) {
        return simpleWithCodeSnippet(DiagnosticCode.SEM_FOR_LOOP_STEP_MUST_BE_ASSIGNMENT, span,
                "The step expression of a for loop must be an assignment expression (e.g. 'i++' or 'i += 1')");
    }

    /**
     * A control-flow construct ({@code if}, {@code while}, …) is missing its
     * condition expression.
     */
    public static Diagnostic missingCondition(IHaveSpan span, SyntaxNode construct) {
        String keyword = switch (construct) {
            case IfClauseSyntax ignored ->
                "if";
            case WhileStmt ignored ->
                "while";
            case ForStmt ignored ->
                "for";
            default ->
                throw new IllegalArgumentException(
                        "Unsupported construct for missing-condition diagnostic: "
                        + construct.getClass().getSimpleName());
        };
        return simpleWithCodeSnippet(DiagnosticCode.SEM_MISSING_CONDITION, span,
                "'" + keyword + "' statement requires a condition expression");
    }

    // ─── Operator errors ────────────────────────────────────────────────────
    /**
     * The left-hand side of an assignment cannot be assigned to.
     */
    public static Diagnostic invalidAssignmentTarget(IHaveSpan span) {
        return simpleWithCodeSnippet(DiagnosticCode.SEM_INVALID_ASSIGNMENT_TARGET, span,
                "Invalid assignment target");
    }

    /**
     * Assignment to a string index was attempted; strings are immutable.
     */
    public static Diagnostic stringIndexAssignment(IHaveSpan span) {
        return simpleWithCodeSnippet(DiagnosticCode.SEM_STRING_INDEX_ASSIGNMENT, span,
                "Cannot assign to a string index — strings are immutable");
    }

    /**
     * The index operator is not defined for the given base and index types.
     */
    public static Diagnostic unsupportedIndexOperator(IHaveSpan span, LangType baseType, LangType indexType) {
        return simpleWithCodeSnippet(DiagnosticCode.SEM_UNSUPPORTED_INDEX_OPERATOR, span,
                "The indexing operator is not defined for '" + baseType + "' indexed by '" + indexType + "'");
    }

    public static Diagnostic unsupportedAssignmentOperator(SyntaxToken operatorToken, LangType left, LangType right) {
        return simpleWithCodeSnippet(DiagnosticCode.SEM_UNSUPPORTED_ASSIGNMENT_OPERATOR, operatorToken,
                "Assignment operator '" + operatorToken.text() + "' is not defined for types '" + left + "' and '" + right + "'");
    }

    public static Diagnostic ambiguousAssignmentOperator(SyntaxToken operatorToken, LangType left, LangType right, List<BoundOperator.Assignment> candidateOperators) {
        return DiagnosticBuilder
                .from(DiagnosticCode.SEM_AMBIGUOUS_ASSIGNMENT_OPERATOR)
                .withCodeSnippet(operatorToken,
                        "Multiple assignment operators named '" + operatorToken.text() + "' are applicable for types '" + left + "' and '" + right + "', and the compiler cannot determine which one to use.")
                .withSuggestion("The following operators are valid candidates:", candidateOperators.stream().map(BoundOperator::getDisplayName).collect(Collectors.toList()))
                .build();
    }

    /**
     * A unary operator is not defined for the given operand type.
     */
    public static Diagnostic unsupportedUnaryOperator(SyntaxToken operatorToken, LangType operand) {
        return simpleWithCodeSnippet(DiagnosticCode.SEM_UNSUPPORTED_UNARY_OPERATOR, operatorToken,
                "Unary operator '" + operatorToken.text() + "' is not defined for type '" + operand + "'");
    }

    public static Diagnostic ambiguousUnaryOperator(SyntaxToken operatorToken, LangType operand, List<BoundOperator.Unary> candidateOperators) {
        return DiagnosticBuilder
                .from(DiagnosticCode.SEM_AMBIGUOUS_UNARY_OPERATOR)
                .withCodeSnippet(operatorToken,
                        "Multiple unary operators named '" + operatorToken.text() + "' are applicable for type '" + operand + "', and the compiler cannot determine which one to use.")
                .withSuggestion("The following operators are valid candidates:", candidateOperators.stream().map(BoundOperator::getDisplayName).collect(Collectors.toList()))
                .build();
    }

    /**
     * A binary operator is not defined for the given operand types.
     */
    public static Diagnostic unsupportedBinaryOperator(SyntaxToken operatorToken, LangType left, LangType right) {
        return simpleWithCodeSnippet(DiagnosticCode.SEM_UNSUPPORTED_BINARY_OPERATOR, operatorToken,
                "Binary operator '" + operatorToken.text() + "' is not defined for types '" + left + "' and '" + right + "'");
    }

    public static Diagnostic ambiguousBinaryOperator(SyntaxToken operatorToken, LangType left, LangType right, List<BoundOperator.Binary> candidateOperators) {
        return DiagnosticBuilder
                .from(DiagnosticCode.SEM_AMBIGUOUS_BINARY_OPERATOR)
                .withCodeSnippet(operatorToken,
                        "Multiple binary operators '" + operatorToken.text() + "' are applicable for types '" + left + "' and '" + right + "', and the compiler cannot determine which one to use.")
                .withSuggestion("The following operators are valid candidates:", candidateOperators.stream().map(BoundOperator::getDisplayName).collect(Collectors.toList()))
                .build();
    }

    // ─── Collection errors ──────────────────────────────────────────────────
    /**
     * A collection name does not begin with a capital letter.
     */
    public static Diagnostic lowercaseCollectionName(IHaveSpan span, String name) {
        return simpleWithCodeSnippet(DiagnosticCode.SEM_LOWERCASE_COLLECTION_NAME, span,
                "Collection name '" + name + "' must start with a capital letter");
    }

    /**
     * A collection name collides with a reserved type name.
     */
    public static Diagnostic reservedTypeName(IHaveSpan span, String name) {
        return simpleWithCodeSnippet(DiagnosticCode.SEM_RESERVED_TYPE_NAME, span,
                "'" + name + "' is a reserved type name and cannot be used for a collection");
    }

    /**
     * A collection's field graph contains a cycle — the collection contains
     * itself.
     */
    public static Diagnostic recursiveCollectionDeclaration(IHaveSpan span, String name) {
        return simpleWithCodeSnippet(DiagnosticCode.SEM_RECURSIVE_COLLECTION_DECLARATION, span,
                "Recursive declaration of collection '" + name
                + "': collections cannot directly or indirectly contain themselves as fields");
    }

    /**
     * A field was declared more than once with the same name in the same
     * collection.
     */
    public static Diagnostic fieldRedeclared(IHaveSpan span, String fieldName, String collectionName) {
        return simpleWithCodeSnippet(DiagnosticCode.SEM_FIELD_REDECLARED, span,
                "Field '" + fieldName + "' is already declared in collection '" + collectionName + "'");
    }

    /**
     * The target of an {@code impl} block is not a collection type.
     */
    public static Diagnostic implOnNonCollectionType(IHaveSpan span) {
        return simpleWithCodeSnippet(DiagnosticCode.SEM_IMPL_ON_NON_COLLECTION_TYPE, span,
                "The target of an impl block must be a collection type");
    }

    /**
     * An impl method declares more than one receiver (untyped) parameter.
     */
    public static Diagnostic multipleReceiverParameters(ParameterSyntax first, ParameterSyntax second, String implName) {
        return DiagnosticBuilder
                .from(DiagnosticCode.SEM_INVALID_RECEIVER_PARAMETER)
                .withCodeSnippet(second,
                        "'" + second.identifier().text()
                        + "' is a second receiver (untyped) parameter; only one is allowed.")
                .withRelated(first, "'" + first.identifier().text() + "' is the first receiver parameter.")
                .build();
    }

    /**
     * The receiver parameter is not in the first position of the parameter
     * list.
     */
    public static Diagnostic receiverParameterNotFirst(ParameterSyntax receiver, String implName) {
        return DiagnosticBuilder
                .from(DiagnosticCode.SEM_INVALID_RECEIVER_PARAMETER)
                .withCodeSnippet(receiver,
                        "'" + receiver.identifier().text()
                        + "' is a receiver parameter but is not in the first position.")
                .build();
    }

    /**
     * A receiver (untyped) parameter appeared in a plain function, where it is
     * not allowed.
     */
    public static Diagnostic receiverParameterInFunction(ParameterSyntax receiver, String functionName) {
        return DiagnosticBuilder
                .from(DiagnosticCode.SEM_INVALID_RECEIVER_PARAMETER)
                .withCodeSnippet(receiver,
                        "'" + receiver.identifier().text()
                        + "' is a receiver parameter, which is only valid in impl methods, not in '"
                        + functionName + "'.")
                .build();
    }

    /**
     * An impl method has no receiver parameter in its first position.
     */
    public static Diagnostic missingReceiverParameter(IHaveSpan span, String methodName, String implName) {
        return simpleWithCodeSnippet(DiagnosticCode.SEM_INVALID_RECEIVER_PARAMETER, span,
                "Method '" + methodName + "' in impl '" + implName
                + "' must have a receiver parameter as its first parameter");
    }

    /**
     * A method was declared more than once with the same name in the same impl.
     */
    public static Diagnostic methodRedeclared(SourceMethodSymbol incoming, MethodSymbol existing) {
        var builder = DiagnosticBuilder.from(DiagnosticCode.SEM_METHOD_REDECLARED)
                .withCodeSnippet(incoming, "This declaration conflicts with another.");

        if (existing instanceof SourceMethodSymbol existingSource) {
            builder = builder.withRelated(existingSource, "The conflicting declaration is here.");
        }

        return builder.build();
    }

    // ─── Constant errors ────────────────────────────────────────────────────
    /**
     * A constant declaration has no initializer expression.
     */
    public static Diagnostic missingVariableInitializer(IHaveSpan span, String name, String modifier) {
        return simpleWithCodeSnippet(DiagnosticCode.SEM_CONSTANT_MISSING_INITIALIZER, span,
                capitalize(modifier) + " '" + name + "' requires an initializer");
    }

    public static Diagnostic cannotInferVariableType(IHaveSpan span, String name) {
        return simpleWithCodeSnippet(DiagnosticCode.SEM_CANNOT_INFER_VARIABLE_TYPE, span,
                "Cannot infer type of variable '" + name + "' without an initializer; an explicit type annotation is required");
    }

    /**
     * An assignment target is a constant.
     */
    public static Diagnostic immutableAssignment(IHaveSpan span, ValueSymbol target) {
        return simpleWithCodeSnippet(DiagnosticCode.SEM_IMMUTABLE_ASSIGNMENT, span,
                capitalize(target.displayName()) + " cannot be assigned to");
    }

    /**
     * A constant declaration is missing its explicit base type annotation.
     */
    public static Diagnostic missingConstantBaseType(IHaveSpan span, String name) {
        return simpleWithCodeSnippet(DiagnosticCode.SEM_CONSTANT_MISSING_BASE_TYPE, span,
                "Constant '" + name + "' requires a base type");
    }

    /**
     * A constant's initializer is not a compile-time constant expression.
     */
    public static Diagnostic constantNonCompileTimeFoldableInitializer(IHaveSpan span, String name) {
        return simpleWithCodeSnippet(DiagnosticCode.SEM_CONSTANT_NON_COMPILE_TIME_FOLDABLE_INITIALIZER, span,
                "Constant '" + name + "' requires a compile-time constant expression");
    }

    /**
     * A constant was declared inside a function; only global constants are
     * allowed.
     */
    public static Diagnostic localConstantDeclaration(IHaveSpan span, String name) {
        return simpleWithCodeSnippet(DiagnosticCode.SEM_LOCAL_CONSTANT_DECLARATION, span,
                "Constant '" + name + "' cannot be declared inside a function; only global constants are allowed");
    }

    // ─── Top-level order ────────────────────────────────────────────────────
    /**
     * A top-level declaration appears in a position that violates the required
     * ordering.
     */
    public static Diagnostic invalidDeclarationOrder(IHaveSpan span, String message) {
        return DiagnosticBuilder.from(DiagnosticCode.SEM_INVALID_DECLARATION_ORDER)
                .withCodeSnippet(span, message)
                .withHelp("Top-level declarations must be ordered as follows: constants, collections, functions, global variables.")
                .build();
    }

    public static Diagnostic topLevelExecutableStatementsWithExplicitMainFunction(IHaveSpan span) {
        return DiagnosticBuilder.from(DiagnosticCode.SEM_TOP_LEVEL_EXECUTABLE_STATEMENTS_WITH_EXPLICIT_MAIN)
                .withCodeSnippet(span, "Either remove this statement or remove the explicit 'main' function declaration.")
                .build();
    }

    public static Diagnostic noTopLevelExecutableStatementsForImplicitMainFunction() {
        return DiagnosticBuilder.from(DiagnosticCode.SEM_NO_TOP_LEVEL_EXECUTABLE_STATEMENTS_FOR_IMPLICIT_MAIN)
                .withHelp("No top-level executable statements were found to serve as the body of the implicit 'main' function. Either add some top-level statements or declare an explicit 'main' function.")
                .build();
    }

    public static Diagnostic topLevelStatementsNotAllowed(List<StatementSyntax> topLevelStatements) {
        var builder = DiagnosticBuilder.from(DiagnosticCode.SEM_TOP_LEVEL_STATEMENTS_NOT_ALLOWED);
        for (StatementSyntax stmt : topLevelStatements) {
            builder = builder.withCodeSnippet(stmt, "Remove this top-level statement or move it inside a 'main' function.");
        }

        return builder.build();
    }

    // ─── Misc ───────────────────────────────────────────────────────────────
    public static Diagnostic unexpectedError(String message) {
        return DiagnosticBuilder.from(DiagnosticCode.UNEXPECTED_ERROR)
                .withSuffixMessage(message)
                .withHelp("An unexpected error occurred. This is likely a bug in the compiler. Please report this to the developers with the steps to reproduce it.")
                .build();
    }

    public static Diagnostic unexpectedError(IHaveSpan span, String message) {
        return simpleWithCodeSnippet(DiagnosticCode.UNEXPECTED_ERROR, span, message);
    }

    // ─── Private Helpers ───────────────────────────────────────────────────────────────
    private static String capitalize(String str) {
        if (str == null || str.isEmpty()) {
            return str;
        }
        return Character.toUpperCase(str.charAt(0)) + str.substring(1);
    }

    private static Diagnostic simpleDefault(DiagnosticCode code) {
        return DiagnosticBuilder.from(code).build();
    }

    private static Diagnostic simpleWithSuffix(DiagnosticCode code, String suffix) {
        return DiagnosticBuilder.from(code)
                .withSuffixMessage(suffix)
                .build();
    }

    /**
     * Creates an error diagnostic with the given message as the source label.
     */
    private static Diagnostic simpleWithCodeSnippet(DiagnosticCode code, IHaveSpan span, String message) {
        var builder = DiagnosticBuilder.from(code);
        builder.withCodeSnippet(span, message);

        return builder.build();
    }

    /**
     * Formats a list of types as {@code 'A' | 'B' | 'C'}. Returns
     * {@code <unknown>} for a null or empty list. Package-private to allow
     * direct use in tests.
     */
    static String formatTypeList(List<LangType> types) {
        if (types == null || types.isEmpty()) {
            return "<unknown>";
        }
        if (types.size() == 1) {
            return "'" + types.get(0) + "'";
        }
        return types.stream()
                .map(t -> "'" + t + "'")
                .distinct()
                .collect(Collectors.joining(" | "));
    }

    /**
     * Formats a list of integers as {@code 1 | 2 | 3} (sorted, deduplicated).
     * Returns {@code <unknown>} for a null or empty list. Package-private to
     * allow direct use in tests.
     */
    static String formatIntList(List<Integer> values) {
        if (values == null || values.isEmpty()) {
            return "<unknown>";
        }
        if (values.size() == 1) {
            return values.get(0).toString();
        }
        return values.stream()
                .distinct()
                .sorted()
                .map(Object::toString)
                .collect(Collectors.joining(" | "));
    }
}
