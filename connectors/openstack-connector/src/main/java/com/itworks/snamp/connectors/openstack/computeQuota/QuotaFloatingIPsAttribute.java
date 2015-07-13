package com.itworks.snamp.connectors.openstack.computeQuota;

import com.itworks.snamp.connectors.attributes.AttributeDescriptor;
import org.openstack4j.api.OSClient;
import org.openstack4j.model.compute.QuotaSet;

import javax.management.openmbean.SimpleType;

/**
 * Represents a number of floating IPs available to the tenant group.
 * @author Roman Sakno
 * @since 1.0
 * @version 1.0
 */
public final class QuotaFloatingIPsAttribute extends AbstractQuotaAttribute<Integer> {
    public static final String NAME = "floatingIPs";
    static final String DESCRIPTION = "Number of floating IP addresses";
    static final SimpleType<Integer> TYPE = SimpleType.INTEGER;

    public QuotaFloatingIPsAttribute(final String tenantID,
                                     final String attributeID,
                                     final AttributeDescriptor descriptor,
                                     final OSClient client) {
        super(tenantID, attributeID, DESCRIPTION, TYPE, descriptor, client);
    }

    static int getValueCore(final QuotaSet quota) {
        return quota.getFloatingIps();
    }

    @Override
    protected Integer getValue(final QuotaSet quota) {
        return getValueCore(quota);
    }
}
