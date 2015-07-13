package com.itworks.snamp.connectors.openstack.image;

import com.itworks.snamp.connectors.attributes.AttributeDescriptor;
import com.itworks.snamp.connectors.attributes.AttributeSpecifier;
import com.itworks.snamp.connectors.openstack.OpenStackResourceAttribute;
import org.openstack4j.api.OSClient;
import org.openstack4j.api.image.ImageService;
import org.openstack4j.model.compute.ext.Hypervisor;
import org.openstack4j.model.image.Image;

import javax.management.MBeanException;
import javax.management.openmbean.OpenType;
import java.util.Objects;

/**
 * @author Roman Sakno
 * @version 1.0
 * @since 1.0
 */
abstract class AbstractImageAttribute<T> extends OpenStackResourceAttribute<T, ImageService> {
    protected final String imageID;

    AbstractImageAttribute(final String imageID,
                                final String attributeID,
                                final String description,
                                final OpenType<T> attributeType,
                                final AttributeDescriptor descriptor,
                                final OSClient client) {
        super(attributeID, description, attributeType, AttributeSpecifier.READ_ONLY, descriptor, client.images());
        this.imageID = imageID;
    }

    protected abstract T getValue(final Image img) throws Exception;

    /**
     * Gets value of this attribute.
     *
     * @return The value of this attribute.
     * @throws Exception Unable to read attribute value.
     */
    @Override
    public final T getValue() throws Exception {
        final Image im = openStackService.get(imageID);
        if(im == null) throw new MBeanException(new IllegalArgumentException(String.format("Image '%s' doesn't exist", imageID)));
        else return getValue(im);
    }

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
}
