package com.itworks.snamp.core;

import com.itworks.snamp.internal.OsgiLoggerBridge;
import com.itworks.snamp.internal.semantics.Partial;
import org.osgi.framework.ServiceReference;
import org.osgi.service.log.LogService;

import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Represents a base class for bundle activators that requires logging functionality.
 * @author Roman Sakno
 * @version 1.0
 * @since 1.0
 */
public abstract class AbstractLoggableServiceLibrary extends AbstractServiceLibrary {
    private static final ActivationProperty<Logger> LOGGER_HOLDER = defineActivationProperty(Logger.class);

    /**
     * Represents {@link org.osgi.service.log.LogService} dependency descriptor.
     * @author Roman Sakno
     * @since 1.0
     * @version 1.0
     */
    public static final class LoggerServiceDependency extends RequiredService<LogService> {
        private final Logger logger;

        /**
         * Initializes a new {@link LogService} dependency descriptor.
         * @throws java.lang.IllegalArgumentException loggerInstance is {@literal null}.
         */
        public LoggerServiceDependency(final Logger logger){
            super(LogService.class);
            this.logger = logger;
        }

        /**
         * Provides matching reference to the conditions of this dependency.
         * <p>This method should be implemented in stateless manner.</p>
         *
         * @param reference The service reference to check.
         * @return {@literal true}, if the specified reference matches to the dependency resolving conditions;
         *         otherwise, {@literal false}.
         */
        @Override
        protected boolean match(final ServiceReference<?> reference) {
            return true;
        }

        @Override
        protected void bind(final LogService serviceInstance,
                            final Dictionary<String, ?> properties) {
            OsgiLoggerBridge.connectToLogService(logger, serviceInstance);
        }

        @Override
        protected void unbind() {
            OsgiLoggerBridge.disconnectFromLogService(logger);
        }
    }

    /**
     * Represents a holder for the public service that requires a logging functionality.
     * @param <S> Contract of the service.
     * @param <T> Implementation of the service.
     * @author Roman Sakno
     * @since 1.0
     * @version 1.0
     */
    protected static abstract class LoggableProvidedService<S extends FrameworkService, T extends S> extends ProvidedService<S, T>{

        /**
         * Initializes a new holder for the provided service.
         *
         * @param contract     Contract of the provided service. Cannot be {@literal null}.
         * @param dependencies A collection of service dependencies.
         * @throws IllegalArgumentException contract is {@literal null}.
         */
        protected LoggableProvidedService(final Class<S> contract, final RequiredService<?>... dependencies) {
            super(contract, dependencies);
        }

        /**
         * Gets logger from the shared context that can be passed into service.
         * @return The logger from the shared context.
         */
        protected final Logger getLogger(){
            return getActivationPropertyValue(LOGGER_HOLDER);
        }
    }

    private final Logger bundleLogger;

    /**
     * Initializes a new instance of the bundle activator.
     * @param loggerName The name of the logger that will be connected to OSGi log service and shared between
     *                   provided services.
     * @param providedServices A collection of provided services.
     */
    protected AbstractLoggableServiceLibrary(final String loggerName, final ProvidedService<?, ?>... providedServices){
        super(providedServices);
        bundleLogger = Logger.getLogger(loggerName);
    }

    /**
     * Initializes a new instance of the bundle activator.
     * @param loggerName The name of the logger that will be connected to OSGi log service and shared between
     *                   provided services.
     * @param providedServices A collection of provided services. Cannot be {@literal null}.
     * @throws java.lang.IllegalArgumentException providedServices is {@literal null}.
     */
    @SuppressWarnings("UnusedDeclaration")
    protected AbstractLoggableServiceLibrary(final String loggerName, final ProvidedServices providedServices){
        super(providedServices);
        bundleLogger = Logger.getLogger(loggerName);
    }

    /**
     * Initializes a new instance of the bundle activator.
     * @param loggerInstance An instance of the logger to be attached to {@link LogService} service.
     * @param providedServices A collection of provided services.
     */
    @SuppressWarnings("UnusedDeclaration")
    protected AbstractLoggableServiceLibrary(final Logger loggerInstance, final ProvidedService<?, ?>... providedServices){
        super(providedServices);
        bundleLogger = loggerInstance != null ? loggerInstance : Logger.getLogger(getClass().getName());
    }

    /**
     * Initializes a new instance of the bundle activator.
     * @param loggerInstance An instance of the logger to be attached to {@link LogService} service.
     * @param providedServices A collection of provided services. Cannot be {@literal null}.
     * @throws java.lang.IllegalArgumentException providedServices is {@literal null}.
     */
    protected AbstractLoggableServiceLibrary(final Logger loggerInstance, final ProvidedServices providedServices){
        super(providedServices);
        bundleLogger = loggerInstance != null ? loggerInstance : Logger.getLogger(getClass().getName());
    }

    /**
     * Gets logger associated with this activator.
     * @return The logger associated with this activator.
     */
    protected final Logger getLogger(){
        return bundleLogger;
    }

    /**
     * Handles an exception thrown by {@link #activate(org.osgi.framework.BundleContext, com.itworks.snamp.core.AbstractBundleActivator.ActivationPropertyPublisher, com.itworks.snamp.core.AbstractBundleActivator.RequiredService[])}  method.
     *
     * @param e                    An exception to handle.
     * @param activationProperties A collection of activation properties to read.
     */
    @Override
    protected void activationFailure(final Exception e, final ActivationPropertyReader activationProperties) {
        bundleLogger.log(Level.SEVERE, "Unable to activate service.", e);
    }

    /**
     * Handles an exception thrown by {@link } method.
     *
     * @param e                    An exception to handle.
     * @param activationProperties A collection of activation properties to read.
     */
    @Override
    protected void deactivationFailure(final Exception e, final ActivationPropertyReader activationProperties) {
        bundleLogger.log(Level.SEVERE, "Unable to deactivate service.", e);
    }

    /**
     * Initializes the library.
     * <p>
     *     You should override this method and call this implementation at the first line using
     *     <b>super keyword</b>.
     * </p>
     * @param bundleLevelDependencies A collection of library-level dependencies to fill.
     * @throws Exception An error occurred during bundle initialization.
     */
    @Override
    @Partial
    protected void start(final Collection<RequiredService<?>> bundleLevelDependencies) throws Exception {
        bundleLevelDependencies.add(new LoggerServiceDependency(bundleLogger));
    }

    /**
     * Activates this service library.
     * <p>
     *     You should override this method and call this implementation at the first line using
     *     <b>super keyword</b>.
     * </p>
     * @param activationProperties A collection of library activation properties to fill.
     * @param dependencies         A collection of resolved library-level dependencies.
     * @throws Exception Unable to activate this library.
     */
    @Override
    @Partial
    protected void activate(final ActivationPropertyPublisher activationProperties, final RequiredService<?>... dependencies) throws Exception {
        activationProperties.publish(LOGGER_HOLDER, bundleLogger);
    }
}
