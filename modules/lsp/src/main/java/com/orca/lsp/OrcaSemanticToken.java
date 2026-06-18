package com.orca.lsp;

import java.util.EnumSet;
import java.util.List;

public final class OrcaSemanticToken {

    private final Type type;
    private final EnumSet<Modifier> modifiers;
    private final int line;
    private final int startChar;
    private final int length;

    public OrcaSemanticToken(Type type, EnumSet<Modifier> modifiers, int line, int startChar, int length) {
        this.type = type;
        this.modifiers = modifiers;
        this.line = line;
        this.startChar = startChar;
        this.length = length;
    }

    public Type getType() {
        return type;
    }

    public void addModifier(Modifier modifier) {
        modifiers.add(modifier);
    }

    public EnumSet<Modifier> getModifiers() {
        return modifiers;
    }

    public int getLine() {
        return line;
    }

    public int getStartChar() {
        return startChar;
    }

    public int getLength() {
        return length;
    }

    public void appendTo(List<Integer> tokenList) {
        tokenList.add(line);
        tokenList.add(startChar);
        tokenList.add(length);
        tokenList.add(type.ordinal());
        int modifierBitmask = 0;
        for (Modifier modifier : modifiers) {
            modifierBitmask |= (1 << modifier.ordinal());
        }
        tokenList.add(modifierBitmask);
    }

    public enum Type {
        NAMESPACE("namespace"),
        TYPE("type"),
        CLASS("class"),
        PARAMETER("parameter"),
        VARIABLE("variable"),
        METHOD("method"),
        FUNCTION("function"),
        KEYWORD("keyword"),
        COMMENT("comment"),
        STRING("string"),
        NUMBER("number"),
        REGEXP("regexp"),
        OPERATOR("operator");

        private final String lspName;

        Type(String lspName) {
            this.lspName = lspName;
        }

        public static final List<String> getLspNames() {
            var names = new java.util.ArrayList<String>();
            for (var type : values()) {
                names.add(type.getLspName());
            }
            return names;
        }

        public int getLspIndex() {
            return ordinal();
        }

        public String getLspName() {
            return lspName;
        }
    }

    public enum Modifier {
        DECLARATION("declaration"),
        IMMUTABLE("readonly"),
        STATIC("static"),
        WRITE("modification");

        private final String lspName;

        Modifier(String lspName) {
            this.lspName = lspName;
        }

        public static final List<String> getLspNames() {
            var names = new java.util.ArrayList<String>();
            for (var modifier : values()) {
                names.add(modifier.getLspName());
            }
            return names;
        }

        public String getLspName() {
            return lspName;
        }
    }
}
