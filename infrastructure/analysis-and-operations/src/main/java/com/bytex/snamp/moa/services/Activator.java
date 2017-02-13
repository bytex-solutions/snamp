package com.bytex.snamp.moa.services;

import com.bytex.snamp.SpecialUse;
import com.bytex.snamp.configuration.ConfigurationManager;
import com.bytex.snamp.core.AbstractServiceLibrary;
import com.bytex.snamp.gateway.Gateway;
import com.bytex.snamp.internal.Utils;
import com.bytex.snamp.moa.DataAnalyzer;
import com.bytex.snamp.moa.topology.TopologyAnalyzer;
import com.google.common.collect.ImmutableMap;
import org.osgi.framework.BundleContext;

import java.io.IOException;
import java.util.Collection;
import java.util.Map;

/**
 * @author Roman Sakno
 * @version 2.0
 * @since 2.0
 */
public final class Activator extends AbstractServiceLibrary {
    private static final class AnalyticsServiceProvider extends ProvidedService<AnalyticalCenter, AnalyticalGateway> {
        private AnalyticsServiceProvider() {
            super(AnalyticalCenter.class, simpleDependencies(ConfigurationManager.class), DataAnalyzer.class, TopologyAnalyzer.class);
        }

        @Override
        protected AnalyticalGateway activateService(final Map<String, Object> identity) throws Exception {
            final AnalyticalGateway service = new AnalyticalGateway(Utils.getBundleContextOfObject(this));
            final ConfigurationManager configurationManager = getDependencies().getDependency(ConfigurationManager.class);
            assert configurationManager != null;
            service.update(configurationManager.getConfiguration());
            return service;
        }

        @Override
        protected void cleanupService(final AnalyticalGateway service, boolean stopBundle) throws Exception {
            service.close();
        }
    }

    @SpecialUse(SpecialUse.Case.OSGi)
    public Activator() {
        super(new AnalyticsServiceProvider());
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
     * @throws Exception Unable to activate this library.
     */
    @Override
    protected void activate(final ActivationPropertyPublisher activationProperties) throws Exception {

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
