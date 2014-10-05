package com.itworks.snamp.adapters.snmp;

import com.itworks.snamp.adapters.AbstractResourceAdapter.AttributeAccessor;
import com.itworks.snamp.connectors.ManagedEntityType;
import org.snmp4j.smi.Counter64;
import org.snmp4j.smi.Variable;

import static com.itworks.snamp.connectors.ManagedEntityTypeHelper.convertFrom;
import static com.itworks.snamp.connectors.ManagedEntityTypeHelper.supportsProjection;
import static org.snmp4j.smi.SMIConstants.SYNTAX_COUNTER64;

@MOSyntax(SYNTAX_COUNTER64)
final class SnmpLongObject extends SnmpScalarObject<Counter64>{
    public static final long defaultValue = -1;

    public SnmpLongObject(final String oid, final AttributeAccessor connector){
        super(oid, connector, new Counter64(defaultValue));
    }

    public static Counter64 convert(final Object value, final ManagedEntityType attributeTypeInfo){
        final Number convertedValue = convertFrom(attributeTypeInfo, value, Number.class, Byte.class, Short.class, Integer.class, Long.class);
        return new Counter64(convertedValue.longValue());
    }

    public static Long convert(final Variable value, final ManagedEntityType attributeTypeInfo){
        if(supportsProjection(attributeTypeInfo, Long.class)) return value.toLong();
        else return logAndReturnDefaultValue(defaultValue, value, attributeTypeInfo);
    }

    @Override
    protected Counter64 convert(final Object value) {
        return convert(value, getMetadata().getType());
    }

    @Override
    protected Long convert(final Counter64 value) {
        return convert(value, getMetadata().getType());
    }
}
