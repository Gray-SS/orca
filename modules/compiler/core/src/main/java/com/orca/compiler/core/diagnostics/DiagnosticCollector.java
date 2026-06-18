package com.orca.compiler.core.diagnostics;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import com.orca.compiler.core.diagnostics.attachments.DiagnosticAttachment;
import com.orca.compiler.core.lexer.TokenKind;
import com.orca.compiler.core.symbols.Symbol;
import com.orca.compiler.core.text.IHaveSpan;
import com.orca.compiler.core.typesystem.LangType;

/**
 * Collecte centralisée des diagnostics (erreurs, avertissements, infos).
 * Remplace les méthodes statiques de reporting dans Compiler.
 */
public class DiagnosticCollector implements Iterable<Diagnostic> {

    private final List<Diagnostic> diagnostics = new ArrayList<>();

    public List<Diagnostic> getErrors() {
        return diagnostics.stream()
                .filter(d -> d.severity() == DiagnosticSeverity.ERROR)
                .toList();
    }

    public Diagnostic getFirstError() {
        return diagnostics.stream()
                .filter(d -> d.severity() == DiagnosticSeverity.ERROR)
                .findFirst()
                .orElse(null);
    }

    /**
     * Ajoute un diagnostic.
     */
    public void report(Diagnostic diagnostic) {
        diagnostics.add(diagnostic);
    }

    /**
     * Ajoute un diagnostic avec message, location et severity.
     */
    public void report(DiagnosticCode code, String message, DiagnosticSeverity severity, List<DiagnosticAttachment> attachments) {
        diagnostics.add(new Diagnostic(code, message, severity, attachments));
    }

    public void reportTypeError(DiagnosticCode code, IHaveSpan span, String message) {
        reportError(code, span, message);
    }

    public void reportCollectionError(DiagnosticCode code, IHaveSpan span, String message) {
        reportError(code, span, message);
    }

    public void reportOperatorError(DiagnosticCode code, IHaveSpan span, String message) {
        reportError(code, span, message);
    }

    public void reportArgumentError(DiagnosticCode code, IHaveSpan span, String message) {
        reportError(code, span, message);
    }

    public void reportMissingConditionError(DiagnosticCode code, IHaveSpan span, String message) {
        reportError(code, span, message);
    }

    public void reportReturnError(DiagnosticCode code, IHaveSpan span, String message) {
        reportError(code, span, message);
    }

    public void reportScopeError(DiagnosticCode code, IHaveSpan span, String message) {
        reportError(code, span, message);
    }

    public void reportUnexpectedCharacter(IHaveSpan span, char ch) {
        reportError(DiagnosticCode.LEX_UNEXPECTED_CHARACTER, span, "Unexpected character: '" + ch + "'");
    }

    public void reportUnexpectedCharacterAfter(IHaveSpan span, char ch, char after) {
        reportError(DiagnosticCode.LEX_UNEXPECTED_CHARACTER, span, "Unexpected character: '" + ch + "' after '" + after + "'");
    }

    public void reportUnterminatedStringLiteral(IHaveSpan span) {
        reportError(DiagnosticCode.LEX_UNTERMINATED_STRING, span, "Unterminated string literal");
    }

    public void reportInvalidEscapeSequence(IHaveSpan span, String sequence) {
        reportError(DiagnosticCode.LEX_INVALID_ESCAPE_SEQUENCE, span, "Invalid escape sequence: '" + sequence + "'");
    }

    public void reportInvalidFloatLiteral(IHaveSpan span, String literal) {
        reportError(DiagnosticCode.LEX_MALFORMED_NUMBER_LITERAL, span, "Invalid float literal: '" + literal + "'");
    }

    public void reportTypeMismatch(IHaveSpan span, LangType expected, LangType actual) {
        reportTypeError(DiagnosticCode.SEM_TYPE_MISMATCH, span, "Type mismatch: expected '" + expected.toString() + "', got '" + actual.toString() + "'");
    }

    public void reportArgumentTypeMismatch(IHaveSpan span, int argumentPosition, LangType expected, LangType actual) {
        reportArgumentError(DiagnosticCode.SEM_ARGUMENT_TYPE_MISMATCH, span, "Argument " + argumentPosition + " type mismatch: expected '" + expected.toString() + "', got '" + actual.toString() + "'");
    }

    public void reportFieldAccessOnNonCollection(IHaveSpan span, LangType actual) {
        reportTypeError(DiagnosticCode.SEM_MEMBER_ACCESS_ON_NON_NAMED_TYPE, span, "Field access requires a collection type, got '" + actual.toString() + "'");
    }

    public void reportArityMismatch(IHaveSpan span, String arityName, int expected, int actual) {
        reportArgumentError(DiagnosticCode.SEM_ARGUMENT_COUNT_MISMATCH, span, arityName + " expected " + expected + " arguments, got " + actual);
    }

    public void reportSymbolNotFound(IHaveSpan span, String symbolName) {
        reportScopeError(DiagnosticCode.SEM_UNDECLARED_IDENTIFIER, span, "Symbol '" + symbolName + "' not found in this scope");
    }

    public void reportSymbolNotAFunction(IHaveSpan span, Symbol symbol) {
        reportScopeError(DiagnosticCode.SEM_SYMBOL_NOT_A_FUNCTION, span, "Symbol '" + symbol.name() + "' is not a function");
    }

    public void reportSymbolNotAVariable(IHaveSpan span, Symbol symbol) {
        reportScopeError(DiagnosticCode.SEM_SYMBOL_NOT_A_VARIABLE, span, "Symbol '" + symbol.name() + "' is not a variable");
    }

    public void reportSymbolAlreadyDeclared(IHaveSpan span, String symbolName) {
        reportScopeError(DiagnosticCode.SEM_SYMBOL_REDECLARED, span, "Symbol '" + symbolName + "' is already declared in this scope");
    }

    public void reportRequiresExplicitCast(IHaveSpan span, LangType fromType, LangType toType) {
        reportTypeError(DiagnosticCode.SEM_IMPLICIT_CONVERSION_UNSUPPORTED, span, "Cannot implicitly convert from '" + fromType.toString() + "' to '" + toType.toString() + "'. An explicit cast is required.");
    }

    public void reportCollectionNameMustStartWithCapital(IHaveSpan span, String collectionName) {
        reportCollectionError(DiagnosticCode.SEM_LOWERCASE_COLLECTION_NAME, span, "Collection name '" + collectionName + "' must start with a capital letter");
    }

    public void reportCollectionNameCannotBeReserved(IHaveSpan span, String collectionName) {
        reportCollectionError(DiagnosticCode.SEM_RESERVED_TYPE_NAME, span, "Collection name '" + collectionName + "' cannot be a reserved keyword");
    }

    public void reportRecursiveCollectionDeclaration(IHaveSpan span, String collectionName) {
        reportCollectionError(DiagnosticCode.SEM_RECURSIVE_COLLECTION_DECLARATION, span, "Recursive declaration of collection '" + collectionName + "' detected. Collections cannot directly or indirectly contain themselves as fields.");
    }

    public void reportDuplicateFieldName(IHaveSpan span, String fieldName, String collectionName) {
        reportCollectionError(DiagnosticCode.SEM_FIELD_REDECLARED, span, "Duplicate field name '" + fieldName + "' in collection '" + collectionName + "'");
    }

    public void reportInvalidTopLevelDeclOrder(IHaveSpan span, String message) {
        reportError(DiagnosticCode.SEM_INVALID_DECLARATION_ORDER, span, message);
    }

    public void reportNestedDeclarationForbidden(IHaveSpan span, String declType) {
        reportError(DiagnosticCode.SEM_FORBIDDEN_NESTED_DECLARATION, span, declType + " declarations are not allowed inside other declarations");
    }

    public void reportConstantRequiresInitializer(IHaveSpan span, String constName) {
        reportError(DiagnosticCode.SEM_CONSTANT_MISSING_INITIALIZER, span, "Constant '" + constName + "' requires an initializer");
    }

    public void reportConstantCannotBeAssigned(IHaveSpan span, String constName) {
        reportError(DiagnosticCode.SEM_IMMUTABLE_ASSIGNMENT, span, "Constant '" + constName + "' cannot be assigned to");
    }

    public void reportConstantRequiresBaseType(IHaveSpan span, String constName) {
        reportError(DiagnosticCode.SEM_CONSTANT_MISSING_BASE_TYPE, span, "Constant '" + constName + "' requires a base type");
    }

    public void reportConstantRequiresConstexpr(IHaveSpan span, String constName) {
        reportError(DiagnosticCode.SEM_CONSTANT_NON_COMPILE_TIME_FOLDABLE_INITIALIZER, span, "Constant '" + constName + "' requires a constexpr");
    }

    public void reportConstantCannotBeLocal(IHaveSpan span, String constName) {
        reportError(DiagnosticCode.SEM_LOCAL_CONSTANT_DECLARATION, span, "Constant '" + constName + "' cannot be declared inside a function. Only global constants are allowed.");
    }

    public void reportVariableTypeCannotBeVoid(IHaveSpan span, String varName) {
        reportError(DiagnosticCode.SEM_VOID_VARIABLE_TYPE, span, "Variable '" + varName + "' cannot be of type VOID");
    }

    public void reportAllPathsMustReturn(IHaveSpan span, String functionName) {
        reportError(DiagnosticCode.SEM_INCOMPLETE_RETURN_PATHS, span, "Not all code paths in function '" + functionName + "' return a value");
    }

    public void reportMissingCondition(IHaveSpan span, String construct) {
        reportMissingConditionError(DiagnosticCode.SEM_MISSING_CONDITION, span, construct + " statements require a condition expression");
    }

    public void reportMustReturnValue(IHaveSpan span, String functionName) {
        reportError(DiagnosticCode.SEM_MISSING_RETURN_VALUE, span, "Function '" + functionName + "' must return a value");
    }

    public void reportCannotReturnValue(IHaveSpan span, String functionName) {
        reportError(DiagnosticCode.SEM_RETURN_VALUE_IN_VOID_FUNCTION, span, "Cannot return a value from VOID function '" + functionName + "'");
    }

    public void reportCannotAssignToStringIndex(IHaveSpan span) {
        reportOperatorError(DiagnosticCode.SEM_STRING_INDEX_ASSIGNMENT, span, "Cannot assign to a string indexer. Strings are immutable.");
    }

    public void reportInvalidAssignmentTarget(IHaveSpan span) {
        reportOperatorError(DiagnosticCode.SEM_INVALID_ASSIGNMENT_TARGET, span, "Invalid assignment target");
    }

    public void reportUndefinedIndexOperator(IHaveSpan span, LangType baseType, LangType indexType) {
        reportOperatorError(DiagnosticCode.SEM_UNSUPPORTED_INDEX_OPERATOR, span, "Indexing operator is not defined for type '" + baseType.toString() + "' with index type '" + indexType.toString() + "'");
    }

    public void reportUndefinedUnaryOperator(IHaveSpan span, String operatorText, LangType operandType) {
        reportOperatorError(DiagnosticCode.SEM_UNSUPPORTED_UNARY_OPERATOR, span, "Operator '" + operatorText + "' is not defined for type '" + operandType.toString() + "'");
    }

    public void reportUndefinedBinaryOperator(IHaveSpan span, String operatorText, LangType leftOperandType, LangType rightOperandType) {
        reportOperatorError(DiagnosticCode.SEM_UNSUPPORTED_BINARY_OPERATOR, span, "Operator '" + operatorText + "' is not defined for types '" + leftOperandType.toString() + "' and '" + rightOperandType.toString() + "'");
    }

    public void reportUseOfUninitializedVariable(IHaveSpan span, String varName) {
        reportError(
                DiagnosticCode.SEM_UNINITIALIZED_VARIABLE,
                span,
                "Variable '" + varName + "' might not have been initialized before use"
        );
    }

    public void reportUnexpectedError(IHaveSpan span, String message) {
        reportError(DiagnosticCode.UNEXPECTED_ERROR, span, message);
    }

    public void reportUnexpectedToken(IHaveSpan span, String expected, TokenKind actual) {
        reportError(DiagnosticCode.PARSER_UNEXPECTED_TOKEN, span, "Expected '" + expected + "', but got '" + actual + "'");
    }

    public void reportError(DiagnosticCode code, IHaveSpan span, String message) {
        diagnostics.add(
                DiagnosticBuilder.from(code)
                        .withCodeSnippet(span, message)
                        .build()
        );
    }

    /**
     * Vérifie s'il y a un diagnostic avec le code spécifié.
     */
    public boolean hasDiagnostic(DiagnosticCode code) {
        return diagnostics.stream().anyMatch(d -> d.code() == code);
    }

    public int countErrors() {
        return (int) diagnostics.stream().filter(d -> d.severity() == DiagnosticSeverity.ERROR).count();
    }

    public int countWarnings() {
        return (int) diagnostics.stream().filter(d -> d.severity() == DiagnosticSeverity.WARNING).count();
    }

    /**
     * Retourne la liste des diagnostics.
     */
    public List<Diagnostic> diagnostics() {
        return Collections.unmodifiableList(diagnostics);
    }

    /**
     * Vérifie s'il y a des erreurs.
     */
    public boolean hasErrors() {
        return diagnostics.stream()
                .anyMatch(d -> d.severity() == DiagnosticSeverity.ERROR);
    }

    /**
     * Efface tous les diagnostics. Utile pour les tests.
     */
    public void clear() {
        diagnostics.clear();
    }

    /**
     * Retourne le nombre de diagnostics.
     */
    public int count() {
        return diagnostics.size();
    }

    @Override
    public Iterator<Diagnostic> iterator() {
        return diagnostics.iterator();
    }
}
