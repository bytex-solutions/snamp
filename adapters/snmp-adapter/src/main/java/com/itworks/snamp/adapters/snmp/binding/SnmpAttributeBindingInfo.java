package com.itworks.snamp.adapters.snmp.binding;

import com.google.common.base.Function;
import com.itworks.snamp.adapters.binding.AttributeBindingInfo;
import com.itworks.snamp.adapters.snmp.SnmpAttributeAccessorImpl;
import com.itworks.snamp.adapters.snmp.SnmpType;
import com.itworks.snamp.jmx.WellKnownType;

/**
 * @author Roman Sakno
 * @version 1.0
 * @since 1.0
 */
final class SnmpAttributeBindingInfo extends AttributeBindingInfo {
    private final SnmpType mappedType;

    SnmpAttributeBindingInfo(final String declaredResource,
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
