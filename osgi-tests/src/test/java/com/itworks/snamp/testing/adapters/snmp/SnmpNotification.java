package com.itworks.snamp.testing.adapters.snmp;

import com.itworks.snamp.connectors.notifications.Severity;
import org.snmp4j.smi.*;

import java.text.ParseException;
import java.util.Date;
import java.util.HashMap;

import static com.itworks.snamp.testing.adapters.snmp.SnmpHelpers.DateTimeFormatter;

/**
 * Represents SNMP notification with attachments.
 * @author Roman Sakno
 * @version 1.0
 * @since 1.0
 */
final class SnmpNotification extends HashMap<OID, Variable> {
    private static final long serialVersionUID = -9064210467469772285L;
    /**
     * Represents identifier of this SNMP notification instance.
     */
    public final OID notificationID;
    private final OID messageId;
    private final OID severityId;
    private final OID sequenceNumberId;
    private final OID timeStampId;
    private final OID categoryId;

    /**
     * Initializes a new SNMP notification message.
     * @param notificationID Notification identifier. Cannot be {@literal null}.
     */
    public SnmpNotification(final OID notificationID, final VariableBinding... bindings){
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
    }

    boolean put(final VariableBinding binding){
        return binding != null && put(binding.getOid(), binding.getVariable()) == null;
    }

    /**
     * Returns an array of variable bindings associated with this message.
     * @return An array of variable bindings associated with this message.
     */
    public final VariableBinding[] getBindings(){
        final VariableBinding[] result = new VariableBinding[size()];
        int i = 0;
        for(final OID id: keySet())
            result[i++] = new VariableBinding(id, get(id));
        return result;
    }

    public final String getMessage(){
        return containsKey(messageId) ? get(messageId).toString() : null;
    }

    private static Severity getSeverity(final Integer32 value){
        return Severity.resolve(value.toInt());
    }

    public final Severity getSeverity(){
        return containsKey(severityId) ? getSeverity((Integer32)get(severityId)) : Severity.UNKNOWN;
    }

    public final long getSequenceNumber(){
        return containsKey(sequenceNumberId) ? get(sequenceNumberId).toLong() : -1L;
    }

    public final Date getTimeStamp(final DateTimeFormatter formatter) throws ParseException {
        return containsKey(timeStampId) ? formatter.convert(((OctetString)get(timeStampId)).toByteArray()) : null;
    }

    public final String getCategory() {
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
