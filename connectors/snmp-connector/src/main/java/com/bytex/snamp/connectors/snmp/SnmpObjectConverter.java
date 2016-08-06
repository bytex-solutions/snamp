package com.bytex.snamp.connectors.snmp;

import org.snmp4j.smi.Variable;

import javax.management.InvalidAttributeValueException;
import javax.management.openmbean.OpenType;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

/**
 * @author Roman Sakno
 * @version 1.2
 * @since 1.0
 */
interface SnmpObjectConverter<V extends Variable> {
    Charset SNMP_ENCODING = StandardCharsets.UTF_8;

    V convert(final Object value) throws InvalidAttributeValueException;
    Object convert(final V value);
    OpenType<?> getOpenType();
}
