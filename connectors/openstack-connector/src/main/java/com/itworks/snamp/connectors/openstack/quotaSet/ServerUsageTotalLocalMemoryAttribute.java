package com.itworks.snamp.connectors.openstack.quotaSet;

import com.google.common.base.MoreObjects;
import com.itworks.snamp.connectors.attributes.AttributeDescriptor;
import org.openstack4j.api.OSClient;
import org.openstack4j.model.compute.SimpleTenantUsage;

import javax.management.openmbean.SimpleType;
import java.math.BigDecimal;

/**
 * The total local GB of data used
 */
public final class ServerUsageTotalLocalMemoryAttribute extends AbstractServerUsageAttribute<BigDecimal> {
    public static final String NAME = "totalLocalMemoryUsage";
    static final String DESCRIPTION = "the total memory usage in MB";
    static final SimpleType<BigDecimal> TYPE = SimpleType.BIGDECIMAL;

    public ServerUsageTotalLocalMemoryAttribute(final String tenantID,
                                                final String attributeID,
                                                final AttributeDescriptor descriptor,
                                                final OSClient client){
        super(tenantID, attributeID, DESCRIPTION, TYPE, descriptor.setUnit("MB"), client);
    }

    static BigDecimal getValueCore(final SimpleTenantUsage usage){
        return MoreObjects.firstNonNull(usage.getTotalMemoryMbUsage(), BigDecimal.ZERO);
    }

    @Override
    protected BigDecimal getValue(final SimpleTenantUsage usage) {
        return getValueCore(usage);
    }
}
