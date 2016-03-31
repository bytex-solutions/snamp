package com.bytex.snamp.adapters.snmp;

import org.snmp4j.agent.*;
import org.snmp4j.smi.OID;

import javax.management.MBeanAttributeInfo;
import java.text.ParseException;

import static com.bytex.snamp.adapters.snmp.SnmpAdapterConfigurationDescriptor.parseOID;


/**
 * @author Roman Sakno
 * @version 1.2
 * @since 1.0
 */
final class SnmpAttributeAccessorImpl extends SnmpAttributeAccessor {
    private final OID attributeID;

    SnmpAttributeAccessorImpl(final MBeanAttributeInfo metadata) throws ParseException {
        super(metadata);
        attributeID = parseOID(metadata);
    }

    @Override
    SnmpAttributeMapping registerManagedObject(final OID context,
                                               final SnmpTypeMapper typeMapper,
                                               final MOServer server) throws DuplicateRegistrationException {
        final SnmpType attributeType = getType(typeMapper);
        assert attributeType != null;
        //do not add the attribute with invalid prefix
        final SnmpAttributeMapping mapping;
        if (attributeID.startsWith(context)) {
            mapping = attributeType.createManagedObject(this);
            server.register(mapping, null);
        }
        else mapping = null;
        return mapping;
    }

    private static ManagedObject unregisterManagedObject(final OID attributeID,
                                                         final MOServer server){
        final MOQuery query = new DefaultMOQuery(new DefaultMOContextScope(null, attributeID, true, attributeID.nextPeer(), true));
        ManagedObject result = server.lookup(query);
        if(result != null)
            result = server.unregister(result, null);
        return result;
    }

    @Override
    ManagedObject unregisterManagedObject(final MOServer server) {
        return unregisterManagedObject(attributeID, server);
    }

    @Override
    public OID getID() {
        return attributeID;
    }
}
