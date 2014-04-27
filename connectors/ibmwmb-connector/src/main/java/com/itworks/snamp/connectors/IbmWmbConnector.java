package com.itworks.snamp.connectors;

import com.ibm.broker.config.proxy.*;
import com.itworks.snamp.Repeater;
import com.itworks.snamp.SimpleTable;
import com.itworks.snamp.TimeSpan;

import java.beans.IntrospectionException;
import java.net.URI;
import java.util.*;
import java.util.concurrent.TimeUnit;

/**
 * Connector class for IBM WebSphere Message Broker
 * exports some basic monitoring managementAttributes of WMB
 *
 * @author Oleg Chernovsky
 *
 */
class IbmWmbConnector extends ManagementConnectorBean
{
    private final static String LOG_CHANGED_NOTIFICATION = "com.snamp.ibm.wmb.log.changed";
    private final static String ACTIVITY_LOG_CHANGED_NOTIFICATION = "com.snamp.ibm.wmb.activity.log.changed";

    final static String NAME = "ibm-wmb";
    private final BrokerProxy mBrokerInstance;
    private final Map<String, String> mObjectFilter;
    private AdministeredObject mEntity = null;  // we cannot get it at the constructor :(
    private final UpdateChecker notificationListener;

    private final class UpdateChecker extends Repeater {
        private Date lastLogDate = null;
        /**
         * Initializes a new repeater.
         *
         * @throws IllegalArgumentException period is {@literal null}.
         */
        protected UpdateChecker() {
            super(new TimeSpan(5, TimeUnit.SECONDS));
        }

        @Override
        protected void doAction() {
            try {
                verifyInitialization(); // assure we have working Broker copy

                final AdministeredObject entity = getAdministeredObject();
                parseLogs(entity);

                if(entity instanceof MessageFlowProxy) { // additional infos
                    parseLogs((MessageFlowProxy) entity);
                }

            } catch (ConfigManagerProxyPropertyNotInitializedException | ConfigManagerProxyLoggedException e) {
                // ignore
            }
        }

        private void parseLogs(final AdministeredObject entity) {
            final List<LogEntry> logsToNotifyAbout = new ArrayList<>();
            Date maxLogTimestamp = null; // intermediate evaluation of max log timestamp

            for(LogEntry log : entity.getLastBIPMessages()) {
                if(maxLogTimestamp == null)
                    maxLogTimestamp = log.getTimestamp();

                if(lastLogDate == null || log.getTimestamp().compareTo(lastLogDate) > 0) // date of log is greater
                    logsToNotifyAbout.add(log);

                maxLogTimestamp = new Date(Math.max(maxLogTimestamp.getTime(), log.getTimestamp().getTime()));
            }

            for(LogEntry log : logsToNotifyAbout)
                emitNotification(LOG_CHANGED_NOTIFICATION, log.isErrorMessage() ? Notification.Severity.ERROR : Notification.Severity.INFO, log.toString(), null);

            if(maxLogTimestamp != null) // predict case with no logs available
                lastLogDate.setTime(maxLogTimestamp.getTime());
        }

        private void parseLogs(final MessageFlowProxy entity) throws ConfigManagerProxyLoggedException, ConfigManagerProxyPropertyNotInitializedException {
            final List<ActivityLogEntry> logsToNotifyAbout = new ArrayList<>();
            Date maxLogTimestamp = null; // intermediate evaluation of max log timestamp
            final Enumeration<ActivityLogEntry> logsEnum = entity.getActivityLog().elements();

            while(logsEnum.hasMoreElements()) {
                final ActivityLogEntry log = logsEnum.nextElement();
                if(maxLogTimestamp == null)
                    maxLogTimestamp = log.getTimestamp();

                if(lastLogDate == null || log.getTimestamp().compareTo(lastLogDate) > 0) // date of log is greater
                    logsToNotifyAbout.add(log);

                maxLogTimestamp = new Date(Math.max(maxLogTimestamp.getTime(), log.getTimestamp().getTime()));
            }

            for(ActivityLogEntry log : logsToNotifyAbout)
                emitNotification(ACTIVITY_LOG_CHANGED_NOTIFICATION, log.isErrorMessage() ? Notification.Severity.ERROR : Notification.Severity.INFO, log.toString(), null);

            if(maxLogTimestamp != null) // predict case with no logs available
                lastLogDate.setTime(maxLogTimestamp.getTime());
        }
    }

    /**
     * Initializes a new management connector for IBM WMB.
     *
     * @throws IllegalArgumentException
     */
    public IbmWmbConnector(String connectionString, Map<String, String> env) throws IntrospectionException {
        super(new IbmWmbTypeSystem());
        try {
            final URI address = URI.create(connectionString);
            if(address.getScheme().equals("wmb"))
            {
                mObjectFilter = env;
                final BrokerConnectionParameters bcp = new MQBrokerConnectionParameters(address.getHost(), address.getPort(), address.getPath().substring(1));
                mBrokerInstance = BrokerProxy.getInstance(bcp);

                notificationListener = new UpdateChecker();
            }
            else
                throw new IllegalArgumentException("Cannot create IBM Connector: insufficient parameters!");
        } catch (Exception e) {
            throw new IntrospectionException(e.toString());
        }
    }

    /**
     * We should check if broker instance is already populated by underlying server
     * Otherwise we will get exceptions for all managementAttributes
     *
     */
    @Override
    protected void verifyInitialization() {
        super.verifyInitialization();
        while(!mBrokerInstance.hasBeenPopulatedByBroker())
            Thread.yield();

        /* if(!mBrokerInstance.hasBeenPopulatedByBroker()) // still not initialized
            throw new IllegalStateException("Broker instance is not populated, please wait!"); */
    }

    private AdministeredObject getAdministeredObject() throws ConfigManagerProxyPropertyNotInitializedException {
        if(mEntity != null)
            return mEntity;

        if(mObjectFilter != null && mObjectFilter.containsKey("executionGroup")) {
            mEntity = mBrokerInstance.getExecutionGroupByName(mObjectFilter.get("executionGroup"));
            if(mEntity == null)
                throw new IllegalArgumentException("No such execution group in this broker instance!");
            else {
                if(mObjectFilter.containsKey("application")) {
                    mEntity = ((ExecutionGroupProxy) mEntity).getApplicationByName(mObjectFilter.get("application"));
                    if(mEntity == null)
                        throw new IllegalArgumentException("No such application in this execution group!");
                    else {
                        if(mObjectFilter.containsKey("messageFlow")) {
                            mEntity = ((ApplicationProxy) mEntity).getMessageFlowByName(mObjectFilter.get("messageFlow"));
                            if(mEntity == null)
                                throw new IllegalArgumentException("No such message flow in this application!");
                            else
                                return mEntity; // entity is Message Flow
                        }
                        else
                            return mEntity;  // entity is Application
                    }
                }
                else
                    return mEntity;  // entity is Execution Group
            }

        }
        else {
            mEntity = mBrokerInstance;
            return mEntity; // entity is Broker
        }
    }

    /**
     * Function that retrieves name of currently looked at administered object
     *
     * @return name of currently monitored object
     */
    final public String getName() {
        try {
            final AdministeredObject entity = getAdministeredObject();
            return entity.getName();
        } catch (ConfigManagerProxyPropertyNotInitializedException e) {
            return null;
        }
    }

    /**
     * Function that retrieves description of currently looked at administered object
     * Often there is no description given
     *
     * @return description of currently monitored object
     */
    final public String getDescription() {
        try {
            final AdministeredObject entity = getAdministeredObject();
            return entity.getShortDescription();
        } catch (ConfigManagerProxyPropertyNotInitializedException e) {
            return null;
        }
    }

    /**
     * Function that retrieves UUID of currently looked at administered object
     * UUIDs are unique within broker instance
     *
     * @return UUID of currently monitored object
     */
    final public String getUUID() {
        try {
            final AdministeredObject entity = getAdministeredObject();
            return entity.getUUID();
        } catch (ConfigManagerProxyPropertyNotInitializedException e) {
            return null;
        }
    }

    /**
     * Function that retrieves type of currently looked at administered object
     * Types ex: "Message Flow", "Execution Group", "Application"
     *
     * @return type of currently monitored object
     */
    final public String getType() {
        try {
            final AdministeredObject entity = getAdministeredObject();
            return entity.getType();
        } catch (ConfigManagerProxyPropertyNotInitializedException e) {
            return null;
        }
    }

    /**
     * Retrieves running children and formats its names as array
     * of strings.
     *
     * Broker children are EGs
     * EG children are applications
     * App children are message flows/libraries
     * and so on
     *
     * @return Array of names of currently running children
     */
    final public String[] getRunningChildrenNames() {
        try {
            final List<String> subcomponentsNames = new ArrayList<>();
            final AdministeredObject entity = getAdministeredObject();
            final Properties filterRunning = new Properties();

            filterRunning.setProperty(AttributeConstants.OBJECT_RUNSTATE_PROPERTY, AttributeConstants.OBJECT_RUNSTATE_RUNNING);
            final Enumeration<AdministeredObject> subcomponentsEnum = entity.getManagedSubcomponents(filterRunning);

            while(subcomponentsEnum.hasMoreElements())
                subcomponentsNames.add(subcomponentsEnum.nextElement().getName()); // just convert to list

            return subcomponentsNames.toArray(new String[subcomponentsNames.size()]); // type system knows about String[]

        } catch (ConfigManagerProxyPropertyNotInitializedException e) {
            return null;
        }
    }

    /**
     * Retrieves array of string representation of last logs
     * These are set when a previously submitted action completes and the response is received by the IBM Integration API (CMP).
     *
     * @return Array of string representation of last logs
     */
    final public String[] getLogMessages() {
        try {
            final List<String> logMessages = new ArrayList<>();
            final AdministeredObject entity = getAdministeredObject();

            final Vector<LogEntry> logVector = entity.getLastBIPMessages();
            if(logVector != null)
                for(LogEntry log : logVector)
                    logMessages.add(log.toString());

            return logMessages.toArray(new String[logMessages.size()]); // type system knows about String[]

        } catch (ConfigManagerProxyPropertyNotInitializedException e) {
            return null;
        }
    }

    /**
     * Represents last activity log messages
     *
     * Activity log exists only for message flow proxy objects, so it won't work if administering
     * anything other.
     *
     * @return string array of activity log messages
     */
    final public String[] getActivityLogMessages() {
        try {
            final AdministeredObject entity = getAdministeredObject();
            if(entity instanceof MessageFlowProxy)
            {
                final List<String> logMessages = new ArrayList<>();
                final ActivityLogProxy log = ((MessageFlowProxy) entity).getActivityLog();
                if(log != null)
                    for(int i = 0; i < log.getSize(); ++i)
                        logMessages.add(log.getLogEntry(i).toString());

                return logMessages.toArray(new String[logMessages.size()]); // type system knows about String[]
            }
            else
                return null;

        } catch (ConfigManagerProxyPropertyNotInitializedException | ConfigManagerProxyLoggedException e) {
            return null;
        }
    }

    /**
     * Function that retrieves last update time of currently looked at administered object
     *
     * @return calendar instance with last update time of currently monitored object
     */
    final public Calendar getLastUpdateTime() {
        try {
            final AdministeredObject entity = getAdministeredObject();
            return entity.getTimeOfLastUpdate();

        } catch (ConfigManagerProxyPropertyNotInitializedException e) {
            return null;
        }
    }

    /**
     * Function that retrieves table of all defined broker parameters
     * Note that this includes both simple properties and advanced properties
     *
     * @return properties hashtable
     * @see SimpleTable
     */
    @ManagementAttribute(typeProvider = "createPropertiesMap")
    final public Properties getProperties() {
        try {
            final AdministeredObject entity = getAdministeredObject();
            return entity.getProperties();

        } catch (ConfigManagerProxyPropertyNotInitializedException e) {
            return null;
        }
    }

    @Override
    public void close() throws Exception {
        super.close();
        notificationListener.stop(new TimeSpan(5, TimeUnit.SECONDS));
        mBrokerInstance.disconnect();
    }
}