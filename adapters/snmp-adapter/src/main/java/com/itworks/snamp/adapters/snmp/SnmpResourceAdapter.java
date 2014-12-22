package com.itworks.snamp.adapters.snmp;

import com.google.common.base.Supplier;
import com.google.common.eventbus.EventBus;
import com.itworks.snamp.adapters.AbstractConcurrentResourceAdapter;
import com.itworks.snamp.connectors.ManagedEntityValue;
import com.itworks.snamp.connectors.notifications.Notification;
import com.itworks.snamp.connectors.notifications.NotificationMetadata;
import com.itworks.snamp.internal.annotations.MethodStub;
import org.osgi.service.event.EventHandler;
import org.snmp4j.agent.mo.snmp.TransportDomains;
import org.snmp4j.smi.OID;
import org.snmp4j.smi.OctetString;
import org.snmp4j.smi.TransportIpAddress;
import org.snmp4j.smi.UdpAddress;

import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.logging.Level;
import java.util.logging.Logger;

import static com.itworks.snamp.adapters.snmp.SnmpAdapterConfigurationDescriptor.*;
import static com.itworks.snamp.adapters.snmp.SnmpHelpers.DateTimeFormatter;

/**
 * @author Roman Sakno
 * @version 1.0
 * @since 1.0
 */
final class SnmpResourceAdapter extends AbstractConcurrentResourceAdapter {
    static final String NAME = SnmpHelpers.ADAPTER_NAME;

    private static final class SnmpNotificationMappingImpl implements SnmpNotificationMapping{
        private final NotificationMetadata metadata;

        private SnmpNotificationMappingImpl(final NotificationMetadata metadata) throws IllegalArgumentException{
            if(metadata.containsKey(TARGET_ADDRESS_PARAM) && metadata.containsKey(TARGET_NAME_PARAM) && metadata.containsKey(OID_PARAM_NAME))
                this.metadata = metadata;
            else throw new IllegalArgumentException("Target address, target name and event OID parameters are not specified for SNMP trap");
        }

        @Override
        public DateTimeFormatter getTimestampFormatter() {
            return SnmpHelpers.createDateTimeFormatter(metadata.get(DATE_TIME_DISPLAY_FORMAT_PARAM));
        }

        @Override
        public OID getTransportDomain() {
            return kindOfIP(metadata.get(TARGET_ADDRESS_PARAM));
        }

        private static OID kindOfIP(final String addr){
            if (addr.contains(":"))
                return TransportDomains.transportDomainUdpIpv6;
            else if (addr.matches("^\\d+\\.\\d+\\.\\d+\\.\\d+/\\d+"))
                return TransportDomains.transportDomainUdpIpv4;
            return TransportDomains.transportDomainUdpDns;
        }

        @Override
        public OctetString getReceiverAddress() {
            final TransportIpAddress addr = new UdpAddress(metadata.get(TARGET_ADDRESS_PARAM));
            return new OctetString(addr.getValue());
        }

        @Override
        public OctetString getReceiverName() {
            return new OctetString(metadata.get(TARGET_NAME_PARAM));
        }

        @Override
        public int getTimeout() {
            return metadata.containsKey(TARGET_NOTIF_TIMEOUT_PARAM) ?
                    Integer.valueOf(metadata.get(TARGET_NOTIF_TIMEOUT_PARAM)) : 0;
        }

        @Override
        public int getRetryCount() {
            return metadata.containsKey(TARGET_RETRY_COUNT_PARAM) ?
                 Integer.valueOf(metadata.get(TARGET_RETRY_COUNT_PARAM)) : 3;
        }

        @Override
        public OID getID() {
            return new OID(metadata.get(OID_PARAM_NAME));
        }

        @Override
        public NotificationMetadata getMetadata() {
            return metadata;
        }
    }

    private static final class SnmpNotificationsModel extends AbstractNotificationsModel<SnmpNotificationMapping> implements EventHandler, AutoCloseable{
        private final EventBus notificationBus;

        private SnmpNotificationsModel(){
            notificationBus = new EventBus();
        }

        public void subscribe(final SnmpNoitificationListener listener){
            notificationBus.register(listener);
        }

        public void unsubscribe(final SnmpNoitificationListener listener){
            notificationBus.unregister(listener);
        }

        /**
         * Creates a new notification metadata representation.
         *
         * @param resourceName User-defined name of the managed resource.
         * @param eventName    The resource-local identifier of the event.
         * @param notifMeta    The notification metadata to wrap.
         * @return A new notification metadata representation.
         */
        @Override
        protected SnmpNotificationMapping createNotificationView(final String resourceName, final String eventName, final NotificationMetadata notifMeta) {
            try {
                return new SnmpNotificationMappingImpl(notifMeta);
            }
            catch (final IllegalArgumentException e){
                SnmpHelpers.log(Level.WARNING, "Event %s is not compatible with SNMP infratructure", eventName, e);
                return null;
            }
        }

        /**
         * Processes SNMP notification.
         * @param sender The name of the managed resource which emits the notification.
         * @param notif                The notification to process.
         * @param notificationMetadata The metadata of the notification.
         */
        @Override
        protected void handleNotification(final String sender, final Notification notif, final SnmpNotificationMapping notificationMetadata) {
            final Object attachment = notif.getAttachment();
            final ManagedEntityValue<?> typedAttachment;
            if(attachment instanceof ManagedEntityValue<?>)
                typedAttachment = (ManagedEntityValue<?>)attachment;
            else if(attachment != null && notificationMetadata.getMetadata().getAttachmentType() != null)
                typedAttachment = new ManagedEntityValue<>(attachment, notificationMetadata.getMetadata().getAttachmentType());
            else typedAttachment = null;
            try {
                final SnmpNotification wrappedNotification = new SnmpNotification(notificationMetadata.getID(),
                        notif,
                        notificationMetadata.getMetadata().getCategory(),
                        typedAttachment,
                        notificationMetadata.getMetadata());
                notificationBus.post(wrappedNotification);
            }
            catch (final Error e){
                throw e;
            }
            catch (final Throwable e){
                SnmpHelpers.log(Level.WARNING, "Unable to create SNMP Trap", e);
            }
            if(notif.getNext() != null)
                handleNotification(sender, notif.getNext(), notificationMetadata);
        }

        @Override
        @MethodStub
        public void close() {

        }
    }

    private static final class SnmpAttributesModel extends AbstractAttributesModel<SnmpAttributeMapping>{

        /**
         * Creates a new domain-specific representation of the management attribute.
         *
         * @param resourceName             User-defined name of the managed resource.
         * @param userDefinedAttributeName User-defined name of the attribute.
         * @param accessor                 An accessor for the individual management attribute.
         * @return A new domain-specific representation of the management attribute.
         */
        @Override
        protected SnmpAttributeMapping createAttributeView(final String resourceName, final String userDefinedAttributeName, final AttributeAccessor accessor) {
            if(accessor.containsKey(OID_PARAM_NAME)){
                final SnmpType type = SnmpType.map(accessor.getType());
                if(type != null){
                    return type.createManagedObject(accessor.get(OID_PARAM_NAME), accessor);
                }
                else {
                    SnmpHelpers.log(Level.WARNING, "Attribute %s has no SNMP-compliant type projection.", userDefinedAttributeName, null);
                    return null;
                }
            }
            else {
                SnmpHelpers.log(Level.WARNING, "Attribute %s has no OID parameter.", userDefinedAttributeName, null);
                return null;
            }
        }
    }

    private final SnmpAgent agent;
    private final SnmpAttributesModel attributes;
    private final SnmpNotificationsModel notifications;

    SnmpResourceAdapter(    final String adapterInstanceName,
                            final int port,
                                  final String hostName,
                                  final SecurityConfiguration securityOptions,
                                  final int socketTimeout,
                                  final Supplier<ExecutorService> threadPoolFactory) throws IOException {
        super(adapterInstanceName, threadPoolFactory);
        agent = new SnmpAgent(port, hostName, securityOptions, socketTimeout);
        attributes = new SnmpAttributesModel();
        notifications = new SnmpNotificationsModel();
    }

    /**
     * Starts the adapter.
     * <p>
     * This method will be called by SNAMP infrastructure automatically.
     * </p>
     * @throws java.lang.Exception Internal adapter error.
     */
    @Override
    protected void start(final ExecutorService threadPool) throws Exception {
        populateModel(attributes);
        populateModel(notifications);
        notifications.subscribe(agent);
        agent.start(attributes.values(), notifications.values(), threadPool);
    }

    /**
     * Stops the adapter.
     * <p>
     * This method will be called by SNAMP infrastructure automatically.
     * </p>
     */
    @Override
    protected void stop(final ExecutorService threadPool) throws Exception {
        notifications.unsubscribe(agent);
        try {
            agent.stop();
        } finally {
            clearModel(attributes);
            clearModel(notifications);
            notifications.close();
        }
        System.gc();
    }

    /**
     * Gets logger associated with this service.
     *
     * @return The logger associated with this service.
     */
    @Override
    public Logger getLogger() {
        return getLogger(NAME);
    }
}