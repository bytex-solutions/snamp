package com.itworks.snamp.connectors.openstack.snapshot;

import com.google.common.collect.Maps;
import com.itworks.snamp.connectors.attributes.AttributeDescriptor;
import com.itworks.snamp.internal.Utils;
import com.itworks.snamp.jmx.CompositeTypeBuilder;
import org.openstack4j.api.OSClient;
import org.openstack4j.model.storage.block.VolumeSnapshot;

import javax.management.openmbean.*;
import java.util.Map;
import java.util.concurrent.Callable;

/**
 * @author Roman Sakno
 * @version 1.0
 * @since 1.0
 */
public final class SnapshotAttribute extends AbstractBlockVolumeSnapshotAttribute<CompositeData> {
    public static final String NAME = "snapshot";
    private static final String DESCRIPTION = "Full information about snapshot";
    private static final String ID_NAME = "snapshotID";
    private static final String ID_DESCR = "ID of the snapshot";

    static final CompositeType TYPE = Utils.interfaceStaticInitialize(new Callable<CompositeType>() {
        @Override
        public CompositeType call() throws OpenDataException {
            return new CompositeTypeBuilder("VolumeSnapshot", "Volume snapshot information")
                    .addItem(SnapshotCreatedAtAttribute.NAME, SnapshotCreatedAtAttribute.DESCRIPTION, SnapshotCreatedAtAttribute.TYPE)
                    .addItem(SnapshotDescriptionAttribute.NAME, SnapshotDescriptionAttribute.DESCRIPTION, SnapshotDescriptionAttribute.TYPE)
                    .addItem(SnapshotNameAttribute.NAME, SnapshotNameAttribute.DESCRIPTION, SnapshotNameAttribute.TYPE)
                    .addItem(SnapshotSizeAttribute.NAME, SnapshotSizeAttribute.DESCRIPTION, SnapshotSizeAttribute.TYPE)
                    .addItem(SnapshotStatusAttribute.NAME, SnapshotStatusAttribute.DESCRIPTION, SnapshotStatusAttribute.TYPE)
                    .addItem(ID_NAME, ID_DESCR, SimpleType.STRING)
                    .build();
        }
    });

    public SnapshotAttribute(final String snapshotID,
                             final String attributeID,
                             final AttributeDescriptor descriptor,
                             final OSClient client) {
        super(snapshotID, attributeID, DESCRIPTION, TYPE, descriptor, client);
    }

    static CompositeData getValueCore(final VolumeSnapshot vs) throws OpenDataException {
        final Map<String, Object> result = Maps.newHashMapWithExpectedSize(TYPE.keySet().size());
        result.put(ID_NAME, vs.getId());
        result.put(SnapshotCreatedAtAttribute.NAME, SnapshotCreatedAtAttribute.getValueCore(vs));
        result.put(SnapshotDescriptionAttribute.NAME, SnapshotDescriptionAttribute.getValueCore(vs));
        result.put(SnapshotNameAttribute.NAME, SnapshotNameAttribute.getValueCore(vs));
        result.put(SnapshotSizeAttribute.NAME, SnapshotSizeAttribute.getValueCore(vs));
        result.put(SnapshotStatusAttribute.NAME, SnapshotStatusAttribute.getValueCore(vs));
        return new CompositeDataSupport(TYPE, result);
    }

    @Override
    protected CompositeData getValue(final VolumeSnapshot vs) throws OpenDataException {
        return getValueCore(vs);
    }
}
