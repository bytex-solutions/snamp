package com.itworks.snamp.connectors.openstack.quotaSet;

import com.itworks.snamp.connectors.attributes.AttributeDescriptor;
import com.itworks.snamp.connectors.openstack.OpenStackResourceAttribute;
import org.openstack4j.api.OSClient;
import org.openstack4j.api.storage.BlockQuotaSetService;
import org.openstack4j.model.compute.QuotaSet;
import org.openstack4j.model.storage.block.BlockQuotaSet;
import org.openstack4j.model.storage.block.BlockQuotaSetUsage;

import javax.management.MBeanException;
import javax.management.openmbean.OpenType;

/**
 * @author Roman Sakno
 * @version 1.0
 * @since 1.0
 */
abstract class AbstractBlockUsageAttribute<T> extends AbstractQuotaAttribute<T, BlockQuotaSetService> {
    AbstractBlockUsageAttribute(final String tenantID,
                                final String attributeID,
                                final String description,
                                final OpenType<T> attributeType,
                                final AttributeDescriptor descriptor,
                                final OSClient openStackService) {
        super(tenantID,
                attributeID,
                description,
                attributeType,
                descriptor,
                openStackService.blockStorage().quotaSets());
    }

    private final BlockQuotaSetUsage getQuotaSet() {
        return openStackService.usageForTenant(tenantID);
    }

    /**
     * Gets value of this attribute.
     *
     * @return The value of this attribute.
     * @throws Exception Unable to read attribute value.
     */
    @Override
    public final T getValue() throws Exception {
        final BlockQuotaSetUsage f = getQuotaSet();
        if (f == null)
            throw new MBeanException(new IllegalArgumentException(String.format("Tenant '%s' doesn't exist", tenantID)));
        else return getValue(f);
    }

    protected abstract T getValue(final BlockQuotaSetUsage usage) throws Exception;
}
