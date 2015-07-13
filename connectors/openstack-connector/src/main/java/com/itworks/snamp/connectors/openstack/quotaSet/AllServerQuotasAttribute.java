package com.itworks.snamp.connectors.openstack.quotaSet;

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
import org.openstack4j.model.compute.QuotaSet;
import org.openstack4j.model.identity.Tenant;

import javax.management.openmbean.ArrayType;
import javax.management.openmbean.CompositeData;
import javax.management.openmbean.OpenDataException;
import java.util.List;
import java.util.concurrent.Callable;

/**
 * Information about all quotas.
 */
public final class AllServerQuotasAttribute extends OpenStackResourceAttribute<CompositeData[], QuotaSetService> {
    public static final String NAME = "serverQuotaSet";
    private static final String DESCRIPTION = "All available quotas over tenant groups";
    private static final ArrayType<CompositeData[]> TYPE = Utils.interfaceStaticInitialize(new Callable<ArrayType<CompositeData[]>>() {
        @Override
        public ArrayType<CompositeData[]> call() throws OpenDataException {
            return new ArrayType<>(1, ServerQuotaAttribute.TYPE);
        }
    });
    private final TenantService identityService;

    public AllServerQuotasAttribute(final String attributeId,
                                    final AttributeDescriptor descriptor,
                                    final OSClient client) {
        super(attributeId, DESCRIPTION, TYPE, AttributeSpecifier.READ_ONLY, descriptor, client.compute().quotaSets());
        if (client.supportsIdentity())
            identityService = client.identity().tenants();
        else identityService = null;
    }

    private List<QuotaSet> getQuotas() {
        if (identityService == null) return ImmutableList.of();
        List<? extends Tenant> tenants = identityService.list();
        if (tenants == null) tenants = ImmutableList.of();
        return Lists.transform(tenants, new Function<Tenant, QuotaSet>() {
            @Override
            public QuotaSet apply(final Tenant tenant) {
                return openStackService.get(tenant.getId());
            }
        });
    }

    @Override
    public CompositeData[] getValue() throws OpenDataException {
        final List<QuotaSet> quotas = getQuotas();
        final CompositeData[] result = new CompositeData[quotas.size()];
        for(int i = 0; i < quotas.size(); i++)
            result[i] = ServerQuotaAttribute.getValueCore(quotas.get(i));
        return result;
    }
}
