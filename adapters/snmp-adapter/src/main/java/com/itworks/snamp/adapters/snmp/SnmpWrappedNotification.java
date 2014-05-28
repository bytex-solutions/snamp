package com.itworks.snamp.adapters.snmp;

import static com.itworks.snamp.connectors.NotificationSupport.Notification;
import static com.itworks.snamp.adapters.snmp.SnmpHelpers.DateTimeFormatter;
import org.snmp4j.agent.NotificationOriginator;
import org.snmp4j.smi.*;

import java.text.ParseException;
import java.util.Date;

/**
 * Represents SNMP notification at sender side.
 * @author Roman Sakno
 * @version 1.0
 * @since 1.0
 */
final class SnmpWrappedNotification extends SnmpNotification {

    private final OID messageId;
    private final OID severityId;
    private final OID sequenceNumberId;
    private final OID timeStampId;
    private final OID categoryId;

    /**
     * Initializes a new SNMP notification message.
     * @param notificationID Notification identifier. Cannot be {@literal null}.
     */
    public SnmpWrappedNotification(final OID notificationID, final VariableBinding... bindings){
        super(notificationID, bindings);
        messageId = new OID(notificationID).append(new OID("1"));
        severityId = new OID(notificationID).append(new OID("2"));
        sequenceNumberId = new OID(notificationID).append(new OID("3"));
        timeStampId = new OID(notificationID).append(new OID("4"));
        categoryId = new OID(notificationID).append(new OID("5"));
    }

    /**
     * Initializes new SNMP notification based on SNAMP generic notification.
     * <p>
     *     This constructor maps the following notification properties:
     *     <ul>
     *         <li>{@link com.itworks.snamp.connectors.NotificationSupport.Notification#getMessage()} into OID .1</li>
     *         <li>{@link com.itworks.snamp.connectors.NotificationSupport.Notification#getSeverity()} ordinal into OID .2</li>
     *         <li>{@link com.itworks.snamp.connectors.NotificationSupport.Notification#getSequenceNumber()} into OID .3</li>
     *     </ul>
     * </p>
     * @param notificationID The notification identifier.
     * @param n The notification to wrap.
     * @param category Notification category.
     */
    public SnmpWrappedNotification(final OID notificationID, final Notification n, final String category){
        this(notificationID);
        put(messageId, new OctetString(n.getMessage()));
        put(severityId, new Integer32(n.getSeverity().ordinal()));
        put(sequenceNumberId, new Counter64(n.getSequenceNumber()));
        put(categoryId, new OctetString(category));
    }

    /**
     * Initializes new SNMP notification based on SNAMP generic notification.
     * <p>
     *     This constructor maps the following notification properties:
     *     <ul>
     *         <li>{@link com.itworks.snamp.connectors.NotificationSupport.Notification#getMessage()} into OID .1</li>
     *         <li>{@link com.itworks.snamp.connectors.NotificationSupport.Notification#getSeverity()} ordinal into OID .2</li>
     *         <li>{@link com.itworks.snamp.connectors.NotificationSupport.Notification#getSequenceNumber()} into OID .3</li>
     *         <li>{@link com.itworks.snamp.connectors.NotificationSupport.Notification#getTimeStamp()} into OID .4</li>
     *     </ul>
     * </p>
     * @param notificationID The notification identifier.
     * @param n The notification to wrap.
     * @param category Notification category.
     * @param formatter Notification timestamp formatter.
     */
    public SnmpWrappedNotification(final OID notificationID, final Notification n, final String category, final DateTimeFormatter formatter){
        this(notificationID, n, category);
        put(timeStampId, new OctetString(formatter.convert(n.getTimeStamp())));
    }

    /**
     * Returns the notification message.
     * @return
     */
    public final String getMessage(){
        return containsKey(messageId) ? get(messageId).toString() : null;
    }

    private static Notification.Severity getSeverity(final Integer32 value){
        return Notification.Severity.values()[value.toInt()];
    }

    public final Notification.Severity getSeverity(){
        return containsKey(severityId) ? getSeverity((Integer32)get(severityId)) : Notification.Severity.UNKNOWN;
    }

    public final long getSequenceNumber(){
        return containsKey(sequenceNumberId) ? get(sequenceNumberId).toLong() : -1L;
    }

    public final Date getTimeStamp(final DateTimeFormatter formatter) throws ParseException{
        return containsKey(timeStampId) ? formatter.convert(((OctetString)get(timeStampId)).toByteArray()) : null;
    }

    public final String getCategory(){
        return containsKey(categoryId) ? get(categoryId).toString() : null;
    }

    /**
     * Sends this notification.
     * @param context
     * @param originator
     * @return
     */
    public final boolean send(final OctetString context, final NotificationOriginator originator){
        return originator.notify(context, notificationID, getBindings()) != null;
    }
}
