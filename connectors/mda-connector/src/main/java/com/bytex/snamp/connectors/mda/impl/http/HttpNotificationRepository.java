package com.bytex.snamp.connectors.mda.impl.http;

import com.bytex.snamp.ArrayUtils;
import com.bytex.snamp.connectors.mda.MDANotificationRepository;
import com.bytex.snamp.connectors.notifications.NotificationDescriptor;
import com.bytex.snamp.core.DistributedServices;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import org.osgi.framework.BundleContext;

import javax.management.openmbean.OpenDataException;
import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author Roman Sakno
 * @version 1.2
 * @since 1.0
 */
final class HttpNotificationRepository extends MDANotificationRepository<HttpNotificationAccessor> {
    private static final Class<HttpNotificationAccessor> FEATURE_TYPE = HttpNotificationAccessor.class;
    private final Logger logger;

    HttpNotificationRepository(final String resourceName,
                               final ExecutorService threadPool,
                               final BundleContext context,
                               final Logger logger) {
        super(resourceName, FEATURE_TYPE, threadPool, DistributedServices.getDistributedCounter(context, "notifications-".concat(resourceName)));
        this.logger = Objects.requireNonNull(logger);
    }

    @Override
    protected Logger getLogger() {
        return logger;
    }

    @Override
    protected HttpNotificationAccessor createNotificationMetadata(final String notifType,
                                                                  final NotificationDescriptor metadata) {
        return new HttpNotificationAccessor(notifType, metadata);
    }

    void fire(final String category, final JsonObject notification, final Gson formatter) throws JsonParseException {

        fire(new NotificationCollector() {
            private static final long serialVersionUID = -8644675346771522318L;

            @Override
            protected void process(final HttpNotificationAccessor metadata) {
                if (category.equals(metadata.getDescriptor().getName(ArrayUtils.getFirst(metadata.getNotifTypes()))))
                    try {
                        enqueue(metadata,
                                metadata.getMessage(notification),
                                metadata.getSequenceNumber(notification),
                                metadata.getTimeStamp(notification),
                                metadata.getUserData(notification, formatter));
                    } catch (final OpenDataException e) {
                        getLogger().log(Level.SEVERE, "Unable to process notification " + notification, e);
                    }
            }
        });
    }

    void fire(final String category, final String notification, final Gson formatter) throws JsonParseException {
        final JsonElement notif = formatter.fromJson(notification, JsonElement.class);
        if (notif != null && notif.isJsonObject())
            fire(category, notif.getAsJsonObject(), formatter);
        else throw new JsonParseException("JSON Object expected");
    }
}
