package com.bytex.snamp.core;

import org.osgi.framework.ServiceReference;

import java.util.function.Supplier;

/**
 * Represents OSGi service provider that combines a service reference and accessor
 * to the service instance.
 * @author Roman Sakno
 * @version 2.0
 * @since 1.0
 */
public interface ServiceProvider<S> extends ServiceReference<S>, Supplier<S> {
}
