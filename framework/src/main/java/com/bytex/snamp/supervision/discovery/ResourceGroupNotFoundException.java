package com.bytex.snamp.supervision.discovery;

import javax.annotation.Nonnull;

/**
 * Indicates that the resource cannot be registered because its group doesn't exist
 * in SNAMP configuration.
 * @author Roman Sakno
 * @version 2.0
 * @since 2.0
 */
public final class ResourceGroupNotFoundException extends ResourceDiscoveryException {
    private static final long serialVersionUID = 5583332904377594876L;
    private final String groupName;

    public ResourceGroupNotFoundException(@Nonnull final String groupName){
        super(String.format("Group of resources with name %s doesn't exist", groupName));
        this.groupName = groupName;
    }

    public String getGroupName(){
        return groupName;
    }
}
