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
public final class ServerUsageTotalLocalDiskAttribute extends AbstractServerUsageAttribute<BigDecimal> {
    public static final String NAME = "totalLocalDiskUsage";
    static final String DESCRIPTION = "The total local GB of data used";
    static final SimpleType<BigDecimal> TYPE = SimpleType.BIGDECIMAL;

    public ServerUsageTotalLocalDiskAttribute(final String tenantID,
                                              final String attributeID,
                                              final AttributeDescriptor descriptor,
                                              final OSClient client){
        super(tenantID, attributeID, DESCRIPTION, TYPE, descriptor.setUnit("GB"), client);
    }

    static BigDecimal getValueCore(final SimpleTenantUsage usage){
        return MoreObjects.firstNonNull(usage.getTotalLocalGbUsage(), BigDecimal.ZERO);
    }

    @Override
    protected BigDecimal getValue(final SimpleTenantUsage usage) {
        return getValueCore(usage);
    }
}
