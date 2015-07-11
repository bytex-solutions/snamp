package com.itworks.snamp.connectors.openstack;

import com.itworks.snamp.connectors.attributes.AttributeDescriptor;
import com.itworks.snamp.connectors.openstack.flavor.*;
import com.itworks.snamp.connectors.openstack.hypervisor.*;
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
        OpenStackResourceAttribute<?, ?> connectAttribute(final String attributeID,
                                                          final AttributeDescriptor descriptor,
                                                          final OSClient openStackClient) throws AttributeNotFoundException, OpenStackAbsentConfigurationParameterException {
            switch (descriptor.getAttributeName()){
                case FlavorDisabledAttribute.NAME:
                    return new FlavorDisabledAttribute(attributeID, descriptor, openStackClient);
                case FlavorRamAttribute.NAME:
                    return new FlavorRamAttribute(attributeID, descriptor, openStackClient);
                case FlavorCpuCountAttribute.NAME:
                    return new FlavorCpuCountAttribute(attributeID, descriptor, openStackClient);
                case FlavorDiskAttribute.NAME:
                    return new FlavorDiskAttribute(attributeID, descriptor, openStackClient);
                case FlavorPublicAttribute.NAME:
                    return new FlavorPublicAttribute(attributeID, descriptor, openStackClient);
                case AllFlavorsAttribute.NAME:
                    return new AllFlavorsAttribute(attributeID, descriptor, openStackClient);
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
        OpenStackResourceAttribute<?, ?> connectAttribute(final String attributeID, final AttributeDescriptor descriptor, final OSClient openStackClient) throws AttributeNotFoundException, OpenStackAbsentConfigurationParameterException {
            switch (descriptor.getAttributeName()){
                case HypervisorFreeRamAttribute.NAME:
                    return new HypervisorFreeRamAttribute(attributeID, descriptor, openStackClient);
                case HypervisorFreeDiskAttribute.NAME:
                    return new HypervisorFreeDiskAttribute(attributeID, descriptor, openStackClient);
                case HypervisorWorkloadAttribute.NAME:
                    return new HypervisorWorkloadAttribute(attributeID, descriptor, openStackClient);
                case HypervisorHostIpAttribute.NAME:
                    return new HypervisorHostIpAttribute(attributeID, descriptor, openStackClient);
                case HypervisorHostnameAttribute.NAME:
                    return new HypervisorHostnameAttribute(attributeID, descriptor, openStackClient);
                default:
                    throw JMExceptionUtils.attributeNotFound(descriptor.getAttributeName());
            }
        }
    };

    private final String resourceType;

    OpenStackResourceType(final String name){
        this.resourceType = name;
    }

    abstract boolean checkCapability(final OSClient client);

    abstract OpenStackResourceAttribute<?, ?> connectAttribute(final String attributeID,
                                                               final AttributeDescriptor descriptor,
                                                               final OSClient openStackClient) throws AttributeNotFoundException, OpenStackAbsentConfigurationParameterException;

    @Override
    public final String toString() {
        return resourceType;
    }

    static OpenStackResourceType parse(final String resourceType){
        for(final OpenStackResourceType type: values())
            if(type.resourceType.equals(resourceType)) return type;
        return null;
    }
}
