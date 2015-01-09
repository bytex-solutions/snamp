package com.itworks.snamp.adapters;

import com.itworks.snamp.core.FrameworkService;
import org.osgi.framework.ServiceListener;

/**
 * Represents resource adapter.
 * @author Roman Sakno
 * @version 1.0
 * @since 1.0
 */
public interface ResourceAdapter extends FrameworkService, ServiceListener, AutoCloseable {
    /**
     * Gets name of the resource adapter instance.
     * @return The name of the resource adapter instance.
     */
    String getInstanceName();

    /**
     * Gets state of this resource adapter.
     * @return The state of this resource adapter.
     */
    AdapterState getState();
}
