package com.bytex.snamp.connector.groovy;

import com.bytex.snamp.SpecialUse;
import com.bytex.snamp.connector.AbstractManagedResourceConnector;
import com.bytex.snamp.connector.ResourceEventListener;
import com.bytex.snamp.connector.metrics.MetricsSupport;
import groovy.util.ResourceException;
import groovy.util.ScriptException;

import java.io.IOException;
import java.util.concurrent.ExecutorService;

import static com.bytex.snamp.MapUtils.toProperties;

/**
 * @author Roman Sakno
 * @version 2.0
 * @since 1.0
 */
final class GroovyResourceConnector extends AbstractManagedResourceConnector {

    @Aggregation(cached = true)
    private final GroovyAttributeRepository attributes;
    @Aggregation(cached = true)
    private final GroovyNotificationRepository events;
    @Aggregation(cached = true)
    private final GroovyOperationRepository operations;
    private final ManagedResourceScriptlet scriptlet;

    GroovyResourceConnector(final String resourceName,
                            final com.bytex.snamp.configuration.ManagedResourceInfo configuration) throws IOException, ResourceException, ScriptException {
        super(configuration);
        final ManagedResourceScriptEngine engine = new ManagedResourceScriptEngine(resourceName,
                getClass().getClassLoader(),
                false,
                toProperties(configuration),
                configuration.getConnectionString());

        scriptlet = engine.createScript(null);
        scriptlet.run();
        final boolean smartMode = GroovyResourceConfigurationDescriptor.getInstance().isSmartModeEnabled(configuration);
        attributes = new GroovyAttributeRepository(resourceName, scriptlet, smartMode);
        final ExecutorService threadPool = GroovyResourceConfigurationDescriptor.getInstance().parseThreadPool(configuration);
        events = new GroovyNotificationRepository(resourceName, scriptlet, threadPool, smartMode);
        events.setSource(this);
        operations = new GroovyOperationRepository(resourceName, scriptlet, smartMode);
    }

    @Aggregation
    @SpecialUse(SpecialUse.Case.REFLECTION)
    protected MetricsSupport createMetricsReader(){
        return assembleMetricsReader(attributes, events, operations);
    }

    /**
     * Adds a new listener for the connector-related events.
     * <p/>
     * The managed resource connector should holds a weak reference to all added event listeners.
     *
     * @param listener An event listener to add.
     */
    @Override
    public void addResourceEventListener(final ResourceEventListener listener) {
        addResourceEventListener(listener, attributes, events, operations);
    }

    /**
     * Removes connector event listener.
     *
     * @param listener The listener to remove.
     */
    @Override
    public void removeResourceEventListener(final ResourceEventListener listener) {
        removeResourceEventListener(listener, attributes, events, operations);
    }

    /**
     * Releases all resources associated with this connector.
     *
     * @throws Exception Unable to release resources associated with this connector.
     */
    @Override
    public void close() throws Exception {
        super.close();
        attributes.close();
        events.close();
        scriptlet.close();
        operations.close();
    }
}
