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
public class IbmMqConnector extends ManagementConnectorBean {
    public static final String NAME = "ibm-mq";

    /**
     * Initializes a new management connector.
     *
     * @param typeBuilder Type information provider that provides property type converter.
     * @throws IllegalArgumentException
     *          typeBuilder is {@literal null}.
     */
    public IbmMqConnector(String connectionString, Map<String, String> connectionProperties, EntityTypeInfoFactory typeBuilder) throws IntrospectionException {
        super(typeBuilder);
        try {
            URI address = URI.create(connectionString);
            if(address.getScheme().equals("mq")) {
                MQEnvironment.hostname = address.getHost();
                MQEnvironment.channel = address.getUserInfo();
                MQEnvironment.port = address.getPort();

                MQQueueManager queueManager = new MQQueueManager(address.getPath().substring(1));
            }
            else
                throw new IllegalArgumentException("Cannot create IBM Connector: insufficient parameters!");
        } catch (Exception e) {
            throw new IntrospectionException(e.toString());
        }
    }
}
