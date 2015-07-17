package com.itworks.snamp.adapters.snmp.runtime;

import com.google.common.base.Function;
import com.itworks.snamp.adapters.AttributeAccessor;
import com.itworks.snamp.adapters.runtime.AttributeBinding;
import com.itworks.snamp.adapters.snmp.SnmpType;
import com.itworks.snamp.jmx.WellKnownType;

import java.text.ParseException;

import static com.itworks.snamp.adapters.snmp.SnmpAdapterConfigurationDescriptor.parseOID;

/**
 * @author Roman Sakno
 * @version 1.0
 * @since 1.0
 */
public final class SnmpAttributeBinding extends AttributeBinding {
    private final SnmpType mappedType;

    public SnmpAttributeBinding(final String declaredResource,
                                final AttributeAccessor accessor,
                                final Function<WellKnownType, SnmpType> typeMapper) throws ParseException {
        super(declaredResource, accessor);
        final WellKnownType attributeType = accessor.getType();
        mappedType = attributeType == null ? null : typeMapper.apply(attributeType);
        put("OID", parseOID(accessor));
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
