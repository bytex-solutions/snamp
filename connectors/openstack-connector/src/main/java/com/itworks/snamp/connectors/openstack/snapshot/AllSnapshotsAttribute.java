package com.itworks.snamp.connectors.openstack.snapshot;

import com.itworks.snamp.connectors.attributes.AttributeDescriptor;
import com.itworks.snamp.connectors.attributes.AttributeSpecifier;
import com.itworks.snamp.connectors.openstack.OpenStackResourceAttribute;
import com.itworks.snamp.connectors.openstack.blockStorage.VolumeAttribute;
import com.itworks.snamp.internal.Utils;
import org.openstack4j.api.OSClient;
import org.openstack4j.api.storage.BlockVolumeSnapshotService;
import org.openstack4j.model.storage.block.Volume;
import org.openstack4j.model.storage.block.VolumeSnapshot;

import javax.management.openmbean.ArrayType;
import javax.management.openmbean.CompositeData;
import javax.management.openmbean.OpenDataException;
import java.util.List;
import java.util.concurrent.Callable;

/**
 * @author Roman Sakno
 * @version 1.0
 * @since 1.0
 */
public final class AllSnapshotsAttribute extends OpenStackResourceAttribute<CompositeData[], BlockVolumeSnapshotService> {
    public static final String NAME = "snapshots";
    private static final String DESCRIPTION = "All snapshots";
    static final ArrayType<CompositeData[]> TYPE = Utils.interfaceStaticInitialize(new Callable<ArrayType<CompositeData[]>>() {
        @Override
        public ArrayType<CompositeData[]> call() throws OpenDataException {
            return new ArrayType<>(1, SnapshotAttribute.TYPE);
        }
    });

    public AllSnapshotsAttribute(final String attributeID,
                                 final AttributeDescriptor descriptor,
                                 final OSClient client){
        super(attributeID, DESCRIPTION, TYPE, AttributeSpecifier.READ_ONLY, descriptor, client.blockStorage().snapshots());
    }


    /**
     * Gets value of this attribute.
     *
     * @return The value of this attribute.
     * @throws Exception Unable to read attribute value.
     */
    @Override
    public CompositeData[] getValue() throws OpenDataException {
        final List<? extends VolumeSnapshot> vols = openStackService.list();
        final CompositeData[] result = new CompositeData[vols.size()];
        for (int i = 0; i < vols.size(); i++)
            result[i] = SnapshotAttribute.getValueCore(vols.get(i));
        return result;
    }
}
