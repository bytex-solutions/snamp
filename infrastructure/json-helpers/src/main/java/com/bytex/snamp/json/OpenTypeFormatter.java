package com.bytex.snamp.json;

import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.JsonProcessingException;
import org.codehaus.jackson.node.BaseJsonNode;
import org.codehaus.jackson.node.ObjectNode;
import org.codehaus.jackson.node.ValueNode;

import javax.management.openmbean.*;
import java.util.*;

/**
 * Represents open type formatter.
 * This class cannot be inherited.
 * @author Roman Sakno
 * @since 1.0
 * @version 2.1
 */
final class OpenTypeFormatter {
    private static final class PrimitiveTypeProcessingException extends JsonProcessingException{
        private static final long serialVersionUID = -1794038092732295869L;

        private PrimitiveTypeProcessingException(final String unexpectedType){
            super(String.format("Unable to recognize simple JMX type %s", unexpectedType));
        }
    }

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

    private OpenTypeFormatter(){
        throw new InstantiationError();
    }

    static ValueNode serialize(final SimpleType<?> openType) {
        if (Objects.equals(openType, SimpleType.VOID))
            return ThreadLocalJsonFactory.getFactory().textNode(VOID_TYPE);
        else if (Objects.equals(openType, SimpleType.BOOLEAN))
            return ThreadLocalJsonFactory.getFactory().textNode(BOOLEAN_TYPE);
        else if (Objects.equals(openType, SimpleType.BYTE))
            return ThreadLocalJsonFactory.getFactory().textNode(BYTE_TYPE);
        else if (Objects.equals(openType, SimpleType.SHORT))
            return ThreadLocalJsonFactory.getFactory().textNode(SHORT_TYPE);
        else if (Objects.equals(openType, SimpleType.INTEGER))
            return ThreadLocalJsonFactory.getFactory().textNode(INTEGER_TYPE);
        else if (Objects.equals(openType, SimpleType.LONG))
            return ThreadLocalJsonFactory.getFactory().textNode(LONG_TYPE);
        else if (Objects.equals(openType, SimpleType.CHARACTER))
            return ThreadLocalJsonFactory.getFactory().textNode(CHAR_TYPE);
        else if (Objects.equals(openType, SimpleType.STRING))
            return ThreadLocalJsonFactory.getFactory().textNode(STRING_TYPE);
        else if (Objects.equals(openType, SimpleType.OBJECTNAME))
            return ThreadLocalJsonFactory.getFactory().textNode(OBJECTNAME_TYPE);
        else if (Objects.equals(openType, SimpleType.FLOAT))
            return ThreadLocalJsonFactory.getFactory().textNode(FLOAT_TYPE);
        else if (Objects.equals(openType, SimpleType.DOUBLE))
            return ThreadLocalJsonFactory.getFactory().textNode(DOUBLE_TYPE);
        else if (Objects.equals(openType, SimpleType.BIGINTEGER))
            return ThreadLocalJsonFactory.getFactory().textNode(BIGINT_TYPE);
        else if (Objects.equals(openType, SimpleType.BIGDECIMAL))
            return ThreadLocalJsonFactory.getFactory().textNode(BIGDEC_TYPE);
        else if (Objects.equals(openType, SimpleType.DATE))
            return ThreadLocalJsonFactory.getFactory().textNode(DATE_TYPE);
        else
            return ThreadLocalJsonFactory.getFactory().nullNode();
    }

    static ObjectNode serialize(final ArrayType<?> arrayType) {
        final ObjectNode node = ThreadLocalJsonFactory.getFactory().objectNode();
        node.put(IS_PRIMITIVE_FIELD, arrayType.isPrimitiveArray());
        node.put(DIMENSIONS_FIELD, arrayType.getDimension());
        node.put(ARRAY_ELEMENT_FIELD, serialize(arrayType.getElementOpenType()));
        return node;
    }

    static ObjectNode serialize(final CompositeType openType){
        final ObjectNode result = ThreadLocalJsonFactory.getFactory().objectNode();
        result.put(TYPE_NAME_FIELD, openType.getTypeName());
        result.put(DESCRIPTION_FIELD, openType.getDescription());
        final ObjectNode items = ThreadLocalJsonFactory.getFactory().objectNode();
        result.put(ITEMS_FIELD, items);
        for(final String itemName: openType.keySet()){
            final ObjectNode item = ThreadLocalJsonFactory.getFactory().objectNode();
            item.put(DESCRIPTION_FIELD, openType.getDescription(itemName));
            item.put(TYPE_FIELD, serialize(openType.getType(itemName)));
            items.put(itemName, item);
        }
        return result;
    }

    static ObjectNode serialize(final TabularType openType){
        final ObjectNode result = ThreadLocalJsonFactory.getFactory().objectNode();
        result.put(ROW_TYPE_FIELD, serialize(openType.getRowType()));
        result.put(INDEX_FIELD, JsonUtils.toJsonArray(openType.getIndexNames()));
        result.put(TYPE_NAME_FIELD, openType.getTypeName());
        result.put(DESCRIPTION_FIELD, openType.getDescription());
        return result;
    }

    private static SimpleType<?> deserializeSimpleType(final JsonNode json) throws PrimitiveTypeProcessingException {
        switch (json.asText()) {
            case VOID_TYPE:
                return SimpleType.VOID;
            case SHORT_TYPE:
                return SimpleType.SHORT;
            case BYTE_TYPE:
                return SimpleType.BYTE;
            case INTEGER_TYPE:
                return SimpleType.INTEGER;
            case LONG_TYPE:
                return SimpleType.LONG;
            case CHAR_TYPE:
                return SimpleType.CHARACTER;
            case BOOLEAN_TYPE:
                return SimpleType.BOOLEAN;
            case STRING_TYPE:
                return SimpleType.STRING;
            case FLOAT_TYPE:
                return SimpleType.FLOAT;
            case DOUBLE_TYPE:
                return SimpleType.DOUBLE;
            case BIGINT_TYPE:
                return SimpleType.BIGINTEGER;
            case OBJECTNAME_TYPE:
                return SimpleType.OBJECTNAME;
            case BIGDEC_TYPE:
                return SimpleType.BIGDECIMAL;
            case DATE_TYPE:
                return SimpleType.DATE;
            default:
                throw new PrimitiveTypeProcessingException(json.getTextValue());
        }
    }

    private static ArrayType<?> deserializeArrayType(final JsonNode json) throws JsonProcessingException {
        final boolean isPrimitive = json.get(IS_PRIMITIVE_FIELD).asBoolean();
        final OpenType<?> elementType = deserialize(json.get(ARRAY_ELEMENT_FIELD));
        final int dimensions = json.get(DIMENSIONS_FIELD).asInt();
        try {
            switch (dimensions) {
                case 1:
                    if (elementType instanceof SimpleType<?>)
                        return new ArrayType<>((SimpleType<?>) elementType, isPrimitive);
                default:
                    return new ArrayType<>(dimensions, elementType);
            }
        } catch (final OpenDataException e) {
            throw new OpenTypeProcessingException(e);
        }
    }

    private static CompositeType deserializeCompositeType(final JsonNode json) throws JsonProcessingException {
        final String typeName = json.get(TYPE_NAME_FIELD).asText();
        final String description = json.get(DESCRIPTION_FIELD).asText();
        final List<String> itemNames = new LinkedList<>();
        final List<String> itemDescriptions = new LinkedList<>();
        final List<OpenType<?>> itemTypes = new LinkedList<>();
        final Iterator<Map.Entry<String, JsonNode>> items = json.get(ITEMS_FIELD).getFields();
        while (items.hasNext()) {
            final Map.Entry<String, JsonNode> itemEntry = items.next();
            itemNames.add(itemEntry.getKey());
            itemDescriptions.add(itemEntry.getValue().get(DESCRIPTION_FIELD).asText());
            itemTypes.add(deserialize(itemEntry.getValue().get(TYPE_FIELD)));
        }
        final String[] EMPTY_STRING_ARRAY = new String[0];
        final OpenType<?>[] EMPTY_TYPE_ARRAY = new OpenType<?>[0];
        try {
            return new CompositeType(typeName,
                    description,
                    itemNames.toArray(EMPTY_STRING_ARRAY),
                    itemDescriptions.toArray(EMPTY_STRING_ARRAY),
                    itemTypes.toArray(EMPTY_TYPE_ARRAY));
        } catch (final OpenDataException e) {
            throw new OpenTypeProcessingException(e);
        }
    }

    private static TabularType deserializeTabularType(final JsonNode json) throws JsonProcessingException {
        final String typeName = json.get(TYPE_NAME_FIELD).asText();
        final String description = json.get(DESCRIPTION_FIELD).asText();
        final String[] indexes = JsonUtils.toStringArray(json.get(INDEX_FIELD));
        final CompositeType rowType = deserializeCompositeType(json.get(ROW_TYPE_FIELD));
        try {
            return new TabularType(typeName, description, rowType, indexes);
        } catch (final OpenDataException e) {
            throw new OpenTypeProcessingException(e);
        }
    }

    private static OpenType<?> deserializeComplexType(final JsonNode json) throws JsonProcessingException{
        if(json.has(IS_PRIMITIVE_FIELD) && json.has(ARRAY_ELEMENT_FIELD) && json.has(DIMENSIONS_FIELD))     //array type
            return deserializeArrayType(json);
        else if(json.has(ITEMS_FIELD)) //composite type
            return deserializeCompositeType(json);
        else if(json.has(INDEX_FIELD) && json.has(ROW_TYPE_FIELD))  //tabular type
            return deserializeTabularType(json);
        else
            throw new OpenTypeProcessingException();
    }

    static OpenType<?> deserialize(final JsonNode json) throws JsonProcessingException{
        if(json.isTextual())
            return deserializeSimpleType(json);
        else if(json.isObject())
            return deserializeComplexType(json);
        else
            throw new OpenTypeProcessingException();
    }

    static BaseJsonNode serialize(final OpenType openType) {
        if (openType instanceof SimpleType<?>)
            return serialize((SimpleType<?>) openType);
        else if (openType instanceof ArrayType<?>)
            return serialize((ArrayType<?>) openType);
        else if (openType instanceof CompositeType)
            return serialize((CompositeType) openType);
        else if (openType instanceof TabularType)
            return serialize((TabularType) openType);
        else
            return ThreadLocalJsonFactory.getFactory().nullNode();
    }
}
