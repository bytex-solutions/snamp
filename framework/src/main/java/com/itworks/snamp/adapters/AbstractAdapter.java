package com.itworks.snamp.adapters;

import org.osgi.framework.BundleActivator;

/**
 * Represents a base class for SNAMP adapters.
 * <p>
 *     SNAMP adapter exposes management information obtained through management connectors to the
 *     outside world. It responsible to convert SNAMP management information object model
 *     to the protocol-specific data.
 * </p>
 * @author Roman Sakno
 * @since 1.0
 * @version 1.0
 */
public abstract class AbstractAdapter implements BundleActivator {

}