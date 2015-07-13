package com.itworks.snamp.connectors.openstack.snapshot;

import com.itworks.snamp.connectors.attributes.AttributeDescriptor;
import org.openstack4j.api.OSClient;
import org.openstack4j.model.storage.block.VolumeSnapshot;

import javax.management.openmbean.SimpleType;
import java.util.Date;

/**
 * Creation time of the volume snapshot.
 * @author Roman Sakno
 * @version 1.0
 * @since 1.0
 */
public final class SnapshotCreatedAtAttribute extends AbstractBlockVolumeSnapshotAttribute<Date> {
    public static final String NAME = "createdAt";
    static final String DESCRIPTION = "Creation time of the volume snapshot";
    static final SimpleType<Date> TYPE = SimpleType.DATE;

    public SnapshotCreatedAtAttribute(final String snapshotID,
                                   final String attributeID,
                                   final AttributeDescriptor descriptor,
                                   final OSClient client) {
        super(snapshotID, attributeID, DESCRIPTION, TYPE, descriptor, client);
    }

    static Date getValueCore(final VolumeSnapshot vs) {
        return vs.getCreated();
    }

    @Override
    protected Date getValue(final VolumeSnapshot vs) {
        return getValueCore(vs);
    }
}