package com.itworks.snamp.adapters.snmp;

import com.itworks.snamp.connectors.attributes.AttributeMetadata;
import org.snmp4j.agent.ManagedObjectValueAccess;
import org.snmp4j.agent.RegisteredManagedObject;

import java.util.logging.Logger;

/**
 * Represents SNMP mapping for the management connector attribute.
 * @author Roman Sakno
 */
interface SnmpAttributeMapping extends ManagedObjectValueAccess, RegisteredManagedObject, SnmpEntity {
    static Logger log = SnmpHelpers.getLogger();

    /**
     * Returns the metadata of the underlying attribute.
     * @return The metadata of the underlying attribute.
     */
    AttributeMetadata getMetadata();
}
