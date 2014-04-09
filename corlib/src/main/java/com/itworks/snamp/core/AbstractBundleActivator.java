package com.itworks.snamp.core;

import com.itworks.snamp.internal.OsgiLoggerBridge;
import org.osgi.framework.*;
import org.osgi.service.log.LogService;

import java.util.*;
import java.util.logging.Logger;

/**
 * Represents a base class for all SNAMP-specific bundle activators.
 * @author Roman Sakno
 * @version 1.0
 * @since 1.0
 */
public abstract class AbstractBundleActivator implements BundleActivator {
    /**
     * Represents service publisher.
     * @author Roman Sakno
     * @since 1.0
     * @version 1.0
     */
    protected static interface ServicePublisher{
        /**
         * Exposes the service into OSGi environment.
         * @param serviceType The service type. Cannot be {@literal null}.
         * @param serviceInstance The service instance. Cannot be {@literal null}.
         * @param properties The registration properties. May be {@literal null}.
         * @param <S> Type of the service contract.
         */
        <S extends PlatformService> void publish(final Class<S> serviceType, final S serviceInstance, final Dictionary<String, ?> properties);
    }

    private static final class ServiceRegistry extends ArrayList<ServiceRegistration<? extends PlatformService>>{
        public ServiceRegistry(){
            super(5);
        }

        public ServicePublisher createPublisher(final BundleContext context){
            return new ServicePublisher() {
                @Override
                public <S extends PlatformService> void publish(final Class<S> serviceType, final S serviceInstance, final Dictionary<String, ?> properties) {
                    final ServiceRegistration<S> registration = context.registerService(serviceType, serviceInstance, properties);
                    add(registration);
                }
            };
        }

        public void clear(final BundleContext context) throws Exception{
            for(final ServiceRegistration<? extends PlatformService> registration: this){
                final PlatformService service = context.getService(registration.getReference());
                if(service instanceof AutoCloseable)
                    ((AutoCloseable)service).close();
                context.ungetService(registration.getReference());
                registration.unregister();
            }
            super.clear();
        }
    }

    private ServiceReference<LogService> logServiceRef;
    /**
     * Represents logger for all services published by SNAMP-specific bundle.
     */
    protected Logger logger;
    private final String loggerName;
    private final ServiceRegistry registeredServices;

    /**
     * Initializes a new SNAMP-specific bundle.
     * @param loggerName The name of the logger that is used by all services published by the bundle.
     */
    protected AbstractBundleActivator(final String loggerName){
        this.loggerName = loggerName;
        registeredServices = new ServiceRegistry();
    }

    /**
     * Exposes service into OSGi environment.
     * @param publisher The service publisher.
     */
    protected abstract void registerServices(final ServicePublisher publisher);

    /**
     * Starts the SNAMP-specific bundle.
     * <p>
     *     This method acquires reference to {@link LogService} OSGi service.
     * </p>
     * @param context The execution context of the bundle being started.
     * @throws Exception If this method throws an exception, this bundle is
     *         marked as stopped and the Framework will remove this bundle's
     *         listeners, unregister all services registered by this bundle, and
     *         release all services used by this bundle.
     */
    @Override
    public final void start(final BundleContext context) throws Exception {
        logServiceRef = context.getServiceReference(LogService.class);
        logger = OsgiLoggerBridge.connectToLogService(loggerName,
                logServiceRef != null ? context.getService(logServiceRef) : null);
        registerServices(registeredServices.createPublisher(context));
    }

    /**
     * Stops the SNAMP-specific bundle.
     * <p>
     *     This method releases reference to {@link LogService} OSGi service.
     * </p>
     * @param context The execution context of the bundle being stopped.
     * @throws Exception If this method throws an exception, the bundle is still
     *                   marked as stopped, and the Framework will remove the bundle's
     *                   listeners, unregister all services registered by the bundle, and
     *                   release all services used by the bundle.
     */
    @Override
    public final void stop(final BundleContext context) throws Exception{
        OsgiLoggerBridge.disconnectFromLogService(logger);
        if(logServiceRef != null) context.ungetService(logServiceRef);
        logServiceRef = null;
        registeredServices.clear(context);
    }
}
