package com.itworks.snamp.core;

import com.itworks.snamp.internal.OsgiLoggerBridge;
import org.osgi.framework.*;
import org.osgi.service.log.LogService;

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
    protected static final class LoggerServiceDependency extends SharedDependency<LogService> {
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
         */
        @Override
        protected void bind(final LogService serviceInstance, final Dictionary<String, ?> properties) {
            OsgiLoggerBridge.connectToLogService(logger, serviceInstance);
        }

        /**
         * Informs this dependency about detaching dependency.
         */
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
         * @throws IllegalArgumentException
         *          contract is {@literal null}.
         */
        protected LoggableProvidedService(final Class<S> contract, final RequiredService<?>... dependencies) {
            super(contract, dependencies);
        }

        /**
         * Gets logger from the shared context that can be passed into service.
         * @return The logger from the shared context.
         */
        protected final Logger getLogger(){
            return getSharedContextProperty(LOGGER_INIT_PROPERTY, LoggerServiceDependency.class).logger;
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
     * Gets logger associated with this activator.
     * @return The logger associated with this activator.
     */
    protected final Logger getLogger(){
        return loggerDependency.logger;
    }

    /**
     * Initializes a bundle and fills the map that will be shared between provided services.
     * <p>
     * In the default implementation this method puts {@link LoggerServiceDependency#logger} to the context
     * with {@link #LOGGER_INIT_PROPERTY} key. Therefore, all provided services can share the same logger instance.
     * </p>
     * @param context The activation context to initialize.
     */
    @Override
    protected void init(final Map<String, Object> context) {
        context.put(LOGGER_INIT_PROPERTY, loggerDependency);
    }
}
