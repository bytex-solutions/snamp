package com.itworks.snamp.connectors.openstack.quotaSet;

import com.google.common.collect.Maps;
import com.itworks.snamp.connectors.attributes.AttributeDescriptor;
import com.itworks.snamp.internal.Utils;
import com.itworks.snamp.jmx.CompositeTypeBuilder;
import org.openstack4j.api.OSClient;
import org.openstack4j.model.compute.SimpleTenantUsage;

import javax.management.openmbean.*;
import java.util.Map;
import java.util.concurrent.Callable;

/**
 * Full information about level of resources utilization for the tenant group.
 */
public final class ServerUsageAttribute extends AbstractServerUsageAttribute<CompositeData> {
    public static final String NAME = "tenantUsage";
    private static final String DESCRIPTION = "Full information about tenant usage";
    static final CompositeType TYPE = Utils.interfaceStaticInitialize(new Callable<CompositeType>() {
        @Override
        public CompositeType call() throws OpenDataException {
            return new CompositeTypeBuilder("TenantUsage", "OpenStack Tenant usage")
                    .addItem(ServerUsageTotalHoursAttribute.NAME, ServerUsageTotalHoursAttribute.DESCRIPTION, ServerUsageTotalHoursAttribute.TYPE)
                    .addItem(ServerUsageTotalLocalDiskAttribute.NAME, ServerUsageTotalLocalDiskAttribute.DESCRIPTION, ServerUsageTotalLocalDiskAttribute.TYPE)
                    .addItem(ServerUsageTotalLocalMemoryAttribute.NAME, ServerUsageTotalLocalMemoryAttribute.DESCRIPTION, ServerUsageTotalLocalMemoryAttribute.TYPE)
                    .addItem(ServerUsageVCPUAttribute.NAME, ServerUsageVCPUAttribute.DESCRIPTION, ServerUsageVCPUAttribute.TYPE)
                    .addItem(TENANT_NAME, TENANT_DESCR, TENANT_TYPE)
                    .build();
        }
    });

    public ServerUsageAttribute(final String tenantID,
                                final String attributeID,
                                final AttributeDescriptor descriptor,
                                final OSClient client){
        super(tenantID, attributeID, DESCRIPTION, TYPE, descriptor, client);
    }

    static CompositeData getValueCore(final SimpleTenantUsage usage) throws OpenDataException {
        final Map<String, Object> result = Maps.newHashMapWithExpectedSize(TYPE.keySet().size());
        result.put(TENANT_NAME, usage.getTenantId());
        result.put(ServerUsageTotalHoursAttribute.NAME, ServerUsageTotalHoursAttribute.getValueCore(usage));
        result.put(ServerUsageTotalLocalDiskAttribute.NAME, ServerUsageTotalLocalDiskAttribute.getValueCore(usage));
        result.put(ServerUsageTotalLocalMemoryAttribute.NAME, ServerUsageTotalLocalMemoryAttribute.getValueCore(usage));
        result.put(ServerUsageVCPUAttribute.NAME, ServerUsageVCPUAttribute.getValueCore(usage));
        return new CompositeDataSupport(TYPE, result);
    }

    @Override
    protected CompositeData getValue(final SimpleTenantUsage usage) throws OpenDataException {
        return getValueCore(usage);
    }
}
