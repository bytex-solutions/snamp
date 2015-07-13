package com.itworks.snamp.connectors.openstack.snapshot;

import com.itworks.snamp.connectors.attributes.AttributeDescriptor;
import org.openstack4j.api.OSClient;
import org.openstack4j.model.storage.block.VolumeSnapshot;

import javax.management.openmbean.SimpleType;

/**
 * Name of the volume snapshot.
 * @author Roman Sakno
 * @version 1.0
 * @since 1.0
 */
public final class SnapshotNameAttribute extends AbstractBlockVolumeSnapshotAttribute<String> {
    public static final String NAME = "name";
    static final String DESCRIPTION = "Name of the volume snapshot";
    static final SimpleType<String> TYPE = SimpleType.STRING;

    public SnapshotNameAttribute(final String snapshotID,
                                 final String attributeID,
                                 final AttributeDescriptor descriptor,
                                 final OSClient client) {
        super(snapshotID, attributeID, DESCRIPTION, TYPE, descriptor, client);
    }

    static String getValueCore(final VolumeSnapshot vs) {
        return vs.getName();
    }

    @Override
    protected String getValue(final VolumeSnapshot vs) {
        return getValueCore(vs);
    }
}