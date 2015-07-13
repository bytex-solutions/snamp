package com.itworks.snamp.connectors.openstack.computeQuota;

import com.google.common.base.Function;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.itworks.snamp.connectors.attributes.AttributeDescriptor;
import com.itworks.snamp.connectors.attributes.AttributeSpecifier;
import com.itworks.snamp.connectors.openstack.OpenStackResourceAttribute;
import com.itworks.snamp.internal.Utils;
import org.openstack4j.api.OSClient;
import org.openstack4j.api.compute.QuotaSetService;
import org.openstack4j.api.identity.TenantService;
import org.openstack4j.model.compute.SimpleTenantUsage;
import org.openstack4j.model.identity.Tenant;

import javax.management.openmbean.ArrayType;
import javax.management.openmbean.CompositeData;
import javax.management.openmbean.OpenDataException;
import java.util.List;
import java.util.concurrent.Callable;

/**
 * Information about all usages.
 */
public final class AllUsagesAttribute extends OpenStackResourceAttribute<CompositeData[], QuotaSetService> {
    public static final String NAME = "computeQuotaUsages";
    private static final String DESCRIPTION = "All available quota usages over tenant groups";
    private static final ArrayType<CompositeData[]> TYPE = Utils.interfaceStaticInitialize(new Callable<ArrayType<CompositeData[]>>() {
        @Override
        public ArrayType<CompositeData[]> call() throws OpenDataException {
            return new ArrayType<>(1, UsageAttribute.TYPE);
        }
    });
    private final TenantService identityService;

    public AllUsagesAttribute(final String attributeId,
                              final AttributeDescriptor descriptor,
                              final OSClient client) {
        super(attributeId, DESCRIPTION, TYPE, AttributeSpecifier.READ_ONLY, descriptor, client.compute().quotaSets());
        if (client.supportsIdentity())
            identityService = client.identity().tenants();
        else identityService = null;
    }

    private List<SimpleTenantUsage> getUsages() {
        if (identityService == null) return ImmutableList.of();
        List<? extends Tenant> tenants = identityService.list();
        if (tenants == null) tenants = ImmutableList.of();
        return Lists.transform(tenants, new Function<Tenant, SimpleTenantUsage>() {
            @Override
            public SimpleTenantUsage apply(final Tenant tenant) {
                return openStackService.getTenantUsage(tenant.getId());
            }
        });
    }

    @Override
    public CompositeData[] getValue() throws OpenDataException {
        final List<SimpleTenantUsage> usages = getUsages();
        final CompositeData[] result = new CompositeData[usages.size()];
        for(int i = 0; i < usages.size(); i++)
            result[i] = UsageAttribute.getValueCore(usages.get(i));
        return result;
    }
}
