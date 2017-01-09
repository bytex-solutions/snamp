package com.bytex.snamp.json;

import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.JsonParser;
import org.codehaus.jackson.JsonProcessingException;
import org.codehaus.jackson.ObjectCodec;
import org.codehaus.jackson.node.ArrayNode;
import org.codehaus.jackson.node.ObjectNode;
import org.codehaus.jackson.node.POJONode;

import javax.management.ObjectName;
import javax.management.openmbean.*;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * Represents converter from {@link javax.management.openmbean.CompositeData} to JSON Object
 * and vice versa. This class cannot be inherited.
 * @author Roman Sakno
 * @version 2.0
 * @since 1.0
 */
final class ComplexTypeFormatter {
    private static final class ClassProcessingException extends JsonProcessingException{
        private static final long serialVersionUID = 5258651961778252090L;

        private ClassProcessingException(final ClassNotFoundException e){
            super(e);
        }
    }

    private static final String TYPE_FIELD = "type";
    private static final String VALUE_FIELD = "value";
    private static final String ROWS_FIELDS = "rows";

    private ComplexTypeFormatter(){
        throw new InstantiationError();
    }

    private static TabularData deserialize(final TabularType type,
                                   final JsonNode rows,
                                   final ObjectCodec codec) throws IOException {
        final TabularData result = new TabularDataSupport(type);
        for(final JsonNode row: rows)
            result.put(deserialize(type.getRowType(), row, codec));
        return result;
    }

    private static CompositeData deserialize(final CompositeType type,
                                     final JsonNode items,
                                     final ObjectCodec codec) throws IOException{
        final Map<String, Object> entries = new HashMap<>();
        for (final String itemName : type.keySet())
            if (items.has(itemName)) {
                final JsonNode itemValue = items.get(itemName);
                final OpenType<?> itemType = type.getType(itemName);
                if(itemType instanceof CompositeType)
                    entries.put(itemName, deserialize((CompositeType)itemType, itemValue, codec));
                else if(itemType instanceof TabularType)
                    entries.put(itemName, deserialize((TabularType)itemType, itemValue, codec));
                try {
                    entries.put(itemName, codec.treeToValue(itemValue, getJavaType(itemType)));
                } catch (final ClassNotFoundException e) {
                    throw new ClassProcessingException(e);
                }
            }
        try {
            return new CompositeDataSupport(type, entries);
        } catch (final OpenDataException e) {
            throw new OpenTypeProcessingException(e);
        }
    }

    private static Class<?> getJavaType(final OpenType<?> openType) throws ClassNotFoundException {
        if(openType instanceof CompositeType)
            return CompositeData.class;
        else if(openType instanceof TabularType)
            return TabularData.class;
        else if(Objects.equals(openType, SimpleType.BIGDECIMAL))
            return BigDecimal.class;
        else if(Objects.equals(openType, SimpleType.BIGINTEGER))
            return BigInteger.class;
        else if(Objects.equals(openType, SimpleType.DATE))
            return Date.class;
        else if(Objects.equals(openType, SimpleType.BOOLEAN))
            return Boolean.class;
        else if(Objects.equals(openType, SimpleType.BYTE))
            return Byte.class;
        else if(Objects.equals(openType, SimpleType.SHORT))
            return Short.class;
        else if(Objects.equals(openType, SimpleType.INTEGER))
            return Integer.class;
        else if(Objects.equals(openType, SimpleType.LONG))
            return Long.class;
        else if(Objects.equals(openType, SimpleType.OBJECTNAME))
            return ObjectName.class;
        else if(Objects.equals(openType, SimpleType.VOID))
            return Void.class;
        else if(Objects.equals(openType, SimpleType.CHARACTER))
            return Character.class;
        else if(Objects.equals(openType, SimpleType.STRING))
            return String.class;
        else if(Objects.equals(openType, SimpleType.FLOAT))
            return Float.class;
        else if(Objects.equals(openType, SimpleType.DOUBLE))
            return Double.class;
        else return Class.forName(openType.getClassName());
    }

    static CompositeData deserializeCompositeData(final JsonParser parser) throws IOException {
        final JsonNode json = parser.readValueAsTree();
        final CompositeType type = (CompositeType) OpenTypeFormatter.deserialize(json.get(TYPE_FIELD));
        return deserialize(type, json.get(VALUE_FIELD), parser.getCodec());
    }

    static TabularData deserializeTabularData(final JsonParser parser) throws IOException {
        final JsonNode json = parser.readValueAsTree();
        final TabularType type = (TabularType) OpenTypeFormatter.deserialize(json.get(TYPE_FIELD));
        return deserialize(type, json.get(ROWS_FIELDS), parser.getCodec());
    }

    @SuppressWarnings("unchecked")
    private static ArrayNode serializeRows(final TabularData src){
        final ArrayNode rows = ThreadLocalJsonFactory.getFactory().arrayNode();
        for(final CompositeData row: (Iterable<? extends CompositeData>)src.values())
            rows.add(serializeFields(row));
        return rows;
    }

    private static ObjectNode serializeFields(final CompositeData src){
        final ObjectNode result = ThreadLocalJsonFactory.getFactory().objectNode();
        for(final String itemName: src.getCompositeType().keySet()){
            final Object itemValue = src.get(itemName);
            if(itemValue instanceof CompositeData)
                result.put(itemName, serializeFields((CompositeData) itemValue));
            else if(itemValue instanceof TabularData)
                result.put(itemName, serializeRows((TabularData)itemValue));
            else if(itemValue == null)
                result.put(itemName, ThreadLocalJsonFactory.getFactory().nullNode());
            else
                result.put(itemName, new POJONode(itemValue));
        }

        return result;
    }

    static ObjectNode serialize(final CompositeData src){
        final ObjectNode result = ThreadLocalJsonFactory.getFactory().objectNode();
        result.put(TYPE_FIELD, OpenTypeFormatter.serialize(src.getCompositeType()));
        result.put(VALUE_FIELD, serializeFields(src));
        return result;
    }

    static ObjectNode serialize(final TabularData src) {
        final ObjectNode result = ThreadLocalJsonFactory.getFactory().objectNode();
        result.put(TYPE_FIELD, OpenTypeFormatter.serialize(src.getTabularType()));
        result.put(ROWS_FIELDS, serializeRows(src));
        return result;
    }
}
