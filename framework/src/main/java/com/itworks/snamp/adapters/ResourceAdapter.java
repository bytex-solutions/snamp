package com.itworks.snamp.adapters;

import com.itworks.snamp.core.FrameworkService;
import org.osgi.framework.ServiceListener;

import java.io.Closeable;

/**
 * Represents resource adapter.
 * @author Roman Sakno
 * @version 1.0
 * @since 1.0
 */
public interface ResourceAdapter extends FrameworkService, ServiceListener, Closeable {
    /**
     * Represents name of the bundle manifest header that contains system name of the adapter.
     */
    String ADAPTER_NAME_MANIFEST_HEADER = "SNAMP-Resource-Adapter";

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
