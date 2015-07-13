package com.itworks.snamp.connectors.openstack.quotaSet;

import com.google.common.collect.Maps;
import com.itworks.snamp.connectors.attributes.AttributeDescriptor;
import com.itworks.snamp.internal.Utils;
import com.itworks.snamp.jmx.CompositeTypeBuilder;
import org.openstack4j.api.OSClient;
import org.openstack4j.model.compute.QuotaSet;

import javax.management.openmbean.*;
import java.util.Map;
import java.util.concurrent.Callable;

/**
 * Exposes access to all quotas.
 * @author Roman Sakno
 * @since 1.0
 * @version 1.0
 */
public final class ServerQuotaAttribute extends AbstractServerQuotaAttribute<CompositeData> {
    public static final String NAME = "serverQuota";
    private static final String DESCRIPTION = "Full information about server quota";

    static final CompositeType TYPE = Utils.interfaceStaticInitialize(new Callable<CompositeType>() {
        @Override
        public CompositeType call() throws OpenDataException {
            return new CompositeTypeBuilder("QuotaSet", "OpenStack QuotaSet")
                    .addItem(ServerQuotaCoresAttribute.NAME, ServerQuotaCoresAttribute.DESCRIPTION, ServerQuotaCoresAttribute.TYPE)
                    .addItem(ServerQuotaFloatingIPsAttribute.NAME, ServerQuotaFloatingIPsAttribute.DESCRIPTION, ServerQuotaFloatingIPsAttribute.TYPE)
                    .addItem(ServerQuotaGigabytesAttribute.NAME, ServerQuotaGigabytesAttribute.DESCRIPTION, ServerQuotaGigabytesAttribute.TYPE)
                    .addItem(ServerQuotaInstancesAttribute.NAME, ServerQuotaInstancesAttribute.DESCRIPTION, ServerQuotaInstancesAttribute.TYPE)
                    .addItem(ServerQuotaRamAttribute.NAME, ServerQuotaRamAttribute.DESCRIPTION, ServerQuotaRamAttribute.TYPE)
                    .addItem(ServerQuotaVolumesAttribute.NAME, ServerQuotaVolumesAttribute.DESCRIPTION, ServerQuotaVolumesAttribute.TYPE)
                    .addItem(TENANT_NAME, TENANT_DESCR, TENANT_TYPE)
                    .build();
        }
    });

    public ServerQuotaAttribute(final String tenantID,
                                final String attributeID,
                                final AttributeDescriptor descriptor,
                                final OSClient client){
        super(tenantID, attributeID, DESCRIPTION, TYPE, descriptor, client);
    }

    static CompositeData getValueCore(final QuotaSet quota) throws OpenDataException {
        final Map<String, Object> result = Maps.newHashMapWithExpectedSize(TYPE.keySet().size());
        result.put(ServerQuotaCoresAttribute.NAME, ServerQuotaCoresAttribute.getValueCore(quota));
        result.put(ServerQuotaFloatingIPsAttribute.NAME, ServerQuotaFloatingIPsAttribute.getValueCore(quota));
        result.put(ServerQuotaGigabytesAttribute.NAME, ServerQuotaGigabytesAttribute.getValueCore(quota));
        result.put(ServerQuotaInstancesAttribute.NAME, ServerQuotaInstancesAttribute.getValueCore(quota));
        result.put(ServerQuotaRamAttribute.NAME, ServerQuotaRamAttribute.getValueCore(quota));
        result.put(ServerQuotaVolumesAttribute.NAME, ServerQuotaVolumesAttribute.getValueCore(quota));
        result.put(TENANT_NAME, quota.getId());
        return new CompositeDataSupport(TYPE, result);
    }

    @Override
    protected CompositeData getValue(final QuotaSet quota) throws OpenDataException {
        return getValueCore(quota);
    }
}
