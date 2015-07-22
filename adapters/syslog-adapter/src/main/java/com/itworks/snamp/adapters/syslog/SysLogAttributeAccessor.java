package com.itworks.snamp.adapters.syslog;

import com.cloudbees.syslog.Facility;
import com.cloudbees.syslog.Severity;
import com.cloudbees.syslog.SyslogMessage;
import com.google.common.collect.ImmutableSet;
import com.itworks.snamp.adapters.modeling.AttributeAccessor;
import com.itworks.snamp.connectors.attributes.AttributeDescriptor;

import javax.management.AttributeNotFoundException;
import javax.management.MBeanAttributeInfo;
import javax.management.MBeanException;
import javax.management.ReflectionException;
import java.io.CharArrayWriter;
import java.util.Date;
import java.util.Set;

import static com.itworks.snamp.adapters.syslog.SysLogConfigurationDescriptor.getApplicationName;
import static com.itworks.snamp.adapters.syslog.SysLogConfigurationDescriptor.getFacility;
import static com.itworks.snamp.adapters.ResourceAdapter.FeatureBindingInfo;

/**
 * @author Roman Sakno
 * @version 1.0
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
        return AttributeDescriptor.getAttributeName(getMetadata().getDescriptor());
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
