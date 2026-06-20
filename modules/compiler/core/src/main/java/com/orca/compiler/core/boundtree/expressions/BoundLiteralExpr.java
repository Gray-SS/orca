package com.orca.compiler.core.boundtree.expressions;

import com.orca.compiler.core.boundtree.BoundExpression;
import com.orca.compiler.core.boundtree.BoundNodeKind;
import com.orca.compiler.core.boundtree.BoundVisitor;
import com.orca.compiler.core.boundtree.constants.BoundBoolConstant;
import com.orca.compiler.core.boundtree.constants.BoundConstant;
import com.orca.compiler.core.boundtree.constants.BoundFloatConstant;
import com.orca.compiler.core.boundtree.constants.BoundIntConstant;
import com.orca.compiler.core.boundtree.constants.BoundStringConstant;
import com.orca.compiler.core.lexer.TokenKind;
import com.orca.compiler.core.typesystem.LangType;

public final class BoundLiteralExpr extends BoundExpression {

    private final BoundConstant constant;

    public BoundLiteralExpr(BoundConstant constant) {
        this.constant = constant;
    }

    @Override
    public BoundConstant getConstant() {
        return constant;
    }

    public BoundBoolConstant getConstantAsBool() {
        if (constant instanceof BoundBoolConstant boolConstant) {
            return boolConstant;
        }

        throw new IllegalStateException("Expected a boolean constant, but got: " + constant.type());
    }

    @Override
    public BoundNodeKind kind() {
        return BoundNodeKind.LITERAL_EXPR;
    }

    @Override
    public <R> R accept(BoundVisitor<R> visitor) {
        return visitor.visitLiteralExpr(this);
    }

    @Override
    public LangType type() {
        return constant.type();
    }

    @Override
    public boolean isCompileTimeFoldable() {
        return true;
    }

    /**
     * Binds a literal token to a corresponding BoundLiteralexpr instance based
     * on the token kind and text.
     *
     * @param kind The kind of the token (e.g., IntegerLiteral, FloatLiteral,
     * BoolLiteral, StringLiteral).
     * @param text The text of the token, which is used to parse the value of
     * the constant.
     * @return A new BoundLiteralExpr instance that corresponds to the given
     * token kind and text.
     * @throws IllegalArgumentException if the token kind is not supported or if
     * the text cannot be parsed into a valid constant value.
     */
    public static BoundLiteralExpr bind(TokenKind kind, String text) {
        BoundConstant constant = switch (kind) {
            case IntegerLiteral -> {
                try {
                    yield new BoundIntConstant(Integer.parseInt(text));
                } catch (NumberFormatException e) {
                    throw new IllegalArgumentException("Invalid integer literal: " + text, e);
                }
            }
            case FloatLiteral -> {
                try {
                    yield new BoundFloatConstant(Float.parseFloat(text));
                } catch (NumberFormatException e) {
                    throw new IllegalArgumentException("Invalid float literal: " + text, e);
                }
            }
            case BoolLiteral -> {
                if (text.equals("true")) {
                    yield new BoundBoolConstant(true);
                } else if (text.equals("false")) {
                    yield new BoundBoolConstant(false);
                } else {
                    throw new IllegalArgumentException("Invalid boolean literal: " + text);
                }
            }
            case StringLiteral -> {
                if (text.startsWith("\"") && text.endsWith("\"")) {
                    yield new BoundStringConstant(text.substring(1, text.length() - 1));
                } else {
                    throw new IllegalArgumentException("Invalid string literal: " + text);
                }
            }
            default ->
                throw new IllegalStateException("Unsupported literal token kind: " + kind);
        };

        return new BoundLiteralExpr(constant);
    }
}
