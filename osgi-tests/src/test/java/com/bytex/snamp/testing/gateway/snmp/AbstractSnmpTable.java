package com.bytex.snamp.testing.gateway.snmp;

import com.bytex.snamp.testing.SnmpTable;
import com.google.common.collect.ImmutableList;
import org.snmp4j.smi.*;

import java.math.BigInteger;

/**
 * @author Roman Sakno
 * @version 2.1
 * @since 1.0
 */
public abstract class AbstractSnmpTable implements SnmpTable {
    private final ImmutableList<Class<?>> columns;

    protected AbstractSnmpTable(final Class<?>... columns){
        this.columns = ImmutableList.copyOf(columns);
    }

    @Override
    public final Object getCell(final int columndIndex, final int rowIndex) {
        return deserialize(getRawCell(columndIndex, rowIndex), columns.get(columndIndex));
    }

    static Variable serialize(final Object value, final Class<?> valueType){
        final Variable var;

        if (valueType == int.class || valueType == Integer.class || valueType == short.class)
        {
            var = new Integer32(Integer.class.cast(value));
        }
        else if (valueType == long.class || valueType == Long.class)
        {
            var = new Counter64(Long.class.cast(value));
        }
        else if (valueType == Boolean.class || valueType == boolean.class)
        {
            var = new Integer32((Boolean.class.cast(value) == Boolean.TRUE)?1:0);
        }
        else if (valueType == byte[].class)
        {
            var = new OctetString((byte[])value);
        }
        else
        {
            var = new OctetString(value.toString());
        }
        return var;
    }

    static <T> T deserialize(final Variable var, final Class<T> className){
        final Object result;
        if(var instanceof Null)
            throw new RuntimeException(String.format("SNMP NULL unexpected: %s", var.getSyntaxString()));
        else if (var instanceof UnsignedInteger32 || var instanceof Integer32)
            result = (className == Boolean.class)?(var.toInt() == 1):var.toInt();
        else if (var instanceof OctetString)
        {
            if (className == BigInteger.class)
                result = new BigInteger(var.toString());
            else if (className == Float.class)
                result = Float.valueOf(var.toString());
            else if (className == byte[].class)
                result = ((OctetString) var).toByteArray();
            else
                result = var.toString();
        }
        else if (var instanceof IpAddress)
            result = var.toString();
        else if (var instanceof Counter64)
            result = var.toLong();
        else result = null;
        return className.cast(result);
    }
}
