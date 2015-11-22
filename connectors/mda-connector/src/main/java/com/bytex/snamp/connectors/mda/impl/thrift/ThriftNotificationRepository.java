package com.bytex.snamp.connectors.mda.impl.thrift;

import com.bytex.snamp.connectors.mda.MDANotificationRepository;
import com.bytex.snamp.connectors.notifications.NotificationDescriptor;
import com.bytex.snamp.core.DistributedServices;
import org.apache.thrift.TException;
import org.apache.thrift.protocol.TField;
import org.apache.thrift.protocol.TProtocol;
import org.apache.thrift.protocol.TProtocolUtil;
import org.apache.thrift.protocol.TType;
import org.osgi.framework.BundleContext;

import javax.management.openmbean.OpenDataException;
import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author Roman Sakno
 * @version 1.0
 * @since 1.0
 */
final class ThriftNotificationRepository extends MDANotificationRepository<ThriftNotificationAccessor> {
    private static final Class<ThriftNotificationAccessor> FEATURE_TYPE = ThriftNotificationAccessor.class;
    private final Logger logger;

    ThriftNotificationRepository(final String resourceName,
                                 final ExecutorService threadPool,
                                 final BundleContext context,
                                 final Logger logger) {
        super(resourceName,
                FEATURE_TYPE,
                threadPool,
                DistributedServices.getDistributedCounter(context, "notifications-".concat(resourceName)));
        this.logger = Objects.requireNonNull(logger);
    }

    @Override
    protected Logger getLogger() {
        return logger;
    }

    @Override
    protected ThriftNotificationAccessor createNotificationMetadata(final String notifType,
                                                             final NotificationDescriptor metadata) throws OpenDataException {
        return new ThriftNotificationAccessor(notifType, metadata);
    }

    boolean fire(final String category, final TProtocol input) throws TException {
        fire(new NotificationCollector() {
            private static final long serialVersionUID = -251554281456696460L;
            private Object userData = null;
            private long sequenceNumber = 0L;
            private long timeStamp = 0L;
            private String message = "";
            private boolean dataAvailable = false;

            private boolean readNotificationData(final ThriftNotificationAccessor metadata) throws TException {
                input.readStructBegin();
                int counter = 0;
                while (true) {
                    final TField field = input.readFieldBegin();
                    final boolean next;
                    if (next = field.type != TType.STOP)
                        switch (field.id) {
                            case 1:
                                message = input.readString();
                                counter += 1;
                                break;
                            case 2:
                                sequenceNumber = input.readI64();
                                counter += 1;
                                break;
                            case 3:
                                timeStamp = input.readI64();
                                counter += 1;
                                break;
                            case 4:
                                userData = metadata.parseUserData(input);
                                counter += 1;
                                break;
                            default:
                                TProtocolUtil.skip(input, field.type);
                                break;
                        }
                    input.readFieldEnd();
                    if (!next) break;
                }
                input.readStructEnd();
                return counter == 4;
            }

            @Override
            protected void process(final ThriftNotificationAccessor metadata) {
                if (category.equals(metadata.getDescriptor().getNotificationCategory())) {
                    if (!dataAvailable)
                        try {
                            dataAvailable = readNotificationData(metadata);
                        } catch (final TException e) {
                            logger.log(Level.SEVERE, "Unable to parse user data from notification " + sequenceNumber, e);
                        }
                    if (dataAvailable)
                        enqueue(metadata, message, sequenceNumber, timeStamp, userData);
                }
            }
        });
        return true;
    }
}
