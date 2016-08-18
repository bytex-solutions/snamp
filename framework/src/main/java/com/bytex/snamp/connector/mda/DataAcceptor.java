package com.bytex.snamp.connector.mda;

import com.bytex.snamp.connector.AbstractManagedResourceConnector;
import com.bytex.snamp.connector.ResourceEventListener;
import com.bytex.snamp.connector.metrics.MetricsReader;

import java.time.Duration;
import java.util.Objects;

/**
 * Represents Monitoring Data Acceptor.
 * @author Roman Sakno
 * @version 2.0
 * @since 1.0
 */
public abstract class DataAcceptor extends AbstractManagedResourceConnector {
    /**
     * Represents timer that measures time of the last attribute access.
     */
    protected final AccessTimer accessTimer;

    protected DataAcceptor(final AccessTimer accessTimer){
        this.accessTimer = Objects.requireNonNull(accessTimer);
    }

    /**
     * Initializes a new resource connector with default implementation of {@link  AccessTimer}.
     */
    protected DataAcceptor(){
        this(new SimpleTimer());
    }

    /**
     * Gets repository of attributes provided by this connector.
     * @return Repository of attributes.
     */
    @Aggregation
    protected abstract MDAAttributeRepository<?> getAttributes();

    /**
     * Gets repository of notifications metadata provided by this connector.
     * @return Repository of notifications metadata.
     */
    @Aggregation
    protected abstract MDANotificationRepository getNotifications();

    @Override
    protected final MetricsReader createMetricsReader(){
        return assembleMetricsReader(getAttributes(), getNotifications());
    }

    final void beginListening(final Duration expirationTime, final Object... dependencies) throws Exception {
        getAttributes().init(expirationTime, accessTimer);
        getNotifications().init(accessTimer);
        beginListening(dependencies);
    }

    /**
     * Starts listening of incoming monitoring data.
     * @param dependencies List of connector dependencies.
     * @throws Exception Unable to start listening data.
     */
    public abstract void beginListening(final Object... dependencies) throws Exception;

    /**
     * Adds a new listener for the connector-related events.
     * <p/>
     * The managed resource connector should holds a weak reference to all added event listeners.
     *
     * @param listener An event listener to add.
     */
    @Override
    public final void addResourceEventListener(final ResourceEventListener listener) {
        addResourceEventListener(listener, getAttributes(), getNotifications());
    }

    /**
     * Removes connector event listener.
     *
     * @param listener The listener to remove.
     */
    @Override
    public final void removeResourceEventListener(final ResourceEventListener listener) {
        removeResourceEventListener(listener, getAttributes(), getNotifications());
    }

    /**
     * Releases all resources associated with this connector.
     *
     * @throws Exception Unable to release resources associated with this connector.
     */
    @Override
    public void close() throws Exception {
        getAttributes().close();
        getNotifications().close();
        super.close();
    }
}
