package com.snamp.adapters;

import com.snamp.connectors.AttributeMetadata;
import org.snmp4j.agent.ManagedObjectValueAccess;
import java.util.logging.Logger;

/**
 * Represents SNMP mapping for the management connector attribute.
 * @author Roman Sakno
 */
interface SnmpAttributeMapping extends ManagedObjectValueAccess {
    static Logger log = Logger.getLogger("snamp.snmp.log");
    /**
     * Returns the metadata of the underlying attribute.
     * @return The metadata of the underlying attribute.
     */
    public AttributeMetadata getMetadata();

}
