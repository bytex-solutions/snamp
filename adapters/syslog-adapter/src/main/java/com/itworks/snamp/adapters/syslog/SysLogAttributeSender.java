package com.itworks.snamp.adapters.syslog;

import com.itworks.snamp.TimeSpan;
import com.itworks.snamp.adapters.AbstractAttributesModel;
import com.itworks.snamp.adapters.PeriodicPassiveCheckSender;

import java.util.Objects;

/**
 * @author Roman Sakno
 * @version 1.0
 * @since 1.0
 */
final class SysLogAttributeSender extends PeriodicPassiveCheckSender<SysLogAttributeAccessor> {
    private final ConcurrentSyslogMessageSender messageSender;

    SysLogAttributeSender(final TimeSpan period,
                          final ConcurrentSyslogMessageSender sender,
                          final AbstractAttributesModel<SysLogAttributeAccessor> attributes){
        super(period, attributes);
        this.messageSender = Objects.requireNonNull(sender);
    }

    @Override
    protected void processAttribute(final String resourceName, final SysLogAttributeAccessor accessor) {
        messageSender.sendMessage(accessor.toMessage(resourceName));
    }
}
