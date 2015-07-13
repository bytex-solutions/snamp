package com.itworks.snamp.connectors.openstack.quotaSet;

import com.google.common.base.Function;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.itworks.snamp.connectors.attributes.AttributeDescriptor;
import com.itworks.snamp.connectors.attributes.AttributeSpecifier;
import com.itworks.snamp.connectors.openstack.OpenStackResourceAttribute;
import com.itworks.snamp.connectors.openstack.flavor.FlavorAttribute;
import com.itworks.snamp.internal.Utils;
import org.openstack4j.api.OSClient;
import org.openstack4j.api.identity.TenantService;
import org.openstack4j.api.storage.BlockQuotaSetService;
import org.openstack4j.model.compute.Flavor;
import org.openstack4j.model.compute.QuotaSet;
import org.openstack4j.model.identity.Tenant;
import org.openstack4j.model.storage.block.BlockQuotaSet;

import javax.management.openmbean.ArrayType;
import javax.management.openmbean.CompositeData;
import javax.management.openmbean.OpenDataException;
import java.util.List;
import java.util.concurrent.Callable;

/**
 * @author Roman Sakno
 * @version 1.0
 * @since 1.0
 */
public final class AllBlockQuotasAttribute extends OpenStackResourceAttribute<CompositeData[], BlockQuotaSetService> {
    public static final String NAME = "blockQuotaSet";
    private static final String DESCRIPTION = "Full set of block storage quotas";
    private static final ArrayType<CompositeData[]> TYPE = Utils.interfaceStaticInitialize(new Callable<ArrayType<CompositeData[]>>() {
        @Override
        public ArrayType<CompositeData[]> call() throws OpenDataException {
            return new ArrayType<>(1, BlockQuotaAttribute.TYPE);
        }
    });
    private final TenantService identityService;

    public AllBlockQuotasAttribute(final String attributeID,
                                   final AttributeDescriptor descriptor,
                                   final OSClient client){
        super(attributeID, DESCRIPTION, TYPE, AttributeSpecifier.READ_ONLY, descriptor, client.blockStorage().quotaSets());
        identityService = client.supportsIdentity() ? client.identity().tenants() : null;
    }

    private List<BlockQuotaSet> getQuotas() {
        if (identityService == null) return ImmutableList.of();
        List<? extends Tenant> tenants = identityService.list();
        if (tenants == null) tenants = ImmutableList.of();
        return Lists.transform(tenants, new Function<Tenant, BlockQuotaSet>() {
            @Override
            public BlockQuotaSet apply(final Tenant tenant) {
                return openStackService.get(tenant.getId());
            }
        });
    }

    /**
     * Gets value of this attribute.
     *
     * @return The value of this attribute.
     * @throws Exception Unable to read attribute value.
     */
    @Override
    public CompositeData[] getValue() throws OpenDataException {
        final List<BlockQuotaSet> quotas = getQuotas();
        final CompositeData[] result = new CompositeData[quotas.size()];
        for (int i = 0; i < quotas.size(); i++)
            result[i] = BlockQuotaAttribute.getValueCore(quotas.get(i));
        return result;
    }
}
