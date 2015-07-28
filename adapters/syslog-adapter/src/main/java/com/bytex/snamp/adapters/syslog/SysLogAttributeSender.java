package com.bytex.snamp.adapters.syslog;

import com.bytex.snamp.TimeSpan;
import com.bytex.snamp.adapters.modeling.ModelOfAttributes;
import com.bytex.snamp.adapters.modeling.PeriodicPassiveChecker;

import java.util.Objects;

/**
 * @author Roman Sakno
 * @version 1.0
 * @since 1.0
 */
final class SysLogAttributeSender extends PeriodicPassiveChecker<SysLogAttributeAccessor> {
    private final ConcurrentSyslogMessageSender messageSender;

    SysLogAttributeSender(final TimeSpan period,
                          final ConcurrentSyslogMessageSender sender,
                          final ModelOfAttributes<SysLogAttributeAccessor> attributes){
        super(period, attributes);
        this.messageSender = Objects.requireNonNull(sender);
    }

    @Override
    protected void processAttribute(final String resourceName, final SysLogAttributeAccessor accessor) {
        messageSender.sendMessage(accessor.toMessage(resourceName));
    }
}