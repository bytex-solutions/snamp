package com.itworks.snamp.adapters.snmp;

import com.itworks.snamp.connectors.AttributeSupport;
import com.itworks.snamp.connectors.ManagementEntityType;
import com.itworks.snamp.TimeSpan;
import org.snmp4j.smi.*;
import static org.snmp4j.smi.SMIConstants.SYNTAX_INTEGER32;
import static com.itworks.snamp.connectors.util.ManagementEntityTypeHelper.*;

/**
 * Represents Integer SNMP mapping,
 */
@MOSyntax(SYNTAX_INTEGER32)
final class SnmpIntegerObject extends SnmpScalarObject<Integer32>{
    public static final int defaultValue = -1;

    public SnmpIntegerObject(final String oid, final AttributeSupport connector, final TimeSpan timeouts){
        super(oid, connector, new Integer32(defaultValue), timeouts);
    }

    public static Integer32 convert(final Object value, final ManagementEntityType attributeTypeInfo){
        final Number convertedValue = convertFrom(attributeTypeInfo, value, Number.class, Byte.class, Short.class, Integer.class);
        return new Integer32(convertedValue.intValue());
    }

    public static Object convert(final Variable value, final ManagementEntityType attributeTypeInfo){
        if(supportsProjection(attributeTypeInfo, Long.class)) return value.toLong();
        else if(supportsProjection(attributeTypeInfo, Short.class)) return new Short((short)value.toLong());
        else if(supportsProjection(attributeTypeInfo, Integer.class)) return value.toInt();
        else if(supportsProjection(attributeTypeInfo, Byte.class)) return new Byte((byte)value.toLong());
        else if(supportsProjection(attributeTypeInfo, String.class)) return value.toString();
        else return logAndReturnDefaultValue(defaultValue, value, attributeTypeInfo);
    }

    @Override
    protected Integer32 convert(final Object value){
        return convert(value, attributeTypeInfo);
    }

    /**
     * Converts the SNMP-compliant value to the management connector native value.
     *
     * @param value The value to convert.
     * @return
     */
    @Override
    protected Object convert(final Integer32 value) {
        return convert(value, attributeTypeInfo);
    }
}
