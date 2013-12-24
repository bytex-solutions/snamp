package com.snamp.adapters;

import com.snamp.TimeSpan;
import com.snamp.connectors.*;
import org.snmp4j.smi.*;
import static org.snmp4j.smi.SMIConstants.SYNTAX_INTEGER32;
import static com.snamp.connectors.util.ManagementEntityTypeHelper.*;

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
        return new Integer32(convertFrom(attributeTypeInfo, value, Integer.class));
    }

    public static Object convert(final Variable value, final ManagementEntityType attributeTypeInfo){
        if(supportsProjection(attributeTypeInfo, Long.class)) return value.toLong();
        else if(supportsProjection(attributeTypeInfo, Integer.class)) return value.toInt();
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
