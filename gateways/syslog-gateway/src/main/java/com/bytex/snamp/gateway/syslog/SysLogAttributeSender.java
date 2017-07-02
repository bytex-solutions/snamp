package com.bytex.snamp.gateway.syslog;

import com.bytex.snamp.core.ClusterMember;
import com.bytex.snamp.core.LoggerProvider;
import com.bytex.snamp.gateway.modeling.ModelOfAttributes;
import com.bytex.snamp.gateway.modeling.PeriodicPassiveChecker;
import com.bytex.snamp.internal.Utils;
import com.cloudbees.syslog.sender.SyslogMessageSender;

import java.io.IOException;
import java.time.Duration;
import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author Roman Sakno
 * @version 2.0
 * @since 1.0
 */
final class SysLogAttributeSender extends PeriodicPassiveChecker<SysLogAttributeAccessor> {
    private final SyslogMessageSender messageSender;
    private final ClusterMember clusterMember;

    SysLogAttributeSender(final Duration period,
                          final SyslogMessageSender sender,
                          final ModelOfAttributes<SysLogAttributeAccessor> attributes){
        super(period, attributes);
        this.messageSender = Objects.requireNonNull(sender);
        this.clusterMember = ClusterMember.get(Utils.getBundleContextOfObject(this));
    }

    private Logger getLogger(){
        return LoggerProvider.getLoggerForObject(this);
    }

    @Override
    public boolean accept(final String resourceName, final SysLogAttributeAccessor accessor) {
        if (clusterMember.isActive())
            try {
                messageSender.sendMessage(accessor.toMessage(resourceName));
                return true;
            } catch (final IOException e) {
                getLogger().log(Level.SEVERE, String.format("Failed to wrap attribute %s of resource %s into syslog message", accessor.getName(), resourceName), e);
            }
        return false;
    }
}
