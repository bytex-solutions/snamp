package com.snamp.connectors;

import com.ibm.broker.config.proxy.*;

import java.beans.IntrospectionException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.Map;


/**
 * In order to build this file you should locate your own copies of ibm java libs
 * When done, execute following commands
 *
 * # mvn install:install-file -Dfile=.\lib\com.ibm.mq.jar -DgroupId=com.ibm.mq -DartifactId=WebsphereMQClassesForJava -Dversion=6.0.2.2 -Dpackaging=jar
 * # mvn install:install-file -Dfile=.\lib\connector.jar -DgroupId=javax.resource.cci -DartifactId=SunConnectorClasses -Dversion=1.3.0 -Dpackaging=jar
 * # mvn install:install-file -Dfile=.\lib\ConfigManagerProxy.jar -DgroupId=com.ibm.broker.config -DartifactId=WMBConfigManagerProxy -Dversion=1.5.0 -Dpackaging=jar
 */
class IbmWmbConnector extends ManagementConnectorBean
{
    private BrokerProxy mBrokerInstance;
    private List<ExecutionGroupProxy> mExecutionGroups = new ArrayList<>();
    private List<ApplicationProxy> mApplications = new ArrayList<>();
    private List<LogEntry> mLogs = new ArrayList<>();


    /**
     * Initializes a new management connector.
     *
     * @param typeBuilder Type information provider that provides property type converter.
     * @throws IllegalArgumentException
     *          typeBuilder is {@literal null}.
     */
    protected IbmWmbConnector(EntityTypeInfoFactory typeBuilder) throws IntrospectionException {
        super(typeBuilder);
    }

    public IbmWmbConnector(Map<String, String> env, EntityTypeInfoFactory typeBuilder) throws IntrospectionException {
        super(typeBuilder);
        try {
            if(env.containsKey("host") && env.containsKey("port") && env.containsKey("qmgr"))
            {
                BrokerConnectionParameters bcp = new MQBrokerConnectionParameters(env.get("host"), Integer.valueOf(env.get("port")), env.get("qmgr"));
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

    private void retrieveExecutionGroups() throws ConfigManagerProxyPropertyNotInitializedException {
        mExecutionGroups.clear();
        Enumeration<ExecutionGroupProxy> egIterator = mBrokerInstance.getExecutionGroups(null);
        while(egIterator.hasMoreElements())
            mExecutionGroups.add(egIterator.nextElement());
    }

    private void retrieveRunningApps() throws ConfigManagerProxyPropertyNotInitializedException {
        mApplications.clear();
        for(ExecutionGroupProxy eg : mExecutionGroups)
        {
            Enumeration<ApplicationProxy> appIterator = eg.getApplications(null);
            while(appIterator.hasMoreElements())
                mApplications.add(appIterator.nextElement());
        }
    }

    private void retrieveLogEntries() throws ConfigManagerProxyPropertyNotInitializedException {
        mLogs.clear();
        Enumeration<LogEntry> logsIterator = mBrokerInstance.getLog().elements();
        while(logsIterator.hasMoreElements())
            mLogs.add(logsIterator.nextElement());
    }

    public Integer getExecutionGroupCount() {
        return mExecutionGroups.size();
    }

    // Request inputs count
    public Integer getApplicationsCount() {
        return mApplications.size();
    }

    public Integer getErrorsCount() {
        Integer count = 0;
        for(LogEntry entry : mLogs)
            if(entry.isErrorMessage())
                count++;

        return count;
    }

    public String getLastErrorMessage()
    {
        for(int i = mLogs.size() - 1; i >= 0; ++i)
            if(mLogs.get(i).isErrorMessage())
                return mLogs.get(i).getMessage();

        return "";
    }

    public Integer getRunningExecutionGroupCount() {
        Integer count = 0;
        try {
            for(ExecutionGroupProxy group : mExecutionGroups)
                if(group.isRunning())
                    count++;
        } catch (ConfigManagerProxyPropertyNotInitializedException e) {
            return -1;
        }
        return count;
    }

    // Free request inputs count
    public Integer getRunningApplicationsCount() {
        Integer count = 0;
        try {
            for(ApplicationProxy app : mApplications)
                if(app.isRunning())
                    count++;
        } catch (ConfigManagerProxyPropertyNotInitializedException e) {
            return -1;
        }
        return count;
    }
}