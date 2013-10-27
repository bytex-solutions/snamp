package com.snamp.adapters.snmp;

import com.snamp.TimeSpan;
import com.snamp.connectors.*;
import org.snmp4j.smi.*;

import static org.snmp4j.smi.SMIConstants.SYNTAX_INTEGER32;

@MOSyntax(SYNTAX_INTEGER32)
final class SnmpBooleanObject extends SnmpScalarObject<Integer32>{
    public static final int defaultValue = -1;

    public SnmpBooleanObject(final String oid, final ManagementConnector connector, final TimeSpan timeouts){
        super(oid, connector, new Integer32(defaultValue), timeouts);
    }

    public static Integer32 convert(final Object value, final AttributeTypeInfo attributeTypeInfo){
        return new Integer32(attributeTypeInfo.convertTo(value, Integer.class));
    }

    public static Boolean convert(final Variable value, final AttributeTypeInfo attributeTypeInfo){
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
