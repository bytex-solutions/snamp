package com.bytex.snamp.json;

import org.codehaus.jackson.node.JsonNodeFactory;
import org.codehaus.jackson.node.POJONode;
import org.codehaus.jackson.node.ValueNode;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.HashMap;
import java.util.function.BiFunction;

/**
 * @author Roman Sakno
 * @version 2.1
 * @since 2.0
 */
public final class ThreadLocalJsonFactory extends ThreadLocal<JsonNodeFactory> {
    private interface ValueNodeConverter<V> extends BiFunction<JsonNodeFactory, V, ValueNode>{
        @Override
        ValueNode apply(final JsonNodeFactory nodeFactory, final V value);
    }

    private static final class ValueNodeConverters extends HashMap<Class<?>, ValueNodeConverter>{
        private static final long serialVersionUID = 6589760475951950265L;

        <V> ValueNodeConverters add(final Class<V> valueType, final ValueNodeConverter<V> converter){
            put(valueType, converter);
            return this;
        }
    }

    private static final ThreadLocalJsonFactory INSTANCE;
    private static final ValueNodeConverters converters;

    static {
        INSTANCE = new ThreadLocalJsonFactory();
        converters = new ValueNodeConverters()
                .add(Boolean.class, JsonNodeFactory::booleanNode)
                .add(Byte.class, JsonNodeFactory::numberNode)
                .add(Short.class, JsonNodeFactory::numberNode)
                .add(Integer.class, JsonNodeFactory::numberNode)
                .add(Long.class, JsonNodeFactory::numberNode)
                .add(Float.class, JsonNodeFactory::numberNode)
                .add(Double.class, JsonNodeFactory::numberNode)
                .add(BigDecimal.class, JsonNodeFactory::numberNode)
                .add(BigInteger.class, JsonNodeFactory::numberNode)
                .add(String.class, JsonNodeFactory::textNode)
                .add(byte[].class, JsonNodeFactory::binaryNode);
    }

    private ThreadLocalJsonFactory(){

    }

    public static JsonNodeFactory getFactory(){
        return INSTANCE.get();
    }

    @Override
    protected JsonNodeFactory initialValue() {
        return JsonNodeFactory.instance;
    }

    @SuppressWarnings("unchecked")
    public static ValueNode toValueNode(final Object value){
        if(value == null)
            return getFactory().nullNode();
        final ValueNodeConverter converter = converters.get(value.getClass());
        return converter == null ? new POJONode(value) : converter.apply(getFactory(), value);
    }
}
