package com.itworks.snamp.adapters.snmp;

import com.itworks.snamp.mapping.TypeLiterals;
import com.itworks.snamp.adapters.AbstractResourceAdapter.AttributeAccessor;
import com.itworks.snamp.connectors.ManagedEntityType;
import org.snmp4j.smi.OctetString;
import org.snmp4j.smi.Variable;

import java.text.ParseException;
import java.util.Date;
import java.util.Map;
import java.util.logging.Level;

import static com.itworks.snamp.adapters.snmp.SnmpAdapterConfigurationDescriptor.DATE_TIME_DISPLAY_FORMAT_PARAM;
import static com.itworks.snamp.adapters.snmp.SnmpHelpers.DateTimeFormatter;
import static com.itworks.snamp.connectors.ManagedEntityTypeHelper.convertFrom;

final class SnmpUnixTimeObject extends SnmpScalarObject<OctetString>{
    public static final String defaultValue = "1970-1-1,00:00:00.0,+0:0";

    private final DateTimeFormatter formatter;

    public SnmpUnixTimeObject(final String oid, final AttributeAccessor connector){
        super(oid, connector, new OctetString(defaultValue));
        formatter = createFormatter(getMetadata());
    }

    private static OctetString convert(final Object value, final ManagedEntityType attributeTypeInfo, final DateTimeFormatter formatter){
        final Date convertedValue = convertFrom(attributeTypeInfo, value, TypeLiterals.DATE);
        return new OctetString(formatter.convert(convertedValue));
    }

    public static OctetString convert(final Object value, final ManagedEntityType attributeTypeInfo, final Map<String, String> options){
        return convert(value, attributeTypeInfo, createFormatter(options));
    }

    private static Object convert(final OctetString value, final DateTimeFormatter formatter){
        try {
            return formatter.convert(value.toByteArray());
        } catch (final ParseException e) {
            SnmpHelpers.log(Level.WARNING, "Invalid date/time string: %s", value, e);
            return null;
        }
    }

    public static Object convert(final Variable value, final Map<String, String> options){
        return convert((OctetString)value, createFormatter(options));
    }

    @Override
    protected OctetString convert(final Object value) {
        return convert(value, getMetadata().getType(), formatter);
    }

    @Override
    protected Object convert(final OctetString value) {
        return convert(value, formatter);
    }

    private static DateTimeFormatter createFormatter(final Map<String, String> options){
        return SnmpHelpers.createDateTimeFormatter(options.containsKey(DATE_TIME_DISPLAY_FORMAT_PARAM) ? options.get(DATE_TIME_DISPLAY_FORMAT_PARAM) : null);
    }
}
