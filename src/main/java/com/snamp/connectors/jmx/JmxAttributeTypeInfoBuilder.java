package com.snamp.connectors.jmx;

import com.snamp.connectors.*;
import javax.management.openmbean.*;

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

    private static AttributeTypeInfo createJmxSimpleType(final SimpleType<?> attributeType){
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

    public static AttributeTypeInfo createJmxType(final OpenType<?> attributeType){
        if(attributeType instanceof SimpleType)
            return createJmxSimpleType((SimpleType<?>)attributeType);
        else return createTypeInfo(JmxAttributeTypeInfoBuilder.class, Object.class);
    }

    public static AttributeTypeInfo createJmxType(final Class<?> attributeType){
        return createTypeInfo(JmxAttributeTypeInfoBuilder.class, attributeType);
    }

    public static AttributeTypeInfo createJmxType(final String attributeType){
        switch (attributeType){
            case "byte":
            case "java.lang.byte": return createJmxType(SimpleType.BYTE);
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
            //TODO: need array support
            default: return createTypeInfo(JmxAttributeTypeInfoBuilder.class, attributeType);
        }
    }
}
