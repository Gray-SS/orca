package com.orca.compiler.core.typesystem;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Structural collection type
 * Compatibility is structural: same set of fields with compatible field types.
 */
public final class CollectionType extends BaseType {
    private final LinkedHashMap<String, LangType> fields;

    public CollectionType(LinkedHashMap<String, LangType> fields) {
        this.fields = new LinkedHashMap<>(fields);
    }

    @Override
    public TypeKind kind() {
        return TypeKind.COLLECTION;
    }

    @Override
    public String displayName() {
        var sb = new StringBuilder();

        sb.append("{");
        boolean first = true;
        for (var e : fields.entrySet()) {
            if (!first) sb.append(", ");
            first = false;
            sb.append(e.getKey()).append(": ").append(e.getValue().displayName());
        }
        sb.append("}");
        return sb.toString();
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) return true;
        if (!(other instanceof CollectionType o)) return false;

        return TypeComparator.areStructurallyEquivalent(this, o);
    }

    @Override
    public int hashCode() {
        return Objects.hash(fields);
    }

    public Map<String, LangType> getFields() {
        return Map.copyOf(fields);
    }

    /** Champs dans l'ordre de déclaration (important pour la construction/CodeGen). */
    public List<Map.Entry<String, LangType>> orderedFields() {
        return List.copyOf(fields.entrySet());
    }

    public LangType getField(String name) {
        return fields.get(name);
    }

    public boolean hasField(String name) {
        return fields.containsKey(name);
    }
}
