package com.bytex.snamp.connector.notifications;

import com.google.common.collect.ImmutableSortedSet;

import java.util.Objects;

/**
 * Represents severity of the event.
 * @author Roman Sakno
 * @version 2.0
 * @since 1.0
 * @see <a href="https://en.wikipedia.org/wiki/Syslog">Syslog severity levels</a>
 */
public enum Severity {
    /**
     * A "panic" condition usually affecting multiple apps/servers/sites.
     * At this level it would usually notify all tech staff on call.
     */
    PANIC(0, "panic"),

    /**
     * Should be corrected immediately, therefore notify staff who can fix the problem.
     * An example would be the loss of a primary ISP connection.
     */
    ALERT(1, "alert"),

    /**
     * Should be corrected immediately, but indicates failure in a secondary system,
     * an example is a loss of a backup ISP connection.
     */
    CRITICAL(2, "critical"),

    /**
     * Non-urgent failures, these should be relayed to developers or admins;
     * each item must be resolved within a given time.
     */
    ERROR(3, "error"),

    /**
     * Warning messages, not an error, but indication that an error will occur if action is not taken,
     * e.g. file system 85% full - each item must be resolved within a given time.
     */
    WARNING(4, "warning"),

    /**
     * Events that are unusual but not error conditions - might be summarized in an email to
     * developers or admins to spot potential problems - no immediate action required.
     */
    NOTICE(5, "notice"),

    /**
     * Normal operational messages - may be harvested for reporting,
     * measuring throughput, etc. - no action required.
     */
    INFO(6, "informational"),

    /**
     * Info useful to developers for debugging the application, not useful during operations.
     */
    DEBUG(7, "debug"),

    /**
     * Severity is unknown.
     */
    UNKNOWN(Integer.MAX_VALUE, "unknown");

    private static final ImmutableSortedSet<Severity> ALL_VALUES = ImmutableSortedSet.copyOf(values());
    private final int level;
    private final String strval;

    Severity(final int level, final String name){
        this.level = level;
        this.strval = name;
    }

    /**
     * Gets severity level.
     * @return The severity level.
     */
    public final int getLevel(){
        return level;
    }

    /**
     * Resolves severity level.
     * @param value A value that represents severity level.
     * @return The severity level; or {@literal null}, if the specified severity level
     * is not valid.
     */
    public static Severity resolve(final int value){
        for(final Severity sev: ALL_VALUES)
            if(sev.level == value)
                return sev;
        return UNKNOWN;
    }

    public static Severity resolve(String value) {
        value = value.toLowerCase();
        switch (value) {
            case "0": //jmx severity level
            case "emerg":
                return PANIC;
            case "1": //jmx severity level
                return ALERT;
            case "2": //jmx severity level
            case "crit":
                return CRITICAL;
            case "3": //jmx severity level
            case "err":
                return ERROR;
            case "4": //jmx severity level
                return WARNING;
            case "5": //jmx severity level
                return NOTICE;
            case "6": //jmx severity level
            case "info":
                return INFO;
            case "7": //jmx severity level
                return DEBUG;
            case "": //jmx severity level
                return UNKNOWN;
            default:
                for (final Severity severity : ALL_VALUES)
                    if (Objects.equals(severity.toString(), value))
                        return severity;
        }
        return UNKNOWN;
    }

    public final boolean isAllowed(final Severity actual){
        return compareTo(actual) >= 0;
    }

    @Override
    public final String toString(){
        return strval;
    }
}
