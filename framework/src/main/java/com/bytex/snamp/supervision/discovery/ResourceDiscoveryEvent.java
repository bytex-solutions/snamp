package com.bytex.snamp.supervision.discovery;

import com.bytex.snamp.connector.ManagedResourceConnectorClient;
import org.osgi.framework.BundleContext;

import javax.annotation.Nonnull;
import javax.management.InstanceNotFoundException;
import java.util.EventObject;

/**
 * Represents resource discovery event.
 * @author Roman Sakno
 * @version 2.0
 * @since 2.0
 */
public class ResourceDiscoveryEvent extends EventObject {
    private static final long serialVersionUID = 4363507741090562209L;
    private final String resourceName;

    protected ResourceDiscoveryEvent(@Nonnull final Object source,
                                     @Nonnull final String resourceName){
        super(source);
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
