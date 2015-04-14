package com.itworks.snamp.adapters.syslog;

import com.cloudbees.syslog.Facility;
import com.cloudbees.syslog.Severity;
import com.itworks.snamp.connectors.notifications.NotificationDescriptor;

import javax.management.MBeanNotificationInfo;

/**
 * @author Roman Sakno
 * @version 1.0
 * @since 1.0
 */
final class SysLogSourceInfo {
    private final Severity severity;
    private final Facility facility;
    private final String applicationName;

    SysLogSourceInfo(final String resourceName,
                     final MBeanNotificationInfo metadata){
        switch (NotificationDescriptor.getSeverity(metadata)){
            case PANIC: severity = Severity.EMERGENCY; break;
            case ALERT: severity = Severity.ALERT; break;
            case CRITICAL: severity = Severity.CRITICAL; break;
            case ERROR: severity = Severity.ERROR; break;
            case WARNING: severity = Severity.WARNING; break;
            case INFO: severity = Severity.INFORMATIONAL; break;
            case DEBUG: severity = Severity.DEBUG; break;
            case NOTICE: severity = Severity.NOTICE; break;
            default: severity = Severity.DEBUG; break;
        }
        facility = SysLogConfigurationDescriptor.getFacility(metadata.getDescriptor(), Facility.DAEMON);
        this.applicationName = SysLogConfigurationDescriptor.getAppliationName(metadata.getDescriptor(), resourceName);
    }

    String getApplicationName(){
        return applicationName;
    }

    Severity getSeverity(){
        return severity;
    }

    Facility getFacility(){
        return facility;
    }
}
