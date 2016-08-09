package com.bytex.snamp.adapters.snmp;

import org.snmp4j.agent.*;
import org.snmp4j.smi.OID;

import javax.management.MBeanAttributeInfo;
import java.text.ParseException;


/**
 * @author Roman Sakno
 * @version 2.0
 * @since 1.0
 */
final class SnmpAttributeAccessorImpl extends SnmpAttributeAccessor {
    private final OID attributeID;

    SnmpAttributeAccessorImpl(final MBeanAttributeInfo metadata) throws ParseException {
        super(metadata);
        attributeID = SnmpAdapterDescriptionProvider.parseOID(metadata, SnmpHelpers.OID_GENERATOR);
    }

    @Override
    SnmpAttributeMapping registerManagedObject(final OID context,
                                               final MOServer server) throws DuplicateRegistrationException {
        //do not add the attribute with invalid prefix
        final SnmpAttributeMapping mapping;
        if (attributeID.startsWith(context)) {
            mapping = getSnmpType().createManagedObject(this);
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
