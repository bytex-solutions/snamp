package com.itworks.snamp.connectors.openstack.snapshot;

import com.itworks.snamp.connectors.attributes.AttributeDescriptor;
import org.openstack4j.api.OSClient;
import org.openstack4j.model.storage.block.VolumeSnapshot;

import javax.management.openmbean.SimpleType;

/**
 * Status of the volume snapshot.
 * @author Roman Sakno
 * @version 1.0
 * @since 1.0
 */
public final class SnapshotStatusAttribute extends AbstractBlockVolumeSnapshotAttribute<String> {
    public static final String NAME = "status";
    static final String DESCRIPTION = "Status of the volume snapshot";
    static final SimpleType<String> TYPE = SimpleType.STRING;

    public SnapshotStatusAttribute(final String snapshotID,
                                   final String attributeID,
                                   final AttributeDescriptor descriptor,
                                   final OSClient client) {
        super(snapshotID, attributeID, DESCRIPTION, TYPE, descriptor, client);
    }

    static String getValueCore(final VolumeSnapshot vs) {
        return vs.getStatus().value();
    }

    @Override
    protected String getValue(final VolumeSnapshot vs) {
        return getValueCore(vs);
    }
}