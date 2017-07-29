package com.bytex.snamp.supervision.discovery;

import javax.annotation.Nonnull;

/**
 * Attempts to register existing resource with different group name.
 * @author Roman Sakno
 * @version 2.1
 * @since 2.0
 */
public final class InvalidResourceGroupException extends ResourceDiscoveryException {
    private static final long serialVersionUID = -7000587336289333690L;
    private final String resourceName, actualGroup, expectedGroup;

    public InvalidResourceGroupException(@Nonnull final String resourceName,
                                  @Nonnull final String actualGroupName,
                                  @Nonnull final String expectedGroupName){
        super(String.format("Unable to override resource group for resource %s. Actual: %s. Expected: %s", resourceName, actualGroupName, expectedGroupName));
        this.resourceName = resourceName;
        this.actualGroup = actualGroupName;
        this.expectedGroup = expectedGroupName;
    }

    public String getResourceName(){
        return resourceName;
    }

    public String getActualGroup(){
        return actualGroup;
    }

    public String getExpectedGroup(){
        return expectedGroup;
    }
}
