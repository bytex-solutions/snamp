package com.itworks.snamp.adapters.snmp;

import com.itworks.snamp.adapters.AbstractResourceAdapter.AttributeAccessor;
import com.itworks.snamp.connectors.ManagementEntityType;
import org.snmp4j.smi.*;
import static com.itworks.snamp.connectors.ManagementEntityTypeHelper.*;

import static org.snmp4j.smi.SMIConstants.SYNTAX_OCTET_STRING;

@MOSyntax(SYNTAX_OCTET_STRING)
final class SnmpStringObject extends SnmpScalarObject<OctetString>{
    public static final String defaultValue = "";

    public SnmpStringObject(final String oid, final AttributeAccessor connector){
        super(oid, connector, new OctetString(defaultValue));
    }

    public static OctetString convert(final Object value, final ManagementEntityType attributeTypeInfo){
        return new OctetString(convertFrom(attributeTypeInfo, value, String.class));
    }

    //do not remove 'type' argument because it is used by reflection in SnmpType
    @SuppressWarnings("UnusedParameters")
    public static String convert(final Variable value, final ManagementEntityType type){
        return value.toString();
    }

    @Override
    protected OctetString convert(final Object value) {
        return convert(value, getMetadata().getType());
    }

    @Override
    protected String convert(final OctetString value) {
        return convert(value, getMetadata().getType());
    }
}