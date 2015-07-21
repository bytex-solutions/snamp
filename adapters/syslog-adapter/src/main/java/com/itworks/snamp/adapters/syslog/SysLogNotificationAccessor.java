package com.itworks.snamp.adapters.syslog;

import com.cloudbees.syslog.Facility;
import com.cloudbees.syslog.Severity;
import com.itworks.snamp.adapters.NotificationListener;
import com.itworks.snamp.adapters.modeling.NotificationRouter;
import com.itworks.snamp.connectors.notifications.NotificationDescriptor;

import javax.management.MBeanNotificationInfo;
import javax.management.Notification;

/**
 * @author Roman Sakno
 * @version 1.0
 * @since 1.0
 */
final class SysLogNotificationAccessor extends NotificationRouter {
    private final String resourceName;

    SysLogNotificationAccessor(final String resourceName,
                               final MBeanNotificationInfo metadata,
                               final NotificationListener destination) {
        super(metadata, destination);
        this.resourceName = resourceName;
    }

    @Override
    protected Notification intercept(final Notification notification) {
        notification.setSource(resourceName);
        return notification;
    }

    static Severity getSeverity(final MBeanNotificationInfo metadata){
        switch (NotificationDescriptor.getSeverity(metadata)){
            case PANIC: return Severity.EMERGENCY;
            case ALERT: return Severity.ALERT;
            case CRITICAL: return Severity.CRITICAL;
            case ERROR: return Severity.ERROR;
            case WARNING: return Severity.WARNING;
            case INFO: return Severity.INFORMATIONAL;
            case DEBUG: return Severity.DEBUG;
            case NOTICE: return Severity.NOTICE;
            default: return Severity.DEBUG;
        }
    }

    static Facility getFacility(final MBeanNotificationInfo metadata){
        return SysLogConfigurationDescriptor.getFacility(metadata.getDescriptor(), Facility.DAEMON);
    }

    static String getApplicationName(final MBeanNotificationInfo metadata,
                                     final String defaultValue){
        return SysLogConfigurationDescriptor.getApplicationName(metadata.getDescriptor(), defaultValue);
    }
}
