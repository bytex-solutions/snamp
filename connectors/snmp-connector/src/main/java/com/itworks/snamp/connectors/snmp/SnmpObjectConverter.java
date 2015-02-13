package com.itworks.snamp.connectors.snmp;

import org.snmp4j.smi.Variable;

import javax.management.InvalidAttributeValueException;
import javax.management.openmbean.OpenType;

/**
 * @author Roman Sakno
 * @version 1.0
 * @since 1.0
 */
interface SnmpObjectConverter<V extends Variable> {
    V convert(final Object value) throws InvalidAttributeValueException;
    Object convert(final V value);
    OpenType<?> getOpenType();
}
