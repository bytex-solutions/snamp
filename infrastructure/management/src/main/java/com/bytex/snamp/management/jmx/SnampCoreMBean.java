package com.bytex.snamp.management.jmx;

import com.bytex.snamp.jmx.OpenMBean;
import com.bytex.snamp.management.AbstractSnampManager;
import com.bytex.snamp.management.FrameworkMBean;
import org.osgi.service.log.LogEntry;
import org.osgi.service.log.LogListener;
import org.osgi.service.log.LogService;

import javax.management.openmbean.OpenDataException;
import java.time.Duration;
import java.util.Objects;
import java.util.logging.Logger;

/**
 * @author Roman Sakno
 * @version 2.0
 * @since 1.0
 */
public final class SnampCoreMBean extends OpenMBean implements LogListener, FrameworkMBean {
    private static final String LOGGER_NAME = "com.bytex.snamp.management";
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
                new StartConnectorOperation(),
                new EnableGatewayOperation(),
                new StopConnectorOperation(),
                new DisableGatewayOperation());
        this.counter = counter;
    }

    public SnampCoreMBean() throws OpenDataException{
        this(new StatisticCounters(DEFAULT_RENEWAL_TIME), new SnampManagerImpl());
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
     * Gets logger associated with this service.
     *
     * @return The logger associated with this service.
     */
    @Override
    public Logger getLogger() {
        return getLoggerImpl();
    }

    static Logger getLoggerImpl() {
        return Logger.getLogger(LOGGER_NAME);
    }

    /**
     * Retrieves the aggregated object.
     *
     * @param objectType Type of the requested object.
     * @return An instance of the aggregated object; or {@literal null} if object is not available.
     */
    @Override
    public <T> T queryObject(final Class<T> objectType) {
        if(objectType == null) return null;
        else if(Objects.equals(objectType, Logger.class))
            return objectType.cast(getLogger());
        else if(objectType.isInstance(this))
            return objectType.cast(this);
        else return null;
    }

}
