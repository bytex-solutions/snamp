package com.bytex.snamp.connector.dataStream;

import com.bytex.snamp.connector.notifications.AbstractNotificationRepository;
import com.bytex.snamp.connector.notifications.NotificationDescriptor;
import com.bytex.snamp.instrumentation.measurements.jmx.SpanNotification;
import com.bytex.snamp.instrumentation.measurements.jmx.TimeMeasurementNotification;
import com.bytex.snamp.instrumentation.measurements.jmx.ValueMeasurementNotification;

import javax.annotation.Nonnull;
import javax.management.AttributeChangeNotification;
import javax.management.Notification;
import java.util.concurrent.ExecutorService;
import java.util.function.Consumer;

/**
 * Represents repository of notifications metadata.
 * @author Roman Sakno
 * @since 2.0
 * @version 2.1
 */
public class SyntheticNotificationRepository extends AbstractNotificationRepository<SyntheticNotificationInfo> implements Consumer<Notification> {
    private DataStreamConnectorConfigurationDescriptionProvider configurationParser;
    private ExecutorService listenerInvoker;

    public SyntheticNotificationRepository(final String resourceName) {
        super(resourceName, SyntheticNotificationInfo.class);
    }

    final void init(final ExecutorService threadPool, final DataStreamConnectorConfigurationDescriptionProvider configurationParser) {
        listenerInvoker = threadPool;
        this.configurationParser = configurationParser;
    }

    private static Notification prepareNotification(final SyntheticNotificationInfo metadata, final Notification notification) {
        return metadata.isNotificationEnabled(notification) ?
                wrapNotification(metadata, notification) :
                null;
    }

    @Override
    public void accept(final Notification notification) {
        fire(notification.getType(), holder -> prepareNotification(holder, notification));
    }

    /**
     * Gets an executor used to execute event listeners.
     *
     * @return Executor service.
     */
    @Nonnull
    @Override
    protected ExecutorService getListenerExecutor() {
        final ExecutorService executor = listenerInvoker;
        return executor == null ? super.getListenerExecutor() : executor;
    }

    @Override
    protected SyntheticNotificationInfo connectNotifications(final String notifType, final NotificationDescriptor metadata) throws Exception {
        final SyntheticNotificationInfo result;
        switch (metadata.getAlternativeName().orElse(notifType)) {
            case AttributeChangeNotification.ATTRIBUTE_CHANGE:
                result = new AttributeChangeNotificationInfo(metadata);
                break;
            case TimeMeasurementNotification.TYPE:
                result = new TimeMeasurementNotificationInfo(metadata);
                break;
            case SpanNotification.TYPE:
                result = new SpanNotificationInfo(metadata);
                break;
            case ValueMeasurementNotification.TYPE:
                result = new ValueMeasurementNotificationInfo(metadata);
                break;
            default:
                result = new SyntheticNotificationInfo(notifType, metadata);
                break;
        }
        result.setupFilter(configurationParser);
        return result;
    }

    /**
     * Removes all notifications from this repository.
     */
    @Override
    public void close() {
        super.close();
        listenerInvoker = null;
        configurationParser = null;
    }
}
