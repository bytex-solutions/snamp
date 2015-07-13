package com.itworks.snamp.connectors.openstack.quotaSet;

import com.itworks.snamp.connectors.attributes.AttributeDescriptor;
import org.openstack4j.api.OSClient;
import org.openstack4j.model.storage.block.BlockQuotaSet;

import javax.management.openmbean.SimpleType;

/**
 * @author Roman Sakno
 * @version 1.0
 * @since 1.0
 */
public final class BlockQuotaSnapshotsAttribute extends AbstractBlockQuotaAttribute<Integer> {
    public static final String NAME = "snapshots";
    static final String DESCRIPTION = "Maximum number of snapshots";
    static final SimpleType<Integer> TYPE = SimpleType.INTEGER;

    public BlockQuotaSnapshotsAttribute(final String tenantID,
                                        final String attributeID,
                                        final AttributeDescriptor descriptor,
                                        final OSClient client){
        super(tenantID, attributeID, DESCRIPTION, TYPE, descriptor, client);
    }

    static int getValueCore(final BlockQuotaSet quota){
        return quota.getSnapshots();
    }

    @Override
    protected Integer getValue(final BlockQuotaSet quota) {
        return getValueCore(quota);
    }
}
