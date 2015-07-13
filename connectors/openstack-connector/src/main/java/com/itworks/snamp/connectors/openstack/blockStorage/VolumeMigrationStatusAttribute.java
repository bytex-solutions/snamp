package com.itworks.snamp.connectors.openstack.blockStorage;

import com.itworks.snamp.connectors.attributes.AttributeDescriptor;
import org.openstack4j.api.OSClient;
import org.openstack4j.model.storage.block.Volume;

import javax.management.openmbean.SimpleType;

/**
 * Migration status of the volume.
 */
public final class VolumeMigrationStatusAttribute extends AbstractBlockVolumeAttribute<String> {
    public static final String NAME = "migrationStatus";
    static final String DESCRIPTION = "Migration status of the volume";
    static final SimpleType<String> TYPE = SimpleType.STRING;

    public VolumeMigrationStatusAttribute(final String volumeID,
                               final String attributeID,
                               final AttributeDescriptor descriptor,
                               final OSClient client) {
        super(volumeID, attributeID, DESCRIPTION, TYPE, descriptor, client);
    }

    static String getValueCore(final Volume vol) {
        return vol.getMigrateStatus().value();
    }

    @Override
    protected String getValue(final Volume vol) {
        return getValueCore(vol);
    }
}
