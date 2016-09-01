package com.bytex.snamp.connector.md;

import com.bytex.snamp.connector.AbstractManagedResourceConnector;
import com.bytex.snamp.connector.ResourceEventListener;
import com.bytex.snamp.connector.notifications.measurement.MeasurementNotification;
import com.bytex.snamp.connector.notifications.measurement.MeasurementSource;

import javax.management.Notification;
import java.util.Map;
import java.util.concurrent.ExecutorService;

/**
 * Represents abstract class for message-driven resource connector.
 * @since 2.0
 * @version 2.0
 */
public abstract class MessageDrivenConnector extends AbstractManagedResourceConnector {

    private final MeasurementSource source;
    private final MessageDrivenAttributeRepository attributes;
    private final ExecutorService threadPool;

    protected MessageDrivenConnector(final String resourceName,
                                     final Map<String, String> parameters,
                                     final MessageDrivenConnectorConfigurationDescriptor descriptor) {

        final String componentInstance = descriptor.parseComponentInstance(parameters, resourceName);
        final String componentName = descriptor.parseComponentName(parameters);
        source = new MeasurementSource(componentName, componentInstance);
        attributes = new MessageDrivenAttributeRepository(resourceName);
        threadPool = descriptor.parseThreadPool(parameters);
    }

    protected Notification parseNotification(final Map<String, ?> headers,
                                             final Object body){

    }

    private void postMessage(final MeasurementNotification notification){
        attributes.post(notification, threadPool);
    }

    public final void postMessage(final Map<String, ?> headers,
                                     final Object body){
        final Notification notification = parseNotification(headers, body);
        //dispatching notification
        if(notification instanceof MeasurementNotification)
            postMessage((MeasurementNotification)notification);
    }

    @Override
    public final void addResourceEventListener(final ResourceEventListener listener) {
        addResourceEventListener(listener, attributes);
    }

    @Override
    public final void removeResourceEventListener(final ResourceEventListener listener) {
        removeResourceEventListener(listener, attributes);
    }

    @Override
    public void close() throws Exception {
        attributes.close();
        super.close();
    }
}