package com.bytex.snamp.gateway.snmp;

import com.bytex.snamp.gateway.modeling.AttributeAccessor;
import org.snmp4j.agent.DuplicateRegistrationException;
import org.snmp4j.agent.MOServer;
import org.snmp4j.agent.ManagedObject;
import org.snmp4j.smi.OID;

import javax.management.MBeanAttributeInfo;
import java.util.Objects;

/**
 * @author Roman Sakno
 * @version 2.1
 * @since 1.0
 */
abstract class SnmpAttributeAccessor extends AttributeAccessor implements SnmpEntity<MBeanAttributeInfo> {
    SnmpAttributeAccessor(final MBeanAttributeInfo metadata) {
        super(metadata);
    }

    abstract SnmpAttributeMapping registerManagedObject(final OID context,
                                               final MOServer server) throws DuplicateRegistrationException;

    abstract ManagedObject unregisterManagedObject(final MOServer server);

    final SnmpType getSnmpType(){
        return SnmpType.map(getType());
    }

    @Override
    public final boolean equals(final MBeanAttributeInfo metadata) {
        return Objects.equals(metadata, get());
    }
}
