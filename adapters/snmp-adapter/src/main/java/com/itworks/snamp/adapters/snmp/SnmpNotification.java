package com.itworks.snamp.adapters.snmp;

import com.itworks.snamp.SafeConsumer;
import com.itworks.snamp.connectors.ManagedEntityValue;
import com.itworks.snamp.connectors.notifications.Notification;
import com.itworks.snamp.connectors.notifications.Severity;
import org.snmp4j.smi.*;

import java.text.ParseException;
import java.util.*;

import static com.itworks.snamp.adapters.snmp.SnmpAdapterConfigurationDescriptor.DATE_TIME_DISPLAY_FORMAT_PARAM;
import static com.itworks.snamp.adapters.snmp.SnmpHelpers.DateTimeFormatter;

/**
 * Represents SNMP notification with attachments.
 * @author Roman Sakno
 * @version 1.0
 * @since 1.0
 */
final class SnmpNotification extends HashMap<OID, Variable> {
    /**
     * Represents identifier of this SNMP notification instance.
     */
    public final OID notificationID;
    private final OID messageId;
    private final OID severityId;
    private final OID sequenceNumberId;
    private final OID timeStampId;
    private final OID categoryId;
    private final OID correlationId;
    private static final int MAX_RESERVED_POSTFIX = 10;

    /**
     * Initializes a new SNMP notification message.
     * @param notificationID Notification identifier. Cannot be {@literal null}.
     */
    SnmpNotification(final OID notificationID, final VariableBinding... bindings){
        super(bindings.length > 0 ? bindings.length : 4);
        if(notificationID == null) throw new IllegalArgumentException("notificationID is null.");
        this.notificationID = notificationID;
        for(final VariableBinding b: bindings)
            put(b);
        messageId = new OID(notificationID).append(1);
        severityId = new OID(notificationID).append(2);
        sequenceNumberId = new OID(notificationID).append(3);
        timeStampId = new OID(notificationID).append(4);
        categoryId = new OID(notificationID).append(5);
        correlationId = new OID(notificationID).append(6);
    }

    /**
     * Initializes new SNMP notification based on SNAMP generic notification.
     * <p>
     *     This constructor maps the following notification properties:
     *     <ul>
     *         <li>{@link com.itworks.snamp.connectors.notifications.Notification#getMessage()} into OID .1</li>
     *         <li>{@link com.itworks.snamp.connectors.notifications.Notification#getSeverity()} ordinal into OID .2</li>
     *         <li>{@link com.itworks.snamp.connectors.notifications.Notification#getSequenceNumber()} into OID .3</li>
     *     </ul>
     * </p>
     * @param notificationID The notification identifier.
     * @param n The notification to wrap.
     * @param category Notification category.
     */
    SnmpNotification(final OID notificationID, final Notification n, final String category,
                     final ManagedEntityValue<?> attachment,
                     final Map<String, String> options) throws Throwable {
        this(notificationID);
        put(messageId, new OctetString(n.getMessage()));
        put(severityId, new Integer32(n.getSeverity().ordinal()));
        put(sequenceNumberId, new Counter64(n.getSequenceNumber()));
        put(categoryId, new OctetString(category));
        put(correlationId, getCorrelationID(n.getCorrelationID()));
        final DateTimeFormatter formatter = SnmpHelpers.createDateTimeFormatter(options.get(DATE_TIME_DISPLAY_FORMAT_PARAM));
        put(timeStampId, new OctetString(formatter.convert(n.getTimeStamp())));
        putAttachment(notificationID, attachment, options, this);
    }

    private static void putAttachment(final OID notificationID,
                                      final ManagedEntityValue<?> attachment,
                                      final Map<String, String> options,
                                      final Map<OID, Variable> output) throws Throwable {
        if (attachment == null) return;
        final SnmpType type = SnmpType.map(attachment.type);
        if (type == null) return;
        else if (type.isScalar()) {
            final Variable value = type.convert(attachment.rawValue, attachment.type);
            output.put(new OID(notificationID).append(MAX_RESERVED_POSTFIX + 1), value != null ? value : new Null());
        } else if (Objects.equals(type, SnmpType.TABLE))
            SnmpTableObject.forEachVariable(attachment, options, new SafeConsumer<VariableBinding>() {
                @Override
                public void accept(final VariableBinding binding) {
                    output.put(new OID(notificationID).append(MAX_RESERVED_POSTFIX + 1).append(binding.getOid()), binding.getVariable());
                }
            });
    }

    private static Variable getCorrelationID(final String correlID){
        return correlID != null ? new OctetString(correlID) : new Null();
    }

    boolean put(final VariableBinding binding) {
        if (binding == null || containsKey(binding.getOid())) return false;
        put(binding.getOid(), binding.getVariable());
        return true;
    }

    /**
     * Returns an array of variable bindings associated with this message.
     * @return An array of variable bindings associated with this message.
     */
    VariableBinding[] getBindings(){
        final VariableBinding[] result = new VariableBinding[size()];
        int i = 0;
        for(final OID id: keySet())
            result[i++] = new VariableBinding(id, get(id));
        return result;
    }

    String getMessage(){
        return containsKey(messageId) ? get(messageId).toString() : null;
    }

    private static Severity getSeverity(final Integer32 value){
        return Severity.values()[value.toInt()];
    }

    Severity getSeverity(){
        return containsKey(severityId) ? getSeverity((Integer32)get(severityId)) : Severity.UNKNOWN;
    }

    long getSequenceNumber(){
        return containsKey(sequenceNumberId) ? get(sequenceNumberId).toLong() : -1L;
    }

    Date getTimeStamp(final DateTimeFormatter formatter) throws ParseException {
        return containsKey(timeStampId) ? formatter.convert(((OctetString)get(timeStampId)).toByteArray()) : null;
    }

    String getCategory() {
        return containsKey(categoryId) ? get(categoryId).toString() : null;
    }

    /**
     * Returns a string representation of this notification.
     * @return A string representation of this notification.
     */
    @Override
    public String toString() {
        return notificationID.toString();
    }
}
