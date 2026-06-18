package com.orca.compiler.core.syntax;

import java.util.List;
import java.util.Optional;

import com.orca.compiler.core.syntax.members.MemberSyntax;
import com.orca.compiler.core.syntax.nodes.ImportSyntax;
import com.orca.compiler.core.syntax.nodes.PackageDirectiveSyntax;
import com.orca.compiler.core.text.SourceSpan;

public class CompilationUnit extends SyntaxNode {

    private final List<ImportSyntax> imports;
    private final Optional<PackageDirectiveSyntax> packageDirectiveSyntax;

    private final List<StatementSyntax> topLevelStatements;
    private final List<MemberSyntax> members;

    public CompilationUnit(Optional<PackageDirectiveSyntax> packageDirectiveSyntax, List<ImportSyntax> imports, List<StatementSyntax> topLevelStatements, List<MemberSyntax> members) {
        this.packageDirectiveSyntax = packageDirectiveSyntax;
        this.imports = imports;
        this.topLevelStatements = topLevelStatements;
        this.members = members;
    }

    @Override
    public SourceSpan span() {
        if (children().isEmpty()) {
            return new SourceSpan(source(), 0, 0);
        }

        return super.span();
    }

    @Override
    public void accept(SyntaxVisitor visitor) {
        visitor.visitCompilationUnit(this);
    }

    /**
     * Gets the package directive of this compilation unit, if any.
     *
     * @return The package directive of this compilation unit, if any.
     */
    public Optional<PackageDirectiveSyntax> packageDirectiveSyntax() {
        return packageDirectiveSyntax;
    }

    /**
     * Gets the list of import declarations in this compilation unit.
     *
     * @return The list of import declarations in this compilation unit.
     */
    public List<ImportSyntax> imports() {
        return imports;
    }

    /**
     * Gets the list of top-level statements declared in this compilation unit.
     *
     * @return The list of top-level statements declared in this compilation
     * unit.
     */
    public List<StatementSyntax> topLevelStatements() {
        return topLevelStatements;
    }

    /**
     * Gets the list of members declared in this compilation unit.
     *
     * @return The list of members declared in this compilation unit.
     */
    public List<MemberSyntax> members() {
        return members;
    }

    @Override
    public List<SyntaxNode> children() {
        var list = new java.util.ArrayList<SyntaxNode>();
        if (packageDirectiveSyntax.isPresent()) {
            list.add(packageDirectiveSyntax.get());
        }

        list.addAll(imports);
        list.addAll(topLevelStatements);
        list.addAll(members);

        return list;
    }

    @Override
    public String toString() {
        return "CompilationUnit";
    }
}
