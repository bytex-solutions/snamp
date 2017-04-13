package com.bytex.snamp.connector;

import com.bytex.snamp.AbstractAggregator;
import com.bytex.snamp.concurrent.LazyStrongReference;
import com.bytex.snamp.core.FilterBuilder;
import com.bytex.snamp.core.LoggerProvider;
import com.bytex.snamp.core.LoggingScope;
import org.osgi.framework.*;

import javax.annotation.Nonnull;
import java.util.Collections;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;

import static com.bytex.snamp.internal.Utils.getBundleContextOfObject;

/**
 * Represents abstract tracker for services of type {@link ManagedResourceConnector}.
 * @author Roman Sakno
 * @version 2.0
 * @since 2.0
 */
public abstract class AbstractManagedResourceTracker extends AbstractAggregator implements ServiceListener {
    private static final class TrackerLoggingScope extends LoggingScope {

        private TrackerLoggingScope(final AbstractAggregator requester,
                                    final String operationName){
            super(requester, operationName);
        }

        static TrackerLoggingScope connectorChangesDetected(final AbstractAggregator requester) {
            return new TrackerLoggingScope(requester, "processResourceConnectorChanges");
        }
    }

    private final LazyStrongReference<Filter> resourceFilterCache = new LazyStrongReference<>();
    /**
     * Represents a thread-safe set of tracked resources.
     */
    protected final Set<String> trackedResources = Collections.newSetFromMap(new ConcurrentHashMap<>());

    protected final BundleContext getBundleContext(){
        return getBundleContextOfObject(this);
    }

    protected final Logger getLogger(){
        return LoggerProvider.getLoggerForBundle(getBundleContext());
    }

    protected abstract void addResource(final ManagedResourceConnectorClient connector);

    protected abstract void removeResource(final ManagedResourceConnectorClient connector);

    /**
     * Returns filter used to query managed resource connectors from OSGi environment.
     * @return A filter used to query managed resource connectors from OSGi environment.
     * @implSpec This operation must be idempotent and return the same filter for every call.
     */
    @Nonnull
    protected abstract FilterBuilder createResourceFilter();

    /**
     * Captures reference to the managed resource connector.
     *
     * @param event The {@code ServiceEvent} object.
     */
    @SuppressWarnings("unchecked")
    @Override
    public final void serviceChanged(final ServiceEvent event) {
        //use cached version of filter
        final Filter filter = resourceFilterCache.lazyGet(this, tracker -> tracker.createResourceFilter().get());
        if (ManagedResourceConnector.isResourceConnector(event.getServiceReference()) && filter.match(event.getServiceReference()))
            try (final LoggingScope logger = TrackerLoggingScope.connectorChangesDetected(this);
                 final ManagedResourceConnectorClient client = new ManagedResourceConnectorClient(getBundleContext(), (ServiceReference<ManagedResourceConnector>) event.getServiceReference())) {
                switch (event.getType()) {
                    case ServiceEvent.MODIFIED_ENDMATCH:
                    case ServiceEvent.UNREGISTERING:
                        try {
                            removeResource(client);
                        } finally {
                            trackedResources.remove(client.getManagedResourceName());
                        }
                        return;
                    case ServiceEvent.REGISTERED:
                        addResource(client);
                        trackedResources.add(client.getManagedResourceName());
                        return;
                    default:
                        logger.info(String.format("Unexpected event %s captured by tracker %s for resource %s",
                                event.getType(),
                                toString(),
                                client.getManagedResourceName()));
                }
            }
    }

    /**
     * Clears internal cache with aggregated objects.
     */
    @Override
    protected final void clearCache() {
        resourceFilterCache.reset();
        super.clearCache();
    }
}
