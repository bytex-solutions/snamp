package com.bytex.snamp.gateway.snmp;

import org.snmp4j.smi.OctetString;
import org.snmp4j.smi.SMIConstants;
import org.snmp4j.smi.Variable;

import javax.management.DescriptorRead;
import javax.management.InvalidAttributeValueException;
import java.text.ParseException;
import java.util.Date;

import static com.bytex.snamp.gateway.snmp.SnmpGatewayDescriptionProvider.parseDateTimeDisplayFormat;

final class SnmpUnixTimeObject extends SnmpScalarObject<OctetString>{
    static final int SYNTAX = SMIConstants.SYNTAX_OCTET_STRING;
    private static final String DEFAULT_VALUE = "1970-1-1,00:00:00.0,+0:0";

    private final DateTimeFormatter formatter;

    SnmpUnixTimeObject(final SnmpAttributeAccessor connector) {
        super(connector, OctetStringHelper.toOctetString(DEFAULT_VALUE));
        formatter = createFormatter(connector.getMetadata());
    }

    private static OctetString toSnmpObject(final Object value, final DateTimeFormatter formatter) {
        if (value instanceof Date)
            return OctetString.fromByteArray(formatter.convert((Date) value));
        else return OctetStringHelper.toOctetString(DEFAULT_VALUE);
    }

    static OctetString toSnmpObject(final Object value, final DescriptorRead options){
        return toSnmpObject(value, createFormatter(options));
    }

    private static Date fromSnmpObject(final OctetString value, final DateTimeFormatter formatter) throws InvalidAttributeValueException {
        try {
            return formatter.convert(value.toByteArray());
        } catch (ParseException e) {
            throw new InvalidAttributeValueException(e.getMessage());
        }
    }

    static Date fromSnmpObject(final Variable value, final DescriptorRead options) throws InvalidAttributeValueException {
        if(value instanceof OctetString)
            return fromSnmpObject((OctetString) value, createFormatter(options));
        else throw unexpectedSnmpType(OctetString.class);
    }

    @Override
    protected OctetString convert(final Object value) {
        return toSnmpObject(value, formatter);
    }

    @Override
    protected Date convert(final OctetString value) throws InvalidAttributeValueException{
        return fromSnmpObject(value, getMetadata());
    }

    private static DateTimeFormatter createFormatter(final DescriptorRead options){
        return SnmpHelpers.createDateTimeFormatter(parseDateTimeDisplayFormat(options));
    }
}
