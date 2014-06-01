package com.itworks.snamp.adapters.snmp;

import com.itworks.snamp.adapters.AbstractResourceAdapter.AttributeAccessor;
import com.itworks.snamp.connectors.ManagementEntityType;
import org.snmp4j.smi.Integer32;
import org.snmp4j.smi.Variable;

import static com.itworks.snamp.connectors.ManagementEntityTypeHelper.convertFrom;
import static org.snmp4j.smi.SMIConstants.SYNTAX_INTEGER32;

@MOSyntax(SYNTAX_INTEGER32)
final class SnmpBooleanObject extends SnmpScalarObject<Integer32>{
    public static final int defaultValue = -1;

    public SnmpBooleanObject(final String oid, final AttributeAccessor connector){
        super(oid, connector, new Integer32(defaultValue));
    }

    public static Integer32 convert(final Object value, final ManagementEntityType attributeTypeInfo){
        return new Integer32(convertFrom(attributeTypeInfo, value, Boolean.class) ? 1 : 0);
    }

    public static Boolean convertToBoolean(final Variable value){
        return value.toLong() != 0;
    }

    /**
     * Converts the attribute value into the SNMP-compliant value.
     *
     * @param value The value to convert.
     * @return SNMP-compliant representation of the specified value.
     */
    @Override
    protected Integer32 convert(final Object value) {
        return convert(value, getMetadata().getType());
    }

    /**
     * Converts SNMP-compliant value to the resource-specific native value.
     *
     * @param value The value to convert.
     * @return Resource-specific representation of SNMP-compliant value.
     */
    @Override
    protected Object convert(final Integer32 value) {
        return convertToBoolean(value);
    }
}
