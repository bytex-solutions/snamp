package com.bytex.snamp.moa.services;

import com.bytex.snamp.SpecialUse;
import com.bytex.snamp.concurrent.ThreadPoolRepository;
import com.bytex.snamp.configuration.ConfigurationManager;
import com.bytex.snamp.configuration.internal.CMSupervisorParser;
import com.bytex.snamp.supervision.HealthStatusProvider;
import com.bytex.snamp.core.AbstractServiceLibrary;
import com.bytex.snamp.moa.DataAnalyzer;
import com.bytex.snamp.moa.topology.TopologyAnalyzer;
import org.osgi.framework.BundleContext;
import org.osgi.framework.Constants;
import org.osgi.service.cm.ManagedService;

import java.util.Collection;
import java.util.Map;

/**
 * @author Roman Sakno
 * @version 2.0
 * @since 2.0
 */
public final class Activator extends AbstractServiceLibrary {
    private static final String ANALYTICAL_THREAD_POOL = "analyticalThreadPool";
    private static final ActivationProperty<AnalyticalGateway> GATEWAY_PROPERTY = defineActivationProperty(AnalyticalGateway.class);

    private static abstract class AnalyticalServiceProvider<S extends DataAnalyzer, T extends S> extends ProvidedService<S, T>{
        @SafeVarargs
        AnalyticalServiceProvider(final Class<S> contract, final RequiredService<?>[] dependencies, final Class<? super S>... subInterfaces) {
            super(contract, dependencies, subInterfaces);
        }

        abstract T activateService(final AnalyticalGateway gateway, final Map<String, Object> identity);

        @Override
        protected T activateService(final Map<String, Object> identity) {
            final AnalyticalGateway gateway = getActivationPropertyValue(GATEWAY_PROPERTY);
            assert gateway != null;
            return activateService(gateway, identity);
        }

        @Override
        protected void cleanupService(final T serviceInstance, final boolean stopBundle) {
            serviceInstance.reset();
        }
    }

    private static final class TopologyAnalyzerProvider extends AnalyticalServiceProvider<TopologyAnalyzer, TopologyAnalysisImpl>{
        TopologyAnalyzerProvider(){
            super(TopologyAnalyzer.class, new RequiredService<?>[0]);
        }

        @Override
        TopologyAnalysisImpl activateService(final AnalyticalGateway gateway, final Map<String, Object> identity) {
            return gateway.getTopologyAnalyzer();
        }
    }

    private static final class HealthAnalyzerProvider extends AnalyticalServiceProvider<HealthAnalyzer, HealthAnalyzerImpl>{
        HealthAnalyzerProvider(){
            super(HealthAnalyzer.class, new RequiredService<?>[0], HealthStatusProvider.class, ManagedService.class);
        }

        @Override
        HealthAnalyzerImpl activateService(final AnalyticalGateway gateway, final Map<String, Object> identity) {
            final HealthAnalyzerImpl healthAnalyzer = gateway.getHealthAnalyzer();
            identity.put(Constants.SERVICE_PID, healthAnalyzer.getPersistentID());
            return healthAnalyzer;
        }
    }

    @SpecialUse(SpecialUse.Case.OSGi)
    public Activator() {
        super(Activator::provideAnalyticalServices);
    }

    private static void provideAnalyticalServices(final Collection<ProvidedService<?, ?>> services,
                                                  final ActivationPropertyReader activationProperties,
                                                      final DependencyManager bundleLevelDependencies) {
        services.add(new HealthAnalyzerProvider());
        services.add(new TopologyAnalyzerProvider());
    }

    /**
     * Starts the bundle and instantiate runtime state of the bundle.
     *
     * @param context                 The execution context of the bundle being started.
     * @param bundleLevelDependencies A collection of bundle-level dependencies to fill.
     * @throws Exception An exception occurred during starting.
     */
    @Override
    protected void start(final BundleContext context, final DependencyManager bundleLevelDependencies) throws Exception {
        bundleLevelDependencies.add(ConfigurationManager.class);
        bundleLevelDependencies.add(ThreadPoolRepository.class);
    }

    @Override
    protected void activate(final BundleContext context, final ActivationPropertyPublisher activationProperties, final DependencyManager dependencies) throws Exception {
        final ConfigurationManager configurationManager = dependencies.getDependency(ConfigurationManager.class);
        assert configurationManager != null;
        final ThreadPoolRepository repository = dependencies.getDependency(ThreadPoolRepository.class);
        assert repository != null;
        final CMSupervisorParser watcherParser = configurationManager.queryObject(CMSupervisorParser.class);
        assert watcherParser != null;
        final AnalyticalGateway service = new AnalyticalGateway(
                context,
                repository.getThreadPool(ANALYTICAL_THREAD_POOL, true),
                watcherParser
        );
        service.update(configurationManager.getConfiguration());
        activationProperties.publish(GATEWAY_PROPERTY, service);
        super.activate(context, activationProperties, dependencies);  //at this point number of analytical services will be instantiated
    }

    /**
     * Deactivates the bundle.
     * <p>
     * This method will be called when at least one bundle-level dependency will be lost.
     * </p>
     *
     * @param context              The execution context of the bundle being deactivated.
     * @param activationProperties A collection of activation properties to read.
     * @throws Exception An exception occurred during bundle deactivation.
     */
    @Override
    protected void deactivate(final BundleContext context, final ActivationPropertyReader activationProperties) throws Exception {
        try {
            activationProperties.getProperty(GATEWAY_PROPERTY).close();
        } finally {
            super.deactivate(context, activationProperties);
        }
    }
}
