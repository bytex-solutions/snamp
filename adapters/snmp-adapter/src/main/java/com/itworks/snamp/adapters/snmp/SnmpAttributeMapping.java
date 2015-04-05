package com.itworks.snamp.adapters.snmp;

import com.itworks.snamp.adapters.AttributeAccessor;
import org.snmp4j.agent.DuplicateRegistrationException;
import org.snmp4j.agent.MOServer;
import org.snmp4j.agent.ManagedObjectValueAccess;
import org.snmp4j.agent.RegisteredManagedObject;
import org.snmp4j.smi.OID;

import javax.management.MBeanAttributeInfo;

/**
 * Represents SNMP mapping for the management connector attribute.
 * @author Roman Sakno
 */
interface SnmpAttributeMapping extends ManagedObjectValueAccess, RegisteredManagedObject, SnmpEntity<MBeanAttributeInfo> {
    boolean connect(final OID context, final MOServer server) throws DuplicateRegistrationException;
    AttributeAccessor disconnect(final MOServer server);
}
