package com.itworks.snamp.connectors.openstack.snapshot;

import com.itworks.snamp.connectors.attributes.AttributeDescriptor;
import com.itworks.snamp.connectors.attributes.AttributeSpecifier;
import com.itworks.snamp.connectors.openstack.OpenStackResourceAttribute;
import org.openstack4j.api.OSClient;
import org.openstack4j.api.storage.BlockVolumeSnapshotService;
import org.openstack4j.model.storage.block.Volume;
import org.openstack4j.model.storage.block.VolumeSnapshot;

import javax.management.MBeanException;
import javax.management.openmbean.OpenType;

/**
 * @author Roman Sakno
 * @version 1.0
 * @since 1.0
 */
abstract class AbstractBlockVolumeSnapshotAttribute<T> extends OpenStackResourceAttribute<T, BlockVolumeSnapshotService> {
    protected final String snapshotID;

    AbstractBlockVolumeSnapshotAttribute(final String snapshotID,
                                 final String attributeID,
                                 final String description,
                                 final OpenType<T> attributeType,
                                 final AttributeDescriptor descriptor,
                                 final OSClient client) {
        super(attributeID, description, attributeType, AttributeSpecifier.READ_ONLY, descriptor, client.blockStorage().snapshots());
        this.snapshotID = snapshotID;
    }

    protected abstract T getValue(final VolumeSnapshot vs) throws Exception;

    /**
     * Gets value of this attribute.
     *
     * @return The value of this attribute.
     * @throws Exception Unable to read attribute value.
     */
    @Override
    public final T getValue() throws Exception {
        final VolumeSnapshot vol = openStackService.get(snapshotID);
        if(vol == null) throw new MBeanException(new IllegalArgumentException(String.format("Snapshot '%s' doesn't exist", snapshotID)));
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
