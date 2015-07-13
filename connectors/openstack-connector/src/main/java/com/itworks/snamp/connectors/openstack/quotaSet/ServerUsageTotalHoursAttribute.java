package com.itworks.snamp.connectors.openstack.quotaSet;

import com.itworks.snamp.connectors.attributes.AttributeDescriptor;
import org.openstack4j.api.OSClient;
import org.openstack4j.model.compute.SimpleTenantUsage;

import javax.management.openmbean.SimpleType;

/**
 * Total uptime.
 */
public final class ServerUsageTotalHoursAttribute extends AbstractServerUsageAttribute<String> {
    public static final String NAME = "serverUptime";
    static final String DESCRIPTION = "Total uptime, in hours";
    static final SimpleType<String> TYPE = SimpleType.STRING;

    public ServerUsageTotalHoursAttribute(final String tenantID,
                                          final String attributeID,
                                          final AttributeDescriptor descriptor,
                                          final OSClient client){
        super(tenantID, attributeID, DESCRIPTION, TYPE, descriptor, client);
    }

    static String getValueCore(final SimpleTenantUsage usage){
        return usage.getTotalHours();
    }

    @Override
    protected String getValue(final SimpleTenantUsage usage) {
        return getValueCore(usage);
    }
}
