package com.itworks.snamp.connectors.openstack;

import com.itworks.snamp.connectors.attributes.AttributeDescriptor;
import com.itworks.snamp.connectors.openstack.blockStorage.*;
import com.itworks.snamp.connectors.openstack.flavor.*;
import com.itworks.snamp.connectors.openstack.hypervisor.*;
import com.itworks.snamp.connectors.openstack.computeQuota.*;
import com.itworks.snamp.connectors.openstack.server.*;
import com.itworks.snamp.jmx.JMExceptionUtils;
import org.openstack4j.api.OSClient;

import javax.management.AttributeNotFoundException;

/**
 * @author Roman Sakno
 * @version 1.0
 * @since 1.0
 */
enum OpenStackResourceType {
    FLAVOR("flavor") {
        @Override
        boolean checkCapability(final OSClient client) {
            return client.supportsCompute();
        }

        @Override
        OpenStackResourceAttribute<?, ?> connectAttribute(final String flavorID,
                                                          final String attributeID,
                                                          final AttributeDescriptor descriptor,
                                                          final OSClient openStackClient) throws AttributeNotFoundException {
            switch (descriptor.getAttributeName()) {
                case FlavorDisabledAttribute.NAME:
                    return new FlavorDisabledAttribute(flavorID, attributeID, descriptor, openStackClient);
                case FlavorRamAttribute.NAME:
                    return new FlavorRamAttribute(flavorID, attributeID, descriptor, openStackClient);
                case FlavorCpuCountAttribute.NAME:
                    return new FlavorCpuCountAttribute(flavorID, attributeID, descriptor, openStackClient);
                case FlavorDiskAttribute.NAME:
                    return new FlavorDiskAttribute(flavorID, attributeID, descriptor, openStackClient);
                case FlavorPublicAttribute.NAME:
                    return new FlavorPublicAttribute(flavorID, attributeID, descriptor, openStackClient);
                case FlavorAttribute.NAME:
                    return new FlavorAttribute(flavorID, attributeID, descriptor, openStackClient);
                default:
                    throw JMExceptionUtils.attributeNotFound(descriptor.getAttributeName());
            }
        }
    },

    HYPERVISOR("hypervisor") {
        @Override
        boolean checkCapability(final OSClient client) {
            return client.supportsCompute();
        }

        @Override
        OpenStackResourceAttribute<?, ?> connectAttribute(final String hypervisorID,
                                                          final String attributeID,
                                                          final AttributeDescriptor descriptor, final OSClient openStackClient) throws AttributeNotFoundException {
            switch (descriptor.getAttributeName()) {
                case HypervisorFreeRamAttribute.NAME:
                    return new HypervisorFreeRamAttribute(hypervisorID, attributeID, descriptor, openStackClient);
                case HypervisorFreeDiskAttribute.NAME:
                    return new HypervisorFreeDiskAttribute(hypervisorID, attributeID, descriptor, openStackClient);
                case HypervisorWorkloadAttribute.NAME:
                    return new HypervisorWorkloadAttribute(hypervisorID, attributeID, descriptor, openStackClient);
                case HypervisorHostIpAttribute.NAME:
                    return new HypervisorHostIpAttribute(hypervisorID, attributeID, descriptor, openStackClient);
                case HypervisorHostnameAttribute.NAME:
                    return new HypervisorHostnameAttribute(hypervisorID, attributeID, descriptor, openStackClient);
                case HypervisorLocalMemoryAttribute.NAME:
                    return new HypervisorLocalMemoryAttribute(hypervisorID, attributeID, descriptor, openStackClient);
                case HypervisorLocalMemoryUsedAttribute.NAME:
                    return new HypervisorLocalMemoryUsedAttribute(hypervisorID, attributeID, descriptor, openStackClient);
                case HypervisorRunningVmAttribute.NAME:
                    return new HypervisorRunningVmAttribute(hypervisorID, attributeID, descriptor, openStackClient);
                case HypervisorCpuCoresAttribute.NAME:
                    return new HypervisorCpuCoresAttribute(hypervisorID, attributeID, descriptor, openStackClient);
                case HypervisorCpuModelAttribute.NAME:
                    return new HypervisorCpuModelAttribute(hypervisorID, attributeID, descriptor, openStackClient);
                case HypervisorCpuFeaturesAttribute.NAME:
                    return new HypervisorCpuFeaturesAttribute(hypervisorID, attributeID, descriptor, openStackClient);
                case HypervisorCpuVendorAttribute.NAME:
                    return new HypervisorCpuVendorAttribute(hypervisorID, attributeID, descriptor, openStackClient);
                case HypervisorCpuArchAttribute.NAME:
                    return new HypervisorCpuArchAttribute(hypervisorID, attributeID, descriptor, openStackClient);
                case HypervisorLocalDiskAttribute.NAME:
                    return new HypervisorLocalDiskAttribute(hypervisorID, attributeID, descriptor, openStackClient);
                case HypervisorLocalDiskUsedAttribute.NAME:
                    return new HypervisorLocalDiskUsedAttribute(hypervisorID, attributeID, descriptor, openStackClient);
                case HypervisorCpuVirtualAttribute.NAME:
                    return new HypervisorCpuVirtualAttribute(hypervisorID, attributeID, descriptor, openStackClient);
                case HypervisorCpuVirtualUsedAttribute.NAME:
                    return new HypervisorCpuVirtualUsedAttribute(hypervisorID, attributeID, descriptor, openStackClient);
                case HypervisorTypeAttribute.NAME:
                    return new HypervisorTypeAttribute(hypervisorID, attributeID, descriptor, openStackClient);
                case HypervisorAttribute.NAME:
                    return new HypervisorAttribute(hypervisorID, attributeID, descriptor, openStackClient);
                default:
                    throw JMExceptionUtils.attributeNotFound(descriptor.getAttributeName());
            }
        }
    },

    SERVER("server") {
        @Override
        boolean checkCapability(final OSClient client) {
            return client.supportsCompute();
        }

        @Override
        OpenStackResourceAttribute<?, ?> connectAttribute(final String serverID, final String attributeID, final AttributeDescriptor descriptor, final OSClient openStackClient) throws AttributeNotFoundException, OpenStackAbsentConfigurationParameterException {
            switch (descriptor.getAttributeName()){
                case ServerStatusAttribute.NAME:
                    return new ServerStatusAttribute(serverID, attributeID, descriptor, openStackClient);
                case ServerFaultAttribute.NAME:
                    return new ServerFaultAttribute(serverID, attributeID, descriptor, openStackClient);
                case ServerAllDiagnosticsAttribute.NAME:
                    return new ServerAllDiagnosticsAttribute(serverID, attributeID, descriptor, openStackClient);
                case ServerDiagnosticsAttribute.NAME:
                    return new ServerDiagnosticsAttribute(serverID, attributeID, descriptor, openStackClient);
                case ServerHostAttribute.NAME:
                    return new ServerHostAttribute(serverID, attributeID, descriptor, openStackClient);
                case ServerFlavorAttribute.NAME:
                    return new ServerFlavorAttribute(serverID, attributeID, descriptor, openStackClient);
                case ServerPowerStateAttribute.NAME:
                    return new ServerPowerStateAttribute(serverID, attributeID, descriptor, openStackClient);
                case ServerVmStateAttribute.NAME:
                    return new ServerVmStateAttribute(serverID, attributeID, descriptor, openStackClient);
                case ServerTerminatedAtAttribute.NAME:
                    return new ServerTerminatedAtAttribute(serverID, attributeID, descriptor, openStackClient);
                case ServerLaunchedAtAttribute.NAME:
                    return new ServerLaunchedAtAttribute(serverID, attributeID, descriptor, openStackClient);
                case ServerNameAttribute.NAME:
                    return new ServerNameAttribute(serverID, attributeID, descriptor, openStackClient);
                case ServerInstanceNameAttribute.NAME:
                    return new ServerInstanceNameAttribute(serverID, attributeID, descriptor, openStackClient);
                case ServerAttribute.NAME:
                    return new ServerAttribute(serverID, attributeID, descriptor, openStackClient);
                default:
                    throw JMExceptionUtils.attributeNotFound(descriptor.getAttributeName());
            }
        }
    },

    COMPUTE_QUOTA("computeQuota"){
        @Override
        boolean checkCapability(final OSClient client) {
            return client.supportsCompute();
        }

        @Override
        OpenStackResourceAttribute<?, ?> connectAttribute(final String tenantID,
                                                          final String attributeID,
                                                          final AttributeDescriptor descriptor,
                                                          final OSClient openStackClient) throws AttributeNotFoundException {
            switch (descriptor.getAttributeName()){
                case QuotaCoresAttribute.NAME:
                    return new QuotaCoresAttribute(tenantID, attributeID, descriptor, openStackClient);
                case QuotaFloatingIPsAttribute.NAME:
                    return new QuotaFloatingIPsAttribute(tenantID, attributeID, descriptor, openStackClient);
                case QuotaGigabytesAttribute.NAME:
                    return new QuotaGigabytesAttribute(tenantID, attributeID, descriptor, openStackClient);
                case QuotaInstancesAttribute.NAME:
                    return new QuotaInstancesAttribute(tenantID, attributeID, descriptor, openStackClient);
                case QuotaRamAttribute.NAME:
                    return new QuotaRamAttribute(tenantID, attributeID, descriptor, openStackClient);
                case QuotaVolumesAttribute.NAME:
                    return new QuotaVolumesAttribute(tenantID, attributeID, descriptor, openStackClient);
                case QuotaAttribute.NAME:
                    return new QuotaAttribute(tenantID, attributeID, descriptor, openStackClient);
                case UsageTotalHoursAttribute.NAME:
                    return new UsageTotalHoursAttribute(tenantID, attributeID, descriptor, openStackClient);
                case UsageTotalLocalDiskAttribute.NAME:
                    return new UsageTotalLocalDiskAttribute(tenantID, attributeID, descriptor, openStackClient);
                case UsageTotalLocalMemoryAttribute.NAME:
                    return new UsageTotalLocalMemoryAttribute(tenantID, attributeID, descriptor, openStackClient);
                case UsageVCPUAttribute.NAME:
                    return new UsageVCPUAttribute(tenantID, attributeID, descriptor, openStackClient);
                case UsageAttribute.NAME:
                    return new UsageAttribute(tenantID, attributeID, descriptor, openStackClient);
                default:
                    throw JMExceptionUtils.attributeNotFound(descriptor.getAttributeName());
            }
        }
    },

    VOLUME("blockVolume"){
        @Override
        boolean checkCapability(final OSClient client) {
            return client.supportsBlockStorage();
        }

        @Override
        OpenStackResourceAttribute<?, ?> connectAttribute(final String volumeID,
                                                          final String attributeID,
                                                          final AttributeDescriptor descriptor,
                                                          final OSClient openStackClient) throws AttributeNotFoundException {
            switch (descriptor.getAttributeName()){
                case VolumeSizeAttribute.NAME:
                    return new VolumeSizeAttribute(volumeID, attributeID, descriptor, openStackClient);
                case VolumeStatusAttribute.NAME:
                    return new VolumeStatusAttribute(volumeID, attributeID, descriptor, openStackClient);
                default:
                    throw JMExceptionUtils.attributeNotFound(descriptor.getAttributeName());
            }
        }
    },

    ALL("all") {
        @Override
        boolean checkCapability(final OSClient client) {
            return true;
        }

        @Override
        OpenStackResourceAttribute<?, ?> connectAttribute(final String entityID,
                                                          final String attributeID,
                                                          final AttributeDescriptor descriptor,
                                                          final OSClient openStackClient) throws AttributeNotFoundException {
            if (openStackClient.supportsCompute())
                switch (descriptor.getAttributeName()) {
                    case AllFlavorsAttribute.NAME:
                        return new AllFlavorsAttribute(attributeID, descriptor, openStackClient);
                    case AllHypervisorsAttribute.NAME:
                        return new AllHypervisorsAttribute(attributeID, descriptor, openStackClient);
                    case AllServersAttribute.NAME:
                        return new AllServersAttribute(attributeID, descriptor, openStackClient);
                    case AllQuotasAttribute.NAME:
                        return new AllQuotasAttribute(attributeID, descriptor, openStackClient);
                    case AllUsagesAttribute.NAME:
                        return new AllUsagesAttribute(attributeID, descriptor, openStackClient);
                }
            throw JMExceptionUtils.attributeNotFound(descriptor.getAttributeName());
        }
    };

    private final String resourceType;

    OpenStackResourceType(final String name) {
        this.resourceType = name;
    }

    abstract boolean checkCapability(final OSClient client);

    abstract OpenStackResourceAttribute<?, ?> connectAttribute(final String entityID,
                                                               final String attributeID,
                                                               final AttributeDescriptor descriptor,
                                                               final OSClient openStackClient) throws AttributeNotFoundException, OpenStackAbsentConfigurationParameterException;

    @Override
    public final String toString() {
        return resourceType;
    }

    static OpenStackResourceType parse(final String resourceType) {
        for (final OpenStackResourceType type : values())
            if (type.resourceType.equals(resourceType)) return type;
        return ALL;
    }
}
