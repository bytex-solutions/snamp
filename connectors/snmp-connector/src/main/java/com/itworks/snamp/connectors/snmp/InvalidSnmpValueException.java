package com.itworks.snamp.connectors.snmp;

import org.snmp4j.smi.AbstractVariable;

/**
 * Represents an exception occurred when Java object cannot be converted into
 * SNMP-compliant value. This class cannot be inherited.
 * @author Roman Sakno
 * @version 1.0
 * @since 1.0
 */
final class InvalidSnmpValueException extends Exception {

    public InvalidSnmpValueException(final Object source, final int expectedSnmpType){
        super(String.format("Unable to convert %s to SMI %s", source, AbstractVariable.getSyntaxString(expectedSnmpType)));
    }
}
