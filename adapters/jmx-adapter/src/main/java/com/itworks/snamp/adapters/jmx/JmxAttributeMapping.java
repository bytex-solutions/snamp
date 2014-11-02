package com.itworks.snamp.adapters.jmx;

import com.itworks.snamp.ArrayUtils;
import com.itworks.snamp.InMemoryTable;
import com.itworks.snamp.Table;
import com.itworks.snamp.TypeLiterals;
import com.itworks.snamp.adapters.AbstractResourceAdapter.AttributeAccessor;
import com.itworks.snamp.connectors.ManagedEntityTabularType;
import com.itworks.snamp.connectors.ManagedEntityType;
import com.itworks.snamp.connectors.attributes.AttributeMetadata;
import com.itworks.snamp.connectors.attributes.AttributeSupportException;
import com.itworks.snamp.connectors.attributes.AttributeValue;

import javax.management.InvalidAttributeValueException;
import javax.management.MBeanAttributeInfo;
import javax.management.openmbean.*;
import java.lang.reflect.Array;
import java.util.*;
import java.util.concurrent.TimeoutException;

import static com.itworks.snamp.connectors.ManagedEntityTypeBuilder.AbstractManagedEntityArrayType.VALUE_COLUMN_NAME;
import static com.itworks.snamp.connectors.WellKnownTypeSystem.*;

/**
 * @author Roman Sakno
 * @version 1.0
 * @since 1.0
 */
final class JmxAttributeMapping implements JmxFeature<MBeanAttributeInfo> {
    private final AttributeAccessor accessor;
    private volatile OpenType<?> attributeType;
    private final boolean pureSerialization;

    public JmxAttributeMapping(final AttributeAccessor accessor, final boolean pureSerialization){
        this.accessor = accessor;
        attributeType = null;
        this.pureSerialization = pureSerialization;
    }

    private static TabularType getAttributeTabularType(final ManagedEntityTabularType tabularType, final Map<String, String> options) throws OpenDataException{
        final List<String> columns = new ArrayList<>(tabularType.getColumns());
        final String[] itemNames = new String[columns.size()];
        final String[] itemDescriptions = new String[columns.size()];
        final OpenType<?>[] itemTypes = new OpenType<?>[columns.size()];
        for(int i = 0; i < columns.size(); i++){
            final String itemName = itemNames[i] = columns.get(i);
            itemTypes[i] = getAttributeType(tabularType.getColumnType(itemName), Collections.<String, String>emptyMap());
            itemDescriptions[i] = options.containsKey(getColumnDescription(itemName)) ?
                    options.get(getColumnDescription(itemName)) :
                    String.format("Description stub for %s table column.", itemName);
        }
        final Collection<String> indicies = new HashSet<>(columns.size());
        for(final String columnName: columns)
            if(tabularType.isIndexed(columnName)) indicies.add(columnName);
        final String typeName = options.containsKey(AttributeMetadata.TYPE_NAME) ?
                options.get(AttributeMetadata.TYPE_NAME):
                tabularType.toString();
        final String typeDescription = options.containsKey(AttributeMetadata.TYPE_DESCRIPTION) ?
                options.get(AttributeMetadata.TYPE_DESCRIPTION):
                "Description stub for table";
        final CompositeType rowType = new CompositeType(typeName, typeDescription, itemNames, itemDescriptions, itemTypes);
        return new TabularType(typeName, typeDescription, rowType, indicies.toArray(new String[indicies.size()]));
    }

    private static ArrayType<?> getAttributeArrayType(final ManagedEntityTabularType arrayType) throws OpenDataException{
        return ArrayType.getArrayType(getAttributeType(arrayType.getColumnType(VALUE_COLUMN_NAME), Collections.<String, String>emptyMap()));
    }

    private static String getColumnDescription(final String columnName){
        return String.format(AttributeMetadata.COLUMN_DESCRIPTION, columnName);
    }

    private static CompositeType getAttributeMapType(final ManagedEntityTabularType mapType, final Map<String, String> options) throws OpenDataException{
        final List<String> columns = new ArrayList<>(mapType.getColumns());
        final String[] itemNames = new String[columns.size()];
        final String[] itemDescriptions = new String[columns.size()];
        final OpenType<?>[] itemTypes = new OpenType<?>[columns.size()];
        for(int i = 0; i < columns.size(); i++){
            final String itemName = itemNames[i] = columns.get(i);
            itemTypes[i] = getAttributeType(mapType.getColumnType(itemName), Collections.<String, String>emptyMap());
            itemDescriptions[i] = options.containsKey(getColumnDescription(itemName)) ?
                    options.get(getColumnDescription(itemName)) :
                    String.format("Description stub for %s composite type item.", itemName);
        }
        final String typeName = options.containsKey(AttributeMetadata.TYPE_NAME) ?
                options.get(AttributeMetadata.TYPE_NAME):
                mapType.toString();
        final String typeDescription = options.containsKey(AttributeMetadata.TYPE_DESCRIPTION) ?
                options.get(AttributeMetadata.TYPE_DESCRIPTION):
                "Description stub";
        return new CompositeType(typeName, typeDescription, itemNames, itemDescriptions, itemTypes);
    }

    private static OpenType<?> getAttributeType(final ManagedEntityType entityType, final Map<String, String> options) throws OpenDataException{
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
        else if(isArray(entityType))
            return getAttributeArrayType((ManagedEntityTabularType)entityType);
        else if(isMap(entityType))
            return getAttributeMapType((ManagedEntityTabularType)entityType, options);
        else if(entityType instanceof ManagedEntityTabularType)
            return getAttributeTabularType((ManagedEntityTabularType)entityType, options);
        else throw new OpenDataException(String.format("Unable to resolve %s entity type to JMX OpenType", entityType));
    }

    private static CompositeData toCompositeData(final AttributeValue<ManagedEntityTabularType> source,
                                                 final Map<String, String> options) throws OpenDataException{
        if(source.canConvertTo(JmxTypeLiterals.COMPOSITE_DATA)){
            final CompositeData dict = source.convertTo(JmxTypeLiterals.COMPOSITE_DATA);
            return new CompositeDataSupport(getAttributeMapType(source.type, options),
                    ArrayUtils.toArray(dict.getCompositeType().keySet(), String.class),
                    dict.values().toArray());
        }
        final Map<String, Object> m = new HashMap<>(10);
        for(final Map.Entry<String, ?> entry: source.convertTo(TypeLiterals.STRING_MAP).entrySet()){
            m.put(entry.getKey(), getValue(new AttributeValue<>(entry.getValue(), source.type.getColumnType(entry.getKey())), Collections.<String, String>emptyMap()));
        }
        return new CompositeDataSupport(getAttributeMapType(source.type, options), m);
    }

    private static TabularData toTabularData(final AttributeValue<ManagedEntityTabularType> source,
                                             final Map<String, String> options) throws OpenDataException{
        final TabularData result;
        if(source.canConvertTo(JmxTypeLiterals.TABULAR_DATA)){
            final TabularData data = source.convertTo(JmxTypeLiterals.TABULAR_DATA);
            result = new TabularDataSupport(getAttributeTabularType(source.type, options));
            for(final Object row: data.values())
                if(row instanceof CompositeData){
                    final CompositeData typedRow = (CompositeData)row;
                    result.put(new CompositeDataSupport(result.getTabularType().getRowType(),
                            ArrayUtils.toArray(typedRow.getCompositeType().keySet(), String.class),
                            typedRow.values().toArray()));
                }
            return result;
        }
        else if(source.canConvertTo(TypeLiterals.STRING_COLUMN_TABLE)) {
            final Table<String> table = source.convertTo(TypeLiterals.STRING_COLUMN_TABLE);
            result = new TabularDataSupport(getAttributeTabularType(source.type, options));
            for (int i = 0; i < table.getRowCount(); i++) {
                final Map<String, Object> row = new HashMap<>(table.getColumns().size());
                for (final String columnName : table.getColumns())
                    row.put(columnName, getValue(new AttributeValue<>(table.getCell(columnName, i), source.type.getColumnType(columnName)), Collections.<String, String>emptyMap()));
                result.put(new CompositeDataSupport(result.getTabularType().getRowType(), row));
            }
            return result;
        }
        else throw new OpenDataException(String.format("Unsupported table type %s", source.type));
    }

    private static Object getValue(final AttributeValue<?> source, final Map<String, String> options) throws OpenDataException{
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
        else if(isArray(source.type))
            return source.rawValue;
        else if(isMap(source.type))
            return toCompositeData(source.cast(ManagedEntityTabularType.class), options);
        else if(source.isTypeOf(ManagedEntityTabularType.class))
            return toTabularData(source.cast(ManagedEntityTabularType.class), options);
        else throw new OpenDataException(String.format("Unable to resolve %s entity type to JMX OpenType", source.type));
    }

    public Object getValue() throws TimeoutException, OpenDataException, AttributeSupportException {
        return getValue(accessor.getValue(), accessor);
    }

    private static Object parseArrayValue(final Object array,
                                          final ManagedEntityTabularType arrayType) throws OpenDataException, InvalidAttributeValueException{
        final Object result = Array.newInstance(array.getClass().getComponentType(), Array.getLength(array));
        for(int i = 0; i < Array.getLength(array); i++)
            Array.set(result, i, parseValue(Array.get(array, i), arrayType.getColumnType(VALUE_COLUMN_NAME)));
        return result;
    }

    private static InvalidAttributeValueException createInvalidAttributeValueException(final OpenType<?> expectedType, final Object value){
        return new InvalidAttributeValueException(String.format("The value %s is not of a type %s", value, expectedType.getClassName()));
    }

    private static Object parseCompositeData(final CompositeData data,
                                             final ManagedEntityTabularType mapType) throws OpenDataException, InvalidAttributeValueException{
        final Map<String, Object> map = new HashMap<>(data.getCompositeType().keySet().size());
        for(final String key: data.getCompositeType().keySet())
            map.put(key, parseValue(data.get(key), mapType.getColumnType(key)));
        return map;
    }

    private static Object parseTabularData(final TabularData data,
                                           final ManagedEntityTabularType tabularType) throws OpenDataException, InvalidAttributeValueException {
        final Table<String> result = new InMemoryTable<>(tabularType.getColumns(), Object.class, data.size());
        for (final Object row : data.values())
            if (row instanceof CompositeData) {
                final CompositeData sourceRow = (CompositeData) row;
                final Map<String, Object> destRow = new HashMap<>(sourceRow.values().size());
                for (final String columnName : sourceRow.getCompositeType().keySet())
                    destRow.put(columnName, parseValue(sourceRow.get(columnName), tabularType.getColumnType(columnName)));
                result.addRow(destRow);
            }
        return result;
    }

    private static Object normalizeArray(final Object array, final ArrayType<?> arrayType) throws OpenDataException{
        final Object result;
        if(array.getClass().isArray())
            try{
                final Class<?> elementType = Class.forName(arrayType.getElementOpenType().getClassName());
                result = Array.newInstance(elementType, Array.getLength(array));
                for(int i = 0; i < Array.getLength(array); i++)
                    Array.set(result, i, Array.get(array, i));
            }
            catch (final ClassNotFoundException e){
                throw new OpenDataException(String.format("Array %s could not be normalized: class %s not found", array, arrayType.getElementOpenType().getClassName()));
            }
        else {
            result = Array.newInstance(array.getClass().getComponentType(), 1);
            Array.set(result, 0, array);
        }
        return result;
    }

    private static Object parseValue(final Object value,
                                     final ManagedEntityType type,
                                     final OpenType<?> openType) throws OpenDataException, InvalidAttributeValueException{

        if(value == null) throw new InvalidAttributeValueException("Attempts to set null.");
        else if(openType instanceof ArrayType<?> && !(value instanceof Object[]))
            return parseValue(normalizeArray(value, (ArrayType<?>)openType), type, openType);
        if(openType.isValue(value))
            if(openType.isArray() && type instanceof ManagedEntityTabularType)
                return parseArrayValue(value, (ManagedEntityTabularType) type);
            else if(value instanceof CompositeData && type instanceof ManagedEntityTabularType)
                return parseCompositeData((CompositeData) value, (ManagedEntityTabularType) type);
            else if(value instanceof TabularData && type instanceof ManagedEntityTabularType)
                return parseTabularData((TabularData) value, (ManagedEntityTabularType) type);
            else if(openType instanceof SimpleType<?>)
                return value;
        throw createInvalidAttributeValueException(openType, value);
    }

    private static Object parseValue(final Object value,
                                     final ManagedEntityType type) throws OpenDataException, InvalidAttributeValueException{
        return parseValue(value, type, getAttributeType(type, Collections.<String, String>emptyMap()));
    }

    public void setValue(final Object value) throws TimeoutException, OpenDataException, InvalidAttributeValueException, AttributeSupportException{
        if(!pureSerialization && JmxAdapterHelpers.isJmxCompliantAttribute(accessor))
            accessor.setValue(value);
        else accessor.setValue(parseValue(value, accessor.getType(), getAttributeType()));
    }

    public OpenType<?> getAttributeType() throws OpenDataException{
        OpenType<?> type = attributeType;
        if(type == null)
            synchronized (this){
                type = attributeType;
                if(type == null)
                    type = attributeType = getAttributeType(accessor.getType(), accessor);
            }
        return type;
    }

    @Override
    public OpenMBeanAttributeInfoSupport createFeature(final String featureName) throws OpenDataException{
        String description = accessor.getDescription(null);
        if(description == null || description.isEmpty()) description = String.format("Description stub for %s attribute.", featureName);
        return new OpenMBeanAttributeInfoSupport(featureName,
                description,
                getAttributeType(),
                accessor.canRead(),
                accessor.canWrite(),
                featureName.indexOf("is") == 0);
    }
}
