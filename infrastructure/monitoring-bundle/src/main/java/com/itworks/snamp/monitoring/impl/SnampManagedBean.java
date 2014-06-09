package com.itworks.snamp.monitoring.impl;

import com.itworks.snamp.TimeSpan;
import com.itworks.snamp.internal.Utils;
import com.itworks.snamp.management.SnampComponentDescriptor;
import com.itworks.snamp.management.SnampManager;
import org.apache.commons.collections4.Closure;
import org.osgi.framework.Filter;
import org.osgi.service.log.LogEntry;
import org.osgi.service.log.LogListener;
import org.osgi.service.log.LogService;

import javax.management.NotificationBroadcasterSupport;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

/**
 * @author Roman Sakno
 * @version 1.0
 * @since 1.0
 */
final class SnampManagedBean extends NotificationBroadcasterSupport implements SnampCommonsMXBean, LogListener {
    public static final TimeSpan DEFAULT_RENEWAL_TIME = TimeSpan.fromSeconds(5);
    private final StatisticCounters counter;
    private final AtomicLong notificationSeqnum;

    public SnampManagedBean(){
        super(OsgiLogEventNotification.createNotificationInfo());
        counter = new StatisticCounters(DEFAULT_RENEWAL_TIME);
        counter.start();
        notificationSeqnum = new AtomicLong(0L);
    }

    @Override
    public long getStatisticRenewalTime() {
        return counter.getRenewalTime().convert(TimeUnit.MILLISECONDS).duration;
    }

    @Override
    public void setStatisticRenewalTime(final long value) {
        counter.setRenewalTime(new TimeSpan(value));
    }

    @Override
    public long getFaultsCount() {
        return counter.getValue(LogService.LOG_ERROR);
    }

    @Override
    public long getWarningMessagesCount() {
        return counter.getValue(LogService.LOG_WARNING);
    }

    @Override
    public long getDebugMessagesCount() {
        return counter.getValue(LogService.LOG_DEBUG);
    }

    @Override
    public long getInformationMessagesCount() {
        return counter.getValue(LogService.LOG_INFO);
    }

    /**
     * Listener method called for each LogEntry object created.
     * <p/>
     * <p/>
     * As with all event listeners, this method should return to its caller as
     * soon as possible.
     *
     * @param entry A {@code LogEntry} object containing log information.
     * @see org.osgi.service.log.LogEntry
     */
    @Override
    public void logged(final LogEntry entry) {
        counter.increment(entry.getLevel());
        //fire notification
        sendNotification(OsgiLogEventNotification.create(entry, this, notificationSeqnum.getAndIncrement()));
    }

    @Override
    public Map<String, Integer> getInstalledConnectors() {
        final Map<String, Integer> result = new HashMap<>(5);
        Utils.processExposedService(getClass(), SnampManager.class, (Filter)null, new Closure<SnampManager>() {
            @Override
            public void execute(final SnampManager input) {
                for(final SnampComponentDescriptor descriptor: input.getInstalledResourceConnectors())
                    result.put(descriptor.getName(null), descriptor.getState());
            }
        });
        return result;
    }

    @Override
    public Map<String, Integer> getInstalledAdapters() {
        final Map<String, Integer> result = new HashMap<>(5);
        Utils.processExposedService(getClass(), SnampManager.class, (Filter)null, new Closure<SnampManager>() {
            @Override
            public void execute(final SnampManager input) {
                for(final SnampComponentDescriptor descriptor: input.getInstalledResourceAdapters())
                    result.put(descriptor.getName(null), descriptor.getState());
            }
        });
        return result;
    }

    @Override
    public Map<String, Integer> getInstalledComponents() {
        final Map<String, Integer> result = new HashMap<>(5);
        Utils.processExposedService(getClass(), SnampManager.class, (Filter)null, new Closure<SnampManager>() {
            @Override
            public void execute(final SnampManager input) {
                for(final SnampComponentDescriptor descriptor: input.getInstalledComponents())
                    result.put(descriptor.getName(null), descriptor.getState());
            }
        });
        return result;
    }
}
