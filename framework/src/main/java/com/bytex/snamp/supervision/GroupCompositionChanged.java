package com.bytex.snamp.supervision;

import com.bytex.snamp.connector.ManagedResourceConnectorClient;
import org.osgi.framework.BundleContext;

import javax.annotation.Nonnull;
import javax.management.InstanceNotFoundException;

/**
 * Indicates that the set of resources inside of the group was changed (removed or added).
 * @author Roman Sakno
 * @version 2.0
 * @since 2.0
 */
public class GroupCompositionChanged extends SupervisionEvent {
    /**
     * Group modification type.
     */
    public enum Modifier {
        /**
         * Resource was added to the group.
         */
        ADDED,

        /**
         * Resource was removed from the group.
         */
        REMOVED
    }
    private static final long serialVersionUID = 4363507741090562209L;
    private final String resourceName;
    private final Modifier modification;

    protected GroupCompositionChanged(@Nonnull final Object source,
                                      @Nonnull final String resourceName,
                                      @Nonnull final String groupName,
                                      @Nonnull final Modifier modification){
        super(source, groupName);
        this.resourceName = resourceName;
        this.modification = modification;
    }

    /**
     * Gets modification type described by this event.
     * @return Modification type.
     */
    public final Modifier getModifier(){
        return modification;
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

    static GroupCompositionChanged resourceAdded(@Nonnull final Object source,
                                                 @Nonnull final String resourceName,
                                                 @Nonnull final String groupName){
        return new GroupCompositionChanged(source, resourceName, groupName, Modifier.ADDED);
    }

    static GroupCompositionChanged resourceRemoved(@Nonnull final Object source,
                                                 @Nonnull final String resourceName,
                                                 @Nonnull final String groupName) {
        return new GroupCompositionChanged(source, resourceName, groupName, Modifier.REMOVED);
    }
}
