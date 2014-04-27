package com.itworks.snamp.core;

import com.itworks.snamp.internal.OsgiLoggerBridge;
import org.osgi.framework.*;
import org.osgi.service.log.LogService;
import static com.itworks.snamp.internal.ReflectionUtils.getProperty;

import java.util.*;
import java.util.logging.Logger;

/**
 * Represents a base class for bundle activators that requires logging functionality.
 * @author Roman Sakno
 * @version 1.0
 * @since 1.0
 */
public abstract class AbstractLoggableBundleActivator extends AbstractBundleActivator {
    /**
     * Represents a property in the shared context that contains a reference to the
     * {@link Logger} instance that wraps OSGi log service.
     */
    protected static final String LOGGER_INIT_PROPERTY = "logger";

    /**
     * Represents {@link org.osgi.service.log.LogService} dependency descriptor.
     * @author Roman Sakno
     * @since 1.0
     * @version 1.0
     */
    protected static final class LoggerServiceDependency extends BundleLevelDependency<LogService> {
        /**
         * Represents logger that wraps OSGi logging service.
         */
        public final Logger logger;

        /**
         * Initializes a new {@link LogService} dependency descriptor.
         * @param loggerName The name of the logger.
         */
        public LoggerServiceDependency(final String loggerName){
            super(LogService.class);
            logger = Logger.getLogger(loggerName);
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

        /**
         * Informs this dependency about resolving dependency.
         *
         * @param serviceInstance An instance of the resolved service.
         * @param properties      Service properties.
         * @param processingContext A map that comes from the provided service that owns this dependency.
         */
        @Override
        protected void bind(final LogService serviceInstance,
                            final Dictionary<String, ?> properties,
                            final Map<String, ?> processingContext) {
            OsgiLoggerBridge.connectToLogService(logger, serviceInstance);
        }

        /**
         * Informs this dependency about detaching dependency.
         * @param processingContext A map that comes from the provided service that owns this dependency.
         */
        @Override
        protected void unbind(final Map<String, ?> processingContext) {
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
            return getProperty(getSharedContext(), LOGGER_INIT_PROPERTY, Logger.class, Logger.getAnonymousLogger());
        }
    }

    private final LoggerServiceDependency loggerDependency;

    /**
     * Initializes a new instance of the bundle activator.
     * @param loggerName The name of the logger that will be connected to OSGi log service and shared between
     *                   provided services.
     * @param providedServices A collection of provided services.
     */
    protected AbstractLoggableBundleActivator(final String loggerName, final ProvidedService<?, ?>... providedServices){
        super(providedServices);
        loggerDependency = new LoggerServiceDependency(loggerName);
    }

    /**
     * Initializes a new instance of the bundle activator.
     * @param loggerName The name of the logger that will be connected to OSGi log service and shared between
     *                   provided services.
     * @param providedServices A collection of provided services. Cannot be {@literal null}.
     * @throws java.lang.IllegalArgumentException providedServices is {@literal null}.
     */
    protected AbstractLoggableBundleActivator(final String loggerName, final ProvidedServices providedServices){
        super(providedServices);
        loggerDependency = new LoggerServiceDependency(loggerName);
    }

    /**
     * Gets logger associated with this activator.
     * @return The logger associated with this activator.
     */
    protected final Logger getLogger(){
        return loggerDependency.logger;
    }

    /**
     * Initializes a bundle and fills the map that will be shared between provided services.
     * <p>
     * In the default implementation this method does nothing.
     * </p>
     *
     * @param sharedContext           The activation context to initialize.
     * @param serviceReg              An object that provides access to the OSGi service registry.
     * @param bundleLevelDependencies A collection of bundle-level dependencies.
     * @throws java.lang.Exception Initialization error.
     */
    @Override
    protected void init(final Map<String, Object> sharedContext,
                        final ServiceRegistryProcessor serviceReg,
                        final Collection<BundleLevelDependency<?>> bundleLevelDependencies) throws Exception{
        bundleLevelDependencies.add(loggerDependency);
        sharedContext.put(LOGGER_INIT_PROPERTY, loggerDependency.logger);
    }
}
