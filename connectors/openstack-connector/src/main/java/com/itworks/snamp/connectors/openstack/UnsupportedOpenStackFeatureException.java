package com.itworks.snamp.connectors.openstack;

/**
 * @author Roman Sakno
 * @version 1.0
 * @since 1.0
 */
final class UnsupportedOpenStackFeatureException extends Exception {
    private final OpenStackResourceType resourceType;

    UnsupportedOpenStackFeatureException(final OpenStackResourceType resourceType){
        super(String.format("OpenStack feature '%s' is not supported", resourceType));
        this.resourceType = resourceType;
    }

    OpenStackResourceType getResourceType(){
        return resourceType;
    }
}
