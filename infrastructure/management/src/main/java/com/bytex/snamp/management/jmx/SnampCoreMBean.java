package com.bytex.snamp.management.jmx;

import com.bytex.snamp.core.AbstractSnampManager;
import com.bytex.snamp.jmx.FrameworkMBean;
import com.bytex.snamp.jmx.OpenMBean;
import com.bytex.snamp.management.DefaultSnampManager;
import org.osgi.service.log.LogEntry;
import org.osgi.service.log.LogListener;
import org.osgi.service.log.LogService;

import javax.annotation.Nonnull;
import javax.management.openmbean.OpenDataException;
import java.time.Duration;

/**
 * @author Roman Sakno
 * @version 2.0
 * @since 1.0
 */
public final class SnampCoreMBean extends OpenMBean implements LogListener, FrameworkMBean {

    public static final String OBJECT_NAME = "com.bytex.snamp.management:type=SnampCore";
    public static final Duration DEFAULT_RENEWAL_TIME = Duration.ofSeconds(5);
    private final StatisticCounters counter;

    private SnampCoreMBean(final StatisticCounters counter, final AbstractSnampManager manager) throws OpenDataException{
        super(  new SummaryMetricsAttribute(),
                new MetricsAttribute(),
                new ResetMetricsOperation(),
                new PlatformVersionAttribute(),
                new RestartOperation(),
                new StatisticRenewalTimeAttribute(counter),
                new LogEventCountAttribute("FaultsCount", counter, LogService.LOG_ERROR),
                new LogEventCountAttribute("WarningMessagesCount", counter, LogService.LOG_WARNING),
                new LogEventCountAttribute("DebugMessagesCount", counter, LogService.LOG_DEBUG),
                new LogEventCountAttribute("InformationMessagesCount", counter, LogService.LOG_INFO),
                new LogEventNotification(),
                new InstalledComponents(manager),
                new InstalledGatewaysAttribute(),
                new InstalledConnectorsAttribute(),
                new EnableConnectorOperation(),
                new EnableGatewayOperation(),
                new DisableConnectorOperation(),
                new DisableGatewayOperation());
        this.counter = counter;
    }

    public SnampCoreMBean() throws OpenDataException{
        this(new StatisticCounters(DEFAULT_RENEWAL_TIME), new DefaultSnampManager());
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
        sendNotification(LogEventNotification.class, entry);
    }

    /**
     * Retrieves the aggregated object.
     *
     * @param objectType Type of the requested object.
     * @return An instance of the aggregated object; or {@literal null} if object is not available.
     */
    @Override
    public <T> T queryObject(@Nonnull final Class<T> objectType) {
        return objectType.isInstance(this) ? objectType.cast(this) : null;
    }
}
