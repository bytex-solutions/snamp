package com.bytex.snamp.core;

import com.google.common.base.Supplier;
import org.osgi.framework.ServiceReference;

/**
 * Represents OSGi service provider that combines a service reference and accessor
 * to the service instance.
 * @author Roman Sakno
 * @version 1.0
 * @since 1.0
 */
public interface ServiceProvider<S> extends ServiceReference<S>, Supplier<S> {
}
