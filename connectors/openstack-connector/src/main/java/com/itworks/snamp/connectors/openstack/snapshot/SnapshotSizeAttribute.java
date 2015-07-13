package com.itworks.snamp.connectors.openstack.snapshot;

import com.itworks.snamp.connectors.attributes.AttributeDescriptor;
import org.openstack4j.api.OSClient;
import org.openstack4j.model.storage.block.VolumeSnapshot;

import javax.management.openmbean.SimpleType;

/**
 * Size of the volume snapshot.
 * @author Roman Sakno
 * @version 1.0
 * @since 1.0
 */
public final class SnapshotSizeAttribute extends AbstractBlockVolumeSnapshotAttribute<Integer> {
    public static final String NAME = "size";
    static final String DESCRIPTION = "Size of the volume snapshot";
    static final SimpleType<Integer> TYPE = SimpleType.INTEGER;

    public SnapshotSizeAttribute(final String snapshotID,
                                 final String attributeID,
                                 final AttributeDescriptor descriptor,
                                 final OSClient client) {
        super(snapshotID, attributeID, DESCRIPTION, TYPE, descriptor, client);
    }

    static int getValueCore(final VolumeSnapshot vs) {
        return vs.getSize();
    }

    @Override
    protected Integer getValue(final VolumeSnapshot vs) {
        return getValueCore(vs);
    }
}