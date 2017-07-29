package com.bytex.snamp.supervision;

import com.bytex.snamp.connector.ManagedResourceConnectorClient;
import org.osgi.framework.BundleContext;

import javax.annotation.Nonnull;
import javax.management.InstanceNotFoundException;

/**
 * Indicates that the set of resources inside of the group was changed (removed or added).
 * @author Roman Sakno
 * @version 2.1
 * @since 2.0
 */
public abstract class GroupCompositionChangedEvent extends SupervisionEvent {
    private static final long serialVersionUID = 4363507741090562209L;
    private final String resourceName;

    protected GroupCompositionChangedEvent(@Nonnull final Object source,
                                           @Nonnull final String resourceName,
                                           @Nonnull final String groupName){
        super(source, groupName);
        this.resourceName = resourceName;
    }

    /**
     * Gets name of the discovered resource.
     * @return Name of the discovered resource.
     */
    public final String getResourceName(){
        return resourceName;
    }

    public final ManagedResourceConnectorClient createResourceClient(final BundleContext context) throws InstanceNotFoundException {
        return ManagedResourceConnectorClient.tryCreate(context, resourceName)
                .orElseThrow(() -> new InstanceNotFoundException(String.format("Resource %s is no longer available", resourceName)));
    }
}
