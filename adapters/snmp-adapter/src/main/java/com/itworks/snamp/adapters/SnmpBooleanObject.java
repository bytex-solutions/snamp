package com.itworks.snamp.adapters;

import com.itworks.snamp.connectors.AttributeSupport;
import com.itworks.snamp.connectors.ManagementEntityType;
import com.itworks.snamp.TimeSpan;
import org.snmp4j.smi.*;
import static com.itworks.snamp.connectors.util.ManagementEntityTypeHelper.*;

import static org.snmp4j.smi.SMIConstants.SYNTAX_INTEGER32;

@MOSyntax(SYNTAX_INTEGER32)
final class SnmpBooleanObject extends SnmpScalarObject<Integer32>{
    public static final int defaultValue = -1;

    public SnmpBooleanObject(final String oid, final AttributeSupport connector, final TimeSpan timeouts){
        super(oid, connector, new Integer32(defaultValue), timeouts);
    }

    public static Integer32 convert(final Object value, final ManagementEntityType attributeTypeInfo){
        return new Integer32(convertFrom(attributeTypeInfo, value, Boolean.class) ? 1 : 0);
    }

    public static Boolean convert(final Variable value, final ManagementEntityType attributeTypeInfo){
        return value.toLong() != 0;
    }

    /**
     * Converts the attribute value into the SNMP-compliant value.
     *
     * @param value The value to convert.
     * @return
     */
    @Override
    protected Integer32 convert(final Object value) {
        return convert(value, attributeTypeInfo);
    }

    /**
     * Converts the SNMP-compliant value to the management connector native value.
     *
     * @param value The value to convert.
     * @return
     */
    @Override
    protected Boolean convert(final Integer32 value) {
        return convert(value, attributeTypeInfo);
    }
}
