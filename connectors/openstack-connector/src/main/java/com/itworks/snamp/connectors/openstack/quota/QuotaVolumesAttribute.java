package com.itworks.snamp.connectors.openstack.quota;

import com.itworks.snamp.connectors.attributes.AttributeDescriptor;
import org.openstack4j.api.OSClient;
import org.openstack4j.model.compute.QuotaSet;

import javax.management.openmbean.SimpleType;

/**
 * Represents a number of permitted volumes
 * @author Roman Sakno
 * @since 1.0
 * @version 1.0
 */
public final class QuotaVolumesAttribute extends AbstractQuotaAttribute<Integer> {
    public static final String NAME = "volumes";
    static final String DESCRIPTION = "Quantity of permitted volumes";
    static final SimpleType<Integer> TYPE = SimpleType.INTEGER;

    public QuotaVolumesAttribute(final String tenantID,
                             final String attributeID,
                             final AttributeDescriptor descriptor,
                             final OSClient client) {
        super(tenantID, attributeID, DESCRIPTION, TYPE, descriptor, client);
    }

    static int getValueCore(final QuotaSet quota) {
        return quota.getVolumes();
    }

    @Override
    protected Integer getValue(final QuotaSet quota) {
        return getValueCore(quota);
    }
}
