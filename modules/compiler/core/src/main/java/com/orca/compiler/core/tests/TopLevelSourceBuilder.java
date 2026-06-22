package com.orca.compiler.core.tests;

import java.util.function.Consumer;

public class TopLevelSourceBuilder extends SourceBuilder {

    public TopLevelSourceBuilder withDefaultPackage() {
        return withPackage("test");
    }

    public TopLevelSourceBuilder withPackage(String packageName) {
        builder.insert(0, String.format("package %s;\n\n", packageName));
        return this;
    }

    public TopLevelSourceBuilder withImport(String importPath) {
        appendFormat("import %s;\n", importPath);
        return this;
    }

    // --- Function declarations ---
    public TopLevelSourceBuilder appendFunctionDecl(String returnType, String name,
            Consumer<ParameterContextSourceBuilder> paramsBuilder,
            Consumer<LocalContextSourceBuilder> bodyBuilder) {
        final String format = "def %s %s(%s) {\n%s\n}\n";
        appendFormat(format, returnType, name,
                acceptAndBuild(new ParameterContextSourceBuilder(), paramsBuilder),
                acceptAndBuild(new LocalContextSourceBuilder(), bodyBuilder));
        return this;
    }

    public TopLevelSourceBuilder appendFunctionDecl(String returnType, String name,
            Consumer<LocalContextSourceBuilder> bodyBuilder) {
        return appendFunctionDecl(returnType, name, params -> {}, bodyBuilder);
    }

    public TopLevelSourceBuilder appendVoidFunctionDecl(String name,
            Consumer<ParameterContextSourceBuilder> paramsBuilder,
            Consumer<LocalContextSourceBuilder> bodyBuilder) {
        final String format = "def %s(%s) {\n%s\n}\n";
        appendFormat(format, name,
                acceptAndBuild(new ParameterContextSourceBuilder(), paramsBuilder),
                acceptAndBuild(new LocalContextSourceBuilder(), bodyBuilder));
        return this;
    }

    public TopLevelSourceBuilder appendVoidFunctionDecl(String name,
            Consumer<LocalContextSourceBuilder> bodyBuilder) {
        return appendVoidFunctionDecl(name, params -> {}, bodyBuilder);
    }

    public TopLevelSourceBuilder appendMainFunction(Consumer<LocalContextSourceBuilder> bodyBuilder) {
        return appendVoidFunctionDecl("main", bodyBuilder);
    }

    // --- Collection declarations ---
    public TopLevelSourceBuilder appendCollectionDecl(String name,
            Consumer<CollectionBodyBuilder> bodyBuilder) {
        final String format = "coll %s {\n%s\n}\n";
        appendFormat(format, name, acceptAndBuild(new CollectionBodyBuilder(), bodyBuilder));
        return this;
    }

    // --- Impl declarations ---
    public TopLevelSourceBuilder appendImplDecl(String collectionName,
            Consumer<ImplBodyBuilder> bodyBuilder) {
        final String format = "impl %s {\n%s\n}\n";
        appendFormat(format, collectionName, acceptAndBuild(new ImplBodyBuilder(), bodyBuilder));
        return this;
    }

    // =========================================================================
    // Inner builders
    // =========================================================================

    public static final class CollectionBodyBuilder extends SourceBuilder {

        public CollectionBodyBuilder appendField(String type, String name) {
            appendFormat("%s %s;\n", type, name);
            return this;
        }
    }

    public static final class ImplBodyBuilder extends SourceBuilder {

        public ImplBodyBuilder appendMethod(String returnType, String name,
                Consumer<ParameterContextSourceBuilder> paramsBuilder,
                Consumer<LocalContextSourceBuilder> bodyBuilder) {
            final String format = "def %s %s(%s) {\n%s\n}\n";
            appendFormat(format, returnType, name,
                    acceptAndBuild(new ParameterContextSourceBuilder(), paramsBuilder),
                    acceptAndBuild(new LocalContextSourceBuilder(), bodyBuilder));
            return this;
        }

        public ImplBodyBuilder appendMethod(String returnType, String name,
                Consumer<LocalContextSourceBuilder> bodyBuilder) {
            return appendMethod(returnType, name, params -> {}, bodyBuilder);
        }

        public ImplBodyBuilder appendVoidMethod(String name,
                Consumer<ParameterContextSourceBuilder> paramsBuilder,
                Consumer<LocalContextSourceBuilder> bodyBuilder) {
            final String format = "def %s(%s) {\n%s\n}\n";
            appendFormat(format, name,
                    acceptAndBuild(new ParameterContextSourceBuilder(), paramsBuilder),
                    acceptAndBuild(new LocalContextSourceBuilder(), bodyBuilder));
            return this;
        }

        public ImplBodyBuilder appendVoidMethod(String name,
                Consumer<LocalContextSourceBuilder> bodyBuilder) {
            return appendVoidMethod(name, params -> {}, bodyBuilder);
        }
    }

    public static final class ParameterContextSourceBuilder extends SourceBuilder {

        public ParameterContextSourceBuilder appendSelfParameter() {
            if (builder.length() > 0) {
                builder.append(", ");
            }
            builder.append("self");
            return this;
        }

        public ParameterContextSourceBuilder appendParameter(String name, String type) {
            if (builder.length() > 0) {
                builder.append(", ");
            }
            appendFormat("%s %s", type, name);
            return this;
        }
    }

    public static final class ArgumentContextSourceBuilder extends SourceBuilder {

        public ArgumentContextSourceBuilder appendArgument(String argument) {
            if (builder.length() > 0) {
                builder.append(", ");
            }
            builder.append(argument);
            return this;
        }
    }

    public static final class LocalContextSourceBuilder extends SourceBuilder {

        public LocalContextSourceBuilder appendStatement(String statement) {
            appendFormat("%s;\n", statement);
            return this;
        }

        public LocalContextSourceBuilder appendAssignment(String target, String expression) {
            return appendStatement(target + " = " + expression);
        }

        public LocalContextSourceBuilder appendInvocation(String name,
                Consumer<ArgumentContextSourceBuilder> argsBuilder) {
            appendStatement(name + "(" + acceptAndBuild(new ArgumentContextSourceBuilder(), argsBuilder) + ")");
            return this;
        }

        public LocalContextSourceBuilder appendPrintln(String value) {
            return appendInvocation("std::io::println", args -> args.appendArgument(value));
        }

        public LocalContextSourceBuilder appendReturn() {
            return appendStatement("return");
        }

        public LocalContextSourceBuilder appendReturn(String expression) {
            return appendStatement("return " + expression);
        }

        public LocalContextSourceBuilder appendIf(String condition,
                Consumer<LocalContextSourceBuilder> bodyBuilder) {
            final String format = "if (%s) {\n%s\n}\n";
            appendFormat(format, condition, acceptAndBuild(new LocalContextSourceBuilder(), bodyBuilder));
            return this;
        }

        public LocalContextSourceBuilder appendElseIf(String condition,
                Consumer<LocalContextSourceBuilder> bodyBuilder) {
            final String format = "else if (%s) {\n%s\n}\n";
            appendFormat(format, condition, acceptAndBuild(new LocalContextSourceBuilder(), bodyBuilder));
            return this;
        }

        public LocalContextSourceBuilder appendElse(Consumer<LocalContextSourceBuilder> bodyBuilder) {
            final String format = "else {\n%s\n}\n";
            appendFormat(format, acceptAndBuild(new LocalContextSourceBuilder(), bodyBuilder));
            return this;
        }

        public LocalContextSourceBuilder appendWhile(String condition,
                Consumer<LocalContextSourceBuilder> bodyBuilder) {
            final String format = "while (%s) {\n%s\n}\n";
            appendFormat(format, condition, acceptAndBuild(new LocalContextSourceBuilder(), bodyBuilder));
            return this;
        }

        public LocalContextSourceBuilder appendFor(String initializer, String condition, String step,
                Consumer<LocalContextSourceBuilder> bodyBuilder) {
            final String format = "for (%s; %s; %s) {\n%s\n}\n";
            appendFormat(format, initializer, condition, step,
                    acceptAndBuild(new LocalContextSourceBuilder(), bodyBuilder));
            return this;
        }
    }
}
