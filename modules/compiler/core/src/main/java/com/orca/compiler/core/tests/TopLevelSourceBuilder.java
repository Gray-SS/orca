package com.orca.compiler.core.tests;

import java.util.function.Consumer;

public class TopLevelSourceBuilder extends SourceBuilder {

    @Override
    public TopLevelSourceBuilder declareLetVariable(String name, String initializer, String type) {
        super.declareLetVariable(name, initializer, type);
        return this;
    }

    @Override
    public TopLevelSourceBuilder declareLetVariable(String name, String initializer) {
        super.declareLetVariable(name, initializer);
        return this;
    }

    @Override
    public TopLevelSourceBuilder declareVarVariable(String name, String initializer, String type) {
        super.declareVarVariable(name, initializer, type);
        return this;
    }

    @Override
    public TopLevelSourceBuilder declareVarVariable(String name, String initializer) {
        super.declareVarVariable(name, initializer);
        return this;
    }

    @Override
    public TopLevelSourceBuilder declareConstVariable(String name, String initializer, String type) {
        super.declareConstVariable(name, initializer, type);
        return this;
    }

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
    public TopLevelSourceBuilder withFunction(String returnType, String name,
            Consumer<ParameterContextSourceBuilder> paramsBuilder,
            Consumer<LocalContextSourceBuilder> bodyBuilder) {
        final String format = "def %s(%s): %s {\n%s\n}\n";
        appendFormat(format, name,
                acceptAndBuild(new ParameterContextSourceBuilder(), paramsBuilder),
                returnType,
                acceptAndBuild(new LocalContextSourceBuilder(), bodyBuilder));
        return this;
    }

    public TopLevelSourceBuilder withFunction(String returnType, String name,
            Consumer<LocalContextSourceBuilder> bodyBuilder) {
        return withFunction(returnType, name, params -> {
        }, bodyBuilder);
    }

    public TopLevelSourceBuilder withVoidFunction(String name,
            Consumer<ParameterContextSourceBuilder> paramsBuilder,
            Consumer<LocalContextSourceBuilder> bodyBuilder) {
        final String format = "def %s(%s) {\n%s\n}\n";
        appendFormat(format, name,
                acceptAndBuild(new ParameterContextSourceBuilder(), paramsBuilder),
                acceptAndBuild(new LocalContextSourceBuilder(), bodyBuilder));
        return this;
    }

    public TopLevelSourceBuilder withVoidFunction(String name,
            Consumer<LocalContextSourceBuilder> bodyBuilder) {
        return withVoidFunction(name, params -> {
        }, bodyBuilder);
    }

    public TopLevelSourceBuilder withMainFunction(Consumer<LocalContextSourceBuilder> bodyBuilder) {
        return withVoidFunction("main", bodyBuilder);
    }

    // --- Collection declarations ---
    public TopLevelSourceBuilder withCollection(String name,
            Consumer<CollectionBodyBuilder> bodyBuilder) {
        final String format = "coll %s {\n%s\n}\n";
        appendFormat(format, name, acceptAndBuild(new CollectionBodyBuilder(), bodyBuilder));
        return this;
    }

    // --- Impl declarations ---
    public TopLevelSourceBuilder withImpl(String typeName, Consumer<ImplBodyBuilder> bodyBuilder) {
        final String format = "impl %s {\n%s\n}\n";
        appendFormat(format, typeName, acceptAndBuild(new ImplBodyBuilder(), bodyBuilder));
        return this;
    }

    // =========================================================================
    // Inner builders
    // =========================================================================
    public static final class CollectionBodyBuilder extends SourceBuilder {

        public CollectionBodyBuilder withField(String type, String name) {
            appendFormat("%s %s;\n", type, name);
            return this;
        }
    }

    public static final class ImplBodyBuilder extends SourceBuilder {

        public ImplBodyBuilder withMethod(String returnType, String name,
                Consumer<ParameterContextSourceBuilder> paramsBuilder,
                Consumer<LocalContextSourceBuilder> bodyBuilder) {
            final String format = "def %s(%s): %s {\n%s\n}\n";
            appendFormat(format, name,
                    acceptAndBuild(new ParameterContextSourceBuilder(), paramsBuilder),
                    returnType,
                    acceptAndBuild(new LocalContextSourceBuilder(), bodyBuilder));
            return this;
        }

        public ImplBodyBuilder withMethod(String returnType, String name,
                Consumer<LocalContextSourceBuilder> bodyBuilder) {
            return withMethod(returnType, name, params -> {
            }, bodyBuilder);
        }

        public ImplBodyBuilder withVoidMethod(String name,
                Consumer<ParameterContextSourceBuilder> paramsBuilder,
                Consumer<LocalContextSourceBuilder> bodyBuilder) {
            final String format = "def %s(%s) {\n%s\n}\n";
            appendFormat(format, name,
                    acceptAndBuild(new ParameterContextSourceBuilder(), paramsBuilder),
                    acceptAndBuild(new LocalContextSourceBuilder(), bodyBuilder));
            return this;
        }

        public ImplBodyBuilder withVoidMethod(String name,
                Consumer<LocalContextSourceBuilder> bodyBuilder) {
            return withVoidMethod(name, params -> {
            }, bodyBuilder);
        }
    }

    public static final class ParameterContextSourceBuilder extends SourceBuilder {

        public ParameterContextSourceBuilder withSelfParameter() {
            if (builder.length() > 0) {
                builder.append(", ");
            }
            builder.append("self");
            return this;
        }

        public ParameterContextSourceBuilder withParameter(String name, String type) {
            if (builder.length() > 0) {
                builder.append(", ");
            }
            appendFormat("%s: %s", name, type);
            return this;
        }
    }

    public static final class LocalContextSourceBuilder extends SourceBuilder {

        public LocalContextSourceBuilder withStatement(String statement) {
            appendFormat("%s;\n", statement);
            return this;
        }

        public LocalContextSourceBuilder withAssignment(String target, String expression) {
            return withStatement(target + " = " + expression);
        }

        public LocalContextSourceBuilder withPrint(String value) {
            return withStatement("std::io::print(" + value + ")");
        }

        public LocalContextSourceBuilder withPrintln() {
            return withStatement("std::io::println()");
        }

        public LocalContextSourceBuilder withPrintln(String value) {
            return withStatement("std::io::println(" + value + ")");
        }

        public LocalContextSourceBuilder withReturn() {
            return withStatement("return");
        }

        public LocalContextSourceBuilder withReturn(String expression) {
            return withStatement("return " + expression);
        }

        public LocalContextSourceBuilder withIf(String condition,
                Consumer<LocalContextSourceBuilder> bodyBuilder) {
            final String format = "if (%s) {\n%s\n}\n";
            appendFormat(format, condition, acceptAndBuild(new LocalContextSourceBuilder(), bodyBuilder));
            return this;
        }

        public LocalContextSourceBuilder withElseIf(String condition,
                Consumer<LocalContextSourceBuilder> bodyBuilder) {
            final String format = "else if (%s) {\n%s\n}\n";
            appendFormat(format, condition, acceptAndBuild(new LocalContextSourceBuilder(), bodyBuilder));
            return this;
        }

        public LocalContextSourceBuilder withElse(Consumer<LocalContextSourceBuilder> bodyBuilder) {
            final String format = "else {\n%s\n}\n";
            appendFormat(format, acceptAndBuild(new LocalContextSourceBuilder(), bodyBuilder));
            return this;
        }

        public LocalContextSourceBuilder withWhile(String condition,
                Consumer<LocalContextSourceBuilder> bodyBuilder) {
            final String format = "while (%s) {\n%s\n}\n";
            appendFormat(format, condition, acceptAndBuild(new LocalContextSourceBuilder(), bodyBuilder));
            return this;
        }

        public LocalContextSourceBuilder withFor(String initializer, String condition, String step,
                Consumer<LocalContextSourceBuilder> bodyBuilder) {
            final String format = "for (%s; %s; %s) {\n%s\n}\n";
            appendFormat(format, initializer, condition, step,
                    acceptAndBuild(new LocalContextSourceBuilder(), bodyBuilder));
            return this;
        }
    }
}
