package com.itworks.snamp.adapters;

import org.snmp4j.smi.Integer32;
import org.snmp4j.smi.Variable;

/**
 * Represents SMIv2 row status as defined in RFC 1903.
 * @author Roman Sakno
 * @version 1.0
 * @since 1.0
 */
enum TableRowStatus {
    /**
     * The conceptual row is available for use by the managed device.
     */
    ACTIVE(1),

    /**
     * The conceptual
     * row exists in the agent, but is unavailable for use by
     * the managed device.
     */
    NOT_IN_SERVICE(2),

    /**
     * The conceptual row
     * exists in the agent, but is missing information
     * necessary in order to be available for use by the
     * managed device.
     */
    NOT_READY(3),

    /**
     * Supplied by a management
     * station wishing to create a new instance of a
     * conceptual row and to have its status automatically set
     * to active, making it available for use by the managed
     * device.
     */
    CREATE_AND_GO(4),

    /**
     * Supplied by a management
     * station wishing to create a new instance of a
     * conceptual row (but not make it available for use by
     * the managed device).
     */
    CREATE_AND_WAIT(5),

    /**
     * Supplied by a management station
     * wishing to delete all of the instances associated with
     * an existing conceptual row.
     */
    DESTROY(6);

    private final int value;

    private TableRowStatus(final int v){
        value = v;
    }

    public Integer32 toManagedScalarValue(){
        return new Integer32(value);
    }

    public static TableRowStatus parse(final int value){
        switch (value){
            case 1: return ACTIVE;
            case 2: return NOT_IN_SERVICE;
            case 3: return NOT_READY;
            case 4: return CREATE_AND_GO;
            case 5: return CREATE_AND_WAIT;
            case 6: return DESTROY;
            default: return null;
        }
    }

    public static TableRowStatus parse(final Integer32 value){
        return value != null ? parse(value.toInt()) : ACTIVE;
    }

    public static TableRowStatus parse(final Variable value){
        return value instanceof Integer32 ? parse((Integer32)value) : ACTIVE;
    }
}
