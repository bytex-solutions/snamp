package com.bytex.snamp.moa.services;

import com.bytex.snamp.SpecialUse;
import com.bytex.snamp.concurrent.ThreadPoolRepository;
import com.bytex.snamp.configuration.ConfigurationManager;
import com.bytex.snamp.configuration.internal.CMManagedResourceGroupWatcherParser;
import com.bytex.snamp.core.AbstractBundleActivator;
import com.bytex.snamp.internal.Utils;
import org.osgi.framework.BundleContext;

/**
 * @author Roman Sakno
 * @version 2.0
 * @since 2.0
 */
public final class Activator extends AbstractBundleActivator {
    private static final String ANALYTICAL_THREAD_POOL = "analyticalThreadPool";
    private static final ActivationProperty<AnalyticalGateway> GATEWAY_PROPERTY = defineActivationProperty(AnalyticalGateway.class);


    @SpecialUse(SpecialUse.Case.OSGi)
    public Activator() {
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
        final CMManagedResourceGroupWatcherParser watcherParser = configurationManager.queryObject(CMManagedResourceGroupWatcherParser.class);
        assert watcherParser != null;
        final AnalyticalGateway service = new AnalyticalGateway(
                Utils.getBundleContextOfObject(this),
                repository.getThreadPool(ANALYTICAL_THREAD_POOL, true),
                watcherParser
        );
        service.update(configurationManager.getConfiguration());
        activationProperties.publish(GATEWAY_PROPERTY, service);
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
        activationProperties.getProperty(GATEWAY_PROPERTY).close();
    }

    /**
     * Stops the bundle.
     *
     * @param context The execution context of the bundle being stopped.
     * @throws Exception An exception occurred during bundle stopping.
     */
    @Override
    protected void shutdown(final BundleContext context) throws Exception {

    }
}
