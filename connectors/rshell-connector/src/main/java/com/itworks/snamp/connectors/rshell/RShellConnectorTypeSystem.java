package com.itworks.snamp.connectors.rshell;

import com.google.common.base.Function;
import com.google.common.collect.ImmutableMap;
import com.itworks.jcommands.impl.XmlParserDefinition;
import com.itworks.jcommands.impl.XmlParsingResultType;
import com.itworks.snamp.InMemoryTable;
import com.itworks.snamp.SafeConsumer;
import com.itworks.snamp.Table;
import com.itworks.snamp.TypeLiterals;
import com.itworks.snamp.connectors.ManagedEntityType;
import com.itworks.snamp.connectors.WellKnownTypeSystem;

import java.util.*;

import static com.itworks.snamp.TableFactory.STRING_TABLE_FACTORY;

/**
 * Represents managed entity type resolved that convert {@link com.itworks.jcommands.impl.XmlParserDefinition}
 * into {@link com.itworks.snamp.connectors.ManagedEntityType}.
 * @author Roman Sakno
 * @version 1.0
 * @since 1.0
 */
final class RShellConnectorTypeSystem extends WellKnownTypeSystem {
    RShellConnectorTypeSystem() {
        registerIdentityConverter(TypeLiterals.STRING_MAP);
        registerConverter(TypeLiterals.STRING_MAP, TypeLiterals.STRING_COLUMN_TABLE, new Function<Map<String,Object>, Table<String>>() {
            @Override
            public Table<String> apply(final Map<String, Object> input) {
                return STRING_TABLE_FACTORY.fromSingleRow(input);
            }
        });
        registerIdentityConverter(TypeLiterals.STRING_COLUMN_TABLE);
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
        return createEntityTabularType(new HashMap<String, ManagedEntityType>(keys.size()) {{
            for (final String k : keys.keySet())
                put(k, createEntityType(keys.get(k)));
        }}, indexed.toArray(new String[indexed.size()]));
    }

    private ManagedEntityType createEntityDictionaryType(final XmlParserDefinition definition) {
        final Map<String, XmlParsingResultType> keys = new HashMap<>();
        definition.exportDictionaryType(keys);
        return createEntityDictionaryType(new HashMap<String, ManagedEntityType>(keys.size()) {{
            for (final String k : keys.keySet())
                put(k, createEntityType(keys.get(k)));
        }});
    }

    static Table<String> toTable(final Collection<Map<String, Object>> value, final XmlParserDefinition definition) {
        final InMemoryTable<String> result = STRING_TABLE_FACTORY.create(new SafeConsumer<ImmutableMap.Builder<String, Class<?>>>() {
            @Override
            public void accept(final ImmutableMap.Builder<String, Class<?>> input) {
                definition.exportTableOrDictionaryType(input);
            }
        },
                value.size());
        for (final Map<String, Object> row : value)
            result.addRow(row);
        return result;
    }

    static Collection<Map<String, Object>> fromTable(final Table<String> value) {
        final Collection<Map<String, Object>> result = new ArrayList<>(value.getRowCount());
        for (int index = 0; index < value.getRowCount(); index++)
            result.add(value.getRow(index));
        return result;
    }
}
