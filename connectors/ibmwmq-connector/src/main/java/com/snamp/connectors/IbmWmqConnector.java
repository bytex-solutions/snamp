package com.snamp.connectors;

import com.ibm.mq.MQEnvironment;
import com.ibm.mq.MQQueueManager;

import java.beans.IntrospectionException;
import java.net.URI;
import java.util.Map;

/**
 * Connector class for integration with WebSphere MQ
 * @author  Chernovsky Oleg
 * @since 1.1.0
 */
public class IbmWmqConnector extends ManagementConnectorBean {
    public static final String NAME = "ibm-wmq";
    public final MQQueueManager mQmgrInstance;

    /**
     * Initializes a new management connector.
     *
     * @param typeBuilder Type information provider that provides property type converter.
     * @throws IllegalArgumentException
     *          typeBuilder is {@literal null}.
     */
    public IbmWmqConnector(String connectionString, Map<String, String> connectionProperties, EntityTypeInfoFactory typeBuilder) throws IntrospectionException {
        super(typeBuilder);
        try {
            URI address = URI.create(connectionString);
            if(address.getScheme().equals("wmq")) {
                MQEnvironment.hostname = address.getHost();
                MQEnvironment.channel = address.getUserInfo();
                MQEnvironment.port = address.getPort();

                mQmgrInstance = new MQQueueManager(address.getPath().substring(1));

            }
            else
                throw new IllegalArgumentException("Cannot create IBM Connector: insufficient parameters!");
        } catch (Exception e) {
            throw new IntrospectionException(e.toString());
        }
    }
}
