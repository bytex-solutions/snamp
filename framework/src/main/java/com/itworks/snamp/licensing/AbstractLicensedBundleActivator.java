package com.itworks.snamp.licensing;

import com.itworks.snamp.core.AbstractLoggableBundleActivator;
import org.apache.commons.lang3.ArrayUtils;

/**
 * Represents a base class for bundles that uses SNAMP licensing service.
 * @author Roman Sakno
 * @version 1.0
 * @since 1.0
 */
public abstract class AbstractLicensedBundleActivator extends AbstractLoggableBundleActivator {
    protected abstract class LicensedService<S extends LicensedPlatformPlugin<?>, T extends S> extends LoggableProvidedService<S, T>{


        /**
         * Initializes a new holder for the provided service.
         *
         * @param contract     Contract of the provided service. Cannot be {@literal null}.
         * @param dependencies A collection of service dependencies.
         * @throws IllegalArgumentException contract is {@literal null}.
         */
        protected LicensedService(final Class<S> contract,
                                  final RequiredServiceAccessor<LicenseReader> licenseDependency,
                                  final RequiredService<?>... dependencies) {
            super(contract, ArrayUtils.addAll(dependencies, licenseDependency));
        }
    }

    /**
     * Initializes a new instance of the bundle activator.
     *
     * @param loggerName       The name of the logger that will be connected to OSGi log service and shared between
     *                         provided services.
     * @param providedServices A collection of provided services.
     */
    protected AbstractLicensedBundleActivator(final String loggerName, final ProvidedService<?, ?>... providedServices) {
        super(loggerName, providedServices);
    }

    /**
     * Initializes a new instance of the bundle activator.
     *
     * @param loggerName       The name of the logger that will be connected to OSGi log service and shared between
     *                         provided services.
     * @param providedServices A collection of provided services. Cannot be {@literal null}.
     * @throws IllegalArgumentException providedServices is {@literal null}.
     */
    protected AbstractLicensedBundleActivator(final String loggerName, final ProvidedServices providedServices) {
        super(loggerName, providedServices);
    }
}
