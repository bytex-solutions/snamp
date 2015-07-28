package com.bytex.snamp.adapters.snmp;

import com.bytex.snamp.adapters.modeling.AttributeAccessor;
import org.snmp4j.agent.DuplicateRegistrationException;
import org.snmp4j.agent.MOServer;
import org.snmp4j.agent.ManagedObject;
import org.snmp4j.smi.OID;

import javax.management.MBeanAttributeInfo;
import java.util.Objects;

/**
 * @author Roman Sakno
 * @version 1.0
 * @since 1.0
 */
abstract class SnmpAttributeAccessor extends AttributeAccessor implements SnmpEntity<MBeanAttributeInfo> {
    SnmpAttributeAccessor(final MBeanAttributeInfo metadata) {
        super(metadata);
    }

    abstract SnmpAttributeMapping registerManagedObject(final OID context,
                                               final SnmpTypeMapper typeMapper,
                                               final MOServer server) throws DuplicateRegistrationException;

    abstract ManagedObject unregisterManagedObject(final MOServer server);

    final SnmpType getType(final SnmpTypeMapper mapper){
        return mapper.apply(getType());
    }

    @Override
    public final boolean equals(final MBeanAttributeInfo metadata) {
        return Objects.equals(metadata, get());
    }
}
