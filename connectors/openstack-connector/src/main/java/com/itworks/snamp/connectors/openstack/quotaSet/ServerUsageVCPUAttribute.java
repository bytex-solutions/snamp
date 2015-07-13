package com.itworks.snamp.connectors.openstack.quotaSet;

import com.google.common.base.MoreObjects;
import com.itworks.snamp.connectors.attributes.AttributeDescriptor;
import org.openstack4j.api.OSClient;
import org.openstack4j.model.compute.SimpleTenantUsage;

import javax.management.openmbean.SimpleType;
import java.math.BigDecimal;

/**
 * The total Virtual CPU Usage
 */
public final class ServerUsageVCPUAttribute extends AbstractServerUsageAttribute<BigDecimal> {
    public static final String NAME = "totalVcpusUsage";
    static final String DESCRIPTION = "The total Virtual CPU Usage";
    static final SimpleType<BigDecimal> TYPE = SimpleType.BIGDECIMAL;

    public ServerUsageVCPUAttribute(final String tenantID,
                                    final String attributeID,
                                    final AttributeDescriptor descriptor,
                                    final OSClient client){
        super(tenantID, attributeID, DESCRIPTION, TYPE, descriptor, client);
    }

    static BigDecimal getValueCore(final SimpleTenantUsage usage){
        return MoreObjects.firstNonNull(usage.getTotalVcpusUsage(), BigDecimal.ZERO);
    }

    @Override
    protected BigDecimal getValue(final SimpleTenantUsage usage) {
        return getValueCore(usage);
    }
}
