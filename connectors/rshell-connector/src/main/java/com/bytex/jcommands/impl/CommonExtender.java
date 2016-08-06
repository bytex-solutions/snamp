package com.bytex.jcommands.impl;

import com.google.common.io.BaseEncoding;
import org.stringtemplate.v4.Interpreter;
import org.stringtemplate.v4.ModelAdaptor;
import org.stringtemplate.v4.ST;
import org.stringtemplate.v4.STGroup;
import org.stringtemplate.v4.misc.STNoSuchPropertyException;

import java.math.BigInteger;

/**
 * Represents access to the advanced properties for many objects using in the string template.
 * @author Roman Sakno
 * @version 2.0
 * @since 1.0
 */
final class CommonExtender implements ModelAdaptor {
    private static final String HEX_PROPERTY = "hex";
    private static final String BASE64_PROPERTY = "base64";
    private static final String BASE32_PROPERTY = "base32";

    private CommonExtender(){

    }

    private static String getHexProperty(final Object value) throws STNoSuchPropertyException{
        if(value instanceof Byte)
            return Integer.toString((Byte)value, 16);
        else if(value instanceof Short)
            return Integer.toString((Short)value, 16);
        else if(value instanceof Integer)
            return Integer.toString((Integer)value, 16);
        else if(value instanceof Long)
            return Long.toString((Long)value, 16);
        else if(value instanceof BigInteger)
            return ((BigInteger)value).toString(16);
        else if(value instanceof byte[])
            return BaseEncoding.base16().encode((byte[]) value);
        else throw createNoSuchPropertyException(value, HEX_PROPERTY);
    }

    private static String getBase64Property(final Object value) throws STNoSuchPropertyException {
        if (value instanceof byte[])
            return BaseEncoding.base64().encode((byte[]) value);
        else throw createNoSuchPropertyException(value, BASE64_PROPERTY);
    }

    private static String getBase32Property(final Object value) throws STNoSuchPropertyException{
        if(value instanceof byte[])
            return BaseEncoding.base32().encode((byte[])value);
        else throw createNoSuchPropertyException(value, BASE32_PROPERTY);
    }

    private static STNoSuchPropertyException createNoSuchPropertyException(final Object owner, final String missingProperty){
        return new STNoSuchPropertyException(new IllegalArgumentException(String.format("Property %s in object %s doesn't exist", missingProperty, owner)), owner, missingProperty);
    }

    @Override
    public Object getProperty(final Interpreter interp,
                              final ST self,
                              final Object o,
                              final Object property,
                              final String propertyName) throws STNoSuchPropertyException {

        switch (propertyName){
            case HEX_PROPERTY: return getHexProperty(o);
            case BASE64_PROPERTY: return getBase64Property(o);
            case BASE32_PROPERTY: return getBase32Property(o);
            default: throw createNoSuchPropertyException(o, propertyName);
        }
    }

    static void register(final STGroup groupDef){
        final ModelAdaptor extender = new CommonExtender();
        groupDef.registerModelAdaptor(Byte.class, extender);
        groupDef.registerModelAdaptor(Short.class, extender);
        groupDef.registerModelAdaptor(Integer.class, extender);
        groupDef.registerModelAdaptor(Long.class, extender);
        groupDef.registerModelAdaptor(BigInteger.class, extender);
        groupDef.registerModelAdaptor(byte[].class, extender);
    }
}
