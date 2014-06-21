package com.itworks.snamp.adapters.rest;

import com.google.gson.*;
import com.itworks.snamp.SimpleTable;
import com.itworks.snamp.Table;
import com.itworks.snamp.connectors.ManagementEntityTabularType;
import com.itworks.snamp.connectors.ManagementEntityType;
import com.itworks.snamp.connectors.attributes.AttributeValue;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.TimeoutException;

import static com.itworks.snamp.adapters.AbstractResourceAdapter.AttributeAccessor;
import static com.itworks.snamp.connectors.ManagementEntityTypeBuilder.AbstractManagementEntityArrayType.VALUE_COLUMN_NAME;
import static com.itworks.snamp.connectors.ManagementEntityTypeBuilder.isArray;
import static com.itworks.snamp.connectors.ManagementEntityTypeBuilder.isMap;
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

    private static JsonArray toJsonArray(final AttributeValue<ManagementEntityTabularType> array, final Gson jsonFormatter){
        final JsonArray result = new JsonArray();
        //invoke all elements and converts each of them to JSON
        for(final Object rawValue: array.convertTo(Object[].class)){
            result.add(toJson(new AttributeValue<>(rawValue, array.type.getColumnType(VALUE_COLUMN_NAME)), jsonFormatter));
        }
        return result;
    }

    private static JsonObject toJsonMap(final AttributeValue<ManagementEntityTabularType> map, final Gson jsonFormatter){
        final JsonObject result = new JsonObject();
        final Map value = map.convertTo(Map.class);
        for(final Object column: value.keySet())
            result.add(Objects.toString(column), toJson(new AttributeValue<>(value.get(column), map.type.getColumnType(Objects.toString(column))), jsonFormatter));
        return result;
    }

    @SuppressWarnings("unchecked")
    private static JsonElement toJsonTable(final AttributeValue<ManagementEntityTabularType> table, final Gson jsonFormatter){
        final JsonArray result = new JsonArray();
        final Table<String> tableReader = table.convertTo(Table.class);
        //table representation in JSON: [{column: value}, {column: value}]
        //therefore, iterates through rows
        for(int rowIndex = 0; rowIndex < tableReader.getRowCount(); rowIndex++){
            final JsonObject row = new JsonObject();
            //iterates through columns
            for(final String columnName: table.type.getColumns())
                row.add(columnName, toJson(new AttributeValue<>(tableReader.getCell(columnName, rowIndex), table.type.getColumnType(columnName)), jsonFormatter));
            result.add(row);
        }
        return result;
    }

    private static JsonElement toJson(final AttributeValue<? extends ManagementEntityType> value, final Gson jsonFormatter){
        if(value == null || value.rawValue == null)
            return JsonNull.INSTANCE;
        else if(supportsString(value.type))
            return new JsonPrimitive(value.convertTo(String.class));
        else if(supportsBoolean(value.type))
            return new JsonPrimitive(value.convertTo(Boolean.class));
        else if(supportsUnixTime(value.type))
            return jsonFormatter.toJsonTree(value.convertTo(Date.class));
        else if(supportsInt8(value.type))
            return new JsonPrimitive(value.convertTo(Byte.class));
        else if(supportsInt16(value.type))
            return new JsonPrimitive(value.convertTo(Short.class));
        else if(supportsInt32(value.type))
            return new JsonPrimitive(value.convertTo(Integer.class));
        else if(supportsInt64(value.type))
            return new JsonPrimitive(value.convertTo(Long.class));
        else if(supportsInteger(value.type))
            return new JsonPrimitive(value.convertTo(BigInteger.class));
        else if(supportsDecimal(value.type))
            return new JsonPrimitive(value.convertTo(BigDecimal.class));
        else if(supportsFloat(value.type))
            return new JsonPrimitive(value.convertTo(Float.class));
        else if(supportsDouble(value.type))
            return new JsonPrimitive(value.convertTo(Double.class));
        else if(isArray(value.type))
            return toJsonArray(value.cast(ManagementEntityTabularType.class), jsonFormatter);
        else if(isMap(value.type))
            return toJsonMap(value.cast(ManagementEntityTabularType.class), jsonFormatter);
        else if(value.isTypeOf(ManagementEntityTabularType.class))
            return toJsonTable(value.cast(ManagementEntityTabularType.class), jsonFormatter);
        else return new JsonPrimitive(value.convertTo(String.class));
    }

    public JsonElement getValueAsJson() throws TimeoutException{
        return toJson(accessor.getValue(), jsonFormatter);
    }

    public String getValue() throws TimeoutException {
        return jsonFormatter.toJson(getValueAsJson());
    }

    private static Object fromArrayJson(final JsonArray attributeValue,
                                 final ManagementEntityType elementType,
                                 final Gson jsonFormatter){
        final Object[] result = new Object[attributeValue.size()];
        for(int i = 0; i < attributeValue.size(); i++){
            result[i] = fromJson(jsonFormatter.toJson(attributeValue.get(i)), elementType, jsonFormatter);
        }
        return result;
    }

    private static Object fromArrayJson(final JsonElement attributeValue,
                                        final ManagementEntityType elementType,
                                        final Gson jsonFormatter){
        if(attributeValue instanceof JsonArray)
            return fromArrayJson((JsonArray) attributeValue, elementType, jsonFormatter);
        else throw new IllegalArgumentException(String.format("Expected JSON array but %s found.", attributeValue));
    }

    private static Object fromArrayJson(final String attributeValue,
                                        final ManagementEntityTabularType attributeType,
                                        final Gson jsonFormatter){
        return fromArrayJson(jsonParser.parse(attributeValue), attributeType.getColumnType(VALUE_COLUMN_NAME), jsonFormatter);
    }

    private static Map<String, Object> fromMapJson(final JsonObject attributeValue,
                                                   final ManagementEntityTabularType attributeType,
                                                   final Gson jsonFormatter){
        final Map<String, Object> result = new HashMap<>(10);
        for(final String column: attributeType.getColumns())
            if(attributeValue.has(column))
                result.put(column, fromJson(jsonFormatter.toJson(attributeValue.get(column)), attributeType.getColumnType(column), jsonFormatter));
            else throw new JsonSyntaxException(String.format("JSON key %s not found.", column));
        return result;
    }

    private static Map<String, Object> fromMapJson(final JsonElement attributeValue,
                                                   final ManagementEntityTabularType attributeType,
                                                   final Gson jsonFormatter){
        if(attributeValue instanceof JsonObject)
            return fromMapJson((JsonObject)attributeValue, attributeType, jsonFormatter);
        else {
            throw new JsonSyntaxException(String.format("Expected JSON object, but actually found %s", jsonFormatter.toJson(attributeValue)));
        }
    }

    private static Map<String, Object> fromMapJson(final String attributeValue,
                                            final ManagementEntityTabularType attributeType,
                                            final Gson jsonFormatter){
        return fromMapJson(jsonParser.parse(attributeValue), attributeType, jsonFormatter);
    }

    private static void insertRow(final Table<String> table,
                                  final JsonObject row,
                                  final ManagementEntityTabularType attributeType,
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
                                               final ManagementEntityTabularType attributeType,
                                               final Gson jsonFormatter){
        final Table<String> result = new SimpleTable<>(new HashMap<String, Class<?>>(){{
            for(final String columnName: attributeType.getColumns())
                put(columnName, Object.class);
        }});
        for(final JsonElement element: attributeValue)
            if(element instanceof JsonObject)
                insertRow(result, (JsonObject)element, attributeType, jsonFormatter);
            else throw new JsonSyntaxException("The element of the JSON array must be a JSON dictionary");
        return result;
    }

    private static Table<String> fromTableJson(final JsonElement attributeValue,
                                               final ManagementEntityTabularType attributeType,
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
                                               final ManagementEntityTabularType attributeType,
                                               final Gson jsonFormatter){
        return fromTableJson(jsonParser.parse(attributeValue), attributeType, jsonFormatter);
    }

    private static Object fromJson(final String attributeValue, final ManagementEntityType attributeType, final Gson jsonFormatter) throws IllegalArgumentException{
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
            return fromArrayJson(attributeValue, (ManagementEntityTabularType) attributeType, jsonFormatter);
        else if(isMap(attributeType))
            return fromMapJson(attributeValue, (ManagementEntityTabularType)attributeType, jsonFormatter);
        else if(isTable(attributeType))
            return fromTableJson(attributeValue, (ManagementEntityTabularType) attributeType, jsonFormatter);
        else throw new IllegalArgumentException(String.format("Unable to convert %s value into resource-specific representation.", attributeValue));
    }

    public boolean setValue(final String value) throws TimeoutException, IllegalArgumentException, JsonSyntaxException {
        return accessor.setValue(fromJson(value, accessor.getType(), jsonFormatter));
    }
}