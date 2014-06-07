package com.itworks.snamp.monitoring.impl;

import com.itworks.snamp.core.AbstractServiceLibrary;
import com.itworks.snamp.management.SnampManager;

import java.util.Collection;
import java.util.Map;

/**
 * @author Roman Sakno
 * @version 1.0
 * @since 1.0
 */
public final class MonitoringServiceLibrary extends AbstractServiceLibrary {
    private static final class SnampManagerProvider extends ProvidedService<SnampManager, SnampManagerImpl>{

        public SnampManagerProvider() {
            super(SnampManager.class);
        }

        /**
         * Creates a new instance of the service.
         *
         * @param identity     A dictionary of properties that uniquely identifies service instance.
         * @param dependencies A collection of dependencies.
         * @return A new instance of the service.
         */
        @Override
        protected SnampManagerImpl activateService(final Map<String, Object> identity, final RequiredService<?>... dependencies) {
            return new SnampManagerImpl();
        }
    }

    public MonitoringServiceLibrary(){
        super(new SnampManagerProvider());
    }

    /**
     * Starts the service library.
     *
     * @param bundleLevelDependencies A collection of library-level dependencies to be required for this library.
     * @throws Exception Unable to start service library.
     */
    @Override
    protected void start(final Collection<RequiredService<?>> bundleLevelDependencies) throws Exception {

    }

    /**
     * Activates this service library.
     *
     * @param activationProperties A collection of library activation properties to fill.
     * @param dependencies         A collection of resolved library-level dependencies.
     * @throws Exception Unable to activate this library.
     */
    @Override
    protected void activate(final ActivationPropertyPublisher activationProperties, final RequiredService<?>... dependencies) throws Exception {

    }

    /**
     * Deactivates this library.
     *
     * @param activationProperties A collection of library activation properties to read.
     * @throws Exception Unable to deactivate this library.
     */
    @Override
    protected void deactivate(final ActivationPropertyReader activationProperties) throws Exception {

    }
}
