package com.bytex.snamp.core;

import com.bytex.snamp.AbstractAggregator;
import com.bytex.snamp.SafeCloseable;
import com.bytex.snamp.internal.Utils;
import org.osgi.framework.*;

import javax.annotation.Nonnull;
import javax.management.InstanceNotFoundException;
import java.util.Collections;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

import static com.bytex.snamp.internal.Utils.getBundleContextOfObject;

/**
 * Represents tracker for SNAMP services.
 * @param <S> Type of service to track.
 * @param <C> Type of service client.
 * @author Roman Sakno
 * @since 2.0
 * @version 2.0
 */
public abstract class AbstractFrameworkServiceTracker<S extends FrameworkService, C extends ServiceHolder<S> & SafeCloseable> extends AbstractAggregator implements ServiceListener {
    private static final class TrackerLoggingScope extends LoggingScope {

        private TrackerLoggingScope(final AbstractAggregator requester,
                                    final String operationName){
            super(requester, operationName);
        }

        static TrackerLoggingScope serviceChanged(final AbstractAggregator requester) {
            return new TrackerLoggingScope(requester, "processServiceChanged");
        }
    }

    /**
     * Represents a thread-safe set of tracked services.
     */
    protected final Set<String> trackedServices = Collections.newSetFromMap(new ConcurrentHashMap<>());
    final Class<S> serviceContract;

    protected AbstractFrameworkServiceTracker(@Nonnull final Class<S> serviceContract){
        this.serviceContract = serviceContract;
    }

    protected final BundleContext getBundleContext(){
        return getBundleContextOfObject(this);
    }

    protected final Logger getLogger(){
        return LoggerProvider.getLoggerForBundle(getBundleContext());
    }

    /**
     * Invoked when new service is detected.
     * @param serviceClient Service client.
     */
    protected abstract void addService(final C serviceClient);

    /**
     * Invoked when service is removed from OSGi Service registry.
     * @param serviceClient Service client.
     */
    protected abstract void removeService(final C serviceClient);

    /**
     * Returns filter used to query managed resource connectors from OSGi environment.
     * @return A filter used to query managed resource connectors from OSGi environment.
     * @implSpec This operation must be idempotent and return the same filter for every call.
     */
    @Nonnull
    protected abstract Filter getServiceFilter();

    @Nonnull
    protected abstract C createClient(final ServiceReference<S> serviceRef) throws InstanceNotFoundException;

    protected abstract String getServiceId(final C client);

    static void logInvalidServiceRef(final Logger logger, final InstanceNotFoundException e){
        logger.log(Level.SEVERE, "Service reference is no longer valid", e);
    }

    /**
     * Captures reference to the managed resource connector.
     *
     * @param event The {@code ServiceEvent} object.
     */
    @SuppressWarnings("unchecked")
    @Override
    public final void serviceChanged(final ServiceEvent event) {
        if (Utils.isInstanceOf(event.getServiceReference(), serviceContract) && getServiceFilter().match(event.getServiceReference())) {
            final LoggingScope logger = TrackerLoggingScope.serviceChanged(this);
            try (final C client = createClient((ServiceReference<S>) event.getServiceReference())) {
                final String serviceId = getServiceId(client);
                switch (event.getType()) {
                    case ServiceEvent.MODIFIED_ENDMATCH:
                    case ServiceEvent.UNREGISTERING:
                        try {
                            removeService(client);
                        } finally {
                            trackedServices.remove(serviceId);
                        }
                        return;
                    case ServiceEvent.REGISTERED:
                        addService(client);
                        trackedServices.add(serviceId);
                        return;
                    default:
                        logger.info(String.format("Unexpected event %s captured by tracker %s for service %s",
                                event.getType(),
                                toString(),
                                serviceId));
                }
            } catch (final InstanceNotFoundException e){
                logInvalidServiceRef(logger, e);
            } finally {
                logger.close();
            }
        }
    }
}
