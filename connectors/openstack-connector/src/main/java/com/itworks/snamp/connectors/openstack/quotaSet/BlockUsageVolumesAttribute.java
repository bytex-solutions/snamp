package com.itworks.snamp.connectors.openstack.quotaSet;

import com.itworks.snamp.connectors.attributes.AttributeDescriptor;
import org.openstack4j.api.OSClient;
import org.openstack4j.model.storage.block.BlockQuotaSetUsage;

import javax.management.openmbean.SimpleType;

/**
 * @author Roman Sakno
 * @version 1.0
 * @since 1.0
 */
public final class BlockUsageVolumesAttribute extends AbstractBlockUsageAttribute<Integer> {
    public static final String NAME = "blockVolumesUsed";
    static final String DESCRIPTION = "Utilized number of volumes";
    static final SimpleType<Integer> TYPE = SimpleType.INTEGER;

    public BlockUsageVolumesAttribute(final String tenantID,
                                        final String attributeID,
                                        final AttributeDescriptor descriptor,
                                        final OSClient client) {
        super(tenantID, attributeID, DESCRIPTION, TYPE, descriptor, client);
    }

    static int getValueCore(final BlockQuotaSetUsage usage) {
        return usage.getVolumes().getInUse();
    }

    @Override
    protected Integer getValue(final BlockQuotaSetUsage usage) {
        return getValueCore(usage);
    }
}
