package com.itworks.snamp.connectors.openstack.computeQuota;

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
public final class UsageAttribute extends AbstractTenantUsageAttribute<CompositeData> {
    public static final String NAME = "tenantUsageInfo";
    private static final String DESCRIPTION = "Full information about tenant usage";
    private static final String ID_NAME = "tenantID";
    private static final String ID_DESCR = "ID of the tenant group";
    static final CompositeType TYPE = Utils.interfaceStaticInitialize(new Callable<CompositeType>() {
        @Override
        public CompositeType call() throws OpenDataException {
            return new CompositeTypeBuilder("TenantUsage", "OpenStack Tenant usage")
                    .addItem(UsageTotalHoursAttribute.NAME, UsageTotalHoursAttribute.DESCRIPTION, UsageTotalHoursAttribute.TYPE)
                    .addItem(UsageTotalLocalDiskAttribute.NAME, UsageTotalLocalDiskAttribute.DESCRIPTION, UsageTotalLocalDiskAttribute.TYPE)
                    .addItem(UsageTotalLocalMemoryAttribute.NAME, UsageTotalLocalMemoryAttribute.DESCRIPTION, UsageTotalLocalMemoryAttribute.TYPE)
                    .addItem(UsageVCPUAttribute.NAME, UsageVCPUAttribute.DESCRIPTION, UsageVCPUAttribute.TYPE)
                    .addItem(ID_NAME, ID_DESCR, SimpleType.STRING)
                    .build();
        }
    });

    public UsageAttribute(final String tenantID,
                          final String attributeID,
                          final AttributeDescriptor descriptor,
                          final OSClient client){
        super(tenantID, attributeID, DESCRIPTION, TYPE, descriptor, client);
    }

    static CompositeData getValueCore(final SimpleTenantUsage usage) throws OpenDataException {
        final Map<String, Object> result = Maps.newHashMapWithExpectedSize(TYPE.keySet().size());
        result.put(ID_NAME, usage.getTenantId());
        result.put(UsageTotalHoursAttribute.NAME, UsageTotalHoursAttribute.getValueCore(usage));
        result.put(UsageTotalLocalDiskAttribute.NAME, UsageTotalLocalDiskAttribute.getValueCore(usage));
        result.put(UsageTotalLocalMemoryAttribute.NAME, UsageTotalLocalMemoryAttribute.getValueCore(usage));
        result.put(UsageVCPUAttribute.NAME, UsageVCPUAttribute.getValueCore(usage));
        return new CompositeDataSupport(TYPE, result);
    }

    @Override
    protected CompositeData getValue(final SimpleTenantUsage usage) throws OpenDataException {
        return getValueCore(usage);
    }
}
