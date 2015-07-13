package com.itworks.snamp.connectors.openstack;

import com.itworks.snamp.connectors.attributes.AttributeDescriptor;
import com.itworks.snamp.connectors.attributes.AttributeDescriptorRead;
import com.itworks.snamp.connectors.attributes.AttributeSpecifier;
import com.itworks.snamp.connectors.attributes.OpenAttributeAccessor;
import org.openstack4j.api.OSClient;
import org.openstack4j.common.RestService;

import javax.management.Descriptor;
import javax.management.MBeanException;
import javax.management.openmbean.CompositeData;
import javax.management.openmbean.OpenMBeanAttributeInfoSupport;
import javax.management.openmbean.OpenType;
import java.util.Objects;

/**
 * Represents attribute of OpenStack resource.
 * @author Roman Sakno
 * @version 1.0
 * @since 1.0
 */
public abstract class OpenStackResourceAttribute<T, A extends RestService> extends OpenAttributeAccessor<T> {
    /**
     * Represents OpenStack REST service.
     */
    protected final A openStackService;

    protected OpenStackResourceAttribute(final String attributeID,
                               final String description,
                               final OpenType<T> attributeType,
                               final AttributeSpecifier specifier,
                               final AttributeDescriptor descriptor,
                               final A openStackService) {
        super(attributeID,
                description,
                attributeType,
                specifier,
                descriptor);
        this.openStackService = Objects.requireNonNull(openStackService);
    }

    /**
     * Gets value of this attribute.
     *
     * @return The value of this attribute.
     * @throws Exception Unable to read attribute value.
     */
    @Override
    public abstract T getValue() throws Exception;

    /**
     * Sets value of this attribute.
     *
     * @param value The value of this attribute.
     * @throws Exception Unable to write attribute value.
     */
    @Override
    public void setValue(final T value) throws Exception {
        throw new MBeanException(new UnsupportedOperationException("Attribute is read-only"));
    }

    /**
     * Releases all resources associated with this attribute.
     */
    public void disconnect(){

    }
}
