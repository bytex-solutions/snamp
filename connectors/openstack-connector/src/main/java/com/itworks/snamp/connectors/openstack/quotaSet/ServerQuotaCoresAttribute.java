package com.itworks.snamp.connectors.openstack.quotaSet;

import com.itworks.snamp.connectors.attributes.AttributeDescriptor;
import org.openstack4j.api.OSClient;
import org.openstack4j.model.compute.QuotaSet;

import javax.management.openmbean.SimpleType;

/**
 * Represents a number of instanceable cores.
 * @author Roman Sakno
 * @since 1.0
 * @version 1.0
 */
public final class ServerQuotaCoresAttribute extends AbstractServerQuotaAttribute<Integer> {
    public static final String NAME = "cores";
    static final String DESCRIPTION = "Number of instanceable cores";
    static final SimpleType<Integer> TYPE = SimpleType.INTEGER;

    public ServerQuotaCoresAttribute(final String tenantID,
                                     final String attributeID,
                                     final AttributeDescriptor descriptor,
                                     final OSClient client){
        super(tenantID, attributeID, DESCRIPTION, TYPE, descriptor, client);
    }

    static int getValueCore(final QuotaSet quota){
        return quota.getCores();
    }

    @Override
    protected Integer getValue(final QuotaSet quota) {
        return getValueCore(quota);
    }
}
