package com.bytex.snamp.management;

import com.bytex.snamp.core.SupportService;

import java.util.Collection;

/**
 * Represents SNAMP monitor and manager.
 * @author Roman Sakno
 * @version 2.0
 * @since 1.0
 */
public interface SnampManager extends SupportService {

    /**
     * Returns a read-only collection of installed resource connectors.
     * @return A read-only collection of installed resource connectors.
     */
    Collection<? extends SnampComponentDescriptor> getInstalledResourceConnectors();

    /**
     * Returns a read-only collection of installed resource adapters.
     * @return A read-only collection of installed resource adapters.
     */
    Collection<? extends SnampComponentDescriptor> getInstalledResourceAdapters();

    /**
     * Returns a read-only collection of installed additional SNAMP components.
     * @return A read-only collection of installed additional SNAMP components.
     */
    Collection<? extends SnampComponentDescriptor> getInstalledComponents();
}
