package com.bytex.snamp.moa.services;

import com.bytex.snamp.SpecialUse;
import com.bytex.snamp.concurrent.ThreadPoolRepository;
import com.bytex.snamp.configuration.ConfigurationManager;
import com.bytex.snamp.core.AbstractServiceLibrary;
import com.bytex.snamp.internal.Utils;
import com.bytex.snamp.moa.DataAnalyzer;
import com.bytex.snamp.moa.topology.TopologyAnalyzer;

import java.util.Collection;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;

/**
 * @author Roman Sakno
 * @version 2.0
 * @since 2.0
 */
public final class Activator extends AbstractServiceLibrary {
    private static final class AnalyticalCenterProvider extends ProvidedService<AnalyticalCenter, AnalyticalGateway> {
        private static final String ANALYTICAL_THREAD_POOL = "analyticalThreadPool";

        private AnalyticalCenterProvider() {
            super(AnalyticalCenter.class, simpleDependencies(ConfigurationManager.class, ThreadPoolRepository.class));
        }

        @Override
        protected AnalyticalGateway activateService(final Map<String, Object> identity) throws Exception {
            final ConfigurationManager configurationManager = getDependencies().getDependency(ConfigurationManager.class);
            assert configurationManager != null;
            final ThreadPoolRepository repository = getDependencies().getDependency(ThreadPoolRepository.class);
            assert repository != null;
            final AnalyticalGateway service = new AnalyticalGateway(
                    Utils.getBundleContextOfObject(this),
                    repository.getThreadPool(ANALYTICAL_THREAD_POOL, true)
            );
            service.update(configurationManager.getConfiguration());
            return service;
        }

        @Override
        protected void cleanupService(final AnalyticalGateway service, boolean stopBundle) throws Exception {
            service.close();
        }
    }

    private static final class AnalyticalServiceProvider<S extends DataAnalyzer> extends ProvidedService<S, S>{
        private final Function<AnalyticalCenter, S> serviceProvider;

        private AnalyticalServiceProvider(final Class<S> serviceType, final Function<AnalyticalCenter, S> serviceProvider){
            super(serviceType, simpleDependencies(AnalyticalCenter.class));
            this.serviceProvider = Objects.requireNonNull(serviceProvider);
        }

        @Override
        protected S activateService(final Map<String, Object> identity) throws Exception {
            final AnalyticalCenter center = getDependencies().getDependency(AnalyticalCenter.class);
            assert center != null;
            final S service = serviceProvider.apply(center);
            assert service != null;
            return service;
        }

        @Override
        protected void cleanupService(final S serviceInstance, final boolean stopBundle) {
            //nothing to do
        }
    }

    @SpecialUse(SpecialUse.Case.OSGi)
    public Activator() {
        super(new AnalyticalCenterProvider(),
                new AnalyticalServiceProvider<>(TopologyAnalyzer.class, AnalyticalCenter::getTopologyAnalyzer));
    }

    /**
     * Starts the service library.
     *
     * @param bundleLevelDependencies A collection of library-level dependencies to be required for this library.
     */
    @Override
    protected void start(final Collection<RequiredService<?>> bundleLevelDependencies) {
    }

    /**
     * Activates this service library.
     *
     * @param activationProperties A collection of library activation properties to fill.
     */
    @Override
    protected void activate(final ActivationPropertyPublisher activationProperties) {
    }

    /**
     * Deactivates this library.
     *
     * @param activationProperties A collection of library activation properties to read.
     */
    @Override
    protected void deactivate(final ActivationPropertyReader activationProperties) {
    }
}
