package com.bytex.snamp.connector;

import com.bytex.snamp.ArrayUtils;
import com.bytex.snamp.SafeCloseable;
import com.bytex.snamp.core.LoggerProvider;
import com.google.common.collect.ImmutableSet;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.util.tracker.ServiceTracker;

import javax.annotation.WillNotClose;
import javax.management.InstanceNotFoundException;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Represents specialization of {@link ServiceTracker} aimed to track managed resources registered in SNAMP environment.
 * @author Roman Sakno
 * @since 2.1
 * @version 2.1
 */
public abstract class AbstractManagedResourceTracker extends ServiceTracker<ManagedResourceConnector, String> implements SafeCloseable {
    private final Logger logger;

    protected AbstractManagedResourceTracker(final BundleContext context,
                                             final ManagedResourceSelector selector) {
        super(context, selector.get(), null);
        logger = LoggerProvider.getLoggerForBundle(context);
    }

    protected AbstractManagedResourceTracker(final BundleContext context){
        this(context, ManagedResourceConnectorClient.selector());
    }

    /**
     * Starts tracking of managed resources.
     */
    @Override
    public final void open() {
        open(true);
    }

    protected abstract void addResource(@WillNotClose final ManagedResourceConnectorClient resource) throws Exception;

    @Override
    public synchronized String addingService(final ServiceReference<ManagedResourceConnector> connectorRef) {
        final String resourceName = ManagedResourceSelector.getManagedResourceName(connectorRef);
        try (final ManagedResourceConnectorClient client = new ManagedResourceConnectorClient(context, connectorRef)) {
            addResource(client);
        } catch (final InstanceNotFoundException e) {
            logger.log(Level.WARNING, "Unable to capture reference to managed resource connector " + resourceName, e);
        } catch (final Exception e){
            logger.log(Level.SEVERE, "Unable to attach new managed resource " + resourceName, e);
        }
        return resourceName;
    }

    /**
     * Gets tracked resources.
     * @return Tracked resources.
     */
    public final Set<String> getTrackedResources() {
        return ImmutableSet.copyOf(getServices(ArrayUtils.emptyArray(String[].class)));
    }

    protected abstract void removeResource(@WillNotClose final ManagedResourceConnectorClient resource) throws Exception;

    @Override
    public void removedService(final ServiceReference<ManagedResourceConnector> connectorRef, final String resourceName) {
        try (final ManagedResourceConnectorClient client = new ManagedResourceConnectorClient(context, connectorRef)) {
            removeResource(client);
        } catch (final InstanceNotFoundException e) {
            logger.log(Level.WARNING, "Unable to release reference to managed resource connector " + resourceName, e);
        } catch (final Exception e){
            logger.log(Level.SEVERE, "Unable to detach managed resource " + resourceName, e);
        }
    }
}
