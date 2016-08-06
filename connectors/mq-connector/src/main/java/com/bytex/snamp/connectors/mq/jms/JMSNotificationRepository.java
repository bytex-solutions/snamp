package com.bytex.snamp.connectors.mq.jms;

import com.bytex.snamp.ArrayUtils;
import com.bytex.snamp.connectors.mda.MDANotificationInfo;
import com.bytex.snamp.connectors.mda.MDANotificationRepository;
import com.bytex.snamp.connectors.notifications.NotificationDescriptor;
import com.bytex.snamp.core.DistributedServices;
import org.osgi.framework.BundleContext;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.management.openmbean.OpenDataException;
import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author Roman Sakno
 * @version 2.0
 * @since 1.0
 */
final class JMSNotificationRepository extends MDANotificationRepository<MDANotificationInfo> {
    private final Logger logger;
    private final JMSNotificationConverter converter;

    JMSNotificationRepository(final String resourceName,
                              final ExecutorService threadPool,
                              final JMSNotificationConverter dataConverter,
                              final BundleContext context,
                              final Logger logger){
        super(resourceName,
                MDANotificationInfo.class,
                threadPool,
                DistributedServices.getDistributedCounter(context, "notifications-".concat(resourceName)));
        this.logger = Objects.requireNonNull(logger);
        this.converter = Objects.requireNonNull(dataConverter);
    }

    @Override
    protected MDANotificationInfo createNotificationMetadata(final String notifType,
                                                             final NotificationDescriptor metadata) {
        return new MDANotificationInfo(notifType, metadata);
    }

    void fire(final Message message) throws JMSException{
        fire(new NotificationCollector() {
            private static final long serialVersionUID = -2162156378136601297L;
            private final String category = converter.getCategory(message);
            private final String notifMessage = converter.getMessage(message);
            private final long sequenceNumber = converter.getSequenceNumber(message);
            private final long timeStamp = message.getJMSTimestamp();

            @Override
            protected void process(final MDANotificationInfo metadata) {
                if(Objects.equals(category, metadata.getDescriptor().getName(ArrayUtils.getFirst(metadata.getNotifTypes()))))
                    if(metadata.getAttachmentType() == null)
                        enqueue(metadata, notifMessage, sequenceNumber, timeStamp, null);
                    else try{
                        enqueue(metadata, notifMessage, sequenceNumber, timeStamp, converter.deserialize(message, metadata.getAttachmentType()));
                    } catch (final JMSException | OpenDataException e){
                        getLogger().log(Level.WARNING, "Unable to send notification");
                    }
            }
        });
    }

    @Override
    protected Logger getLogger() {
        return logger;
    }
}
