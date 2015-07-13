package com.itworks.snamp.connectors.openstack.quotaSet;

import com.itworks.snamp.connectors.attributes.AttributeDescriptor;
import com.itworks.snamp.connectors.attributes.AttributeSpecifier;
import com.itworks.snamp.connectors.openstack.OpenStackResourceAttribute;
import org.openstack4j.api.OSClient;
import org.openstack4j.api.compute.QuotaSetService;
import org.openstack4j.model.compute.SimpleTenantUsage;

import javax.management.MBeanException;
import javax.management.openmbean.OpenType;

/**
 * Represents a superclass for quota usages.
 */
abstract class AbstractServerUsageAttribute<T> extends AbstractQuotaAttribute<T, QuotaSetService> {
    AbstractServerUsageAttribute(final String tenantID,
                                 final String attributeID,
                                 final String description,
                                 final OpenType<T> attributeType,
                                 final AttributeDescriptor descriptor,
                                 final OSClient openStackService) {
        super(tenantID, attributeID, description, attributeType, descriptor, openStackService.compute().quotaSets());
    }

    private final SimpleTenantUsage getTenantUsage() {
        return openStackService.getTenantUsage(tenantID);
    }

    /**
     * Gets value of this attribute.
     *
     * @return The value of this attribute.
     * @throws Exception Unable to read attribute value.
     */
    @Override
    public final T getValue() throws Exception {
        final SimpleTenantUsage f = getTenantUsage();
        if (f == null)
            throw new MBeanException(new IllegalArgumentException(String.format("Tenant '%s' doesn't exist", tenantID)));
        else return getValue(f);
    }

    protected abstract T getValue(final SimpleTenantUsage usage) throws Exception;
}
