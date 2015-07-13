package com.itworks.snamp.connectors.openstack.quotaSet;

import com.itworks.snamp.connectors.attributes.AttributeDescriptor;
import com.itworks.snamp.connectors.attributes.AttributeSpecifier;
import com.itworks.snamp.connectors.openstack.OpenStackResourceAttribute;
import org.openstack4j.api.OSClient;
import org.openstack4j.common.RestService;

import javax.management.MBeanException;
import javax.management.openmbean.OpenType;
import javax.management.openmbean.SimpleType;

/**
 * @author Roman Sakno
 * @version 1.0
 * @since 1.0
 */
abstract class AbstractQuotaAttribute<T, A extends RestService> extends OpenStackResourceAttribute<T, A> {
    protected final String tenantID;
    static final String TENANT_NAME = "tenantID";
    static final String TENANT_DESCR = "ID of the tenant group";
    static final SimpleType<String> TENANT_TYPE = SimpleType.STRING;

    AbstractQuotaAttribute(final String tenantID,
                                 final String attributeID,
                                 final String description,
                                 final OpenType<T> attributeType,
                                 final AttributeDescriptor descriptor,
                                 final A service) {
        super(attributeID, description, attributeType, AttributeSpecifier.READ_ONLY, descriptor, service);
        this.tenantID = tenantID;
    }

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
