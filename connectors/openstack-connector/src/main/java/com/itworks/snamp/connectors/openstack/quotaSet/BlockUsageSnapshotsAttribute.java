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
public final class BlockUsageSnapshotsAttribute extends AbstractBlockUsageAttribute<Integer> {
    public static final String NAME = "blockSnapshotsUsed";
    static final String DESCRIPTION = "Utilized number of snapshots";
    static final SimpleType<Integer> TYPE = SimpleType.INTEGER;

    public BlockUsageSnapshotsAttribute(final String tenantID,
                                      final String attributeID,
                                      final AttributeDescriptor descriptor,
                                      final OSClient client) {
        super(tenantID, attributeID, DESCRIPTION, TYPE, descriptor, client);
    }

    static int getValueCore(final BlockQuotaSetUsage usage) {
        return usage.getSnapshots().getInUse();
    }

    @Override
    protected Integer getValue(final BlockQuotaSetUsage usage) {
        return getValueCore(usage);
    }
}
