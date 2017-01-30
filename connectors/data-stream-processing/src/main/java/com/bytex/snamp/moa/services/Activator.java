package com.bytex.snamp.moa.services;

import com.bytex.snamp.SpecialUse;
import com.bytex.snamp.configuration.ConfigurationManager;
import com.bytex.snamp.core.AbstractServiceLibrary;
import com.bytex.snamp.moa.DataAnalyzer;
import com.bytex.snamp.moa.topology.TopologyAnalyzer;

import java.io.IOException;
import java.util.Collection;
import java.util.Map;

/**
 * @author Roman Sakno
 * @version 2.0
 * @since 2.0
 */
public final class Activator extends AbstractServiceLibrary {
    private static final class TopologyAnalyzerServiceProvider extends ProvidedService<TopologyAnalyzer, TopologyAnalyzerService> {
        private TopologyAnalyzerServiceProvider() {
            super(TopologyAnalyzer.class, simpleDependencies(ConfigurationManager.class), DataAnalyzer.class);
        }

        @Override
        protected TopologyAnalyzerService activateService(final Map<String, Object> identity) throws IOException {
            final TopologyAnalyzerService service = new TopologyAnalyzerService(getDependencies().getDependency(ConfigurationManager.class));
            service.init();
            return service;
        }

        @Override
        protected void cleanupService(final TopologyAnalyzerService serviceInstance, final boolean stopBundle) {
            serviceInstance.close();
        }
    }

    @SpecialUse(SpecialUse.Case.OSGi)
    public Activator(){
        super(new TopologyAnalyzerServiceProvider());
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
