package com.bytex.snamp.moa.services;

import com.bytex.snamp.SpecialUse;
import com.bytex.snamp.concurrent.ThreadPoolRepository;
import com.bytex.snamp.configuration.ConfigurationManager;
import com.bytex.snamp.configuration.internal.CMManagedResourceGroupWatcherParser;
import com.bytex.snamp.connector.supervision.HealthSupervisor;
import com.bytex.snamp.core.AbstractServiceLibrary;
import com.bytex.snamp.internal.Utils;
import com.bytex.snamp.moa.DataAnalyzer;
import com.bytex.snamp.moa.topology.TopologyAnalyzer;
import org.osgi.framework.Constants;
import org.osgi.service.cm.ManagedService;

import javax.annotation.Nonnull;
import java.util.Collection;
import java.util.Map;

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
            final CMManagedResourceGroupWatcherParser watcherParser = configurationManager.queryObject(CMManagedResourceGroupWatcherParser.class);
            assert watcherParser != null;
            final AnalyticalGateway service = new AnalyticalGateway(
                    Utils.getBundleContextOfObject(this),
                    repository.getThreadPool(ANALYTICAL_THREAD_POOL, true),
                    watcherParser
            );
            service.update(configurationManager.getConfiguration());
            return service;
        }

        @Override
        protected void cleanupService(final AnalyticalGateway service, boolean stopBundle) throws Exception {
            service.close();
        }
    }

    private static abstract class AnalyticalServiceProvider<S extends DataAnalyzer> extends ProvidedService<S, S>{

        @SafeVarargs
        AnalyticalServiceProvider(final Class<S> serviceType, final Class<? super S>... subInterfaces){
            super(serviceType, simpleDependencies(AnalyticalCenter.class), subInterfaces);
        }

        @Nonnull
        abstract S activateService(final AnalyticalCenter service, final Map<String, Object> identity);

        @Override
        protected final S activateService(final Map<String, Object> identity) throws Exception {
            final AnalyticalCenter center = getDependencies().getDependency(AnalyticalCenter.class);
            assert center != null;
            return activateService(center, identity);
        }

        @Override
        protected void cleanupService(final S serviceInstance, final boolean stopBundle) {
            serviceInstance.reset();
        }
    }

    private static final class TopologyAnalyzerProvider extends AnalyticalServiceProvider<TopologyAnalyzer>{
        TopologyAnalyzerProvider(){
            super(TopologyAnalyzer.class);
        }

        @Nonnull
        @Override
        TopologyAnalyzer activateService(final AnalyticalCenter service, final Map<String, Object> identity) {
            return service.getTopologyAnalyzer();
        }
    }

    private static final class HealthAnalyzerProvider extends AnalyticalServiceProvider<HealthAnalyzer>{
        HealthAnalyzerProvider(){
            super(HealthAnalyzer.class, ManagedService.class, HealthSupervisor.class);
        }

        @Nonnull
        @Override
        HealthAnalyzer activateService(final AnalyticalCenter service, final Map<String, Object> identity) {
            final HealthAnalyzer analyzer = service.getHealthAnalyzer();
            identity.put(Constants.SERVICE_PID, analyzer.getPersistentID());
            return analyzer;
        }
    }

    @SpecialUse(SpecialUse.Case.OSGi)
    public Activator() {
        super(new AnalyticalCenterProvider(),
                new TopologyAnalyzerProvider(),
                new HealthAnalyzerProvider());
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
