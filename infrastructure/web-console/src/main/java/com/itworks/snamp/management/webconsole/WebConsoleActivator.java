package com.itworks.snamp.management.webconsole;

import com.itworks.snamp.core.AbstractBundleActivator;
import org.osgi.framework.BundleContext;

import java.util.Collection;

/**
 * @author Roman Sakno
 * @version 1.0
 * @since 1.0
 */
public final class WebConsoleActivator extends AbstractBundleActivator {

    /**
     * Starts the bundle and instantiate runtime state of the bundle.
     *
     * @param context                 The execution context of the bundle being started.
     * @param bundleLevelDependencies A collection of bundle-level dependencies to fill.
     * @throws Exception An exception occurred during starting.
     */
    @Override
    protected void start(final BundleContext context, final Collection<RequiredService<?>> bundleLevelDependencies) throws Exception {

    }

    /**
     * Activates the bundle.
     * <p>
     * This method will be called when all bundle-level dependencies will be resolved.
     * </p>
     *
     * @param context              The execution context of the bundle being activated.
     * @param activationProperties A collection of bundle's activation properties to fill.
     * @param dependencies         A collection of resolved dependencies.
     * @throws Exception An exception occurred during activation.
     */
    @Override
    protected void activate(final BundleContext context, final ActivationPropertyPublisher activationProperties, final RequiredService<?>... dependencies) throws Exception {

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
