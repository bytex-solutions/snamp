package com.snamp.adapters;

import com.snamp.TimeSpan;
import com.snamp.connectors.*;
import org.snmp4j.smi.*;
import static com.snamp.connectors.util.ManagementEntityTypeHelper.*;
import static com.snamp.adapters.SnmpHelpers.DateTimeFormatter;
import static com.snamp.configuration.SnmpAdapterConfigurationDescriptor.DATE_TIME_DISPLAY_FORMAT_PARAM;

import java.text.ParseException;
import java.util.*;
import java.util.logging.Level;

final class SnmpUnixTimeObject extends SnmpScalarObject<OctetString>{
    public static final String defaultValue = "1970-1-1,00:00:00.0,+0:0";

    private final DateTimeFormatter formatter;

    public SnmpUnixTimeObject(final String oid, final AttributeSupport connector, final TimeSpan timeouts){
        super(oid, connector, new OctetString(defaultValue), timeouts);
        formatter = createFormatter(getMetadata());
    }

    private static OctetString convert(final Object value, final ManagementEntityType attributeTypeInfo, final DateTimeFormatter formatter){
        final Date convertedValue = convertFrom(attributeTypeInfo, value, Date.class);
        return new OctetString(formatter.convert(convertedValue));
    }

    public static OctetString convert(final Object value, final ManagementEntityType attributeTypeInfo, final Map<String, String> options){
        return convert(value, attributeTypeInfo, createFormatter(options));
    }

    private static Object convert(final OctetString value, final ManagementEntityType attributeTypeInfo, final DateTimeFormatter formatter){
        try {
            return formatter.convert(value.toByteArray());
        } catch (final ParseException e) {
            log.log(Level.WARNING, String.format("Invalid date/time string %s", e));
            return null;
        }
    }

    public static Object convert(final Variable value, final ManagementEntityType attributeTypeInfo, final Map<String, String> options){
        return convert((OctetString)value, attributeTypeInfo, createFormatter(options));
    }

    /**
     * Converts the attribute value into the SNMP-compliant value.
     *
     * @param value The value to convert.
     * @return
     */
    @Override
    protected OctetString convert(final Object value) {
        return convert(value, attributeTypeInfo, formatter);
    }

    /**
     * Converts the SNMP-compliant value to the management connector native value.
     *
     * @param value The value to convert.
     * @return
     */
    @Override
    protected Object convert(final OctetString value) {
        return convert(value, attributeTypeInfo, formatter);
    }

    private static DateTimeFormatter createFormatter(final Map<String, String> options){
        return SnmpHelpers.createDateTimeFormatter(options.containsKey(DATE_TIME_DISPLAY_FORMAT_PARAM) ? options.get(DATE_TIME_DISPLAY_FORMAT_PARAM) : null);
    }
}
