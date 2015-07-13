package com.itworks.snamp.connectors.openstack.flavor;

import com.itworks.snamp.connectors.attributes.AttributeDescriptor;
import com.itworks.snamp.connectors.attributes.AttributeSpecifier;
import com.itworks.snamp.connectors.openstack.OpenStackResourceAttribute;
import org.openstack4j.api.OSClient;
import org.openstack4j.api.compute.FlavorService;
import org.openstack4j.model.compute.Flavor;

import javax.management.MBeanException;
import javax.management.openmbean.OpenType;

/**
 * @author Roman Sakno
 * @version 1.0
 * @since 1.0
 */
abstract class AbstractFlavorAttribute<T> extends OpenStackResourceAttribute<T, FlavorService> {
    protected final String flavorID;

    AbstractFlavorAttribute(final String flavorID,
                            final String attributeID,
                            final String description,
                            final OpenType<T> attributeType,
                            final AttributeDescriptor descriptor,
                            final OSClient openStackService) {
        this(flavorID, attributeID, description, attributeType, descriptor, false, openStackService);
    }

    AbstractFlavorAttribute(final String flavorID,
                            final String attributeID,
                            final String description,
                            final OpenType<T> attributeType,
                            final AttributeDescriptor descriptor,
                            final boolean isIs,
                            final OSClient openStackService) {
        super(attributeID, description, attributeType, AttributeSpecifier.READ_ONLY.flag(isIs), descriptor, openStackService.compute().flavors());
        this.flavorID = flavorID;
    }

    private final Flavor getFlavor() {
        return openStackService.get(flavorID);
    }

    /**
     * Gets value of this attribute.
     *
     * @return The value of this attribute.
     * @throws Exception Unable to read attribute value.
     */
    @Override
    public final T getValue() throws Exception {
        final Flavor f = getFlavor();
        if (f == null)
            throw new MBeanException(new IllegalArgumentException(String.format("Flavor '%s' doesn't exist", flavorID)));
        else return getValue(getFlavor());
    }

    protected abstract T getValue(final Flavor flavor) throws Exception;

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
