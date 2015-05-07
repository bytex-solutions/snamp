package com.itworks.snamp.connectors.wmq;

import com.ibm.mq.MQEnvironment;
import com.ibm.mq.MQException;
import com.ibm.mq.MQQueueManager;
import com.ibm.mq.pcf.PCFMessageAgent;

import java.net.URI;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * Represents MQ connection properties. This class cannot be inherited.
 * @author Roman Sakno
 * @version 1.0
 * @since 1.0
 */
final class MQConnectionProperties extends HashMap<String, String> {
    /**
     * Represents WebSphere MQ scheme name.
     */
    public static final String SCHEME = "wsmq";

    private static final long serialVersionUID = -4622612002025780031L;

    private final String hostName;
    private final int port;
    private final String userID;
    private final String password;
    private final String channelName;
    private final String queueName;

    MQConnectionProperties(final URI connectionString, final Map<String, String> options){
        super(options);
        if(connectionString == null)
            throw new IllegalArgumentException("connectionString is null.");
        else if(!Objects.equals(connectionString.getScheme(), SCHEME))
            throw new IllegalArgumentException(String.format("Incorrect URI scheme. %s expected.", SCHEME));
        //host name
        hostName = connectionString.getHost();
        //port
        port = MQConnectorConfigurationDescriptor.getPort(connectionString);
        //credentials
        final String credentials = connectionString.getUserInfo();
        if(credentials != null && credentials.length() > 0){
            final String[] pair = credentials.split(":", 1);
            switch (pair.length){
                case 2: userID = pair[0]; password = pair[1]; break;
                default: userID = password = ""; break;
            }
        }
        else
            userID = password = "";
        String path = connectionString.getPath();
        if(path.length() < 2) throw new IllegalArgumentException("Channel name and queue name expected");
        else path = path.substring(1);
        final String[] pathComponents = path.split("\\/");
        switch (pathComponents.length){
            case 3:
                //channel name
                channelName = pathComponents[0];
                //queue name
                queueName = pathComponents[1];
                break;
            default: throw new IllegalArgumentException("Invalid specification of channel and queue name.");
        }
    }

    public MQConnectionProperties(final String connectionString, final Map<String, String> options){
        this(URI.create(connectionString), options);
    }

    public String getQueueManagerName(){
        return MQConnectorConfigurationDescriptor.getQueueManagerName(this);
    }

    public int getPort() {
        return port;
    }

    public String getHostName(){
        return hostName;
    }

    public String getUserID() {
        return userID;
    }

    public String getPassword() {
        return password;
    }

    public String getChannelName() {
        return channelName;
    }

    public String getQueueName() {
        return queueName;
    }

    /**
     * Overwrites fields in the {@link MQEnvironment} class.
     * @see MQEnvironment
     */
    private void setupEnvironment(){
        MQEnvironment.port = port;
        MQEnvironment.channel = channelName;
        MQEnvironment.userID = userID;
        MQEnvironment.password = password;
    }

    /**
     * Creates a new queue manager based on properties from this instance.
     * @return A new queue manager based on properties from this instance.
     * @throws MQException Queue manager cannot be instantiated.
     */
    public MQQueueManager createQueueManager() throws MQException {
        return new MQQueueManager(getQueueManagerName());
    }

    public PCFMessageAgent createMessageAgent() throws MQException{
        synchronized (MQEnvironment.class){   //synchronization required because MQEnvironment provides static fields only
            setupEnvironment();
            final PCFMessageAgent agent = new PCFMessageAgent();
            agent.connect(createQueueManager());
            return agent;
        }
    }
}
