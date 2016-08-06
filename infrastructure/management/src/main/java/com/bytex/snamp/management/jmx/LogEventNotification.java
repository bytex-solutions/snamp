package com.bytex.snamp.management.jmx;

import com.bytex.snamp.jmx.OpenMBean;
import org.osgi.service.log.LogEntry;
import org.osgi.service.log.LogService;

/**
 * The type Log event notification.
 * @author Roman Sakno
 * @version 2.0
 * @since 1.0
 */
final class LogEventNotification extends OpenMBean.OpenNotification<LogEntry> {

    /**
     * The constant ERROR_NOTIF_TYPE.
     */
    private static final String ERROR_NOTIF_TYPE = "com.bytex.snamp.monitoring.error";

    /**
     * The constant WARNING_NOTIF_TYPE.
     */
    private static final String WARNING_NOTIF_TYPE  ="com.bytex.snamp.monitoring.warning";

    /**
     * The constant INFO_NOTIF_TYPE.
     */
    private static final String INFO_NOTIF_TYPE  = "com.bytex.snamp.monitoring.info";

    /**
     * The constant DEBUG_NOTIF_TYPE.
     */
    private static final String DEBUG_NOTIF_TYPE = "com.bytex.snamp.monitoring.debug";

    /**
     * Instantiates a new Log event notification.
     */
    LogEventNotification(){
        super("SnampLogEvent", LogEntry.class,
                ERROR_NOTIF_TYPE,
                WARNING_NOTIF_TYPE,
                INFO_NOTIF_TYPE,
                DEBUG_NOTIF_TYPE);
    }

    @SuppressWarnings("ThrowableResultOfMethodCallIgnored")
    @Override
    protected String getMessage(final LogEntry eventObject) {
        return eventObject.getException() != null ?
                String.format("%s. Reason: %s", eventObject.getMessage(), eventObject.getException()):
                eventObject.getMessage();
    }

    @Override
    protected String getType(final LogEntry eventObject) {
        switch (eventObject.getLevel()){
            case LogService.LOG_ERROR: return ERROR_NOTIF_TYPE;
            case LogService.LOG_DEBUG: return DEBUG_NOTIF_TYPE;
            case LogService.LOG_WARNING: return WARNING_NOTIF_TYPE;
            default: return INFO_NOTIF_TYPE;
        }
    }
}
