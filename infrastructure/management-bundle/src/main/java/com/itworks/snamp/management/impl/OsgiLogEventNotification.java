package com.itworks.snamp.management.impl;

import org.osgi.service.log.LogEntry;
import org.osgi.service.log.LogService;

import javax.management.MBeanNotificationInfo;
import javax.management.Notification;

/**
 * @author Roman Sakno
 * @version 1.0
 * @since 1.0
 */
final class OsgiLogEventNotification {
    public static final String ERROR_NOTIF_TYPE = "itworks.snamp.monitoring.error";
    public static final String WARNING_NOTIF_TYPE  ="itworks.snamp.monitoring.warning";
    public static final String INFO_NOTIF_TYPE  = "itworks.snamp.monitoring.info";
    public static final String DEBUG_NOTIF_TYPE = "itworks.snamp.monitoring.debug";

    private OsgiLogEventNotification(){

    }

    @SuppressWarnings("ThrowableResultOfMethodCallIgnored")
    public static Notification create(final LogEntry entry, final Object source, final long sequenceNumber){
        final String message = entry.getException() != null ?
                String.format("%s. Reason: %s", entry.getMessage(), entry.getException()):
                entry.getMessage();
        return new Notification(getNotificationType(entry), source, sequenceNumber, entry.getTime(), message);
    }

    public static String getNotificationType(final LogEntry entry){
        switch (entry.getLevel()){
            case LogService.LOG_ERROR: return ERROR_NOTIF_TYPE;
            case LogService.LOG_DEBUG: return DEBUG_NOTIF_TYPE;
            case LogService.LOG_WARNING: return WARNING_NOTIF_TYPE;
            default: return INFO_NOTIF_TYPE;
        }
    }

    public static MBeanNotificationInfo createNotificationInfo(){
        return new MBeanNotificationInfo(new String[]{DEBUG_NOTIF_TYPE, INFO_NOTIF_TYPE, ERROR_NOTIF_TYPE, WARNING_NOTIF_TYPE},
                "SnampLogEvent",
                "Exposes OSGi log events through JMX.");
    }
}
