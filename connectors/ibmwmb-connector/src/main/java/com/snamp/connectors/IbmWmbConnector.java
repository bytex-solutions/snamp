package com.snamp.connectors;

import com.ibm.broker.config.proxy.*;

import java.beans.IntrospectionException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.Map;


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
    private BrokerProxy mBrokerInstance;

    /**
     * Initializes a new management connector for IBM WMB.
     *
     * @param typeBuilder Type information provider that provides property type converter.
     * @throws IllegalArgumentException
     */
    public IbmWmbConnector(Map<String, String> env, EntityTypeInfoFactory typeBuilder) throws IntrospectionException {
        super(typeBuilder);
        try {
            if(env.containsKey("host") && env.containsKey("port") && env.containsKey("qmgr"))
            {
                final BrokerConnectionParameters bcp = new MQBrokerConnectionParameters(env.get("host"), Integer.valueOf(env.get("port")), env.get("qmgr"));
                // that's blocking call. Does it make sense?
                mBrokerInstance = BrokerProxy.getInstance(bcp);
                while(!mBrokerInstance.hasBeenPopulatedByBroker())
                    Thread.yield();
            }
            else
                throw new IllegalArgumentException("Cannot create IBM Connector: insufficient parameters!");
        } catch (ConfigManagerProxyLoggedException e) {
            throw new IntrospectionException(e.toString());
        }
    }

    /**
     * Retrieves list of execution group from this broker instance. Broker must be initialized already.
     * Throws error otherwise
     *
     * @return list of currently known execution groups for current broker instance
     * @throws ConfigManagerProxyPropertyNotInitializedException
     */
    private List<ExecutionGroupProxy> retrieveExecutionGroups() throws ConfigManagerProxyPropertyNotInitializedException {
        final List<ExecutionGroupProxy> mExecutionGroups = new ArrayList<>();

        final Enumeration<ExecutionGroupProxy> egIterator = mBrokerInstance.getExecutionGroups(null);
        while(egIterator.hasMoreElements())
            mExecutionGroups.add(egIterator.nextElement());

        return mExecutionGroups;
    }

    /**
     * Retrieves list of applications of all execution groups from this broker instance. Broker must be initialized already.
     * Throws error otherwise
     *
     * @return list of currently known applications for current broker instance
     * @throws ConfigManagerProxyPropertyNotInitializedException
     */
    private List<ApplicationProxy> retrieveRunningApps() throws ConfigManagerProxyPropertyNotInitializedException {
        final List<ApplicationProxy> mApplications = new ArrayList<>();

        for(ExecutionGroupProxy eg : retrieveExecutionGroups())
        {
            Enumeration<ApplicationProxy> appIterator = eg.getApplications(null);
            while(appIterator.hasMoreElements())
                mApplications.add(appIterator.nextElement());
        }

        return mApplications;
    }

    /**
     * Retrieves list of log entries from this broker instance. Broker must be initialized already.
     * Throws error otherwise
     *
     * @return list of log entries for current broker instance
     * @throws ConfigManagerProxyPropertyNotInitializedException
     */
    private List<LogEntry> retrieveLogEntries() throws ConfigManagerProxyPropertyNotInitializedException {
        final List<LogEntry> mLogs = new ArrayList<>();

        final Enumeration<LogEntry> logsIterator = mBrokerInstance.getLog().elements();
        while(logsIterator.hasMoreElements())
            mLogs.add(logsIterator.nextElement());

        return mLogs;
    }

    /**
     * Getter for attribute that holds current execution group count
     * @return count of known execution groups
     */
    final public Integer getExecutionGroupCount() {
        try {
            return retrieveExecutionGroups().size();
        } catch (ConfigManagerProxyPropertyNotInitializedException e) {
            return null;
        }
    }

    /**
     * Getter for attribute that holds current applications count for all execution groups
     * @return count of known applications
     */
    final public Integer getApplicationsCount() {
        try {
            return retrieveRunningApps().size();
        } catch (ConfigManagerProxyPropertyNotInitializedException e) {
            return null;
        }
    }

    /**
     * Getter for attribute that holds current error count
     * @return count of errors in log entries
     */
    final public Integer getErrorsCount() {
        Integer count = 0;
        try {
            final List<LogEntry> mLogs = retrieveLogEntries();
            for(LogEntry entry : mLogs)
                if(entry.isErrorMessage())
                    count++;
        } catch (ConfigManagerProxyPropertyNotInitializedException e) {
            return null;
        }
        return count;
    }

    /**
     * Getter for attribute that holds last error message
     * @return last error message as a string
     */
    final public String getLastErrorMessage()
    {
        final List<LogEntry> mLogs;
        try
        {
            mLogs = retrieveLogEntries();
            for(int i = mLogs.size() - 1; i >= 0; ++i)
                if(mLogs.get(i).isErrorMessage())
                    return mLogs.get(i).getMessage();
        } catch (ConfigManagerProxyPropertyNotInitializedException ignored) {}

        return "";
    }

    /**
     * Getter for attribute that holds current running execution group count
     * @return count of known running execution groups
     */
    final public Integer getRunningExecutionGroupCount() {
        Integer count = 0;
        try {
            for(ExecutionGroupProxy group : retrieveExecutionGroups())
                if(group.isRunning())
                    count++;
        } catch (ConfigManagerProxyPropertyNotInitializedException e) {
            return null;
        }
        return count;
    }

    /**
     * Getter for attribute that holds current running applications count
     * @return count of known running applications
     */
    final public Integer getRunningApplicationsCount() {
        Integer count = 0;
        try {
            for(ApplicationProxy app : retrieveRunningApps())
                if(app.isRunning())
                    count++;
        } catch (ConfigManagerProxyPropertyNotInitializedException e) {
            return -1;
        }
        return count;
    }
}