package com.snamp.adapters;

import com.snamp.TimeSpan;
import com.snamp.connectors.*;
import org.snmp4j.smi.*;

import java.math.*;
import java.util.Objects;

import static org.snmp4j.smi.SMIConstants.SYNTAX_OCTET_STRING;
import static com.snamp.connectors.util.ManagementEntityTypeHelper.*;

@MOSyntax(SYNTAX_OCTET_STRING)
final class SnmpBigNumberObject extends SnmpScalarObject<OctetString>{
    public static final Number defaultValue = 0;

    public SnmpBigNumberObject(final String oid, final AttributeSupport connector, final TimeSpan timeouts){
        super(oid, connector, new OctetString(defaultValue.toString()), timeouts);
    }

    public static OctetString convert(final Object value, final ManagementEntityType attributeTypeInfo){
        return new OctetString(Objects.toString(convertFrom(attributeTypeInfo, value, Number.class, BigInteger.class, BigDecimal.class), defaultValue.toString()));
    }

    public static Number convert(final Variable value, final ManagementEntityType attributeTypeInfo){
        return convertFrom(attributeTypeInfo, value.toString(), Number.class, fallbackWithDefaultValue(defaultValue, value, attributeTypeInfo), BigDecimal.class, BigInteger.class);
    }

    /**
     * Converts the attribute value into the SNMP-compliant value.
     *
     * @param value The value to convert.
     * @return
     */
    @Override
    protected final OctetString convert(final Object value) {
        return convert(value, attributeTypeInfo);
    }

    /**
     * Converts the SNMP-compliant value to the management connector native value.
     *
     * @param value The value to convert.
     * @return
     */
    @Override
    protected final Object convert(final OctetString value) {
        return convert(value, attributeTypeInfo);
    }
}
