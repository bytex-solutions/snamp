package com.itworks.snamp.connectors.snmp;

import com.itworks.snamp.connectors.ManagedEntityType;
import org.snmp4j.smi.OID;
import org.snmp4j.smi.Variable;

import java.util.Map;

/**
 * Represents SNMP-specific entity type.
 * @author Roman Sakno
 * @version 1.0
 * @since 1.0
 */
interface SnmpManagedEntityType extends ManagedEntityType {
    Map<OID, Variable> convertToSnmp(final Object value) throws InvalidSnmpValueException;
}
