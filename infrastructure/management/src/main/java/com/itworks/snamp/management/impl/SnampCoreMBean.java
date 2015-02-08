package com.itworks.snamp.management.impl;

import com.itworks.snamp.TimeSpan;
import com.itworks.snamp.management.AbstractSnampManager;
import com.itworks.snamp.management.jmx.FrameworkMBean;
import com.itworks.snamp.management.jmx.OpenMBean;
import org.osgi.service.log.LogEntry;
import org.osgi.service.log.LogListener;
import org.osgi.service.log.LogService;

import javax.management.openmbean.OpenDataException;
import java.util.Objects;
import java.util.logging.Logger;

/**
 * @author Roman Sakno
 * @version 1.0
 * @since 1.0
 * @TODO for temni: move from the webconsole all the methods related to attrubites
 */
final class SnampCoreMBean extends OpenMBean implements LogListener, FrameworkMBean {
    public static final String OBJECT_NAME = "com.itworks.snamp.management:type=SnampCore";
    public static final TimeSpan DEFAULT_RENEWAL_TIME = TimeSpan.fromSeconds(5);
    private final StatisticCounters counter;

    private SnampCoreMBean(final StatisticCounters counter, final AbstractSnampManager manager) throws OpenDataException{
        super(  new GetConnectorConfigurationSchemaOperation(manager),
                new LicenseAttribute(),
                new RestartOperation(),
                new GetAdapterConfigurationSchemaOperation(manager),
                new StatisticRenewalTimeAttribute(counter),
                new CountAttribute("FaultsCount", counter, LogService.LOG_ERROR),
                new CountAttribute("WarningMessagesCount", counter, LogService.LOG_WARNING),
                new CountAttribute("DebugMessagesCount", counter, LogService.LOG_DEBUG),
                new CountAttribute("InformationMessagesCount", counter, LogService.LOG_INFO),
                new LogEventNotification(),
                new InstalledComponents(manager));
        counter.start();
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
        return MonitoringUtils.getLogger();
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
        else if(Objects.equals(objectType, LogListener.class))
            return objectType.cast(this);
        else return null;
    }
}
