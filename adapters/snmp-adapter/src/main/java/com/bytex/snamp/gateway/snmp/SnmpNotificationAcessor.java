package com.bytex.snamp.gateway.snmp;

import com.bytex.snamp.gateway.modeling.NotificationAccessor;
import com.bytex.snamp.connector.notifications.NotificationDescriptor;
import com.bytex.snamp.jmx.WellKnownType;
import org.snmp4j.agent.NotificationOriginator;
import org.snmp4j.agent.mo.snmp.TransportDomains;
import org.snmp4j.smi.OID;
import org.snmp4j.smi.OctetString;
import org.snmp4j.smi.TransportIpAddress;
import org.snmp4j.smi.UdpAddress;

import javax.management.MBeanNotificationInfo;
import javax.management.Notification;
import java.lang.ref.WeakReference;
import java.text.ParseException;
import java.util.Objects;
import java.util.regex.Pattern;

import static com.bytex.snamp.gateway.snmp.SnmpGatewayDescriptionProvider.*;

/**
 * @author Roman Sakno
 * @version 2.0
 * @since 1.0
 */
final class SnmpNotificationAcessor extends NotificationAccessor implements SnmpNotificationMapping {
    private static final Pattern IPv4_PATTERN = Pattern.compile("^\\d+\\.\\d+\\.\\d+\\.\\d+/\\d+");
    private WeakReference<NotificationOriginator> notificationOriginator;
    private final String resourceName;
    private final OID notificationID;

    SnmpNotificationAcessor(final MBeanNotificationInfo metadata,
                            final String resourceName) throws IllegalArgumentException, ParseException {
        super(metadata);
        this.notificationOriginator = null;
        this.resourceName = resourceName;
        notificationID = parseOID(metadata, SnmpHelpers.OID_GENERATOR);
    }

    @Override
    public OID getTransportDomain() {
        return kindOfIP(parseTargetAddress(getMetadata()));
    }

    private static OID kindOfIP(final String addr) {
        if (addr.contains(":"))
            return TransportDomains.transportDomainUdpIpv6;
        else if (IPv4_PATTERN.matcher(addr).matches())
            return TransportDomains.transportDomainUdpIpv4;
        return TransportDomains.transportDomainUdpDns;
    }

    @Override
    public OctetString getReceiverAddress() {
        final TransportIpAddress addr = new UdpAddress(parseTargetAddress(getMetadata()));
        return new OctetString(addr.getValue());
    }

    @Override
    public OctetString getReceiverName() {
        return OctetStringHelper.toOctetString(parseTargetName(getMetadata()));
    }

    @Override
    public int getTimeout() {
        return parseNotificationTimeout(this);
    }

    @Override
    public int getRetryCount() {
        return parseRetryCount(this);
    }

    @Override
    public OID getID() {
        return notificationID;
    }

    @Override
    public boolean equals(final MBeanNotificationInfo metadata) {
        return Objects.equals(parseTargetName(getMetadata()), parseTargetName(metadata));
    }

    @Override
    public void setNotificationOriginator(final NotificationOriginator originator) {
        notificationOriginator = new WeakReference<>(originator);
    }

    SnmpType getSnmpType(){
        final WellKnownType attachmentType = WellKnownType.getType(NotificationDescriptor.getUserDataType(get()));
        return attachmentType == null ? null : SnmpType.map(attachmentType);
    }

    @Override
    public void handleNotification(final Notification notification, final Object handback) {
        final WeakReference<NotificationOriginator> originatorRef = this.notificationOriginator;
        final NotificationOriginator originator = originatorRef != null ?
                originatorRef.get() :
                null;
        if (originator != null) {
            notification.setSource(resourceName);
            final SnmpNotification snmpTrap = new SnmpNotification(notificationID,
                    notification,
                    get());
            originator.notify(new OctetString(), snmpTrap.notificationID, snmpTrap.getBindings()); //for SNMPv3 sending
            originator.notify(OctetStringHelper.toOctetString("public"), snmpTrap.notificationID, snmpTrap.getBindings()); //for SNMPv2 sending
        }
    }

    @Override
    public void disconnected() {
        final WeakReference<NotificationOriginator> originatorRef = this.notificationOriginator;
        this.notificationOriginator = null;
        if (originatorRef != null) originatorRef.clear();
    }
}
