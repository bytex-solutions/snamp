package com.itworks.snamp.adapters.rest;

import com.google.gson.*;
import com.itworks.snamp.ExceptionPlaceholder;
import com.itworks.snamp.connectors.ManagedEntityTabularType;
import com.itworks.snamp.connectors.ManagedEntityType;
import com.itworks.snamp.connectors.ManagedEntityValue;
import com.itworks.snamp.connectors.MapReader;
import com.itworks.snamp.internal.annotations.Temporary;
import com.itworks.snamp.mapping.*;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Date;
import java.util.Set;

import static com.itworks.snamp.connectors.ManagedEntityTypeBuilder.AbstractManagedEntityArrayType.VALUE_COLUMN_NAME;
import static com.itworks.snamp.connectors.ManagedEntityTypeBuilder.isArray;
import static com.itworks.snamp.connectors.ManagedEntityTypeBuilder.isMap;
import static com.itworks.snamp.connectors.WellKnownTypeSystem.*;
import static com.itworks.snamp.connectors.WellKnownTypeSystem.supportsDouble;

/**
 * @author Roman Sakno
 * @version 1.0
 * @since 1.0
 */
final class JsonTypeSystem {
    private static final JsonParser jsonParser = new JsonParser();

    private JsonTypeSystem(){

    }

    private static JsonArray toJsonArray(final ManagedEntityValue<ManagedEntityTabularType> array, final Gson jsonFormatter){
        final JsonArray result = new JsonArray();
        //invoke all elements and converts each of them to JSON
        for(final Object rawValue: array.convertTo(TypeLiterals.OBJECT_ARRAY)){
            result.add(toJson(new ManagedEntityValue<>(rawValue, array.type.getColumnType(VALUE_COLUMN_NAME)), jsonFormatter));
        }
        return result;
    }

    private static JsonObject toJsonMap(final ManagedEntityValue<ManagedEntityTabularType> map,
                                        final Gson jsonFormatter){
        final JsonObject result = new JsonObject();
        map.convertTo(TypeLiterals.NAMED_RECORD_SET).sequential().forEach(new MapReader<ExceptionPlaceholder>(map.type) {
            @Override
            protected void read(final String column, final ManagedEntityValue<?> value) {
                result.add(column, toJson(value, jsonFormatter));
            }
        });
        return result;
    }

    private static JsonElement toJsonTable(final ManagedEntityValue<ManagedEntityTabularType> table,
                                           final Gson jsonFormatter) {
        final JsonArray result = new JsonArray();
        //table representation in JSON: [{column: value}, {column: value}]
        //therefore, iterates through rows
        table.convertTo(TypeLiterals.ROW_SET).sequential().forEach(new RecordReader<Integer, RecordSet<String, ?>, ExceptionPlaceholder>() {
            @Override
            public void read(final Integer index, final RecordSet<String, ?> value) {
                final JsonObject row = new JsonObject();
                //iterates through columns
                value.sequential().forEach(new MapReader<ExceptionPlaceholder>(table.type) {
                    @Override
                    protected void read(final String columnName, final ManagedEntityValue<?> value) {
                        row.add(columnName, toJson(value, jsonFormatter));
                    }
                });
                result.add(row);
            }
        });
        return result;
    }

    static JsonElement toJson(final ManagedEntityValue<? extends ManagedEntityType> value, final Gson jsonFormatter){
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

    private static RecordSet<String, ?> fromMapJson(final JsonObject attributeValue,
                                                    final ManagedEntityTabularType attributeType,
                                                    final Gson jsonFormatter) {
        return new KeyedRecordSet<String, Object>() {
            @Override
            protected Set<String> getKeys() {
                return attributeType.getColumns();
            }

            @Override
            protected Object getRecord(final String column) {
                return fromJson(jsonFormatter.toJson(attributeValue.get(column)), attributeType.getColumnType(column), jsonFormatter);
            }
        };
    }

    private static RecordSet<String, ?> fromMapJson(final JsonElement attributeValue,
                                                    final ManagedEntityTabularType attributeType,
                                                    final Gson jsonFormatter){
        if(attributeValue instanceof JsonObject)
            return fromMapJson((JsonObject)attributeValue, attributeType, jsonFormatter);
        else {
            throw new JsonSyntaxException(String.format("Expected JSON object, but actually found %s", jsonFormatter.toJson(attributeValue)));
        }
    }

    private static RecordSet<String, ?> fromMapJson(final String attributeValue,
                                                    final ManagedEntityTabularType attributeType,
                                                    final Gson jsonFormatter){
        return fromMapJson(jsonParser.parse(attributeValue), attributeType, jsonFormatter);
    }

    private static RowSet<?> fromTableJson(final JsonArray attributeValue,
                                           final ManagedEntityTabularType attributeType,
                                           final Gson jsonFormatter){
        return new AbstractRowSet<Object>() {
            @Override
            protected Object getCell(final String columnName, final int rowIndex) {
                @Temporary
                final JsonObject row = attributeValue.get(rowIndex).getAsJsonObject();
                final ManagedEntityType cellType = attributeType.getColumnType(columnName);
                return fromJson(jsonFormatter.toJson(row.get(columnName)), cellType, jsonFormatter);
            }

            @Override
            public Set<String> getColumns() {
                return attributeType.getColumns();
            }

            @Override
            public boolean isIndexed(final String columnName) {
                return attributeType.isIndexed(columnName);
            }

            @Override
            public int size() {
                return attributeValue.size();
            }
        };
    }

    private static RowSet<?> fromTableJson(final JsonElement attributeValue,
                                           final ManagedEntityTabularType attributeType,
                                           final Gson jsonFormatter){
        if(attributeValue instanceof JsonArray)
            return fromTableJson((JsonArray) attributeValue, attributeType, jsonFormatter);
        else if(attributeValue instanceof JsonObject){
            final JsonArray array = new JsonArray();
            array.add(attributeValue);
            return fromTableJson(array, attributeType, jsonFormatter);
        }
        else
            throw  new JsonSyntaxException(String.format("Expected JSON array, but actually found %s", jsonFormatter.toJson(attributeValue)));
    }

    private static RowSet<?> fromTableJson(final String attributeValue,
                                           final ManagedEntityTabularType attributeType,
                                           final Gson jsonFormatter){
        return fromTableJson(jsonParser.parse(attributeValue), attributeType, jsonFormatter);
    }

    static Object fromJson(final String attributeValue, final ManagedEntityType attributeType, final Gson jsonFormatter) throws IllegalArgumentException{
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
}
