package com.itworks.snamp.connectors.openstack.hypervisor;

import com.google.common.collect.Maps;
import com.itworks.snamp.connectors.attributes.AttributeDescriptor;
import com.itworks.snamp.jmx.CompositeTypeBuilder;
import org.openstack4j.api.OSClient;
import org.openstack4j.model.compute.ext.Hypervisor;

import javax.management.openmbean.*;
import java.util.Map;

/**
 * @author Roman Sakno
 * @version 1.0
 * @since 1.0
 */
public final class HypervisorAttribute extends AbstractHypervisorAttribute<CompositeData> {
    public static final String NAME = "hypervisorInfo";
    private static final String DESCRIPTION = "Information about hypervisor";
    static final CompositeType TYPE;
    private static final String ID_NAME = "hypervisorID";
    private static final String ID_DESCR = "ID of the hypervisor";

    static {
        try {
            TYPE = new CompositeTypeBuilder("Hypervisor", "OpenStack Hypervisor")
                    .addItem(HypervisorCpuArchAttribute.NAME, HypervisorCpuArchAttribute.DESCRIPTION, HypervisorCpuArchAttribute.TYPE)
                    .addItem(HypervisorCpuCoresAttribute.NAME, HypervisorCpuCoresAttribute.DESCRIPTION, HypervisorCpuCoresAttribute.TYPE)
                    .addItem(HypervisorCpuFeaturesAttribute.NAME, HypervisorCpuFeaturesAttribute.DESCRIPTION, HypervisorCpuFeaturesAttribute.TYPE)
                    .addItem(HypervisorCpuModelAttribute.NAME, HypervisorCpuModelAttribute.DESCRIPTION, HypervisorCpuModelAttribute.TYPE)
                    .addItem(HypervisorCpuVendorAttribute.NAME, HypervisorCpuVendorAttribute.DESCRIPTION, HypervisorCpuVendorAttribute.TYPE)
                    .addItem(HypervisorCpuVirtualAttribute.NAME, HypervisorCpuVirtualAttribute.DESCRIPTION, HypervisorCpuVirtualAttribute.TYPE)
                    .addItem(HypervisorCpuVirtualUsedAttribute.NAME, HypervisorCpuVirtualUsedAttribute.DESCRIPTION, HypervisorCpuVirtualUsedAttribute.TYPE)
                    .addItem(HypervisorFreeDiskAttribute.NAME, HypervisorFreeDiskAttribute.DESCRIPTION, HypervisorFreeDiskAttribute.TYPE)
                    .addItem(HypervisorFreeRamAttribute.NAME, HypervisorFreeRamAttribute.DESCRIPTION, HypervisorFreeRamAttribute.TYPE)
                    .addItem(HypervisorHostIpAttribute.NAME, HypervisorHostIpAttribute.DESCRIPTION, HypervisorHostIpAttribute.TYPE)
                    .addItem(HypervisorHostnameAttribute.NAME, HypervisorHostnameAttribute.DESCRIPTION, HypervisorHostnameAttribute.TYPE)
                    .addItem(HypervisorLocalDiskAttribute.NAME, HypervisorLocalDiskAttribute.DESCRIPTION, HypervisorLocalDiskAttribute.TYPE)
                    .addItem(HypervisorLocalDiskUsedAttribute.NAME, HypervisorLocalDiskUsedAttribute.DESCRIPTION, HypervisorLocalDiskUsedAttribute.TYPE)
                    .addItem(HypervisorLocalMemoryAttribute.NAME, HypervisorLocalMemoryAttribute.DESCRIPTION, HypervisorLocalMemoryAttribute.TYPE)
                    .addItem(HypervisorLocalMemoryUsedAttribute.NAME, HypervisorLocalMemoryUsedAttribute.DESCRIPTION, HypervisorLocalMemoryUsedAttribute.TYPE)
                    .addItem(HypervisorRunningVmAttribute.NAME, HypervisorRunningVmAttribute.DESCRIPTION, HypervisorRunningVmAttribute.TYPE)
                    .addItem(HypervisorTypeAttribute.NAME, HypervisorTypeAttribute.DESCRIPTION, HypervisorTypeAttribute.TYPE)
                    .addItem(HypervisorWorkloadAttribute.NAME, HypervisorWorkloadAttribute.DESCRIPTION, HypervisorWorkloadAttribute.TYPE)
                    .addItem(ID_NAME, ID_DESCR, SimpleType.STRING)
                    .build();
        } catch (final OpenDataException e) {
            throw new ExceptionInInitializerError(e);
        }
    }

    public HypervisorAttribute(final String hypervisorID,
                               final String attributeID,
                               final AttributeDescriptor descriptor,
                               final OSClient client){
        super(hypervisorID, attributeID, DESCRIPTION, TYPE, descriptor, client);
    }

    static CompositeData getValueCore(final Hypervisor hv) throws OpenDataException{
        final Map<String, Object> result = Maps.newHashMapWithExpectedSize(TYPE.keySet().size());
        result.put(HypervisorCpuArchAttribute.NAME, HypervisorCpuArchAttribute.getValueCore(hv));
        result.put(HypervisorCpuCoresAttribute.NAME, HypervisorCpuCoresAttribute.getValueCore(hv));
        result.put(HypervisorCpuFeaturesAttribute.NAME, HypervisorCpuFeaturesAttribute.getValueCore(hv));
        result.put(HypervisorCpuModelAttribute.NAME, HypervisorCpuModelAttribute.getValueCore(hv));
        result.put(HypervisorCpuVendorAttribute.NAME,  HypervisorCpuVendorAttribute.getValueCore(hv));
        result.put(HypervisorCpuVirtualAttribute.NAME, HypervisorCpuVirtualAttribute.getValueCore(hv));
        result.put(HypervisorCpuVirtualUsedAttribute.NAME, HypervisorCpuVirtualUsedAttribute.getValueCore(hv));
        result.put(HypervisorFreeDiskAttribute.NAME, HypervisorFreeDiskAttribute.getValueCore(hv));
        result.put(HypervisorFreeRamAttribute.NAME, HypervisorFreeRamAttribute.getValueCore(hv));
        result.put(HypervisorHostIpAttribute.NAME, HypervisorHostIpAttribute.getValueCore(hv));
        result.put(HypervisorHostnameAttribute.NAME, HypervisorHostnameAttribute.getValueCore(hv));
        result.put(HypervisorLocalDiskAttribute.NAME, HypervisorLocalDiskAttribute.getValueCore(hv));
        result.put(HypervisorLocalDiskUsedAttribute.NAME, HypervisorLocalDiskUsedAttribute.getValueCore(hv));
        result.put(HypervisorLocalMemoryAttribute.NAME, HypervisorLocalMemoryAttribute.getValueCore(hv));
        result.put(HypervisorLocalMemoryUsedAttribute.NAME, HypervisorLocalMemoryUsedAttribute.getValueCore(hv));
        result.put(HypervisorRunningVmAttribute.NAME, HypervisorRunningVmAttribute.getValueCore(hv));
        result.put(HypervisorTypeAttribute.NAME, HypervisorTypeAttribute.getValueCore(hv));
        result.put(HypervisorWorkloadAttribute.NAME, HypervisorWorkloadAttribute.getValueCore(hv));
        result.put(ID_NAME, hv.getId());
        return new CompositeDataSupport(TYPE, result);
    }

    @Override
    protected CompositeData getValue(final Hypervisor hv) throws OpenDataException {
        return getValueCore(hv);
    }
}
