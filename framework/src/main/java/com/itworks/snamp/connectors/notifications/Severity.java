package com.itworks.snamp.connectors.notifications;

import java.io.Serializable;

/**
 * Represents severity of the event.
 * @author Roman Sakno
 * @version 1.0
 * @since 1.0
 */
public enum Severity implements Serializable, Comparable<Severity> {
    /**
     * Severity is unknown.
     */
    UNKNOWN(0),

    /**
     * A "panic" condition usually affecting multiple apps/servers/sites.
     * At this level it would usually notify all tech staff on call.
     */
    PANIC(1),

    /**
     * Should be corrected immediately, therefore notify staff who can fix the problem.
     * An example would be the loss of a primary ISP connection.
     */
    ALERT(2),

    /**
     * Should be corrected immediately, but indicates failure in a secondary system,
     * an example is a loss of a backup ISP connection.
     */
    CRITICAL(3),

    /**
     * Non-urgent failures, these should be relayed to developers or admins;
     * each item must be resolved within a given time.
     */
    ERROR(4),

    /**
     * Warning messages, not an error, but indication that an error will occur if action is not taken,
     * e.g. file system 85% full - each item must be resolved within a given time.
     */
    WARNING(5),

    /**
     * Events that are unusual but not error conditions - might be summarized in an email to
     * developers or admins to spot potential problems - no immediate action required.
     */
    NOTICE(6),

    /**
     * Normal operational messages - may be harvested for reporting,
     * measuring throughput, etc. - no action required.
     */
    INFO(7),

    /**
     * Info useful to developers for debugging the application, not useful during operations.
     */
    DEBUG(8);

    private final int level;

    private Severity(final int level){
        this.level = level;
    }

    /**
     * Gets severity level.
     * @return The severity level.
     */
    public int getLevel(){
        return level;
    }

    /**
     * Resolves severity level.
     * @param value A value that represents severity level.
     * @return The severity level; or {@literal null}, if the specified severity level
     * is not valid.
     */
    public static Severity resolve(final int value){
        for(final Severity sev: values())
            if(sev.level == value)
                return sev;
        return null;
    }
}
