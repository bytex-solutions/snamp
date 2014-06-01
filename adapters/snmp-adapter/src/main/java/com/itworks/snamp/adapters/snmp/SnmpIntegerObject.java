package com.itworks.snamp.adapters.snmp;

import com.itworks.snamp.adapters.AbstractResourceAdapter.AttributeAccessor;
import com.itworks.snamp.connectors.ManagementEntityType;
import org.snmp4j.smi.Integer32;
import org.snmp4j.smi.Variable;

import static com.itworks.snamp.connectors.ManagementEntityTypeHelper.convertFrom;
import static com.itworks.snamp.connectors.ManagementEntityTypeHelper.supportsProjection;
import static org.snmp4j.smi.SMIConstants.SYNTAX_INTEGER32;

/**
 * Represents Integer SNMP mapping,
 */
@MOSyntax(SYNTAX_INTEGER32)
final class SnmpIntegerObject extends SnmpScalarObject<Integer32>{
    public static final int defaultValue = -1;

    public SnmpIntegerObject(final String oid, final AttributeAccessor connector){
        super(oid, connector, new Integer32(defaultValue));
    }

    public static Integer32 convert(final Object value, final ManagementEntityType attributeTypeInfo){
        final Number convertedValue = convertFrom(attributeTypeInfo, value, Number.class, Byte.class, Short.class, Integer.class);
        return new Integer32(convertedValue.intValue());
    }

    public static Object convert(final Variable value, final ManagementEntityType attributeTypeInfo){
        if(supportsProjection(attributeTypeInfo, Long.class)) return value.toLong();
        else if(supportsProjection(attributeTypeInfo, Short.class)) return (short) value.toLong();
        else if(supportsProjection(attributeTypeInfo, Integer.class)) return value.toInt();
        else if(supportsProjection(attributeTypeInfo, Byte.class)) return (byte) value.toLong();
        else if(supportsProjection(attributeTypeInfo, String.class)) return value.toString();
        else return logAndReturnDefaultValue(defaultValue, value, attributeTypeInfo);
    }

    @Override
    protected Integer32 convert(final Object value){
        return convert(value, getMetadata().getType());
    }

    @Override
    protected Object convert(final Integer32 value) {
        return convert(value, getMetadata().getType());
    }
}
