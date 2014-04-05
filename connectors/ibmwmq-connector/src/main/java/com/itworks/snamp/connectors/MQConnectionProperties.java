package com.itworks.snamp.connectors;

import com.ibm.mq.*;
import com.ibm.mq.pcf.PCFMessageAgent;
import static com.itworks.snamp.configuration.IbmWmqConnectorConfigurationDescriptor.*;

import java.net.URI;
import java.util.*;

/**
 * Represents MQ connection properties. This class cannot be inherited.
 * @author Roman Sakno
 * @version 1.0
 * @since 1.0
 */
final class MQConnectionProperties {
    /**
     * Represents WebSphere MQ scheme name.
     */
    public static final String SCHEME = "wsmq";

    /**
     * Represents default port of Queue Manager.
     */
    public static final int DEFAULT_PORT = 1414;

    /**
     * Represents default MQ host (localhost).
     */
    public static final String DEFAULT_HOST = "127.0.0.1";

    private final String hostName;
    private final int port;
    private final String userID;
    private final String password;
    private final String channelName;
    private final String queueName;
    private final Map<String, String> options;

    public MQConnectionProperties(final String hostName,
                                  final int port,
                                  final String channelName,
                                  final String queueName,
                                  final String userID,
                                  final String password,
                                  final Map<String, String> options){
        this.hostName = hostName == null || hostName.isEmpty() ? DEFAULT_HOST : hostName;
        this.port = port > 0 ? port : DEFAULT_PORT;
        this.channelName = channelName == null ? "" : channelName;
        this.queueName = queueName == null ? "" : queueName;
        this.userID = userID == null ? "" : userID;
        this.password = password == null ? "" : password;
        this.options = options == null ? Collections.<String, String>emptyMap() : new HashMap<>(options);
    }

    public MQConnectionProperties(final URI connectionString, final Map<String, String> options){
        if(connectionString == null)
            throw new IllegalArgumentException("connectionString is null.");
        else if(!Objects.equals(connectionString.getScheme(), SCHEME))
            throw new IllegalArgumentException(String.format("Incorrect URI scheme. %s expected.", SCHEME));
        //host name
        hostName = connectionString.getHost();
        //port
        port = connectionString.getPort() > 0 ? connectionString.getPort() : DEFAULT_PORT;
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
        path = password.substring(1);
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
        this.options = options == null ? Collections.<String, String>emptyMap() : new HashMap<>(options);
    }

    public MQConnectionProperties(final String connectionString, final Map<String, String> options){
        this(URI.create(connectionString), options);
    }

    public Map<String, String> getAdvancedOptions(){
        return options;
    }

    public String getQueueManagerName(){
        return options.get(QUEUE_MANAGER_PARAM);
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
    public void setupEnvironment(){
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
        setupEnvironment();
        return new MQQueueManager(getQueueManagerName());
    }

    public PCFMessageAgent createMessageAgent() throws MQException {
        return new PCFMessageAgent(hostName, port, channelName);
    }
}
