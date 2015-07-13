package com.itworks.snamp.connectors.openstack.blockStorage;

import com.itworks.snamp.connectors.attributes.AttributeDescriptor;
import com.itworks.snamp.connectors.attributes.AttributeSpecifier;
import com.itworks.snamp.connectors.openstack.OpenStackResourceAttribute;
import org.openstack4j.api.OSClient;
import org.openstack4j.api.storage.BlockVolumeService;
import org.openstack4j.model.storage.block.Volume;

import javax.management.MBeanException;
import javax.management.openmbean.OpenType;

/**
 * Superclass for attribute indicating storage volume characteristics.
 */
abstract class AbstractBlockVolumeAttribute<T> extends OpenStackResourceAttribute<T, BlockVolumeService> {
    protected final String volumeID;

    AbstractBlockVolumeAttribute(final String volumeID,
                            final String attributeID,
                            final String description,
                            final OpenType<T> attributeType,
                            final AttributeDescriptor descriptor,
                            final OSClient client) {
        super(attributeID, description, attributeType, AttributeSpecifier.READ_ONLY, descriptor, client.blockStorage().volumes());
        this.volumeID = volumeID;
    }

    protected abstract T getValue(final Volume vol) throws Exception;

    /**
     * Gets value of this attribute.
     *
     * @return The value of this attribute.
     * @throws Exception Unable to read attribute value.
     */
    @Override
    public final T getValue() throws Exception {
        final Volume vol = openStackService.get(volumeID);
        if(vol == null) throw new MBeanException(new IllegalArgumentException(String.format("Volume '%s' doesn't exist", volumeID)));
        else return getValue(vol);
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
