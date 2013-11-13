package com.snamp.connectors;

import com.ibm.broker.config.proxy.BrokerConnectionParameters;
import com.ibm.broker.config.proxy.BrokerProxy;
import com.ibm.broker.config.proxy.ConfigManagerProxyLoggedException;
import com.ibm.broker.config.proxy.MQBrokerConnectionParameters;
import com.snamp.TimeSpan;

import java.util.Map;
import java.util.concurrent.TimeoutException;


/**
 * In order to build this file you should locate your own copies of ibm java libs
 * When done, execute following commands
 *
 * # mvn install:install-file -Dfile=.\lib\com.ibm.mq.jar -DgroupId=com.ibm.mq -DartifactId=WebsphereMQClassesForJava -Dversion=6.0.2.2 -Dpackaging=jar
 * # mvn install:install-file -Dfile=.\lib\connector.jar -DgroupId=javax.resource.cci -DartifactId=SunConnectorClasses -Dversion=1.3.0 -Dpackaging=jar
 * # mvn install:install-file -Dfile=.\lib\ConfigManagerProxy.jar -DgroupId=com.ibm.broker.config -DartifactId=WMBConfigManagerProxy -Dversion=1.5.0 -Dpackaging=jar
 */
class IbmWmbConnector extends AbstractManagementConnector
{
    BrokerProxy mBrokerInstance;
    /**
     * Represents IBM MQ connector name.
     */
    public static final String connectorName = "ibmwmb";

    public IbmWmbConnector(Map<String, String> env) throws ConfigManagerProxyLoggedException
    {
        if(env.containsKey("host") && env.containsKey("port") && env.containsKey("qmgr"))
        {
            BrokerConnectionParameters bcp = new MQBrokerConnectionParameters(env.get("host"), Integer.valueOf(env.get("port")), env.get("qmgr"));
            // that's blocking call. Does it make sense?
            mBrokerInstance = BrokerProxy.getInstance(bcp);
        }
        else
            throw new IllegalArgumentException("Cannot create IBM Connector: insufficient parameters!");
    }

    /**
     * Throws an exception if the connector is not initialized.
     */
    @Override
    protected void verifyInitialization() //throws Exception
    {
        //Class.forName("com.ibm.broker.config.proxy.BrokerProxy");
    }

    @Override
    protected GenericAttributeMetadata<?> connectAttributeCore(String attributeName, Map<String, String> options)
    {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    protected Object getAttributeValue(AttributeMetadata attribute, TimeSpan readTimeout, Object defaultValue) throws TimeoutException
    {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    protected boolean setAttributeValue(AttributeMetadata attribute, TimeSpan writeTimeout, Object value)
    {
        return false;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public Object doAction(String actionName, Arguments args, TimeSpan timeout) throws UnsupportedOperationException, TimeoutException
    {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void close() throws Exception
    {
        if(mBrokerInstance != null)
            mBrokerInstance.disconnect();
    }
}