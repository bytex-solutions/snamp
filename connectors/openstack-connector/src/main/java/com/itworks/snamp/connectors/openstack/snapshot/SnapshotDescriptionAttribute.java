package com.itworks.snamp.connectors.openstack.snapshot;

import com.itworks.snamp.connectors.attributes.AttributeDescriptor;
import org.openstack4j.api.OSClient;
import org.openstack4j.model.storage.block.VolumeSnapshot;

import javax.management.openmbean.SimpleType;

/**
 * Description of the volume snapshot.
 * @author Roman Sakno
 * @version 1.0
 * @since 1.0
 */
public final class SnapshotDescriptionAttribute extends AbstractBlockVolumeSnapshotAttribute<String> {
    public static final String NAME = "description";
    static final String DESCRIPTION = "Description of the volume snapshot";
    static final SimpleType<String> TYPE = SimpleType.STRING;

    public SnapshotDescriptionAttribute(final String snapshotID,
                                 final String attributeID,
                                 final AttributeDescriptor descriptor,
                                 final OSClient client) {
        super(snapshotID, attributeID, DESCRIPTION, TYPE, descriptor, client);
    }

    static String getValueCore(final VolumeSnapshot vs) {
        return vs.getDescription();
    }

    @Override
    protected String getValue(final VolumeSnapshot vs) {
        return getValueCore(vs);
    }
}