package com.bytex.snamp.gateway.syslog;

import com.bytex.snamp.connector.notifications.NotificationDescriptor;
import com.bytex.snamp.gateway.NotificationListener;
import com.bytex.snamp.gateway.modeling.NotificationRouter;
import com.cloudbees.syslog.Facility;
import com.cloudbees.syslog.Severity;
import com.google.common.collect.ImmutableSet;

import javax.management.MBeanNotificationInfo;

import static com.bytex.snamp.gateway.Gateway.FeatureBindingInfo;

/**
 * @author Roman Sakno
 * @version 2.0
 * @since 1.0
 */
final class SysLogNotificationAccessor extends NotificationRouter implements FeatureBindingInfo<MBeanNotificationInfo> {
    private static final String FACILITY_PARAM = "facility";
    private static final String SEVERITY_PARAM = "severity";

    SysLogNotificationAccessor(final String resourceName,
                               final MBeanNotificationInfo metadata,
                               final NotificationListener destination) {
        super(resourceName, metadata, destination);
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

    private Severity getSeverity(){
        return getSeverity(get());
    }

    private Facility getFacility(){
        return getFacility(get());
    }

    @Override
    public Object getProperty(final String propertyName) {
        switch (propertyName){
            case FACILITY_PARAM: return getFacility();
            case SEVERITY_PARAM: return getSeverity();
            default: return null;
        }
    }

    @Override
    public ImmutableSet<String> getProperties() {
        return ImmutableSet.of(SEVERITY_PARAM, FACILITY_PARAM);
    }

    @Override
    public boolean setProperty(final String propertyName, final Object value) {
        return false;
    }

    static Facility getFacility(final MBeanNotificationInfo metadata){
        return SysLogConfigurationDescriptor.getFacility(metadata.getDescriptor(), Facility.DAEMON);
    }

    static String getApplicationName(final MBeanNotificationInfo metadata,
                                     final String defaultValue){
        return SysLogConfigurationDescriptor.getApplicationName(metadata.getDescriptor(), defaultValue);
    }
}
