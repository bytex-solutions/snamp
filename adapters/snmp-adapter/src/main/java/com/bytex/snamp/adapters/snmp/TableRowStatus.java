package com.bytex.snamp.adapters.snmp;

import org.snmp4j.agent.mo.snmp.RowStatus;
import org.snmp4j.smi.Integer32;
import org.snmp4j.smi.Variable;

/**
 * Represents SMIv2 row status as defined in RFC 1903.
 * @author Roman Sakno
 * @version 1.2
 * @since 1.0
 */
enum TableRowStatus {
    /**
     * The conceptual row is available for use by the managed device.
     */
    ACTIVE(RowStatus.active),

    /**
     * The conceptual
     * row exists in the agent, but is unavailable for use by
     * the managed device.
     */
    NOT_IN_SERVICE(RowStatus.notInService),

    /**
     * The conceptual row
     * exists in the agent, but is missing information
     * necessary in order to be available for use by the
     * managed device.
     */
    NOT_READY(RowStatus.notReady),

    /**
     * Supplied by a management
     * station wishing to create a new instance of a
     * conceptual row and to have its status automatically set
     * to active, making it available for use by the managed
     * device.
     */
    CREATE_AND_GO(RowStatus.createAndGo),

    /**
     * Supplied by a management
     * station wishing to create a new instance of a
     * conceptual row (but not make it available for use by
     * the managed device).
     */
    CREATE_AND_WAIT(RowStatus.createAndWait),

    /**
     * Supplied by a management station
     * wishing to delete all of the instances associated with
     * an existing conceptual row.
     */
    DESTROY(RowStatus.destroy);

    private final int value;

    TableRowStatus(final int v){
        value = v;
    }

    public Integer32 toManagedScalarValue(){
        return new Integer32(value);
    }

    public static TableRowStatus parse(final int value){
        for(final TableRowStatus status: values())
            if(value == status.value) return status;
        return null;
    }

    public static TableRowStatus parse(final Integer32 value){
        return value != null ? parse(value.toInt()) : ACTIVE;
    }

    public static TableRowStatus parse(final Variable value){
        return value instanceof Integer32 ? parse((Integer32)value) : ACTIVE;
    }
}
