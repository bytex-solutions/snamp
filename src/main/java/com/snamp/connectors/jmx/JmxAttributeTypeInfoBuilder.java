package com.snamp.connectors.jmx;

import com.snamp.connectors.*;
import javax.management.openmbean.*;
import java.lang.reflect.Array;
import java.math.*;
import java.util.*;

/**
 * Represents JMX attribute type information.
 * @author roman
 */
final class JmxAttributeTypeInfoBuilder extends AttributePrimitiveTypeBuilder {
    /**
     * Initializes a new builder of JMX attributes.
     */
    public JmxAttributeTypeInfoBuilder(){

    }

    private static interface AttrbuteJmxCompositeType extends AttributeConvertibleTypeInfo, AttributeDictionaryType{

    }

    private static AttrbuteJmxCompositeType createJmxCompositeType(final CompositeType ct){
        return new AttrbuteJmxCompositeType() {
            /**
             * Returns a set of dictionary keys.
             * @return A set of dictionary keys.
             */
            @Override
            public final Set<String> getItems() {
                return ct.keySet();
            }

            /**
             * Returns the type of the dictionary value by its key.
             * @param itemName The item name.
             * @return
             */
            @Override
            public AttributeTypeInfo getItemType(final String itemName) {
                return createJmxType(ct.getType(itemName));
            }

            /**
             *
             * @param target The result of the conversion.
             * @param <T>
             * @return
             */
            @Override
            public <T> boolean canConvertTo(final Class<T> target) {
                return target != null && target.isAssignableFrom(Map.class);
            }

            private Map<String, Object> convertToMap(final CompositeData cdata){
                final Map<String, Object> result = new HashMap<>(ct.keySet().size());
                for(final String itemName: ct.keySet())
                    result.put(itemName, cdata.get(itemName));
                return result;
            }

            @Override
            public <T> T convertTo(final Object value, final Class<T> target) throws IllegalArgumentException {
                if(value instanceof CompositeData && target != null)
                    if(target.isAssignableFrom(Map.class)) return (T)convertToMap((CompositeData)value);
                throw new IllegalArgumentException(String.format("Value %s is not supported", value));
            }

            @Override
            public <T> boolean canConvertFrom(final Class<T> source) {
                return source != null && (Map.class.isAssignableFrom(source) || CompositeData.class.isAssignableFrom(source));
            }

            private CompositeData convertFrom(final Map<String, Object> source) throws OpenDataException {
                return new CompositeDataSupport(ct, source);
            }

            /**
             * Converts the specified value into the
             *
             * @param value The value to convert.
             * @return The value of the attribute.
             */
            @Override
            public Object convertFrom(final Object value) throws IllegalArgumentException {
                if(value instanceof Map)
                    try {
                        return convertFrom((Map<String, Object>)value);
                    }
                    catch (final OpenDataException e) {
                        throw new IllegalArgumentException(e);
                    }
                else if(value instanceof CompositeData) return value;
                else throw new IllegalArgumentException(String.format("Value %s cannot be converted to composite data.", value));
            }

            /**
             * Returns the underlying Java class.
             *
             * @return The underlying Java class.
             */
            @Override
            public Class<?> getNativeClass() {
                return CompositeData.class;
            }
        };
    }

    private static final class AttributeJmxArrayType<T> extends AttributeArrayType implements AttributeConvertibleTypeInfo<T[]>{
        private final AttributeConvertibleTypeInfo<T> elementType;

        public AttributeJmxArrayType(final AttributeConvertibleTypeInfo<T> elementType){
            super(elementType);
            this.elementType = elementType;
        }

        /**
         * Converts the specified value into the
         *
         * @param value The value to convert.
         * @return The value of the attribute.
         */
        @Override
        public T[] convertFrom(final Object value) throws IllegalArgumentException {
            if(isArray(value) && super.elementType.canConvertFrom(value.getClass().getComponentType())){
                final Object result = Array.newInstance(elementType.getNativeClass(), Array.getLength(value));
                for(int i = 0; i < Array.getLength(value); i++)
                    Array.set(result, i, Array.get(value, i));
                return (T[])result;
            }
            else throw new IllegalArgumentException(String.format("Cannot convert %s value.", value));
        }

        /**
         * Returns the underlying Java class.
         *
         * @return The underlying Java class.
         */
        @Override
        public Class<T[]> getNativeClass() {
            final Object obj = Array.newInstance(elementType.getNativeClass(), 0);
            return (Class<T[]>)obj.getClass();
        }
    }

    private static AttributeConvertibleTypeInfo<?> createJmxArrayType(final ArrayType<?> attributeType){
        return new AttributeJmxArrayType(createJmxType(attributeType.getElementOpenType()));
    }

    private static AttributeConvertibleTypeInfo<?> createJmxSimpleType(final SimpleType<?> attributeType){
        if(attributeType == SimpleType.BOOLEAN)
            return createBooleanType(JmxAttributeTypeInfoBuilder.class);
        else if(attributeType == SimpleType.BIGDECIMAL)
            return createDecimalType(JmxAttributeTypeInfoBuilder.class);
        else if(attributeType == SimpleType.BIGINTEGER)
            return createIntegerType(JmxAttributeTypeInfoBuilder.class);
        else if(attributeType == SimpleType.BYTE)
            return createInt8Type(JmxAttributeTypeInfoBuilder.class);
        else if(attributeType == SimpleType.CHARACTER || attributeType == SimpleType.STRING)
            return createStringType(JmxAttributeTypeInfoBuilder.class);
        else if(attributeType == SimpleType.DATE)
            return createUnixTimeType(JmxAttributeTypeInfoBuilder.class);
        else if(attributeType == SimpleType.DOUBLE)
            return createDoubleType(JmxAttributeTypeInfoBuilder.class);
        else if(attributeType == SimpleType.FLOAT)
            return createFloatType(JmxAttributeTypeInfoBuilder.class);
        else if(attributeType == SimpleType.SHORT)
            return createInt16Type(JmxAttributeTypeInfoBuilder.class);
        else if(attributeType == SimpleType.LONG)
            return createInt64Type(JmxAttributeTypeInfoBuilder.class);
        else if(attributeType == SimpleType.STRING)
            return createStringType(JmxAttributeTypeInfoBuilder.class);
        else return createTypeInfo(JmxAttributeTypeInfoBuilder.class, Object.class);
    }

    public static AttributeConvertibleTypeInfo<?> createJmxType(final OpenType<?> attributeType){
        if(attributeType instanceof SimpleType)
            return createJmxSimpleType((SimpleType<?>)attributeType);
        else if(attributeType instanceof CompositeType)
            return createJmxCompositeType((CompositeType)attributeType);
        else return createTypeInfo(JmxAttributeTypeInfoBuilder.class, Object.class);
    }

    public static AttributeConvertibleTypeInfo<?> createJmxType(final Class<?> attributeType){
        return createTypeInfo(JmxAttributeTypeInfoBuilder.class, attributeType);
    }

    public static AttributeConvertibleTypeInfo<?> createJmxType(final String attributeType){
        switch (attributeType){
            case "byte":
            case "java.lang.Byte": return createJmxType(SimpleType.BYTE);
            case "short":
            case "java.lang.Short": return createJmxType(SimpleType.SHORT);
            case "int":
            case "java.lang.Integer": return createJmxType(SimpleType.INTEGER);
            case "long":
            case "java.lang.Long": return createJmxType(SimpleType.LONG);
            case "java.lang.String": return createJmxType(SimpleType.STRING);
            case "java.lang.Date": return createJmxType(SimpleType.DATE);
            case "float":
            case "java.lang.Float": return createJmxType(SimpleType.FLOAT);
            case "double":
            case "java.lang.Double": return createJmxType(SimpleType.DOUBLE);
            case "char":
            case "java.lang.Character": return createJmxType(SimpleType.CHARACTER);
            case "boolean":
            case "java.lang.Boolean": return createJmxType(SimpleType.BOOLEAN);
            case "java.math.BigInteger": return createJmxType(SimpleType.BIGINTEGER);
            case "java.math.BigDecimal": return createJmxType(SimpleType.BIGDECIMAL);
            case "byte[]":
            case "java.lang.Byte[]": return createJmxArrayType(ArrayType.getPrimitiveArrayType(Byte[].class));
            case "short[]":
            case "java.lang.Short[]": return createJmxArrayType(ArrayType.getPrimitiveArrayType(Short[].class));
            case "int[]":
            case "java.lang.Integer[]": return createJmxArrayType(ArrayType.getPrimitiveArrayType(Integer[].class));
            case "long[]":
            case "java.lang.Long[]": return createJmxArrayType(ArrayType.getPrimitiveArrayType(Long[].class));
            case "java.lang.String[]": return createJmxType(ArrayType.getPrimitiveArrayType(String[].class));
            case "java.lang.Date[]": return createJmxType(ArrayType.getPrimitiveArrayType(Date[].class));
            case "float[]":
            case "java.lang.Float[]": return createJmxType(ArrayType.getPrimitiveArrayType(Float[].class));
            case "double[]":
            case "java.lang.Double[]": return createJmxType(ArrayType.getPrimitiveArrayType(Double[].class));
            case "char[]":
            case "java.lang.Character[]": return createJmxType(ArrayType.getPrimitiveArrayType(Character[].class));
            case "boolean[]":
            case "java.lang.Boolean[]": return createJmxType(ArrayType.getPrimitiveArrayType(Boolean[].class));
            case "java.math.BigInteger[]": return createJmxType(ArrayType.getPrimitiveArrayType(BigInteger[].class));
            case "java.math.BigDecimal[]": return createJmxType(ArrayType.getPrimitiveArrayType(BigDecimal[].class));
            default: return createTypeInfo(JmxAttributeTypeInfoBuilder.class, attributeType);
        }
    }
}
