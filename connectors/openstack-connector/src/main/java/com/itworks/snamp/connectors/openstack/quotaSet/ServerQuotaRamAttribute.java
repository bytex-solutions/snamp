package com.itworks.snamp.connectors.openstack.quotaSet;

import com.itworks.snamp.connectors.attributes.AttributeDescriptor;
import org.openstack4j.api.OSClient;
import org.openstack4j.model.compute.QuotaSet;

import javax.management.openmbean.SimpleType;

/**
 * Represents quantity of instanceable RAM (MBytes).
 * @author Roman Sakno
 * @since 1.0
 * @version 1.0
 */
public final class ServerQuotaRamAttribute extends AbstractServerQuotaAttribute<Integer> {
    public static final String NAME = "RAM";
    static final String DESCRIPTION = "Quantity of instanceable RAM (MBytes)";
    static final SimpleType<Integer> TYPE = SimpleType.INTEGER;

    public ServerQuotaRamAttribute(final String tenantID,
                                   final String attributeID,
                                   final AttributeDescriptor descriptor,
                                   final OSClient client) {
        super(tenantID, attributeID, DESCRIPTION, TYPE, descriptor.setUnit("MB"), client);
    }

    static int getValueCore(final QuotaSet quota) {
        return quota.getRam();
    }

    @Override
    protected Integer getValue(final QuotaSet quota) {
        return getValueCore(quota);
    }
}