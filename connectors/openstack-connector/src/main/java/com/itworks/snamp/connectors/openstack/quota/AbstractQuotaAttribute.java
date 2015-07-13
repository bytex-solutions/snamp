package com.itworks.snamp.connectors.openstack.quota;

import com.itworks.snamp.connectors.attributes.AttributeDescriptor;
import com.itworks.snamp.connectors.attributes.AttributeSpecifier;
import com.itworks.snamp.connectors.openstack.OpenStackResourceAttribute;
import org.openstack4j.api.OSClient;
import org.openstack4j.api.compute.QuotaSetService;
import org.openstack4j.model.compute.QuotaSet;

import javax.management.MBeanException;
import javax.management.openmbean.OpenType;

/**
 * Represents a superclass for all quota-based metrics.
 */
abstract class AbstractQuotaAttribute<T> extends OpenStackResourceAttribute<T, QuotaSetService> {
    protected final String tenantID;

    AbstractQuotaAttribute(final String tenantID,
                           final String attributeID,
                           final String description,
                           final OpenType<T> attributeType,
                           final AttributeDescriptor descriptor,
                           final OSClient openStackService) {
        super(attributeID, description, attributeType, AttributeSpecifier.READ_ONLY, descriptor, openStackService.compute().quotaSets());
        this.tenantID = tenantID;
    }

    private final QuotaSet getQuotaSet() {
        return openStackService.get(tenantID);
    }

    /**
     * Gets value of this attribute.
     *
     * @return The value of this attribute.
     * @throws Exception Unable to read attribute value.
     */
    @Override
    public final T getValue() throws Exception {
        final QuotaSet f = getQuotaSet();
        if (f == null)
            throw new MBeanException(new IllegalArgumentException(String.format("Tenant '%s' doesn't exist", tenantID)));
        else return getValue(f);
    }

    protected abstract T getValue(final QuotaSet quota) throws Exception;

    /**
     * Sets value of this attribute.
     *
     * @param value The value of this attribute.
     * @throws Exception Unable to write attribute value.
     */
    @Override
    public final void setValue(final T value) throws Exception {
        throw new MBeanException(new UnsupportedOperationException("Attribute is read-only"));
    }
}
