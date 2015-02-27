package com.itworks.snamp.management.impl;

import com.itworks.snamp.management.jmx.OpenMBean;
import org.osgi.service.log.LogEntry;
import org.osgi.service.log.LogService;

/**
* @author Roman Sakno
* @version 1.0
* @since 1.0
*/
final class LogEventNotification extends OpenMBean.OpenNotification<LogEntry> {
    public static final String ERROR_NOTIF_TYPE = "itworks.snamp.monitoring.error";
    public static final String WARNING_NOTIF_TYPE  ="itworks.snamp.monitoring.warning";
    public static final String INFO_NOTIF_TYPE  = "itworks.snamp.monitoring.info";
    public static final String DEBUG_NOTIF_TYPE = "itworks.snamp.monitoring.debug";

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