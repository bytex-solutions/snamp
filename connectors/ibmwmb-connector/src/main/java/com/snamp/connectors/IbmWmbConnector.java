package com.snamp.connectors;

import com.ibm.broker.config.proxy.*;
import com.snamp.SimpleTable;

import java.beans.IntrospectionException;
import java.net.URI;
import java.util.*;


/*
 * In order to build this file you should locate your own copies of ibm java libs
 * When done, execute following commands
 *
 * # mvn install:install-file -Dfile=./ibmjsseprovider2.jar -DgroupId=com.ibm -DartifactId=JSSEProvider -Dversion=2.0.0 -Dpackaging=jar
 *
 * # mvn install:install-file -Dfile=./com.ibm.mq.commonservices.jar -DgroupId=com.ibm.mq -DartifactId=WebSphereCommonServices -Dversion=6.0.0.0 -Dpackaging=jar
 * # mvn install:install-file -Dfile=./com.ibm.mq.headers.jar -DgroupId=com.ibm.mq -DartifactId=WebSphereMQHeaders -Dversion=6.0.0.0 -Dpackaging=jar
 * # mvn install:install-file -Dfile=./com.ibm.mq.jar -DgroupId=com.ibm.mq -DartifactId=WebSphereMQ -Dversion=6.0.0.0 -Dpackaging=jar
 * # mvn install:install-file -Dfile=./com.ibm.mq.jmqi.jar -DgroupId=com.ibm.mq -DartifactId=WebSphereJMQI -Dversion=6.0.0.0 -Dpackaging=jar
 * # mvn install:install-file -Dfile=./connector.jar -DgroupId=javax.resource.cci -DartifactId=SunConnectorClasses -Dversion=1.3.0 -Dpackaging=jar
 * # mvn install:install-file -Dfile=./ConfigManagerProxy.jar -DgroupId=com.ibm.broker.config -DartifactId=WMBConfigManagerProxy -Dversion=1.5.0 -Dpackaging=jar
 */


/**
 * Connector class for IBM WebSphere Message Broker
 * exports some basic monitoring attributes of WMB
 *
 * @author Oleg Chernovsky
 *
 */
class IbmWmbConnector extends ManagementConnectorBean
{
    final static String NAME = "ibm-wmb";
    private final BrokerProxy mBrokerInstance;
    private final Map<String, String> mObjectFilter;
    private AdministeredObject mEntity = null;  // we cannot get it at the constructor :(

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
            }
            else
                throw new IllegalArgumentException("Cannot create IBM Connector: insufficient parameters!");
        } catch (Exception e) {
            throw new IntrospectionException(e.toString());
        }
    }

    /**
     * We should check if broker instance is already populated by underlying server
     * Otherwise we will get exceptions for all attributes
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

    final public String[] getActivityLogMessages() {
        try {
            final AdministeredObject entity = getAdministeredObject();
            if(entity instanceof MessageFlowProxy)
                ((MessageFlowProxy) entity).getActivityLog();

            final List<String> logMessages = new ArrayList<>();


            final ActivityLogProxy log = ((MessageFlowProxy) entity).getActivityLog();
            if(log != null)
                log.elements().nextElement().
                for(LogEntry log : logVector)
                    logMessages.add(log.toString());

            return logMessages.toArray(new String[logMessages.size()]); // type system knows about String[]

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
    @AttributeInfo(typeProvider = "createPropertiesMap")
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
        mBrokerInstance.disconnect();
    }
}