package com.itworks.snamp.connectors.openstack.quotaSet;

import com.google.common.collect.ImmutableMap;
import com.itworks.snamp.connectors.attributes.AttributeDescriptor;
import com.itworks.snamp.internal.Utils;
import com.itworks.snamp.jmx.CompositeTypeBuilder;
import org.openstack4j.api.OSClient;
import org.openstack4j.model.storage.block.BlockQuotaSetUsage;

import javax.management.openmbean.*;
import java.util.concurrent.Callable;

/**
 * @author Roman Sakno
 * @version 1.0
 * @since 1.0
 */
public final class BlockUsageAttribute extends AbstractBlockUsageAttribute<CompositeData> {
    public static final String NAME = "blockUsage";
    private static final String DESCRIPTION = "Full information about usage of block stores";
    static final CompositeType TYPE = Utils.interfaceStaticInitialize(new Callable<CompositeType>() {
        @Override
        public CompositeType call() throws OpenDataException {
            return new CompositeTypeBuilder("BlockUsage", DESCRIPTION)
                    .addItem(BlockUsageGigabytesAttribute.NAME, BlockUsageGigabytesAttribute.DESCRIPTION, BlockUsageGigabytesAttribute.TYPE)
                    .addItem(BlockUsageVolumesAttribute.NAME, BlockUsageVolumesAttribute.DESCRIPTION, BlockUsageVolumesAttribute.TYPE)
                    .addItem(BlockUsageSnapshotsAttribute.NAME, BlockUsageSnapshotsAttribute.DESCRIPTION, BlockUsageSnapshotsAttribute.TYPE)
                    .addItem(TENANT_NAME, TENANT_DESCR, TENANT_TYPE)
                    .build();
        }
    });

    public BlockUsageAttribute(final String tenantID,
                                        final String attributeID,
                                        final AttributeDescriptor descriptor,
                                        final OSClient client) {
        super(tenantID, attributeID, DESCRIPTION, TYPE, descriptor, client);
    }

    static CompositeData getValueCore(final BlockQuotaSetUsage usage, final String tenantID) throws OpenDataException {
        return new CompositeDataSupport(TYPE, ImmutableMap.of(
                TENANT_NAME, tenantID,
                BlockUsageGigabytesAttribute.NAME, BlockUsageGigabytesAttribute.getValueCore(usage),
                BlockUsageVolumesAttribute.NAME, BlockUsageVolumesAttribute.getValueCore(usage),
                BlockUsageSnapshotsAttribute.NAME, BlockUsageSnapshotsAttribute.getValueCore(usage)
        ));
    }

    @Override
    protected CompositeData getValue(final BlockQuotaSetUsage usage) throws OpenDataException {
        return getValueCore(usage, tenantID);
    }
}
