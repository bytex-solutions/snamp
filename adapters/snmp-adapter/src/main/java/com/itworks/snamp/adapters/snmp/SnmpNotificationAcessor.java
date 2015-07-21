package com.itworks.snamp.adapters.snmp;

import com.google.common.base.Function;
import com.itworks.snamp.adapters.modeling.NotificationAccessor;
import com.itworks.snamp.connectors.notifications.NotificationDescriptor;
import com.itworks.snamp.jmx.WellKnownType;
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

import static com.itworks.snamp.adapters.snmp.SnmpAdapterConfigurationDescriptor.*;

/**
 * @author Roman Sakno
 * @version 1.0
 * @since 1.0
 */
public final class SnmpNotificationAcessor extends NotificationAccessor implements SnmpNotificationMapping {
    private static final Pattern IPv4_PATTERN = Pattern.compile("^\\d+\\.\\d+\\.\\d+\\.\\d+/\\d+");
    private WeakReference<NotificationOriginator> notificationOriginator;
    private final String resourceName;
    private SnmpTypeMapper typeMapper;
    private final OID notificationID;

    SnmpNotificationAcessor(final MBeanNotificationInfo metadata,
                            final String resourceName) throws IllegalArgumentException, ParseException {
        super(metadata);
        this.notificationOriginator = null;
        this.resourceName = resourceName;
        notificationID = parseOID(this);
    }

    @Override
    public void setTypeMapper(final SnmpTypeMapper value) {
        typeMapper = value;
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
        return SnmpHelpers.toOctetString(parseTargetName(getMetadata()));
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

    SnmpType getType(final SnmpTypeMapper typeMapper){
        final WellKnownType attachmentType = WellKnownType.getType(NotificationDescriptor.getUserDataType(get()));
        return attachmentType == null ? null : typeMapper.apply(attachmentType);
    }

    @Override
    public void handleNotification(final Notification notification, final Object handback) {
        final WeakReference<NotificationOriginator> originatorRef = this.notificationOriginator;
        final NotificationOriginator originator = originatorRef != null ?
                originatorRef.get() :
                null;
        if (originator != null && typeMapper != null) {
            notification.setSource(resourceName);
            final SnmpNotification snmpTrap = new SnmpNotification(notificationID,
                    notification,
                    get(),
                    typeMapper);
            originator.notify(new OctetString(), snmpTrap.notificationID, snmpTrap.getBindings()); //for SNMPv3 sending
            originator.notify(SnmpHelpers.toOctetString("public"), snmpTrap.notificationID, snmpTrap.getBindings()); //for SNMPv2 sending
        }
    }

    @Override
    public void disconnected() {
        final WeakReference<NotificationOriginator> originatorRef = this.notificationOriginator;
        this.notificationOriginator = null;
        if (originatorRef != null) originatorRef.clear();
        typeMapper = null;
    }
}
