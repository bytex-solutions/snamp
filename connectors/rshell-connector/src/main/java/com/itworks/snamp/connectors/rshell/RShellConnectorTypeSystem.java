package com.itworks.snamp.connectors.rshell;

import com.itworks.jcommands.impl.XmlParserDefinition;
import com.itworks.jcommands.impl.XmlParsingResultType;
import com.itworks.snamp.SimpleTable;
import com.itworks.snamp.Table;
import com.itworks.snamp.connectors.ManagedEntityType;
import com.itworks.snamp.connectors.WellKnownTypeSystem;
import org.apache.commons.collections4.Closure;
import org.apache.commons.collections4.Put;
import org.apache.commons.collections4.Transformer;

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
        registerIdentityConverter(Map.class, Map.class);
        registerConverter(Map.class, Table.class, new Transformer<Map, Table>() {
            @SuppressWarnings("unchecked")
            @Override
            public Table<String> transform(final Map input) {
                return SimpleTable.fromRow((Map<String, ?>) input);
            }
        });
        registerIdentityConverter(Table.class, Table.class);
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
        final SimpleTable<String> result = new SimpleTable<>(new Closure<Put<String, Class<?>>>() {
            @Override
            public void execute(final Put<String, Class<?>> input) {
                definition.exportTableOrDictionaryType(input);
            }
        },
                10,
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
