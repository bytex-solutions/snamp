package com.itworks.snamp.management.impl;

import com.google.common.collect.Maps;
import com.itworks.snamp.SafeConsumer;
import com.itworks.snamp.TimeSpan;
import com.itworks.snamp.configuration.ConfigurationEntityDescriptionProvider;
import com.itworks.snamp.internal.Utils;
import com.itworks.snamp.licensing.LicensingDescriptionService;
import com.itworks.snamp.management.Maintainable;
import com.itworks.snamp.management.SnampComponentDescriptor;
import com.itworks.snamp.management.SnampManager;
import com.itworks.snamp.management.jmx.FrameworkMBean;
import com.itworks.snamp.management.jmx.OpenMBean;
import org.osgi.service.log.LogEntry;
import org.osgi.service.log.LogListener;
import org.osgi.service.log.LogService;

import javax.management.openmbean.*;
import java.io.IOException;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

import static com.itworks.snamp.internal.Utils.getBundleContextByObject;

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

    private static final class GetConnectorConfigurationSchemaOperation extends OpenOperation<CompositeData, CompositeType>{
        private static final String NAME = "getConnectorConfigurationSchema";
        private static final OpenMBeanParameterInfo CONNECTOR_NAME_PARAM = new OpenMBeanParameterInfoSupport(
            "connectorName",
            "The name of the managed resource connector",
            SimpleType.STRING
        );
        private static final OpenMBeanParameterInfo LOCALE_PARAM = new OpenMBeanParameterInfoSupport(
                "locale",
                "The expected localization of the configuration schema",
                SimpleType.STRING);

        private final SnampManager manager;

        private GetConnectorConfigurationSchemaOperation(final SnampManager manager){
            super(NAME, SnampCoreMBeanUtils.COMPONENT_CONFIG_SCHEMA, CONNECTOR_NAME_PARAM, LOCALE_PARAM);
            this.manager = Objects.requireNonNull(manager);
        }

        private CompositeData invoke(final String connectorName, final String locale) throws OpenDataException{
            return SnampCoreMBeanUtils.getConnectorConfigurationSchema(manager, connectorName, locale);
        }

        @Override
        public CompositeData invoke(final Map<String, ?> arguments) throws OpenDataException {
            return invoke(
                    Utils.getProperty(arguments, CONNECTOR_NAME_PARAM.getName(), String.class),
                    Utils.getProperty(arguments, LOCALE_PARAM.getName(), String.class)
            );
        }
    }

    private static final class LicenseAttribute extends OpenAttribute<String, SimpleType<String>>{
        private static final String NAME = "license";

        private LicenseAttribute(){
            super(NAME, SimpleType.STRING);
        }

        @Override
        public String getValue() throws IOException {
            return LicenseManager.getLicenseContent(getBundleContextByObject(this));
        }

        @Override
        public void setValue(final String licenseContent) throws IOException {
            LicenseManager.setLicenseContent(getBundleContextByObject(this), licenseContent);
        }

        @Override
        protected String getDescription() {
            return "SNAMP Configuration";
        }
    }

    private static final class StatisticRenewalTimeAttribute extends OpenAttribute<Long, SimpleType<Long>>{
        private final StatisticCounters counter;
        private static final String NAME = "StatisticRenewalTime";

        private StatisticRenewalTimeAttribute(final StatisticCounters counter){
            super(NAME, SimpleType.LONG);
            this.counter = counter;
        }

        /**
         * Gets description of this attribute.
         *
         * @return The description of this attribute.
         */
        @Override
        protected String getDescription() {
            return "Renewal time for SNAMP statistics, in milliseconds. When renewal time comes then SNAMP resets all counters.";
        }

        @Override
        public Long getValue() {
            return counter.getRenewalTime().convert(TimeUnit.MILLISECONDS).duration;
        }

        @Override
        public void setValue(final Long value) {
            counter.setRenewalTime(new TimeSpan(value));
        }
    }

    private static final class CountAttribute extends OpenAttribute<Long, SimpleType<Long>>{
        private final StatisticCounters counter;
        private final int logLevel;

        private CountAttribute(final String attributeName,
                              final StatisticCounters counter,
                              final int logLevel){
            super(attributeName, SimpleType.LONG);
            this.counter = counter;
            this.logLevel = logLevel;
        }

        /**
         * Gets description of this attribute.
         *
         * @return The description of this attribute.
         */
        @Override
        protected String getDescription() {
            switch (logLevel){
                case LogService.LOG_ERROR: return "A number of faults occurred in SNAMP. Increasing of this value may be interpreted as SNAMP malfunction.";
                case LogService.LOG_WARNING: return "A number of alert messages received by OSGI logger for the last time.";
                case LogService.LOG_DEBUG: return "A number of debug messages received by OSGI logger for the last time. You may ignore this attribute.";
                default: return "A number of information messages received by OSGI logger for the last time.";
            }
        }

        @Override
        public Long getValue() {
            return counter.getValue(logLevel);
        }
    }

    private static final class InstalledComponents extends OpenAttribute<TabularData, TabularType>{
        private static final String NAME_COLUMN = "Name";
        private static final String DESCRIPTION_COLUMN = "Description";
        private static final String VERSION_COLUMN = "Version";
        private static final String BUNDLE_STATE_COLUMN = "State";
        private static final String IS_LICENSED_COLUMN = "IsCommerciallyLicensed";
        private static final String IS_MANAGEABLE_COLUMN = "IsManageable";
        private static final String IS_CONFIG_DESCR_AVAIL_COLUMN = "IsConfigurationDescriptionAvailable";
        private static final String[] COLUMNS = new String[]{
            NAME_COLUMN,
            DESCRIPTION_COLUMN,
            VERSION_COLUMN,
            BUNDLE_STATE_COLUMN,
            IS_LICENSED_COLUMN,
            IS_MANAGEABLE_COLUMN,
            IS_CONFIG_DESCR_AVAIL_COLUMN,
        };
        private static final String[] DESCRIPTIONS = new String[]{
            "Display name of SNAMP component",
            "Description of SNAMP component",
            "SNAMP component version",
            "State of the component inside of OSGI environment.",
            "SNAMP component is commercially licensed",
            "SNAMP component supports command-line interaction",
            "SNAMP component provides description of its configuration schema"
        };
        private static final OpenType<?>[] COLUMN_TYPES = new OpenType<?>[]{
            SimpleType.STRING,
            SimpleType.STRING,
            SimpleType.STRING,
            SimpleType.INTEGER,
            SimpleType.BOOLEAN,
            SimpleType.BOOLEAN,
            SimpleType.BOOLEAN
        };

        private static TabularType createTabularType() throws OpenDataException{
            final CompositeType rowType = new CompositeType("com.itworks.snamp.management.SnampComponent",
                    "SNAMP component descriptor",
                    COLUMNS,
                    DESCRIPTIONS,
                    COLUMN_TYPES);
            return new TabularType("com.itworks.snamp.management.SnampComponents",
                    "A set of SNAMP components", rowType, new String[]{NAME_COLUMN});
        }

        private final SnampManager manager;

        public InstalledComponents(final SnampManager manager) throws OpenDataException{
            super("InstalledComponents", createTabularType());
            this.manager = manager;
        }

        private CompositeData createRow(final SnampComponentDescriptor component) throws OpenDataException{
            final Map<String, Object> row = Maps.newHashMapWithExpectedSize(COLUMNS.length);
            row.put(NAME_COLUMN, component.getName(null));
            row.put(DESCRIPTION_COLUMN, component.getDescription(null));
            row.put(VERSION_COLUMN, Objects.toString(component.getVersion(), "0.0"));
            row.put(BUNDLE_STATE_COLUMN, component.getState());
            row.put(IS_MANAGEABLE_COLUMN, false);
            row.put(IS_LICENSED_COLUMN, false);
            row.put(IS_CONFIG_DESCR_AVAIL_COLUMN, false);
            try {
                component.invokeSupportService(Maintainable.class, new SafeConsumer<Maintainable>() {
                    @Override
                    public void accept(final Maintainable input) {
                        row.put(IS_MANAGEABLE_COLUMN, input != null);
                    }
                });
                component.invokeSupportService(ConfigurationEntityDescriptionProvider.class, new SafeConsumer<ConfigurationEntityDescriptionProvider>() {
                    @Override
                    public void accept(final ConfigurationEntityDescriptionProvider input) {
                        row.put(IS_CONFIG_DESCR_AVAIL_COLUMN, input != null);
                    }
                });
                component.invokeSupportService(LicensingDescriptionService.class, new SafeConsumer<LicensingDescriptionService>() {
                    @Override
                    public void accept(final LicensingDescriptionService input) {
                        row.put(IS_LICENSED_COLUMN, input != null);
                    }
                });
            }
            catch (final Exception e){
                MonitoringUtils.getLogger().log(Level.WARNING, e.getLocalizedMessage(), e);
            }
            return new CompositeDataSupport(openType.getRowType(), row);
        }

        @Override
        public TabularData getValue() throws OpenDataException{
            final TabularData result = new TabularDataSupport(openType);
            for(final SnampComponentDescriptor component: manager.getInstalledResourceAdapters())
                result.put(createRow(component));
            for(final SnampComponentDescriptor component: manager.getInstalledResourceConnectors())
                result.put(createRow(component));
            for(final SnampComponentDescriptor component: manager.getInstalledComponents())
                result.put(createRow(component));
            return result;
        }
    }

    private static final class LogEventNotification extends OpenNotification<LogEntry>{
        public static final String ERROR_NOTIF_TYPE = "itworks.snamp.monitoring.error";
        public static final String WARNING_NOTIF_TYPE  ="itworks.snamp.monitoring.warning";
        public static final String INFO_NOTIF_TYPE  = "itworks.snamp.monitoring.info";
        public static final String DEBUG_NOTIF_TYPE = "itworks.snamp.monitoring.debug";

        public LogEventNotification(){
            super("SnampLogEvent", LogEntry.class,
                    ERROR_NOTIF_TYPE,
                    WARNING_NOTIF_TYPE,
                    INFO_NOTIF_TYPE,
                    DEBUG_NOTIF_TYPE);
        }

        @SuppressWarnings("ThrowableResultOfMethodCallIgnored")
        @Override
        protected String getMessage(final LogEntry eventObject) {
            return eventObject.getException() != null ?
                    String.format("%s. Reason: %s", eventObject.getMessage(), eventObject.getException()):
                    eventObject.getMessage();
        }

        @Override
        protected String getType(final LogEntry eventObject) {
            switch (eventObject.getLevel()){
                case LogService.LOG_ERROR: return ERROR_NOTIF_TYPE;
                case LogService.LOG_DEBUG: return DEBUG_NOTIF_TYPE;
                case LogService.LOG_WARNING: return WARNING_NOTIF_TYPE;
                default: return INFO_NOTIF_TYPE;
            }
        }
    }

    private SnampCoreMBean(final StatisticCounters counter, final SnampManager manager) throws OpenDataException{
        super(  new GetConnectorConfigurationSchemaOperation(manager),
                new LicenseAttribute(),
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
