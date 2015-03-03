package com.itworks.snamp.adapters.snmp;

import com.itworks.snamp.Aggregator;
import org.snmp4j.agent.ManagedObjectValueAccess;
import org.snmp4j.agent.RegisteredManagedObject;

import javax.management.MBeanAttributeInfo;

/**
 * Represents SNMP mapping for the management connector attribute.
 * @author Roman Sakno
 */
interface SnmpAttributeMapping extends ManagedObjectValueAccess, RegisteredManagedObject, SnmpEntity, Aggregator {

    /**
     * Returns the metadata of the underlying attribute.
     * @return The metadata of the underlying attribute.
     */
    MBeanAttributeInfo getMetadata();
}
