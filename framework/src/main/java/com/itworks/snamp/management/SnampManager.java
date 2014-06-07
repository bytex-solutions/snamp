package com.itworks.snamp.management;

import com.itworks.snamp.core.FrameworkService;

import java.util.Collection;

/**
 * Represents SNAMP monitor and manager.
 * @author Roman Sakno
 * @version 1.0
 * @since 1.0
 */
public interface SnampManager extends FrameworkService {
    /**
     * Returns a read-only collection of installed resource connectors.
     * @return A read-only collection of installed resource connectors.
     */
    Collection<SnampComponentDescriptor> getInstalledResourceConnectors();

    /**
     * Returns a read-only collection of installed resource adapters.
     * @return A read-only collection of installed resource adapters.
     */
    Collection<SnampComponentDescriptor> getInstalledResourceAdapters();

    /**
     * Returns a read-only collection of installed additional SNAMP components.
     * @return A read-only collection of installed additional SNAMP components.
     */
    Collection<SnampComponentDescriptor> getInstalledComponents();
}
