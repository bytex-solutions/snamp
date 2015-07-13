package com.itworks.snamp.connectors.openstack.blockStorage;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import com.itworks.snamp.connectors.attributes.AttributeDescriptor;
import com.itworks.snamp.internal.Utils;
import com.itworks.snamp.jmx.CompositeTypeBuilder;
import org.openstack4j.api.OSClient;
import org.openstack4j.model.storage.block.Volume;

import javax.management.openmbean.CompositeData;
import javax.management.openmbean.CompositeDataSupport;
import javax.management.openmbean.CompositeType;
import javax.management.openmbean.OpenDataException;
import java.util.Map;
import java.util.concurrent.Callable;

/**
 * @author Roman Sakno
 * @version 1.0
 * @since 1.0
 */
public final class VolumeAttribute extends AbstractBlockVolumeAttribute<CompositeData> {
    public static final String NAME = "volumeInfo";
    private static final String DESCRIPTION = "Full information about block storage";
    private static final String ID_NAME = "volumeID";
    private static final String ID_DESCR = "ID of the volume";

    static final CompositeType TYPE = Utils.interfaceStaticInitialize(new Callable<CompositeType>() {
        @Override
        public CompositeType call() throws OpenDataException {
            return new CompositeTypeBuilder("BlockStorageVolume", "Block storage info")
                    .addItem(VolumeDescriptionAttribute.NAME, VolumeDescriptionAttribute.DESCRIPTION, VolumeDescriptionAttribute.TYPE)
                    .addItem(VolumeMigrationStatusAttribute.NAME, VolumeMigrationStatusAttribute.DESCRIPTION, VolumeMigrationStatusAttribute.TYPE)
                    .addItem(VolumeSizeAttribute.NAME, VolumeSizeAttribute.DESCRIPTION, VolumeSizeAttribute.TYPE)
                    .addItem(VolumeStatusAttribute.NAME, VolumeStatusAttribute.DESCRIPTION, VolumeStatusAttribute.TYPE)
                    .addItem(VolumeTypeAttribute.NAME, VolumeTypeAttribute.DESCRIPTION, VolumeTypeAttribute.TYPE)
                    .build();
        }
    });

    public VolumeAttribute(final String volumeID,
                           final String attributeID,
                           final AttributeDescriptor descriptor,
                           final OSClient client) {
        super(volumeID, attributeID, DESCRIPTION, TYPE, descriptor, client);
    }

    static CompositeData getValueCore(final Volume vol) throws OpenDataException {
        final Map<String, Object> result = Maps.newHashMapWithExpectedSize(TYPE.keySet().size());
        result.put(VolumeDescriptionAttribute.NAME, VolumeDescriptionAttribute.getValueCore(vol));
        result.put(VolumeMigrationStatusAttribute.NAME, VolumeMigrationStatusAttribute.getValueCore(vol));
        result.put(VolumeSizeAttribute.NAME, VolumeSizeAttribute.getValueCore(vol));
        result.put(VolumeStatusAttribute.NAME, VolumeStatusAttribute.getValueCore(vol));
        result.put(VolumeTypeAttribute.NAME, VolumeTypeAttribute.getValueCore(vol));
        result.put(ID_NAME, vol.getId());
        return new CompositeDataSupport(TYPE, result);
    }

    @Override
    protected CompositeData getValue(final Volume vol) throws OpenDataException {
        return getValueCore(vol);
    }
}
