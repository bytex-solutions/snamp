package com.bytex.snamp.gateway.syslog;

import com.bytex.snamp.connector.attributes.AttributeDescriptor;
import com.bytex.snamp.gateway.modeling.AttributeAccessor;
import com.cloudbees.syslog.Facility;
import com.cloudbees.syslog.Severity;
import com.cloudbees.syslog.SyslogMessage;
import com.google.common.collect.ImmutableSet;

import javax.management.AttributeNotFoundException;
import javax.management.MBeanAttributeInfo;
import javax.management.MBeanException;
import javax.management.ReflectionException;
import java.io.CharArrayWriter;
import java.util.Date;

import static com.bytex.snamp.gateway.Gateway.FeatureBindingInfo;
import static com.bytex.snamp.gateway.syslog.SysLogConfigurationDescriptor.getApplicationName;
import static com.bytex.snamp.gateway.syslog.SysLogConfigurationDescriptor.getFacility;

/**
 * @author Roman Sakno
 * @version 2.0
 * @since 1.0
 */
final class SysLogAttributeAccessor extends AttributeAccessor implements FeatureBindingInfo<MBeanAttributeInfo> {
    private static final String MESSAGE_ID_PARAM = "messageID";
    private static final String FACILITY_PARAM = "facility";

    SysLogAttributeAccessor(final MBeanAttributeInfo metadata) {
        super(metadata);
    }

    private static CharArrayWriter toCharArray(final Object value){
        final CharArrayWriter result = new CharArrayWriter();
        if(value != null)
            result.append(value.toString());
        return result;
    }

    private String getMessageID(){
        return AttributeDescriptor.getName(getMetadata());
    }

    private Facility getLogFacility(){
        return getFacility(getMetadata().getDescriptor(), Facility.AUDIT);
    }

    @Override
    public Object getProperty(final String propertyName) {
        switch (propertyName){
            case MESSAGE_ID_PARAM:
                return getMessageID();
            case FACILITY_PARAM:
                return getLogFacility();
            default:
                return null;
        }
    }

    @Override
    public ImmutableSet<String> getProperties() {
        return ImmutableSet.of(FACILITY_PARAM, MESSAGE_ID_PARAM);
    }

    @Override
    public boolean setProperty(final String propertyName, final Object value) {
        return false;
    }

    SyslogMessage toMessage(final String resourceName){
        final SyslogMessage message = new SyslogMessage()
                .withTimestamp(new Date())
                .withAppName(getApplicationName(getMetadata().getDescriptor(), resourceName))
                .withFacility(getLogFacility())
                .withMsgId(getMessageID())
                .withProcId(SysLogUtils.getProcessId(resourceName));
        try {
            final Object value = getValue();
            message
                    .withSeverity(Severity.INFORMATIONAL)
                    .withMsg(toCharArray(value));
        } catch (final MBeanException | ReflectionException e) {
            message
                    .withSeverity(Severity.ERROR)
                    .withMsg(toCharArray(e.getMessage()));
        } catch (final AttributeNotFoundException e) {
            message
                    .withSeverity(Severity.WARNING)
                    .withMsg(toCharArray(e.getMessage()));
        }
        return message;
    }

    @Override
    public boolean canWrite() {
        return false;
    }
}
