package com.itworks.snamp.configuration.impl;

import com.itworks.snamp.configuration.AbstractConfigurationBundleActivator;
import com.itworks.snamp.internal.annotations.MethodStub;

import java.util.Collection;
import java.util.Map;

/**
 * Represents activator for the configuration manager.
 * @author Roman Sakno
 * @version 1.0
 * @since 1.0
 */
public final class ConfigurationBundleActivator extends AbstractConfigurationBundleActivator {
    private static final class XmlConfigurationManagerProvider extends ConfigurationManagerProvider<XmlConfigurationManager>{

        /**
         * Creates a new instance of the service.
         *
         * @param identity     A dictionary of properties that uniquely identifies service instance.
         * @param dependencies A collection of dependencies.
         * @return A new instance of the service.
         */
        @Override
        protected XmlConfigurationManager activateService(final Map<String, Object> identity, final RequiredService<?>... dependencies) {
            return new XmlConfigurationManager();
        }
    }

    /**
     * Initializes a new instance of the configuration bundle activator.
     */
    public ConfigurationBundleActivator(){
        super(new XmlConfigurationManagerProvider());
    }

    /**
     * Starts the service library.
     *
     * @param bundleLevelDependencies A collection of library-level dependencies to be required for this library.
     */
    @Override
    @MethodStub
    protected void start(final Collection<RequiredService<?>> bundleLevelDependencies) {

    }

    /**
     * Activates this service library.
     *
     * @param activationProperties A collection of library activation properties to fill.
     * @param dependencies         A collection of resolved library-level dependencies.
     */
    @Override
    @MethodStub
    protected void activate(final ActivationPropertyPublisher activationProperties, final RequiredService<?>... dependencies) {

    }

    /**
     * Deactivates this library.
     *
     * @param activationProperties A collection of library activation properties to read.
     */
    @Override
    @MethodStub
    protected void deactivate(final ActivationPropertyReader activationProperties) {

    }
}
