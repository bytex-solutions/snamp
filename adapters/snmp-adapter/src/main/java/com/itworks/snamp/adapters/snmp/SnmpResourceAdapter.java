package com.itworks.snamp.adapters.snmp;

import com.itworks.snamp.adapters.AbstractResourceAdapter;
import com.itworks.snamp.configuration.AgentConfiguration.ManagedResourceConfiguration;
import com.itworks.snamp.connectors.notifications.Notification;
import com.itworks.snamp.connectors.notifications.NotificationMetadata;
import org.snmp4j.agent.NotificationOriginator;
import org.snmp4j.agent.mo.snmp.TransportDomains;
import org.snmp4j.smi.OID;
import org.snmp4j.smi.OctetString;
import org.snmp4j.smi.TransportIpAddress;
import org.snmp4j.smi.UdpAddress;

import java.io.IOException;
import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import static com.itworks.snamp.adapters.snmp.SnmpAdapterConfigurationDescriptor.*;
import static com.itworks.snamp.adapters.snmp.SnmpHelpers.DateTimeFormatter;

/**
 * @author Roman Sakno
 * @version 1.0
 * @since 1.0
 */
final class SnmpResourceAdapter extends AbstractResourceAdapter {
    private static final class SnmpNotificationMappingImpl implements SnmpNotificationMapping{
        private final NotificationMetadata metadata;

        public SnmpNotificationMappingImpl(final NotificationMetadata metadata) throws IllegalArgumentException{
            if(metadata.containsKey(TARGET_ADDRESS_PARAM) && metadata.containsKey(TARGET_NAME_PARAM) && metadata.containsKey(OID_PARAM_NAME))
                this.metadata = metadata;
            else throw new IllegalArgumentException("Incompatible event metadata with SNMP infrastructure.");
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

    private static final class SnmpNotificationsModel extends AbstractNotificationsModel<SnmpNotificationMapping> implements ModelEventsSupport<SnmpAgent, SnmpAgent>{
        private Reference<NotificationOriginator> snmpDeliveryChannel = null;

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
                SnmpHelpers.getLogger().log(Level.WARNING, String.format("Event %s is not compatible with SNMP infratructure", eventName));
                return null;
            }
        }

        /**
         * Processes SNMP notification.
         *
         * @param notif                The notification to process.
         * @param notificationMetadata The metadata of the notification.
         */
        @Override
        protected void handleNotification(final Notification notif, final SnmpNotificationMapping notificationMetadata) {
            final NotificationOriginator originator = snmpDeliveryChannel != null ? snmpDeliveryChannel.get() : null;
            if(originator != null){
                final SnmpNotification wrappedNotification = new SnmpNotification(notificationMetadata.getID(),
                        notif,
                        notificationMetadata.getMetadata().getCategory(),
                        notificationMetadata.getTimestampFormatter());
                originator.notify(new OctetString(), wrappedNotification.notificationID, wrappedNotification.getBindings()); //for SNMPv3 sending
                originator.notify(new OctetString("public"), wrappedNotification.notificationID, wrappedNotification.getBindings()); //for SNMPv2 sending
            }
        }

        /**
         * Called by SNAMP infrastructure after filling of this model.
         *
         * @param e Additional event arguments.
         * @see com.itworks.snamp.adapters.AbstractResourceAdapter#populateModel(com.itworks.snamp.adapters.AbstractResourceAdapter.AbstractAttributesModel, Object)
         */
        @Override
        public void onPopulate(final SnmpAgent e) {
            this.snmpDeliveryChannel = new WeakReference<NotificationOriginator>(e);
        }

        /**
         * Called by SNAMP infrastructure after clearing of this model.
         *
         * @param e Additional event arguments.
         */
        @Override
        public void onClear(final SnmpAgent e) {
            this.snmpDeliveryChannel = null;
        }

        /**
         * Removes all of the mappings from this map.
         * The map will be empty after this call returns.
         */
        @Override
        public void clear() {
            super.clear();
            this.snmpDeliveryChannel = null;
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
                    SnmpHelpers.getLogger().log(Level.WARNING, String.format("Attribute %s has no SNMP-compliant type projection.", userDefinedAttributeName));
                    return null;
                }
            }
            else {
                SnmpHelpers.getLogger().log(Level.WARNING, String.format("Attribute %s has no OID parameter.", userDefinedAttributeName));
                return null;
            }
        }
    }

    private final SnmpAgent agent;
    private final SnmpAttributesModel attributes;
    private final SnmpNotificationsModel notifications;

    /**
     * Initializes a new resource adapter.
     *
     * @param resources A collection of managed resources to be exposed in protocol-specific manner
     *                  to the outside world.
     */
    protected SnmpResourceAdapter(final int port,
                                  final String hostName,
                                  final SecurityConfiguration securityOptions,
                                  final int socketTimeout,
                                  final Map<String, ManagedResourceConfiguration> resources) throws IOException {
        super(resources);
        agent = new SnmpAgent(port, hostName, securityOptions, socketTimeout);
        attributes = new SnmpAttributesModel();
        notifications = new SnmpNotificationsModel();
    }

    /**
     * Gets logger associated with this adapter.
     *
     * @return The logger associated with this adapter.
     */
    @Override
    public Logger getLogger() {
        return SnmpHelpers.getLogger();
    }

    /**
     * Starts the adapter.
     * <p>
     * This method will be called by SNAMP infrastructure automatically.
     * </p>
     *
     * @return {@literal true}, if adapter is started successfully; otherwise, {@literal false}.
     */
    @Override
    protected boolean start() {
        try {
            populateModel(attributes);
            populateModel(notifications, agent);
            return agent.start(attributes.values(), notifications.values());
        }
        catch (final IOException e) {
            getLogger().log(Level.SEVERE, "Unable to start SNMP Agent", e);
            return false;
        }
    }

    /**
     * Stops the adapter.
     * <p>
     * This method will be called by SNAMP infrastructure automatically.
     * </p>
     */
    @Override
    protected void stop() {
        agent.stop();
        clearModel(attributes);
        clearModel(notifications, agent);
    }
}
