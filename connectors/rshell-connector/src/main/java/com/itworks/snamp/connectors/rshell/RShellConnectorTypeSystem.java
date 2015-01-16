package com.itworks.snamp.connectors.rshell;

import com.google.common.base.Function;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Maps;
import com.google.common.reflect.TypeToken;
import com.itworks.jcommands.impl.TypeTokens;
import com.itworks.jcommands.impl.XmlParserDefinition;
import com.itworks.jcommands.impl.XmlParsingResultType;
import com.itworks.snamp.ArrayUtils;
import com.itworks.snamp.connectors.ManagedEntityType;
import com.itworks.snamp.connectors.WellKnownTypeSystem;
import com.itworks.snamp.mapping.*;

import java.util.*;

/**
 * Represents managed entity type resolved that convert {@link com.itworks.jcommands.impl.XmlParserDefinition}
 * into {@link com.itworks.snamp.connectors.ManagedEntityType}.
 * @author Roman Sakno
 * @version 1.0
 * @since 1.0
 */
final class RShellConnectorTypeSystem extends WellKnownTypeSystem {
    RShellConnectorTypeSystem() {
        registerConverter(TypeTokens.DICTIONARY_TYPE_TOKEN, TypeLiterals.NAMED_RECORD_SET,
                new Function<Map<String, ?>, RecordSet<String, ?>>() {
                    @Override
                    public RecordSet<String, ?> apply(final Map<String, ?> input) {
                        return toNamedRecordSet(input);
                    }
                });
        registerConverter(TypeTokens.DICTIONARY_TYPE_TOKEN, TypeLiterals.ROW_SET,
                new Function<Map<String, ?>, RowSet<?>>() {
                    @Override
                    public RowSet<?> apply(final Map<String, ?> input) {
                        return toRowSet(input);
                    }
                });
        registerIdentityConverter(TypeLiterals.ROW_SET);
        registerIdentityConverter(TypeLiterals.NAMED_RECORD_SET);
    }

    private ManagedEntityType createEntityType(final XmlParsingResultType type){
        switch (type){
            case BYTE:
                return createInt8Type();
            case SHORT:
                return createInt16Type();
            case INTEGER:
                return createInt32Type();
            case LONG:
                return createInt64Type();
            case BIG_INTEGER:
                return createIntegerType();
            case BIG_DECIMAL:
                return createDecimalType();
            case STRING:
                return createStringType();
            case BOOLEAN:
                return createBooleanType();
            case FLOAT:
                return createFloatType();
            case DOUBLE:
                return createDoubleType();
            case BLOB:
                return createEntityArrayType(createInt8Type());
            case DATE_TIME:
                return createUnixTimeType();
            default: throw new IllegalArgumentException(String.format("Unsupported type %s", type));
        }
    }

    ManagedEntityType createEntityType(final XmlParserDefinition definition){
        switch (definition.getParsingResultType()){
            case DICTIONARY:
                return createEntityDictionaryType(definition);
            case TABLE:
                return createEntityTabularType(definition);
            default:
                return createEntityType(definition.getParsingResultType());
        }
    }

    private ManagedEntityType createEntityTabularType(final XmlParserDefinition definition) {
        final Map<String, XmlParsingResultType> keys = new HashMap<>();
        final Set<String> indexed = new HashSet<>();
        definition.exportTableType(keys, indexed);
        return createEntityTabularType(Maps.transformEntries(keys, new Maps.EntryTransformer<String, XmlParsingResultType, ManagedEntityType>() {
            @Override
            public ManagedEntityType transformEntry(final String key, final XmlParsingResultType value) {
                return createEntityType(value);
            }
        }), ArrayUtils.toArray(indexed, String.class));
    }

    private ManagedEntityType createEntityDictionaryType(final XmlParserDefinition definition) {
        final Map<String, XmlParsingResultType> keys = new HashMap<>();
        definition.exportDictionaryType(keys);
        return createEntityDictionaryType(Maps.transformEntries(keys, new Maps.EntryTransformer<String, XmlParsingResultType, ManagedEntityType>() {
            @Override
            public ManagedEntityType transformEntry(final String key, final XmlParsingResultType value) {
                return createEntityType(value);
            }
        }));
    }

    static RecordSet<String, ?> toNamedRecordSet(final Map<String, ?> value){
        return new KeyedRecordSet<String, Object>() {
            @Override
            protected Set<String> getKeys() {
                return value.keySet();
            }

            @Override
            protected Object getRecord(final String key) {
                return value.get(key);
            }
        };
    }

    static RowSet<?> toRowSet(final Map<String, ?> value){
        return RecordSetUtils.singleRow(value);
    }

    static RowSet<?> toRowSet(final List<? extends Map<String, ?>> value, final XmlParserDefinition definition) {
        return new AbstractRowSet<Object>() {
            private final ImmutableSet<String> columns;

            {
                final ImmutableMap.Builder<String, TypeToken<?>> builder = ImmutableMap.builder();
                definition.exportTableOrDictionaryType(builder);
                columns = builder.build().keySet();
            }

            @Override
            protected Object getCell(final String columnName, final int rowIndex) {
                final Map<String, ?> row = value.get(rowIndex);
                return row.get(columnName);
            }

            @Override
            public Set<String> getColumns() {
                return columns;
            }

            @Override
            public boolean isIndexed(final String columnName) {
                return false;
            }

            @Override
            public int size() {
                return value.size();
            }
        };
    }

    static Map<String, ?> mapFromNamedRecordSet(final RecordSet<String, ?> value){
        return RecordSetUtils.toMap(value);
    }

    static List<? extends Map<String, ?>> tableFromRowSet(final RowSet<?> value) {
        return RecordSetUtils.toList(value);
    }
}
