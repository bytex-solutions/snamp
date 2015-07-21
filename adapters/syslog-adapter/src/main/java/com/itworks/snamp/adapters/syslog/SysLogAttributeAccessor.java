package com.itworks.snamp.adapters.syslog;

import com.cloudbees.syslog.Facility;
import com.cloudbees.syslog.Severity;
import com.cloudbees.syslog.SyslogMessage;
import com.itworks.snamp.adapters.modeling.AttributeAccessor;
import com.itworks.snamp.connectors.attributes.AttributeDescriptor;

import javax.management.AttributeNotFoundException;
import javax.management.MBeanAttributeInfo;
import javax.management.MBeanException;
import javax.management.ReflectionException;
import java.io.CharArrayWriter;
import java.util.Date;

import static com.itworks.snamp.adapters.syslog.SysLogConfigurationDescriptor.getApplicationName;
import static com.itworks.snamp.adapters.syslog.SysLogConfigurationDescriptor.getFacility;

/**
 * @author Roman Sakno
 * @version 1.0
 * @since 1.0
 */
final class SysLogAttributeAccessor extends AttributeAccessor {

    SysLogAttributeAccessor(final MBeanAttributeInfo metadata) {
        super(metadata);
    }

    private static CharArrayWriter toCharArray(final Object value){
        final CharArrayWriter result = new CharArrayWriter();
        if(value != null)
            result.append(value.toString());
        return result;
    }

    SyslogMessage toMessage(final String resourceName){
        final SyslogMessage message = new SyslogMessage()
                .withTimestamp(new Date())
                .withAppName(getApplicationName(getMetadata().getDescriptor(), resourceName))
                .withFacility(getFacility(getMetadata().getDescriptor(), Facility.AUDIT))
                .withMsgId(AttributeDescriptor.getAttributeName(getMetadata().getDescriptor()))
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
