package com.itworks.snamp.adapters.jmx;

import com.google.common.collect.ObjectArrays;
import com.google.common.reflect.TypeToken;
import com.itworks.snamp.ArrayUtils;
import com.itworks.snamp.connectors.ManagedEntityTabularType;
import com.itworks.snamp.connectors.ManagedEntityType;
import com.itworks.snamp.connectors.ManagedEntityValue;
import com.itworks.snamp.connectors.MapReader;
import com.itworks.snamp.internal.annotations.Temporary;
import com.itworks.snamp.mapping.*;

import javax.management.InvalidAttributeValueException;
import javax.management.openmbean.*;
import java.lang.reflect.Array;
import java.util.*;

import static com.itworks.snamp.connectors.ManagedEntityMetadata.*;
import static com.itworks.snamp.connectors.ManagedEntityTypeBuilder.AbstractManagedEntityArrayType.VALUE_COLUMN_NAME;
import static com.itworks.snamp.connectors.ManagedEntityTypeBuilder.isArray;
import static com.itworks.snamp.connectors.ManagedEntityTypeBuilder.isMap;
import static com.itworks.snamp.connectors.WellKnownTypeSystem.*;

/**
 * @author Roman Sakno
 * @version 1.0
 * @since 1.0
 */
final class JmxTypeSystem {
    private JmxTypeSystem(){}

    static final TypeToken<CompositeData> COMPOSITE_DATA = TypeToken.of(CompositeData.class);

    static final TypeToken<TabularData> TABULAR_DATA = TypeToken.of(TabularData.class);

    private static Object parseArrayValue(final Object array,
                                          final ManagedEntityTabularType arrayType) throws OpenDataException, InvalidAttributeValueException {
        final Object result = Array.newInstance(array.getClass().getComponentType(), Array.getLength(array));
        for(int i = 0; i < Array.getLength(array); i++)
            Array.set(result, i, parseValue(Array.get(array, i), arrayType.getColumnType(VALUE_COLUMN_NAME)));
        return result;
    }

    private static InvalidAttributeValueException createInvalidAttributeValueException(final OpenType<?> expectedType, final Object value){
        return new InvalidAttributeValueException(String.format("The value %s is not of a type %s", value, expectedType.getClassName()));
    }

    private static RecordSet<String, ?> parseCompositeData(final CompositeData data){
        return new KeyedRecordSet<String, Object>() {
            @Override
            protected Set<String> getKeys() {
                return data.getCompositeType().keySet();
            }

            @Override
            protected Object getRecord(final String key) {
                return data.get(key);
            }
        };
    }

    private static RowSet<?> parseTabularData(final TabularData data) {
        return new AbstractRowSet<Object>() {
            @SuppressWarnings("unchecked")
            private final List<CompositeData> rows = new ArrayList<>((Collection<CompositeData>)data.values());

            @Override
            protected Object getCell(final String columnName, final int rowIndex) {
                @Temporary
                final CompositeData row = rows.get(rowIndex);
                return row.get(columnName);
            }

            @Override
            public Set<String> getColumns() {
                return data.getTabularType().getRowType().keySet();
            }

            @Override
            public boolean isIndexed(final String columnName) {
                return data.getTabularType().getIndexNames().contains(columnName);
            }

            @Override
            public int size() {
                return data.size();
            }
        };
    }

    private static Object[] normalizeArray(final Object array, final ArrayType<?> arrayType) throws OpenDataException {
        final Object[] result;
        if (array.getClass().isArray())
            try {
                final Class<?> elementType = Class.forName(arrayType.getElementOpenType().getClassName());
                result = ObjectArrays.newArray(elementType, Array.getLength(array));
                for (int i = 0; i < Array.getLength(array); i++)
                    result[i] = Array.get(array, i);
            } catch (final ClassNotFoundException e) {
                throw new OpenDataException(String.format("Array %s could not be normalized: class %s not found", array, arrayType.getElementOpenType().getClassName()));
            }
        else {
            result = ObjectArrays.newArray(array.getClass().getComponentType(), 1);
            result[0] = array;
        }
        return result;
    }

    static Object parseValue(final Object value,
                                     final ManagedEntityType type,
                                     final OpenType<?> openType) throws OpenDataException, InvalidAttributeValueException{

        if(value == null) throw new InvalidAttributeValueException("Attempts to set null.");
        else if(openType instanceof ArrayType<?> && !(value instanceof Object[]))
            return parseValue(normalizeArray(value, (ArrayType<?>)openType), type, openType);
        if(openType.isValue(value))
            if(openType.isArray() && type instanceof ManagedEntityTabularType)
                return parseArrayValue(value, (ManagedEntityTabularType) type);
            else if(value instanceof CompositeData && type instanceof ManagedEntityTabularType)
                return parseCompositeData((CompositeData) value);
            else if(value instanceof TabularData && type instanceof ManagedEntityTabularType)
                return parseTabularData((TabularData) value);
            else if(openType instanceof SimpleType<?>)
                return value;
        throw createInvalidAttributeValueException(openType, value);
    }

    private static Object parseValue(final Object value,
                                     final ManagedEntityType type) throws OpenDataException, InvalidAttributeValueException{
        return parseValue(value, type, JmxTypeSystem.getType(type, Collections.<String, String>emptyMap()));
    }

    private static CompositeData toCompositeData(final ManagedEntityValue<ManagedEntityTabularType> source,
                                                 final Map<String, String> options) throws OpenDataException{
        if(source.canConvertTo(JmxTypeSystem.COMPOSITE_DATA)){
            final CompositeData dict = source.convertTo(JmxTypeSystem.COMPOSITE_DATA);
            return new CompositeDataSupport(getAttributeMapType(source.type, options),
                    ArrayUtils.toArray(dict.getCompositeType().keySet(), String.class),
                    dict.values().toArray());
        }
        else if(source.canConvertTo(TypeLiterals.NAMED_RECORD_SET)){
            final Map<String, Object> m = new HashMap<>(source.type.getColumns().size());
            source.convertTo(TypeLiterals.NAMED_RECORD_SET).sequential().forEach(new MapReader<OpenDataException>(source.type) {
                @Override
                protected void read(final String key, final ManagedEntityValue<?> value) throws OpenDataException{
                    m.put(key, getValue(value, Collections.<String, String>emptyMap()));
                }
            });
            return new CompositeDataSupport(getAttributeMapType(source.type, options), m);
        }
        else throw new OpenDataException(String.format("Unable to convert %s to CompositeData", source.rawValue));
    }

    private static TabularData toTabularData(final ManagedEntityValue<ManagedEntityTabularType> source,
                                             final Map<String, String> options) throws OpenDataException {
        if (source.canConvertTo(JmxTypeSystem.TABULAR_DATA)) {
            final TabularData data = source.convertTo(JmxTypeSystem.TABULAR_DATA);
            final TabularData result = new TabularDataSupport(getAttributeTabularType(source.type, options));
            for (final Object row : data.values())
                if (row instanceof CompositeData) {
                    final CompositeData typedRow = (CompositeData) row;
                    result.put(new CompositeDataSupport(result.getTabularType().getRowType(),
                            ArrayUtils.toArray(typedRow.getCompositeType().keySet(), String.class),
                            typedRow.values().toArray()));
                }
            return result;
        } else if (source.canConvertTo(TypeLiterals.ROW_SET)) {
            final TabularData result = new TabularDataSupport(getAttributeTabularType(source.type, options));
            source.convertTo(TypeLiterals.ROW_SET).sequential().forEach(new RecordReader<Integer, RecordSet<String, ?>, OpenDataException>() {
                @Override
                public void read(final Integer index, final RecordSet<String, ?> value) throws OpenDataException {
                    final Map<String, Object> row = new HashMap<>(result.getTabularType().getRowType().keySet().size());
                    value.sequential().forEach(new MapReader<OpenDataException>(source.type) {
                        @Override
                        protected void read(final String columnName, final ManagedEntityValue<?> value) throws OpenDataException {
                            row.put(columnName, getValue(value, Collections.<String, String>emptyMap()));
                        }
                    });
                    result.put(new CompositeDataSupport(result.getTabularType().getRowType(), row));
                }
            });
            return result;
        } else throw new OpenDataException(String.format("Unsupported table type %s", source.type));
    }

    static Object getValue(final ManagedEntityValue<?> source, final Map<String, String> options) throws OpenDataException{
        if(supportsBoolean(source.type))
            return source.convertTo(TypeLiterals.BOOLEAN);
        else if(supportsInt8(source.type))
            return source.convertTo(TypeLiterals.BYTE);
        else if(supportsInt16(source.type))
            return source.convertTo(TypeLiterals.SHORT);
        else if(supportsInt32(source.type))
            return source.convertTo(TypeLiterals.INTEGER);
        else if(supportsInt64(source.type))
            return source.convertTo(TypeLiterals.LONG);
        else if(supportsFloat(source.type))
            return source.convertTo(TypeLiterals.FLOAT);
        else if(supportsDouble(source.type))
            return source.convertTo(TypeLiterals.DOUBLE);
        else if(supportsUnixTime(source.type))
            return source.convertTo(TypeLiterals.DATE);
        else if(supportsDecimal(source.type))
            return source.convertTo(TypeLiterals.BIG_DECIMAL);
        else if(supportsInteger(source.type))
            return source.convertTo(TypeLiterals.BIG_INTEGER);
        else if(supportsString(source.type))
            return source.convertTo(TypeLiterals.STRING);
        else if(supportsCharacter(source.type))
            return source.convertTo(TypeLiterals.CHAR);
        else if(isArray(source.type))
            return source.rawValue;
        else if(isMap(source.type))
            return toCompositeData(source.cast(ManagedEntityTabularType.class), options);
        else if(source.isTypeOf(ManagedEntityTabularType.class))
            return toTabularData(source.cast(ManagedEntityTabularType.class), options);
        else throw new OpenDataException(String.format("Unable to resolve %s entity type to JMX OpenType", source.type));
    }

    static OpenType<?> getType(final ManagedEntityType entityType, final Map<String, String> options) throws OpenDataException{
        if(supportsBoolean(entityType))
            return SimpleType.BOOLEAN;
        else if(supportsInt8(entityType))
            return SimpleType.BYTE;
        else if(supportsInt16(entityType))
            return SimpleType.SHORT;
        else if(supportsInt32(entityType))
            return SimpleType.INTEGER;
        else if(supportsInt64(entityType))
            return SimpleType.LONG;
        else if(supportsFloat(entityType))
            return SimpleType.FLOAT;
        else if(supportsDouble(entityType))
            return SimpleType.DOUBLE;
        else if(supportsUnixTime(entityType))
            return SimpleType.DATE;
        else if(supportsDecimal(entityType))
            return SimpleType.BIGDECIMAL;
        else if(supportsInteger(entityType))
            return SimpleType.BIGINTEGER;
        else if(supportsString(entityType))
            return SimpleType.STRING;
        else if(supportsCharacter(entityType))
            return SimpleType.CHARACTER;
        else if(isArray(entityType))
            return getAttributeArrayType((ManagedEntityTabularType)entityType);
        else if(isMap(entityType))
            return getAttributeMapType((ManagedEntityTabularType)entityType, options);
        else if(entityType instanceof ManagedEntityTabularType)
            return getAttributeTabularType((ManagedEntityTabularType)entityType, options);
        else throw new OpenDataException(String.format("Unable to resolve %s entity type to JMX OpenType", entityType));
    }

    private static TabularType getAttributeTabularType(final ManagedEntityTabularType tabularType, final Map<String, String> options) throws OpenDataException{
        final List<String> columns = new ArrayList<>(tabularType.getColumns());
        final String[] itemDescriptions = new String[columns.size()];
        final OpenType<?>[] itemTypes = new OpenType<?>[columns.size()];
        final Collection<String> indexes = new HashSet<>(columns.size());
        for(int i = 0; i < columns.size(); i++){
            final String itemName = columns.get(i);
            if(tabularType.isIndexed(itemName))
                indexes.add(itemName);
            itemTypes[i] = getType(tabularType.getColumnType(itemName), Collections.<String, String>emptyMap());
            itemDescriptions[i] = options.containsKey(getColumnDescription(itemName)) ?
                    options.get(getColumnDescription(itemName)) :
                    String.format("Description stub for %s table column.", itemName);
        }
        final String typeName = options.containsKey(TYPE_NAME) ?
                options.get(TYPE_NAME):
                tabularType.toString();
        final String typeDescription = options.containsKey(TYPE_DESCRIPTION) ?
                options.get(TYPE_DESCRIPTION):
                "Description stub for table";
        final CompositeType rowType = new CompositeType(typeName,
                typeDescription,
                ArrayUtils.toArray(columns, String.class),
                itemDescriptions,
                itemTypes);
        return new TabularType(typeName, typeDescription, rowType, ArrayUtils.toArray(indexes, String.class));
    }

    private static ArrayType<?> getAttributeArrayType(final ManagedEntityTabularType arrayType) throws OpenDataException{
        return ArrayType.getArrayType(getType(arrayType.getColumnType(VALUE_COLUMN_NAME), Collections.<String, String>emptyMap()));
    }

    private static String getColumnDescription(final String columnName){
        return String.format(COLUMN_DESCRIPTION, columnName);
    }

    private static CompositeType getAttributeMapType(final ManagedEntityTabularType mapType, final Map<String, String> options) throws OpenDataException{
        final List<String> columns = new ArrayList<>(mapType.getColumns());
        final String[] itemNames = new String[columns.size()];
        final String[] itemDescriptions = new String[columns.size()];
        final OpenType<?>[] itemTypes = new OpenType<?>[columns.size()];
        for(int i = 0; i < columns.size(); i++){
            final String itemName = itemNames[i] = columns.get(i);
            itemTypes[i] = getType(mapType.getColumnType(itemName), Collections.<String, String>emptyMap());
            itemDescriptions[i] = options.containsKey(getColumnDescription(itemName)) ?
                    options.get(getColumnDescription(itemName)) :
                    String.format("Description stub for %s composite type item.", itemName);
        }
        final String typeName = options.containsKey(TYPE_NAME) ?
                options.get(TYPE_NAME):
                mapType.toString();
        final String typeDescription = options.containsKey(TYPE_DESCRIPTION) ?
                options.get(TYPE_DESCRIPTION):
                "Description stub";
        return new CompositeType(typeName, typeDescription, itemNames, itemDescriptions, itemTypes);
    }
}
