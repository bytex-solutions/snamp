package com.bytex.snamp.jmx.json;

import com.google.gson.*;

import javax.management.openmbean.*;
import java.lang.reflect.Type;
import java.util.Collection;
import java.util.Map;
import java.util.Objects;

/**
 * Represents open type formatter.
 * This class cannot be inherited.
 * @author Roman Sakno
 * @since 1.0
 * @version 1.2
 */
public final class OpenTypeFormatter implements JsonSerializer<OpenType<?>>, JsonDeserializer<OpenType<?>> {
    private static final String VOID_TYPE = "void";
    private static final String BOOLEAN_TYPE = "bool";
    private static final String BYTE_TYPE = "int8";
    private static final String SHORT_TYPE = "int16";
    private static final String INTEGER_TYPE = "int32";
    private static final String LONG_TYPE = "int64";
    private static final String CHAR_TYPE = "char";
    private static final String STRING_TYPE = "string";
    private static final String OBJECTNAME_TYPE = "objectname";
    private static final String FLOAT_TYPE = "float32";
    private static final String DOUBLE_TYPE = "float64";
    private static final String BIGINT_TYPE = "bigint";
    private static final String BIGDEC_TYPE = "bigdec";
    private static final String DATE_TYPE = "date";

    private static final String IS_PRIMITIVE_FIELD = "isPrimitive";
    private static final String ARRAY_ELEMENT_FIELD = "elementType";
    private static final String DIMENSIONS_FIELD = "dimensions";

    private static final String TYPE_NAME_FIELD = "typeName";
    private static final String DESCRIPTION_FIELD = "description";
    private static final String ITEMS_FIELD = "items";
    private static final String TYPE_FIELD = "type";
    private static final String ROW_TYPE_FIELD = "rowType";
    private static final String INDEX_FIELD = "index";

    /**
     * Serializes simple JMX type into JSON.
     * @param openType An open type to serialize.
     * @return JSON representation of JMX type.
     */
    public static JsonElement serialize(final SimpleType<?> openType){
        if(Objects.equals(openType, SimpleType.VOID))
            return new JsonPrimitive(VOID_TYPE);
        else if(Objects.equals(openType, SimpleType.BOOLEAN))
            return new JsonPrimitive(BOOLEAN_TYPE);
        else if(Objects.equals(openType, SimpleType.BYTE))
            return new JsonPrimitive(BYTE_TYPE);
        else if(Objects.equals(openType, SimpleType.SHORT))
            return new JsonPrimitive(SHORT_TYPE);
        else if(Objects.equals(openType, SimpleType.INTEGER))
            return new JsonPrimitive(INTEGER_TYPE);
        else if(Objects.equals(openType, SimpleType.LONG))
            return new JsonPrimitive(LONG_TYPE);
        else if(Objects.equals(openType, SimpleType.CHARACTER))
            return new JsonPrimitive(CHAR_TYPE);
        else if(Objects.equals(openType, SimpleType.STRING))
            return new JsonPrimitive(STRING_TYPE);
        else if(Objects.equals(openType, SimpleType.OBJECTNAME))
            return new JsonPrimitive(OBJECTNAME_TYPE);
        else if(Objects.equals(openType, SimpleType.FLOAT))
            return new JsonPrimitive(FLOAT_TYPE);
        else if(Objects.equals(openType, SimpleType.DOUBLE))
            return new JsonPrimitive(DOUBLE_TYPE);
        else if(Objects.equals(openType, SimpleType.BIGINTEGER))
            return new JsonPrimitive(BIGINT_TYPE);
        else if(Objects.equals(openType, SimpleType.BIGDECIMAL))
            return new JsonPrimitive(BIGDEC_TYPE);
        else if(Objects.equals(openType, SimpleType.DATE))
            return new JsonPrimitive(DATE_TYPE);
        else return JsonNull.INSTANCE;
    }

    public static JsonObject serialize(final ArrayType<?> arrayType){
        final JsonObject result = new JsonObject();
        result.addProperty(IS_PRIMITIVE_FIELD, arrayType.isPrimitiveArray());
        result.add(ARRAY_ELEMENT_FIELD, serialize(arrayType.getElementOpenType()));
        result.addProperty(DIMENSIONS_FIELD, arrayType.getDimension());
        return result;
    }

    public static JsonObject serialize(final CompositeType openType){
        final JsonObject result = new JsonObject();
        result.addProperty(TYPE_NAME_FIELD, openType.getTypeName());
        result.addProperty(DESCRIPTION_FIELD, openType.getDescription());
        result.add(ITEMS_FIELD, new JsonObject());
        for(final String itemName: openType.keySet()){
            final JsonObject item = new JsonObject();
            item.addProperty(DESCRIPTION_FIELD, openType.getDescription(itemName));
            item.add(TYPE_FIELD, serialize(openType.getType(itemName)));
            result.getAsJsonObject(ITEMS_FIELD).add(itemName, item);
        }
        return result;
    }

    public static JsonObject serialize(final TabularType openType){
        final JsonObject result = new JsonObject();
        result.add(ROW_TYPE_FIELD, serialize(openType.getRowType()));
        result.add(INDEX_FIELD, JsonUtils.toJsonArray(openType.getIndexNames()));
        result.addProperty(TYPE_NAME_FIELD, openType.getTypeName());
        result.addProperty(DESCRIPTION_FIELD, openType.getDescription());
        return result;
    }

    public static JsonElement serialize(final OpenType<?> openType){
        if(openType instanceof SimpleType<?>)
            return serialize((SimpleType<?>)openType);
        else if(openType instanceof ArrayType<?>)
            return serialize((ArrayType<?>)openType);
        else if(openType instanceof CompositeType)
            return serialize((CompositeType)openType);
        else return JsonNull.INSTANCE;
    }

    private static SimpleType<?> deserialize(final JsonPrimitive json) throws JsonParseException{
        switch (json.getAsString()){
            case VOID_TYPE: return SimpleType.VOID;
            case SHORT_TYPE: return SimpleType.SHORT;
            case BYTE_TYPE: return SimpleType.BYTE;
            case INTEGER_TYPE: return SimpleType.INTEGER;
            case LONG_TYPE: return SimpleType.LONG;
            case CHAR_TYPE: return SimpleType.CHARACTER;
            case BOOLEAN_TYPE: return SimpleType.BOOLEAN;
            case STRING_TYPE: return SimpleType.STRING;
            case FLOAT_TYPE: return SimpleType.FLOAT;
            case DOUBLE_TYPE: return SimpleType.DOUBLE;
            case BIGINT_TYPE: return SimpleType.BIGINTEGER;
            case OBJECTNAME_TYPE: return SimpleType.OBJECTNAME;
            case BIGDEC_TYPE: return SimpleType.BIGDECIMAL;
            case DATE_TYPE: return SimpleType.DATE;
            default: throw new JsonParseException("Malformed JSON. Unable to recognize JMX simple type");
        }
    }

    private static ArrayType<?> deserializeArrayType(final JsonObject json) throws JsonParseException{
        final boolean isPrimitive = json.get(IS_PRIMITIVE_FIELD).getAsBoolean();
        final OpenType<?> elementType = deserialize(json.get(ARRAY_ELEMENT_FIELD));
        final int dimensions = json.get(DIMENSIONS_FIELD).getAsInt();
        try {
            switch (dimensions) {
                case 1:
                    if(elementType instanceof SimpleType<?>)
                        return new ArrayType<>((SimpleType<?>)elementType, isPrimitive);
                default:
                    return new ArrayType<Object>(dimensions, elementType);
            }
        }
        catch (final OpenDataException e){
            throw new JsonParseException(e);
        }
    }

    private static CompositeType deserializeCompositeType(final JsonObject json) throws JsonParseException{
        final String typeName = json.get(TYPE_NAME_FIELD).getAsString();
        final String description = json.get(DESCRIPTION_FIELD).getAsString();
        final Collection<Map.Entry<String, JsonElement>> items = json.getAsJsonObject(ITEMS_FIELD).entrySet();
        final String[] itemNames = new String[items.size()];
        final String[] itemDescriptions = new String[itemNames.length];
        final OpenType<?>[] itemTypes = new OpenType<?>[itemDescriptions.length];
        int index = 0;
        for(final Map.Entry<String, JsonElement> itemEntry: items){
            itemNames[index] = itemEntry.getKey();
            final JsonObject item = itemEntry.getValue().getAsJsonObject();
            itemDescriptions[index] = item.get(DESCRIPTION_FIELD).getAsString();
            itemTypes[index] = deserialize(item.get(TYPE_FIELD));
            index += 1;
        }
        try {
            return new CompositeType(typeName, description, itemNames, itemDescriptions, itemTypes);
        } catch (final OpenDataException e) {
            throw new JsonParseException(e);
        }
    }

    private static TabularType deserializeTabularType(final JsonObject json) throws JsonParseException {
        final String typeName = json.get(TYPE_NAME_FIELD).getAsString();
        final String description = json.get(DESCRIPTION_FIELD).getAsString();
        final String[] indexes = JsonUtils.parseStringArray(json.getAsJsonArray(INDEX_FIELD));
        final CompositeType rowType = deserializeCompositeType(json.getAsJsonObject(ROW_TYPE_FIELD));
        try {
            return new TabularType(typeName, description, rowType, indexes);
        } catch (final OpenDataException e) {
            throw new JsonParseException(e);
        }
    }

    private static OpenType<?> deserialize(final JsonObject json) throws JsonParseException{
        if(json.has(IS_PRIMITIVE_FIELD) && json.has(ARRAY_ELEMENT_FIELD) && json.has(DIMENSIONS_FIELD))     //array type
            return deserializeArrayType(json);
        else if(json.has(ITEMS_FIELD)) //composite type
            return deserializeCompositeType(json);
        else if(json.has(INDEX_FIELD) && json.has(ROW_TYPE_FIELD))  //tabular type
            return deserializeTabularType(json);
        else throw new JsonParseException("Malformed JSON. Unable to detect JMX meta type");
    }

    public static OpenType<?> deserialize(final JsonElement json) throws JsonParseException{
        if(json.isJsonPrimitive())
            return deserialize(json.getAsJsonPrimitive());
        else if(json.isJsonObject())
            return deserialize(json.getAsJsonObject());
        else throw new JsonParseException("Malformed JSON. Unable to recognize JMX open type.");
    }

    @Override
    public OpenType<?> deserialize(final JsonElement jsonElement, final Type type, final JsonDeserializationContext jsonDeserializationContext) throws JsonParseException {
        return deserialize(jsonElement);
    }

    @Override
    public JsonElement serialize(final OpenType<?> openType, final Type type, final JsonSerializationContext jsonSerializationContext) {
        return serialize(openType);
    }
}
