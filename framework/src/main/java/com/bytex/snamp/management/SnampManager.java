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
     * Returns a read-only collection of installed resource connector.
     * @return A read-only collection of installed resource connector.
     */
    Collection<? extends SnampComponentDescriptor> getInstalledResourceConnectors();

    /**
     * Returns a read-only collection of installed gateways.
     * @return A read-only collection of installed gateways.
     */
    Collection<? extends SnampComponentDescriptor> getInstalledGateways();

    /**
     * Returns a read-only collection of installed additional SNAMP components.
     * @return A read-only collection of installed additional SNAMP components.
     */
    Collection<? extends SnampComponentDescriptor> getInstalledComponents();
}
