package com.itworks.snamp.adapters.snmp;

import com.google.common.base.Function;
import com.itworks.snamp.jmx.WellKnownType;
import org.snmp4j.agent.*;
import org.snmp4j.smi.OID;

import javax.management.MBeanAttributeInfo;
import java.text.ParseException;

import static com.itworks.snamp.adapters.snmp.SnmpAdapterConfigurationDescriptor.parseOID;


/**
 * @author Roman Sakno
 * @version 1.0
 * @since 1.0
 */
public final class SnmpAttributeAccessorImpl extends SnmpAttributeAccessor {
    private final OID attributeID;

    SnmpAttributeAccessorImpl(final MBeanAttributeInfo metadata) throws ParseException {
        super(metadata);
        attributeID = parseOID(metadata);
    }

    public SnmpType getType(final Function<WellKnownType, SnmpType> mapper){
        return mapper.apply(getType());
    }

    @Override
    SnmpAttributeMapping registerManagedObject(final OID context,
                                               final SnmpTypeMapper typeMapper,
                                               final MOServer server) throws DuplicateRegistrationException {
        final SnmpAttributeMapping mapping;
        final SnmpType attributeType = getType(typeMapper);
        assert attributeType != null : attributeType;
        //do not add the attribute with invalid prefix
        if (attributeID.startsWith(context)) {
            mapping = attributeType.createManagedObject(this);
            server.register(mapping, null);
        }
        else mapping = null;
        return mapping;
    }

    private static ManagedObject unregisterManagedObject(final OID attributeID,
                                                         final MOServer server){
        final MOQuery query = new DefaultMOQuery(new DefaultMOContextScope(null, attributeID, true, attributeID, true));
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
