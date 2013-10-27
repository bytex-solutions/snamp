package com.snamp.adapters.snmp;

import com.snamp.TimeSpan;
import com.snamp.connectors.*;
import org.snmp4j.smi.*;
import static org.snmp4j.smi.SMIConstants.SYNTAX_INTEGER32;

/**
 * Represents Integer SNMP mapping,
 */
@MOSyntax(SYNTAX_INTEGER32)
final class SnmpIntegerObject extends SnmpScalarObject<Integer32>{
    public static final int defaultValue = -1;

    public SnmpIntegerObject(final String oid, final ManagementConnector connector, final TimeSpan timeouts){
        super(oid, connector, new Integer32(defaultValue), timeouts);
    }

    public static Integer32 convert(final Object value, final AttributeTypeInfo attributeTypeInfo){
        return new Integer32(attributeTypeInfo.convertTo(value, Integer.class));
    }

    public static Object convert(final Variable value, final AttributeTypeInfo attributeTypeInfo){
        if(attributeTypeInfo.canConvertFrom(Long.class)) return value.toLong();
        else if(attributeTypeInfo.canConvertFrom(Integer.class)) return value.toInt();
        else if(attributeTypeInfo.canConvertFrom(String.class)) return value.toString();
        else return defaultValue;
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
