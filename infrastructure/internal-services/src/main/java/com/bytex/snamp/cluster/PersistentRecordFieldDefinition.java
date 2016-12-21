package com.bytex.snamp.cluster;

import com.bytex.snamp.io.IOUtils;
import com.google.common.collect.ImmutableSortedSet;
import com.google.gson.Gson;
import com.orientechnologies.orient.core.metadata.schema.OClass;
import com.orientechnologies.orient.core.metadata.schema.OType;
import com.orientechnologies.orient.core.record.impl.ODocument;

import java.io.*;
import java.time.Instant;
import java.util.*;
import java.util.function.Function;
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
    RAW_VALUE(OType.CUSTOM, "serializedObject"){
        @Override
        boolean setField(final Object fieldValue, final ODocument document) {
            final boolean success;
            if (success = fieldValue instanceof Serializable)
                document.field(super.fieldName, fieldValue);
            return success;
        }

        @Override
        Serializable getField(final ODocument document) {
            return document.field(super.fieldName);
        }
    },
    TEXT_VALUE(OType.STRING, "textValue"){
        @Override
        boolean setField(final Object fieldValue, final ODocument document) {
            final boolean success;
            if (success = fieldValue instanceof String)
                document.field(super.fieldName, fieldValue);
            return success;
        }

        @Override
        String getField(final ODocument document) {
            return document.field(super.fieldName, String.class);
        }
    },
    LONG_VALUE(OType.LONG, "longValue"){
        @Override
        boolean setField(final Object fieldValue, final ODocument document) {
            final boolean success;
            if (success = fieldValue instanceof Number)
                document.field(super.fieldName, fieldValue);
            return success;
        }

        @Override
        Long getField(final ODocument document) {
            final Number result = document.field(super.fieldName, Number.class);
            return result instanceof Long ? (Long) result : result.longValue();
        }
    },
    DOUBLE_VALUE(OType.DOUBLE, "doubleValue"){
        @Override
        boolean setField(final Object fieldValue, final ODocument document) {
            final boolean success;
            if (success = fieldValue instanceof Number)
                document.field(super.fieldName, fieldValue);
            return success;
        }

        @Override
        Double getField(final ODocument document) {
            final Number result = document.field(super.fieldName, Number.class);
            return result instanceof Double ? (Double) result : result.longValue();
        }
    },
    MAP_VALUE(OType.EMBEDDEDMAP, "documentValue"){
        @Override
        boolean setField(final Object fieldValue, final ODocument document) {
            final boolean success;
            if (success = fieldValue instanceof Map<?, ?>)
                document.field(super.fieldName, fieldValue);
            return success;
        }

        @Override
        Map getField(final ODocument document) {
            return document.field(super.fieldName, Map.class);
        }
    },
    JSON_DOCUMENT_VALUE(OType.EMBEDDED, "jsonDocumentValue"){
        private final Gson formatter = new Gson();

        @Override
        boolean setField(final Object fieldValue, final ODocument document) {
            final boolean success;
            if (success = fieldValue instanceof Reader){
                final ODocument subDocument;
                try{
                    subDocument = new ODocument().fromJSON(IOUtils.toString((Reader) fieldValue));
                } catch (final IOException e){
                    throw new UncheckedIOException(e);
                }
                document.field(super.fieldName, subDocument);
            }
            return success;
        }

        @Override
        Reader getField(final ODocument document) {
            return new StringReader(document.<ODocument>field(super.fieldName, ODocument.class).toJSON("prettyPrint"));
        }
    };

    private static final ImmutableSortedSet<PersistentRecordFieldDefinition> ALL_FIELDS = ImmutableSortedSet.copyOf(values());
    private static final ImmutableSortedSet<PersistentRecordFieldDefinition> INDEX_FIELDS = ImmutableSortedSet.copyOf(ALL_FIELDS.stream().filter(PersistentRecordFieldDefinition::isIndex).iterator());
    private final OType fieldType;
    private final String fieldName;
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

    static void createIndex(final OClass documentClass, final String indexName) {
        documentClass.createIndex(indexName, OClass.INDEX_TYPE.UNIQUE_HASH_INDEX, INDEX_FIELDS.stream().map(f -> f.fieldName).toArray(String[]::new));
    }

    @Override
    public String toString() {
        return fieldName;
    }
}
