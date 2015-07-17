package com.itworks.snamp.adapters.snmp.runtime;

import com.google.common.base.Function;
import com.google.common.collect.Lists;
import com.google.common.collect.Multimap;
import com.itworks.snamp.adapters.AttributeAccessor;
import com.itworks.snamp.adapters.runtime.AttributeBinding;
import com.itworks.snamp.adapters.snmp.SnmpAttributeAccessorImpl;
import com.itworks.snamp.adapters.snmp.SnmpType;
import com.itworks.snamp.jmx.WellKnownType;

import java.text.ParseException;
import java.util.Collection;
import java.util.List;

/**
 * @author Roman Sakno
 * @version 1.0
 * @since 1.0
 */
final class SnmpAttributeBinding extends AttributeBinding {
    private final SnmpType mappedType;

    SnmpAttributeBinding(final String declaredResource,
                                final SnmpAttributeAccessorImpl accessor,
                                final Function<WellKnownType, SnmpType> typeMapper) {
        super(declaredResource, accessor);
        mappedType = accessor.getType(typeMapper);
        put("OID", accessor.getID());
    }

    /**
     * Gets information about attribute type inside of the adapter.
     *
     * @return The information about attribute type inside of the adapter.
     */
    @Override
    public SnmpType getMappedType() {
        return mappedType;
    }
}
