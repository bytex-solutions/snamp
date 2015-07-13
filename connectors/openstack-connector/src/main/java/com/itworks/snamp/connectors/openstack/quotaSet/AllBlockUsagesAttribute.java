package com.itworks.snamp.connectors.openstack.quotaSet;

import com.google.common.base.Function;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.itworks.snamp.connectors.attributes.AttributeDescriptor;
import com.itworks.snamp.connectors.attributes.AttributeSpecifier;
import com.itworks.snamp.connectors.openstack.OpenStackResourceAttribute;
import com.itworks.snamp.internal.Utils;
import org.openstack4j.api.OSClient;
import org.openstack4j.api.identity.TenantService;
import org.openstack4j.api.storage.BlockQuotaSetService;
import org.openstack4j.model.identity.Tenant;
import org.openstack4j.model.storage.block.BlockQuotaSetUsage;

import javax.management.openmbean.ArrayType;
import javax.management.openmbean.CompositeData;
import javax.management.openmbean.OpenDataException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;

/**
 * @author Roman Sakno
 * @version 1.0
 * @since 1.0
 */
public final class AllBlockUsagesAttribute extends OpenStackResourceAttribute<CompositeData[], BlockQuotaSetService> {
    public static final String NAME = "blockUsages";
    private static final String DESCRIPTION = "Full information about block storage utilization";
    private static final ArrayType<CompositeData[]> TYPE = Utils.interfaceStaticInitialize(new Callable<ArrayType<CompositeData[]>>() {
        @Override
        public ArrayType<CompositeData[]> call() throws OpenDataException {
            return new ArrayType<>(1, BlockUsageAttribute.TYPE);
        }
    });

    private final TenantService identityService;

    public AllBlockUsagesAttribute(final String attributeID,
                                   final AttributeDescriptor descriptor,
                                   final OSClient client) {
        super(attributeID, DESCRIPTION, TYPE, AttributeSpecifier.READ_ONLY, descriptor, client.blockStorage().quotaSets());
        identityService = client.supportsIdentity() ? client.identity().tenants() : null;
    }

    private Map<String, BlockQuotaSetUsage> getUsages() {
        if (identityService == null) return ImmutableMap.of();
        List<? extends Tenant> tenants = identityService.list();
        if (tenants == null) tenants = ImmutableList.of();
        final Map<String, BlockQuotaSetUsage> result = Maps.newHashMapWithExpectedSize(tenants.size());
        for(final Tenant t: tenants)
            result.put(t.getId(), openStackService.usageForTenant(t.getId()));
        return result;
    }

    /**
     * Gets value of this attribute.
     *
     * @return The value of this attribute.
     * @throws Exception Unable to read attribute value.
     */
    @Override
    public CompositeData[] getValue() throws OpenDataException {
        final Map<String, BlockQuotaSetUsage> usages = getUsages();
        final CompositeData[] result = new CompositeData[usages.size()];
        int i = 0;
        for (final Map.Entry<String, BlockQuotaSetUsage> entry : usages.entrySet())
            result[i++] = BlockUsageAttribute.getValueCore(entry.getValue(), entry.getKey());
        return result;
    }
}
