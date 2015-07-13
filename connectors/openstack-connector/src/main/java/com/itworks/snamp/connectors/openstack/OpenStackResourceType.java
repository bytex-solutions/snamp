package com.itworks.snamp.connectors.openstack;

import com.itworks.snamp.connectors.attributes.AttributeDescriptor;
import com.itworks.snamp.connectors.openstack.blockStorage.*;
import com.itworks.snamp.connectors.openstack.flavor.*;
import com.itworks.snamp.connectors.openstack.hypervisor.*;
import com.itworks.snamp.connectors.openstack.quotaSet.*;
import com.itworks.snamp.connectors.openstack.server.*;
import com.itworks.snamp.connectors.openstack.snapshot.*;
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
        OpenStackResourceAttribute<?, ?> connectAttribute(final String serverID,
                                                          final String attributeID,
                                                          final AttributeDescriptor descriptor,
                                                          final OSClient openStackClient) throws AttributeNotFoundException, OpenStackAbsentConfigurationParameterException {
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

    QUOTA_SET("quotaSet"){
        @Override
        boolean checkCapability(final OSClient client) {
            return client.supportsCompute() || client.supportsBlockStorage();
        }

        @Override
        OpenStackResourceAttribute<?, ?> connectAttribute(final String tenantID,
                                                          final String attributeID,
                                                          final AttributeDescriptor descriptor,
                                                          final OSClient openStackClient) throws AttributeNotFoundException {
            if (openStackClient.supportsCompute())
                switch (descriptor.getAttributeName()) {
                    case ServerQuotaCoresAttribute.NAME:
                        return new ServerQuotaCoresAttribute(tenantID, attributeID, descriptor, openStackClient);
                    case ServerQuotaFloatingIPsAttribute.NAME:
                        return new ServerQuotaFloatingIPsAttribute(tenantID, attributeID, descriptor, openStackClient);
                    case ServerQuotaGigabytesAttribute.NAME:
                        return new ServerQuotaGigabytesAttribute(tenantID, attributeID, descriptor, openStackClient);
                    case ServerQuotaInstancesAttribute.NAME:
                        return new ServerQuotaInstancesAttribute(tenantID, attributeID, descriptor, openStackClient);
                    case ServerQuotaRamAttribute.NAME:
                        return new ServerQuotaRamAttribute(tenantID, attributeID, descriptor, openStackClient);
                    case ServerQuotaVolumesAttribute.NAME:
                        return new ServerQuotaVolumesAttribute(tenantID, attributeID, descriptor, openStackClient);
                    case ServerQuotaAttribute.NAME:
                        return new ServerQuotaAttribute(tenantID, attributeID, descriptor, openStackClient);
                    case ServerUsageTotalHoursAttribute.NAME:
                        return new ServerUsageTotalHoursAttribute(tenantID, attributeID, descriptor, openStackClient);
                    case ServerUsageTotalLocalDiskAttribute.NAME:
                        return new ServerUsageTotalLocalDiskAttribute(tenantID, attributeID, descriptor, openStackClient);
                    case ServerUsageTotalLocalMemoryAttribute.NAME:
                        return new ServerUsageTotalLocalMemoryAttribute(tenantID, attributeID, descriptor, openStackClient);
                    case ServerUsageVCPUAttribute.NAME:
                        return new ServerUsageVCPUAttribute(tenantID, attributeID, descriptor, openStackClient);
                    case ServerUsageAttribute.NAME:
                        return new ServerUsageAttribute(tenantID, attributeID, descriptor, openStackClient);
                }
            if (openStackClient.supportsBlockStorage())
                switch (descriptor.getAttributeName()) {
                    case BlockQuotaGigabytesAttribute.NAME:
                        return new BlockQuotaGigabytesAttribute(tenantID, attributeID, descriptor, openStackClient);
                    case BlockQuotaSnapshotsAttribute.NAME:
                        return new BlockQuotaSnapshotsAttribute(tenantID, attributeID, descriptor, openStackClient);
                    case BlockQuotaVolumesAttribute.NAME:
                        return new BlockQuotaVolumesAttribute(tenantID, attributeID, descriptor, openStackClient);
                    case BlockQuotaAttribute.NAME:
                        return new BlockQuotaAttribute(tenantID, attributeID, descriptor, openStackClient);
                    case BlockUsageGigabytesAttribute.NAME:
                        return new BlockUsageGigabytesAttribute(tenantID, attributeID, descriptor, openStackClient);
                    case BlockUsageVolumesAttribute.NAME:
                        return new BlockUsageVolumesAttribute(tenantID, attributeID, descriptor, openStackClient);
                    case BlockUsageSnapshotsAttribute.NAME:
                        return new BlockUsageSnapshotsAttribute(tenantID, attributeID, descriptor, openStackClient);
                    case BlockUsageAttribute.NAME:
                        return new BlockUsageAttribute(tenantID, attributeID, descriptor, openStackClient);
                }
            throw JMExceptionUtils.attributeNotFound(descriptor.getAttributeName());
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
                case VolumeDescriptionAttribute.NAME:
                    return new VolumeDescriptionAttribute(volumeID, attributeID, descriptor, openStackClient);
                case VolumeTypeAttribute.NAME:
                    return new VolumeTypeAttribute(volumeID, attributeID, descriptor, openStackClient);
                case VolumeMigrationStatusAttribute.NAME:
                    return new VolumeMigrationStatusAttribute(volumeID, attributeID, descriptor, openStackClient);
                case VolumeAttribute.NAME:
                    return new VolumeAttribute(volumeID, attributeID, descriptor, openStackClient);
                default:
                    throw JMExceptionUtils.attributeNotFound(descriptor.getAttributeName());
            }
        }
    },

    SNAPSHOT("snapshot"){
        @Override
        boolean checkCapability(final OSClient client) {
            return client.supportsBlockStorage();
        }

        @Override
        OpenStackResourceAttribute<?, ?> connectAttribute(final String snapshotID,
                                                          final String attributeID,
                                                          final AttributeDescriptor descriptor,
                                                          final OSClient openStackClient) throws AttributeNotFoundException {
            switch (descriptor.getAttributeName()) {
                case SnapshotStatusAttribute.NAME:
                    return new SnapshotStatusAttribute(snapshotID, attributeID, descriptor, openStackClient);
                case SnapshotSizeAttribute.NAME:
                    return new SnapshotSizeAttribute(snapshotID, attributeID, descriptor, openStackClient);
                case SnapshotCreatedAtAttribute.NAME:
                    return new SnapshotCreatedAtAttribute(snapshotID, attributeID, descriptor, openStackClient);
                case SnapshotNameAttribute.NAME:
                    return new SnapshotNameAttribute(snapshotID, attributeID, descriptor, openStackClient);
                case SnapshotDescriptionAttribute.NAME:
                    return new SnapshotDescriptionAttribute(snapshotID, attributeID, descriptor, openStackClient);
                case SnapshotAttribute.NAME:
                    return new SnapshotAttribute(snapshotID, attributeID, descriptor, openStackClient);
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
                    case AllServerQuotasAttribute.NAME:
                        return new AllServerQuotasAttribute(attributeID, descriptor, openStackClient);
                    case AllTenantUsagesAttribute.NAME:
                        return new AllTenantUsagesAttribute(attributeID, descriptor, openStackClient);
                }
            if (openStackClient.supportsBlockStorage())
                switch (descriptor.getAttributeName()) {
                    case AllVolumesAttribute.NAME:
                        return new AllVolumesAttribute(attributeID, descriptor, openStackClient);
                    case AllSnapshotsAttribute.NAME:
                        return new AllSnapshotsAttribute(attributeID, descriptor, openStackClient);
                    case AllBlockQuotasAttribute.NAME:
                        return new AllBlockQuotasAttribute(attributeID, descriptor, openStackClient);
                    case AllBlockUsagesAttribute.NAME:
                        return new AllBlockUsagesAttribute(attributeID, descriptor, openStackClient);
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
