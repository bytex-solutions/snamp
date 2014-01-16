package com.snamp.adapters;

import org.snmp4j.smi.*;

import java.util.HashMap;

/**
 * Represents SNMP notification with attachments.
 * @author Roman Sakno
 * @version 1.0
 * @since 1.0
 */
public class SnmpNotification extends HashMap<OID, Variable> {
    /**
     * Represents identifier of this SNMP notification instance.
     */
    public final OID notificationID;

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
    }

    public final boolean put(final VariableBinding binding){
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

    /**
     * Returns a string representation of this notification.
     * @return A string representation of this notification.
     */
    @Override
    public String toString() {
        return notificationID.toString();
    }
}
