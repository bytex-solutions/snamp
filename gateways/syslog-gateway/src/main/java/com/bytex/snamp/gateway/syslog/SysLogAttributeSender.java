package com.bytex.snamp.gateway.syslog;

import com.bytex.snamp.core.ClusterMember;
import com.bytex.snamp.gateway.modeling.ModelOfAttributes;
import com.bytex.snamp.gateway.modeling.PeriodicPassiveChecker;
import com.bytex.snamp.internal.Utils;

import java.time.Duration;
import java.util.Objects;

/**
 * @author Roman Sakno
 * @version 2.0
 * @since 1.0
 */
final class SysLogAttributeSender extends PeriodicPassiveChecker<SysLogAttributeAccessor> {
    private final ConcurrentSyslogMessageSender messageSender;
    private final ClusterMember clusterMember;

    SysLogAttributeSender(final Duration period,
                          final ConcurrentSyslogMessageSender sender,
                          final ModelOfAttributes<SysLogAttributeAccessor> attributes){
        super(period, attributes);
        this.messageSender = Objects.requireNonNull(sender);
        this.clusterMember = ClusterMember.get(Utils.getBundleContextOfObject(this));
    }

    @Override
    public boolean accept(final String resourceName, final SysLogAttributeAccessor accessor) {
        if(clusterMember.isActive()) {
            messageSender.sendMessage(accessor.toMessage(resourceName));
            return true;
        } else return false;
    }
}
