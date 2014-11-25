package com.itworks.snamp.adapters.rest;

import com.google.gson.*;
import com.itworks.snamp.MapBuilder;
import com.itworks.snamp.Table;
import com.itworks.snamp.TypeLiterals;
import com.itworks.snamp.connectors.ManagedEntityTabularType;
import com.itworks.snamp.connectors.ManagedEntityType;
import com.itworks.snamp.connectors.attributes.AttributeSupportException;
import com.itworks.snamp.connectors.ManagedEntityValue;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeoutException;

import static com.itworks.snamp.TableFactory.STRING_TABLE_FACTORY;
import static com.itworks.snamp.adapters.AbstractResourceAdapter.AttributeAccessor;
import static com.itworks.snamp.connectors.ManagedEntityTypeBuilder.AbstractManagedEntityArrayType.VALUE_COLUMN_NAME;
import static com.itworks.snamp.connectors.ManagedEntityTypeBuilder.isArray;
import static com.itworks.snamp.connectors.ManagedEntityTypeBuilder.isMap;
import static com.itworks.snamp.connectors.WellKnownTypeSystem.*;

/**
 * Represents attribute of the managed resource accessible through REST service.
 * This class cannot be inherited.
 * @author Roman Sakno
 * @version 1.0
 * @since 1.0
 */
final class HttpAttributeMapping {
    private final AttributeAccessor accessor;
    private final Gson jsonFormatter;
    private static final JsonParser jsonParser = new JsonParser();

    public HttpAttributeMapping(final AttributeAccessor accessor, final Gson jsonFormatter){
        this.accessor = accessor;
        this.jsonFormatter = jsonFormatter;
    }

    private static JsonArray toJsonArray(final ManagedEntityValue<ManagedEntityTabularType> array, final Gson jsonFormatter){
        final JsonArray result = new JsonArray();
        //invoke all elements and converts each of them to JSON
        for(final Object rawValue: array.convertTo(TypeLiterals.OBJECT_ARRAY)){
            result.add(toJson(new ManagedEntityValue<>(rawValue, array.type.getColumnType(VALUE_COLUMN_NAME)), jsonFormatter));
        }
        return result;
    }

    private static JsonObject toJsonMap(final ManagedEntityValue<ManagedEntityTabularType> map, final Gson jsonFormatter){
        final JsonObject result = new JsonObject();
        final Map<String, Object> value = map.convertTo(TypeLiterals.STRING_MAP);
        for(final String column: value.keySet())
            result.add(column, toJson(new ManagedEntityValue<>(value.get(column), map.type.getColumnType(column)), jsonFormatter));
        return result;
    }

    private static JsonElement toJsonTable(final ManagedEntityValue<ManagedEntityTabularType> table, final Gson jsonFormatter){
        final JsonArray result = new JsonArray();
        final Table<String> tableReader = table.convertTo(TypeLiterals.STRING_COLUMN_TABLE);
        //table representation in JSON: [{column: value}, {column: value}]
        //therefore, iterates through rows
        for(int rowIndex = 0; rowIndex < tableReader.getRowCount(); rowIndex++){
            final JsonObject row = new JsonObject();
            //iterates through columns
            for(final String columnName: table.type.getColumns())
                row.add(columnName, toJson(new ManagedEntityValue<>(tableReader.getCell(columnName, rowIndex), table.type.getColumnType(columnName)), jsonFormatter));
            result.add(row);
        }
        return result;
    }

    private static JsonElement toJson(final ManagedEntityValue<? extends ManagedEntityType> value, final Gson jsonFormatter){
        if(value == null || value.rawValue == null)
            return JsonNull.INSTANCE;
        else if(supportsString(value.type))
            return new JsonPrimitive(value.convertTo(TypeLiterals.STRING));
        else if(supportsBoolean(value.type))
            return new JsonPrimitive(value.convertTo(TypeLiterals.BOOLEAN));
        else if(supportsUnixTime(value.type))
            return jsonFormatter.toJsonTree(value.convertTo(TypeLiterals.DATE));
        else if(supportsInt8(value.type))
            return new JsonPrimitive(value.convertTo(TypeLiterals.BYTE));
        else if(supportsInt16(value.type))
            return new JsonPrimitive(value.convertTo(TypeLiterals.SHORT));
        else if(supportsInt32(value.type))
            return new JsonPrimitive(value.convertTo(TypeLiterals.INTEGER));
        else if(supportsInt64(value.type))
            return new JsonPrimitive(value.convertTo(TypeLiterals.LONG));
        else if(supportsInteger(value.type))
            return new JsonPrimitive(value.convertTo(TypeLiterals.BIG_INTEGER));
        else if(supportsDecimal(value.type))
            return new JsonPrimitive(value.convertTo(TypeLiterals.BIG_DECIMAL));
        else if(supportsFloat(value.type))
            return new JsonPrimitive(value.convertTo(TypeLiterals.FLOAT));
        else if(supportsDouble(value.type))
            return new JsonPrimitive(value.convertTo(TypeLiterals.DOUBLE));
        else if(isArray(value.type))
            return toJsonArray(value.cast(ManagedEntityTabularType.class), jsonFormatter);
        else if(isMap(value.type))
            return toJsonMap(value.cast(ManagedEntityTabularType.class), jsonFormatter);
        else if(value.isTypeOf(ManagedEntityTabularType.class))
            return toJsonTable(value.cast(ManagedEntityTabularType.class), jsonFormatter);
        else return new JsonPrimitive(value.convertTo(TypeLiterals.STRING));
    }

    public JsonElement getValueAsJson() throws TimeoutException, AttributeSupportException{
        return toJson(accessor.getValue(), jsonFormatter);
    }

    public String getValue() throws TimeoutException, AttributeSupportException {
        return jsonFormatter.toJson(getValueAsJson());
    }

    private static Object fromArrayJson(final JsonArray attributeValue,
                                 final ManagedEntityType elementType,
                                 final Gson jsonFormatter){
        final Object[] result = new Object[attributeValue.size()];
        for(int i = 0; i < attributeValue.size(); i++){
            result[i] = fromJson(jsonFormatter.toJson(attributeValue.get(i)), elementType, jsonFormatter);
        }
        return result;
    }

    private static Object fromArrayJson(final JsonElement attributeValue,
                                        final ManagedEntityType elementType,
                                        final Gson jsonFormatter){
        if(attributeValue instanceof JsonArray)
            return fromArrayJson((JsonArray) attributeValue, elementType, jsonFormatter);
        else throw new IllegalArgumentException(String.format("Expected JSON array but %s found.", attributeValue));
    }

    private static Object fromArrayJson(final String attributeValue,
                                        final ManagedEntityTabularType attributeType,
                                        final Gson jsonFormatter){
        return fromArrayJson(jsonParser.parse(attributeValue), attributeType.getColumnType(VALUE_COLUMN_NAME), jsonFormatter);
    }

    private static Map<String, Object> fromMapJson(final JsonObject attributeValue,
                                                   final ManagedEntityTabularType attributeType,
                                                   final Gson jsonFormatter){
        final Map<String, Object> result = MapBuilder.createStringHashMap(10);
        for(final String column: attributeType.getColumns())
            if(attributeValue.has(column))
                result.put(column, fromJson(jsonFormatter.toJson(attributeValue.get(column)), attributeType.getColumnType(column), jsonFormatter));
            else throw new JsonSyntaxException(String.format("JSON key %s not found.", column));
        return result;
    }

    private static Map<String, Object> fromMapJson(final JsonElement attributeValue,
                                                   final ManagedEntityTabularType attributeType,
                                                   final Gson jsonFormatter){
        if(attributeValue instanceof JsonObject)
            return fromMapJson((JsonObject)attributeValue, attributeType, jsonFormatter);
        else {
            throw new JsonSyntaxException(String.format("Expected JSON object, but actually found %s", jsonFormatter.toJson(attributeValue)));
        }
    }

    private static Map<String, Object> fromMapJson(final String attributeValue,
                                            final ManagedEntityTabularType attributeType,
                                            final Gson jsonFormatter){
        return fromMapJson(jsonParser.parse(attributeValue), attributeType, jsonFormatter);
    }

    private static void insertRow(final Table<String> table,
                                  final JsonObject row,
                                  final ManagedEntityTabularType attributeType,
                                  final Gson jsonFormatter){
        final Map<String, Object> insertedRow = new HashMap<>(10);
        //iterates through each column
        for(final Map.Entry<String, JsonElement> column: row.entrySet()){
            insertedRow.put(column.getKey(),
                    fromJson(jsonFormatter.toJson(row.get(column.getKey())), attributeType.getColumnType(column.getKey()), jsonFormatter));
        }
        table.addRow(insertedRow);
    }

    private static Table<String> fromTableJson(final JsonArray attributeValue,
                                               final ManagedEntityTabularType attributeType,
                                               final Gson jsonFormatter){
        final Table<String> result = STRING_TABLE_FACTORY.create(attributeType.getColumns(), Object.class, attributeValue.size());
        for(final JsonElement element: attributeValue)
            if(element instanceof JsonObject)
                insertRow(result, (JsonObject)element, attributeType, jsonFormatter);
            else throw new JsonSyntaxException("The element of the JSON array must be a JSON dictionary");
        return result;
    }

    private static Table<String> fromTableJson(final JsonElement attributeValue,
                                               final ManagedEntityTabularType attributeType,
                                               final Gson jsonFormatter){
        if(attributeValue instanceof JsonArray)
            return fromTableJson((JsonArray) attributeValue, attributeType, jsonFormatter);
        else if(attributeValue instanceof JsonObject){
            final JsonArray array = new JsonArray();
            array.add(attributeValue);
            return fromTableJson(array, attributeType, jsonFormatter);
        }
        else {
            throw  new JsonSyntaxException(String.format("Expected JSON array, but actually found %s", jsonFormatter.toJson(attributeValue)));
        }
    }

    private static Table<String> fromTableJson(final String attributeValue,
                                               final ManagedEntityTabularType attributeType,
                                               final Gson jsonFormatter){
        return fromTableJson(jsonParser.parse(attributeValue), attributeType, jsonFormatter);
    }

    private static Object fromJson(final String attributeValue, final ManagedEntityType attributeType, final Gson jsonFormatter) throws IllegalArgumentException{
        if(supportsBoolean(attributeType))
            return jsonFormatter.fromJson(attributeValue, Boolean.class);
        else if(supportsString(attributeType))
            return jsonFormatter.fromJson(attributeValue, String.class);
        else if(supportsInt8(attributeType))
            return jsonFormatter.fromJson(attributeValue, Byte.class);
        else if(supportsInt16(attributeType))
            return jsonFormatter.fromJson(attributeValue, Short.class);
        else if(supportsInt32(attributeType))
            return jsonFormatter.fromJson(attributeValue, Integer.class);
        else if(supportsInt64(attributeType))
            return jsonFormatter.fromJson(attributeValue, Long.class);
        else if(supportsInteger(attributeType))
            return jsonFormatter.fromJson(attributeValue, BigInteger.class);
        else if(supportsDecimal(attributeType))
            return jsonFormatter.fromJson(attributeValue, BigDecimal.class);
        else if(supportsDouble(attributeType))
            return jsonFormatter.fromJson(attributeValue, Double.class);
        else if(supportsFloat(attributeType))
            return jsonFormatter.fromJson(attributeValue, Float.class);
        else if(supportsUnixTime(attributeType))
            return jsonFormatter.fromJson(attributeValue, Date.class);
        else if(isArray(attributeType))
            return fromArrayJson(attributeValue, (ManagedEntityTabularType) attributeType, jsonFormatter);
        else if(isMap(attributeType))
            return fromMapJson(attributeValue, (ManagedEntityTabularType)attributeType, jsonFormatter);
        else if(isTable(attributeType))
            return fromTableJson(attributeValue, (ManagedEntityTabularType) attributeType, jsonFormatter);
        else throw new IllegalArgumentException(String.format("Unable to convert %s value into resource-specific representation.", attributeValue));
    }

    public void setValue(final String value) throws TimeoutException, IllegalArgumentException, JsonSyntaxException, AttributeSupportException {
        accessor.setValue(fromJson(value, accessor.getType(), jsonFormatter));
    }
}
