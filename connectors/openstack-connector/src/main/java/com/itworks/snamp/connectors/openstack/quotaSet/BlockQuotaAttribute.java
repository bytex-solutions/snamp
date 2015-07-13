package com.itworks.snamp.connectors.openstack.quotaSet;

import com.google.common.collect.ImmutableMap;
import com.itworks.snamp.connectors.attributes.AttributeDescriptor;
import com.itworks.snamp.internal.Utils;
import com.itworks.snamp.jmx.CompositeTypeBuilder;
import org.openstack4j.api.OSClient;
import org.openstack4j.model.storage.block.BlockQuotaSet;

import javax.management.openmbean.CompositeData;
import javax.management.openmbean.CompositeDataSupport;
import javax.management.openmbean.CompositeType;
import javax.management.openmbean.OpenDataException;
import java.util.concurrent.Callable;

/**
 * @author Roman Sakno
 * @version 1.0
 * @since 1.0
 */
public final class BlockQuotaAttribute extends AbstractBlockQuotaAttribute<CompositeData> {
    public static final String NAME = "blockQuota";
    private static final String DESCRIPTION = "Full information about block storage quota";
    static final CompositeType TYPE = Utils.interfaceStaticInitialize(new Callable<CompositeType>() {
        @Override
        public CompositeType call() throws OpenDataException {
            return new CompositeTypeBuilder("BlockQuota", DESCRIPTION)
                    .addItem(BlockQuotaGigabytesAttribute.NAME, BlockQuotaGigabytesAttribute.DESCRIPTION, BlockQuotaGigabytesAttribute.TYPE)
                    .addItem(BlockQuotaSnapshotsAttribute.NAME, BlockQuotaSnapshotsAttribute.DESCRIPTION, BlockQuotaSnapshotsAttribute.TYPE)
                    .addItem(BlockQuotaVolumesAttribute.NAME, BlockQuotaVolumesAttribute.DESCRIPTION, BlockQuotaVolumesAttribute.TYPE)
                    .addItem(TENANT_NAME, TENANT_DESCR, TENANT_TYPE)
                    .build();
        }
    });

    public BlockQuotaAttribute(final String tenantID,
                               final String attributeID,
                               final AttributeDescriptor descriptor,
                               final OSClient client){
        super(tenantID, attributeID, DESCRIPTION, TYPE, descriptor, client);
    }

    static CompositeData getValueCore(final BlockQuotaSet quota) throws OpenDataException{
        return new CompositeDataSupport(TYPE, ImmutableMap.of(
            BlockQuotaGigabytesAttribute.NAME, BlockQuotaGigabytesAttribute.getValueCore(quota),
            BlockQuotaSnapshotsAttribute.NAME, BlockQuotaSnapshotsAttribute.getValueCore(quota),
            BlockQuotaVolumesAttribute.NAME, BlockQuotaVolumesAttribute.getValueCore(quota),
            TENANT_NAME, quota.getId()
        ));
    }

    @Override
    protected CompositeData getValue(final BlockQuotaSet quota) throws OpenDataException {
        return getValueCore(quota);
    }
}
