package com.bytex.snamp.cluster;

import com.google.common.collect.ImmutableSortedSet;
import com.orientechnologies.orient.core.metadata.schema.OClass;
import com.orientechnologies.orient.core.metadata.schema.OType;
import com.orientechnologies.orient.core.record.impl.ODocument;

import java.time.Instant;
import java.util.*;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;

/**
 * Represents field definition of the persistent record.
 */
enum PersistentRecordFieldDefinition {
    DECIMAL_KEY(OType.DECIMAL, "nKey", key -> key instanceof Number ? Optional.of((Number) key) : Optional.empty()),
    STRING_KEY(OType.STRING, "sKey", key -> {
        if (key instanceof String)
            return Optional.of(key.toString());
        else if (key instanceof Enum<?>)
            return Optional.of(((Enum<?>) key).name());
        else
            return Optional.empty();
    }),
    DATE_KEY(OType.DATE, "dKey", key -> {
        if (key instanceof Date)
            return Optional.of((Date) key);
        else if (key instanceof Instant)
            return Optional.of(Date.from((Instant) key));
        else
            return Optional.empty();
    }),
    VALUE(OType.ANY, "value");

    static final String INDEX_NAME = "SnampIndex";
    private static final ImmutableSortedSet<PersistentRecordFieldDefinition> ALL_FIELDS = ImmutableSortedSet.copyOf(values());
    private static final ImmutableSortedSet<PersistentRecordFieldDefinition> INDEX_FIELDS = ImmutableSortedSet.copyOf(ALL_FIELDS.stream().filter(PersistentRecordFieldDefinition::isIndex).iterator());
    private final OType fieldType;
    final String fieldName;
    private final Function<Comparable<?>, Optional<?>> keyTransformer;

    PersistentRecordFieldDefinition(final OType type, final String fieldName, final Function<Comparable<?>, Optional<?>> keyTransformer) {
        this.fieldType = type;
        this.fieldName = fieldName;
        this.keyTransformer = Objects.requireNonNull(keyTransformer);
    }

    PersistentRecordFieldDefinition(final OType type, final String fieldName){
        this.fieldType = type;
        this.fieldName = fieldName;
        this.keyTransformer = null;
    }

    private boolean isIndex(){
        return keyTransformer != null;
    }

    static List<?> getCompositeKey(final Comparable<?> key) {
        return INDEX_FIELDS.stream().map(index -> index.keyTransformer.apply(key).orElse(null)).collect(Collectors.toCollection(LinkedList::new));
    }

    static void setKey(final Comparable<?> key, final ODocument document) {
        for (final PersistentRecordFieldDefinition index : INDEX_FIELDS)
            if (index.setField(key, document))
                return;
        throw new IllegalArgumentException(String.format("Unsupported key %s", key));
    }

    boolean setField(final Object fieldValue, final ODocument document) {
        if (keyTransformer == null) {
            document.field(fieldName, fieldValue);
            return true;
        } else if (fieldValue instanceof Comparable<?>) {
            final Optional<?> key = keyTransformer.apply((Comparable<?>) fieldValue);
            if (key.isPresent()) {
                document.field(fieldName, key.get());
                return true;
            }
        }
        return false;
    }

    Object getField(final ODocument document) {
        return document.field(fieldName, fieldType);
    }

    private void registerProperty(final OClass documentClass) {
        documentClass.createProperty(fieldName, fieldType).setNotNull(true);
    }

    static void defineFields(final OClass documentClass) {
        for (final PersistentRecordFieldDefinition field : ALL_FIELDS)
            field.registerProperty(documentClass);
    }

    static void createIndex(final OClass documentClass) {
        documentClass.createIndex(INDEX_NAME, OClass.INDEX_TYPE.DICTIONARY_HASH_INDEX, INDEX_FIELDS.stream().map(f -> f.fieldName).toArray(String[]::new));
    }

    @Override
    public String toString() {
        return fieldName;
    }
}
