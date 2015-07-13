package com.itworks.snamp.connectors.openstack.computeQuota;

import com.itworks.snamp.connectors.attributes.AttributeDescriptor;
import org.openstack4j.api.OSClient;
import org.openstack4j.model.compute.QuotaSet;

import javax.management.openmbean.SimpleType;

/**
 * Represents a number of gigabytes allowed.
 * @author Roman Sakno
 * @since 1.0
 * @version 1.0
 */
public final class QuotaGigabytesAttribute extends AbstractQuotaAttribute<Integer> {
    public static final String NAME = "gigabytes";
    static final String DESCRIPTION = "Number of gigabytes allowed";
    static final SimpleType<Integer> TYPE = SimpleType.INTEGER;

    public QuotaGigabytesAttribute(final String tenantID,
                                     final String attributeID,
                                     final AttributeDescriptor descriptor,
                                     final OSClient client) {
        super(tenantID, attributeID, DESCRIPTION, TYPE, descriptor.setUnit("GB"), client);
    }

    static int getValueCore(final QuotaSet quota) {
        return quota.getGigabytes();
    }

    @Override
    protected Integer getValue(final QuotaSet quota) {
        return getValueCore(quota);
    }
}
