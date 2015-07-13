package com.itworks.snamp.connectors.openstack.blockStorage;

import com.itworks.snamp.connectors.attributes.AttributeDescriptor;
import org.openstack4j.api.OSClient;
import org.openstack4j.model.storage.block.Volume;

import javax.management.openmbean.SimpleType;

/**
 * Description of the volume.
 */
public final class VolumeDescriptionAttribute extends AbstractBlockVolumeAttribute<String> {
    public static final String NAME = "description";
    static final String DESCRIPTION = "Description of the volume";
    static final SimpleType<String> TYPE = SimpleType.STRING;

    public VolumeDescriptionAttribute(final String volumeID,
                                 final String attributeID,
                                 final AttributeDescriptor descriptor,
                                 final OSClient client) {
        super(volumeID, attributeID, DESCRIPTION, TYPE, descriptor, client);
    }

    static String getValueCore(final Volume vol) {
        return vol.getDescription();
    }

    @Override
    protected String getValue(final Volume vol) {
        return getValueCore(vol);
    }
}
