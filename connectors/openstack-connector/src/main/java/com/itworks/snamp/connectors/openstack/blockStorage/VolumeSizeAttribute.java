package com.itworks.snamp.connectors.openstack.blockStorage;

import com.itworks.snamp.connectors.attributes.AttributeDescriptor;
import org.openstack4j.api.OSClient;
import org.openstack4j.model.storage.block.Volume;

import javax.management.openmbean.SimpleType;

/**
 * Size of the volume.
 */
public final class VolumeSizeAttribute extends AbstractBlockVolumeAttribute<Integer> {
    public static final String NAME = "size";
    static final String DESCRIPTION = "Size of the volume";
    static final SimpleType<Integer> TYPE = SimpleType.INTEGER;

    public VolumeSizeAttribute(final String volumeID,
                               final String attributeID,
                               final AttributeDescriptor descriptor,
                               final OSClient client){
        super(volumeID, attributeID, DESCRIPTION, TYPE, descriptor, client);
    }

    static int getValueCore(final Volume vol){
        return vol.getSize();
    }

    @Override
    protected Integer getValue(final Volume vol) {
        return getValueCore(vol);
    }
}
