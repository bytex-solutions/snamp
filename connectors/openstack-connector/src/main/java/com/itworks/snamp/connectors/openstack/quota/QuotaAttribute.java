package com.itworks.snamp.connectors.openstack.quota;

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
public final class QuotaAttribute extends AbstractQuotaAttribute<CompositeData> {
    public static final String NAME = "quotaInfo";
    private static final String DESCRIPTION = "Full information about quotas";
    private static final String ID_NAME = "tenantID";
    private static final String ID_DESCR = "ID of the tenant group";
    static final CompositeType TYPE = Utils.interfaceStaticInitialize(new Callable<CompositeType>() {
        @Override
        public CompositeType call() throws OpenDataException {
            return new CompositeTypeBuilder("QuotaSet", "OpenStack QuotaSet")
                    .addItem(QuotaCoresAttribute.NAME, QuotaCoresAttribute.DESCRIPTION, QuotaCoresAttribute.TYPE)
                    .addItem(QuotaFloatingIPsAttribute.NAME, QuotaFloatingIPsAttribute.DESCRIPTION, QuotaFloatingIPsAttribute.TYPE)
                    .addItem(QuotaGigabytesAttribute.NAME, QuotaGigabytesAttribute.DESCRIPTION, QuotaGigabytesAttribute.TYPE)
                    .addItem(QuotaInstancesAttribute.NAME, QuotaInstancesAttribute.DESCRIPTION, QuotaInstancesAttribute.TYPE)
                    .addItem(QuotaRamAttribute.NAME, QuotaRamAttribute.DESCRIPTION, QuotaRamAttribute.TYPE)
                    .addItem(QuotaVolumesAttribute.NAME, QuotaVolumesAttribute.DESCRIPTION, QuotaVolumesAttribute.TYPE)
                    .addItem(ID_NAME, ID_DESCR, SimpleType.STRING)
                    .build();
        }
    });

    public QuotaAttribute(final String tenantID,
                          final String attributeID,
                          final AttributeDescriptor descriptor,
                          final OSClient client){
        super(tenantID, attributeID, DESCRIPTION, TYPE, descriptor, client);
    }

    static CompositeData getValueCore(final QuotaSet quota) throws OpenDataException {
        final Map<String, Object> result = Maps.newHashMapWithExpectedSize(TYPE.keySet().size());
        result.put(QuotaCoresAttribute.NAME, QuotaCoresAttribute.getValueCore(quota));
        result.put(QuotaFloatingIPsAttribute.NAME, QuotaFloatingIPsAttribute.getValueCore(quota));
        result.put(QuotaGigabytesAttribute.NAME, QuotaGigabytesAttribute.getValueCore(quota));
        result.put(QuotaInstancesAttribute.NAME, QuotaInstancesAttribute.getValueCore(quota));
        result.put(QuotaRamAttribute.NAME, QuotaRamAttribute.getValueCore(quota));
        result.put(QuotaVolumesAttribute.NAME, QuotaVolumesAttribute.getValueCore(quota));
        result.put(ID_NAME, quota.getId());
        return new CompositeDataSupport(TYPE, result);
    }

    @Override
    protected CompositeData getValue(final QuotaSet quota) throws OpenDataException {
        return getValueCore(quota);
    }
}
