package com.orca.compiler.core.syntax;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nullable;

import com.orca.compiler.core.Debug;
import com.orca.compiler.core.syntax.expressions.ErrorExpressionSyntax;
import com.orca.compiler.core.syntax.members.ErrorMemberSyntax;
import com.orca.compiler.core.syntax.statements.ErrorStatementSyntax;
import com.orca.compiler.core.syntax.types.ErrorTypeSyntax;
import com.orca.compiler.core.text.IHaveSpan;
import com.orca.compiler.core.text.SourceLocation;
import com.orca.compiler.core.text.SourceSpan;
import com.orca.compiler.core.text.TextSource;

public abstract class SyntaxNode implements IHaveSpan {

    private @Nullable
    SyntaxNode parent;

    /**
     * Accept a visitor to traverse the syntax tree.
     *
     * @param visitor The visitor to accept.
     */
    public abstract void accept(SyntaxVisitor visitor);

    /**
     * Get a string representation of this syntax node.
     *
     * @return A string representation of this syntax node.
     */
    @Override
    public abstract String toString();

    /**
     * Get the text source of this syntax node.
     *
     * @return The text source of this syntax node.
     */
    public TextSource source() {
        return span().source();
    }

    /**
     * Get the text of this syntax node.
     *
     * @return The text of this syntax node.
     */
    @Override
    public String text() {
        return source().getText(span());
    }

    public boolean isError() {
        return this instanceof ErrorExpressionSyntax
                || this instanceof ErrorStatementSyntax
                || this instanceof ErrorMemberSyntax
                || this instanceof ErrorTypeSyntax
                || this instanceof MissingSyntaxToken;
    }

    public boolean hasError() {
        if (isError()) {
            return true;
        }

        for (SyntaxNode child : children()) {
            if (child.hasError()) {
                return true;
            }
        }

        return false;
    }

    /**
     * Get the text span of this syntax node.
     *
     * @return The text span of this syntax node.
     */
    @Override
    public SourceSpan span() {
        var children = children();
        if (children.isEmpty()) {
            throw new IllegalStateException("Syntax node '" + this.getClass().getName() + "' has no children to determine span. Override span() in this node type.");
        }

        SourceSpan firstSpan = children
                .stream()
                .filter((child) -> child != null)
                .min((a, b) -> a.span().compareTo(b.span()))
                .get()
                .span();

        SourceSpan lastSpan = children
                .stream()
                .filter((child) -> child.span() != null)
                .max((a, b) -> a.span().compareTo(b.span()))
                .get()
                .span();

        return SourceSpan.fromBounds(firstSpan, lastSpan);
    }

    /**
     * Get the start location of this syntax node.
     *
     * @return The start location of this syntax node.
     */
    public SourceLocation loc() {
        return span().loc();
    }

    /**
     * Get the parent node of this syntax node.
     *
     * @return The parent node of this syntax node.
     */
    public SyntaxNode parent() {
        Debug.assertTrue(!(this instanceof CompilationUnit) && parent != null, "Parent is not set for node of type '" + this.getClass().getSimpleName() + "'. This should never happen for non-root nodes.");

        return parent;
    }

    /**
     * Set the parent node of this syntax node.
     *
     * @param parent The parent node to set.
     */
    public void setParent(SyntaxNode parent) {
        Debug.requireNotNull(parent, "parent cannot be null");
        Debug.assertTrue(parent != this, "Parent cannot be the node itself");
        Debug.assertTrue(this.parent == null, "Parent is already set for this node");

        this.parent = parent;
    }

    /**
     * Get the end location of this syntax node.
     *
     * @return The end location of this syntax node.
     */
    public SourceLocation endLoc() {
        return span().endLoc();
    }

    /**
     * Get the child nodes of this syntax node. Note: This is inefficient but in
     * a educative project, it's acceptable for simplicity.
     *
     * @return A list of child nodes.
     */
    public List<SyntaxNode> children() {
        Field[] fields = this.getClass().getDeclaredFields();
        ArrayList<SyntaxNode> children = new ArrayList<>();

        for (Field field : fields) {
            if (SyntaxNode.class.isAssignableFrom(field.getType())) {
                field.setAccessible(true);
                try {
                    SyntaxNode child = (SyntaxNode) field.get(this);
                    if (child != null) {
                        children.add(child);
                    }
                } catch (IllegalAccessException e) {
                    Debug.warning("Failed to access field '" + field.getName() + "' of syntax node '" + this.getClass().getSimpleName() + "': " + e.getMessage());
                } catch (ClassCastException e) {
                    Debug.warning("Field '" + field.getName() + "' of syntax node '" + this.getClass().getSimpleName() + "' is not a SyntaxNode: " + e.getMessage());
                } catch (IllegalArgumentException e) {
                    Debug.warning("Illegal argument error while accessing field '" + field.getName() + "' of syntax node '" + this.getClass().getSimpleName() + "': " + e.getMessage());
                }
            }
        }

        if (children.isEmpty()) {
            Debug.warning("Syntax node of type '" + this.getClass().getSimpleName() + "' has no child nodes. Override children() in this node to avoid bugs.");
        }

        return children;
    }

    /**
     * Get all descendant nodes of this syntax node.
     *
     * @return A list of all descendant nodes.
     */
    public List<SyntaxNode> getAllDescendants() {
        List<SyntaxNode> descendants = new ArrayList<>();
        for (SyntaxNode child : children()) {
            descendants.add(child);
            descendants.addAll(child.getAllDescendants());
        }

        return descendants;
    }
}
