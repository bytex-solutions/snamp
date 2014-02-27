package com.snamp.connectors;

import com.ibm.mq.*;
import com.ibm.mq.constants.CMQC;
import com.ibm.mq.constants.CMQCFC;
import com.ibm.mq.headers.MQDataException;
import com.ibm.mq.headers.pcf.PCFException;
import com.ibm.mq.pcf.PCFMessage;
import com.ibm.mq.headers.pcf.PCFParameter;
import com.ibm.mq.pcf.PCFMessageAgent;


import java.beans.IntrospectionException;
import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * In order to use this class you need for this jar to be present in your classpath
 *
 * # mvn install:install-file -Dfile=./com.ibm.mq.pcf.jar -DgroupId=com.ibm.mq -DartifactId=WebSphereMQPCF -Dversion=6.0.0.0 -Dpackaging=jar
 *
 * Connector class for integration with WebSphere MQ
 * @author  Chernovsky Oleg
 * @since 1.1.0
 */
class IbmWmqConnector extends ManagementConnectorBean {
    public static final String NAME = "ibm-wmq";
    private final Map<String, String> mObjectFilter;
    private final MQQueueManager mQmgrInstance;
    private final PCFMessageAgent mMonitor;

    /**
     * Initializes a new management connector.
     *
     * @param  connectionString string with address of the MQ and channel
     * @throws IllegalArgumentException
     *          typeBuilder is {@literal null}.
     */
    public IbmWmqConnector(String connectionString, Map<String, String> connectionProperties) throws IntrospectionException {
        super(new IbmWmqTypeSystem());
        try {
            final URI address = URI.create(connectionString);
            if(address.getScheme().equals("wmq")) {
                MQEnvironment.hostname = address.getHost();
                MQEnvironment.channel = address.getUserInfo();
                MQEnvironment.port = address.getPort();

                //blocking calls, capped by 5 secs, exception on timeout
                mQmgrInstance = new MQQueueManager(address.getPath().substring(1));
                mMonitor = new PCFMessageAgent(mQmgrInstance);
                mObjectFilter = connectionProperties;
            }
            else
                throw new IllegalArgumentException("Cannot create IBM Connector: insufficient parameters!");
        } catch (Exception e) {
            throw new IntrospectionException(e.toString());
        }
    }

    /**
     * Function for further groovy investigation
     *
     * @return list of messages in a options-supplied queue
     */
    // TODO: Сделать notification-support для getQueueMessages на новое сообщение (фильтр по putDateTime)
    final public List<MQMessage> getQueueMessages() {
        if(mObjectFilter.containsKey("parseQueue")) {
            final List<MQMessage> messages = new ArrayList<>();

            try {
                final MQQueue subQueue = mQmgrInstance.accessQueue(mObjectFilter.get("parseQueue"), CMQC.MQOO_BROWSE);
                MQGetMessageOptions gmo = new MQGetMessageOptions();
                gmo.options = gmo.options + CMQC.MQGMO_BROWSE_NEXT + CMQC.MQGMO_ACCEPT_TRUNCATED_MSG; // accept only first 4 Kbytes of message
                MQMessage myMessage = new MQMessage();
                    while (true) {
                        myMessage.clearMessage();
                        myMessage.correlationId = CMQC.MQCI_NONE;
                        myMessage.messageId     = CMQC.MQMI_NONE;
                        subQueue.get(myMessage, gmo);

                        messages.add(myMessage);
                    }
            } catch (MQException e) {
                if(e.reasonCode == CMQC.MQRC_NO_MSG_AVAILABLE)
                    return messages;
            } catch (IOException ignored) {
                // cannot clear message
            }
        }

        return null;
    }

    /**
     * This function represents the table that holds all the attributes for queue status
     * Each row is filled with some queue statistics data
     *
     *
     * @return Table of queue attributes
     */
    @AttributeInfo(typeProvider = "createQueueStatusTableType")
    final public IbmWmqTypeSystem.QueueStatusTable getQueuesStatus() throws MQException {
        try {
            final PCFMessage inquireQueueStatus = new PCFMessage(CMQCFC.MQCMD_INQUIRE_Q_STATUS);
            if(mObjectFilter.containsKey("queueFilter"))
                inquireQueueStatus.addParameter(CMQC.MQCA_Q_NAME, mObjectFilter.get("queueFilter"));
            else
                inquireQueueStatus.addParameter(CMQC.MQCA_Q_NAME, "*");
            inquireQueueStatus.addParameter(CMQCFC.MQIACF_Q_STATUS_TYPE, CMQCFC.MQIACF_Q_STATUS);

            final PCFMessage[] statistics = mMonitor.send(inquireQueueStatus);
            return new IbmWmqTypeSystem.QueueStatusTable(Arrays.asList(statistics));

        } catch (IOException | MQException e) {
            return null;
        }
    }

    /**
     * This function represents the table that holds all the attributes for channel status
     * Each row is filled with some channel statistics data
     * This table is static due to
     *
     *
     * @return Table of queue attributes
     */
    @AttributeInfo(typeProvider = "createChannelStatusTableType")
    final public IbmWmqTypeSystem.ChannelStatusTable getChannelsStatus() {
        try {
            final PCFMessage inquireChannelStatus = new PCFMessage(CMQCFC.MQCMD_INQUIRE_CHANNEL_STATUS);
            if(mObjectFilter.containsKey("channelFilter"))
                inquireChannelStatus.addParameter(CMQCFC.MQCACH_CHANNEL_NAME, mObjectFilter.get("channelFilter"));
            else
                inquireChannelStatus.addParameter(CMQCFC.MQCACH_CHANNEL_NAME, "*");
            //inquireChannelStatus.addParameter(CMQCFC.MQIACH_CHANNEL_INSTANCE_ATTRS, new int[] { CMQCFC.MQIACF_ALL  }); // this is the default
            final PCFMessage[] statistics = mMonitor.send(inquireChannelStatus);
            return new IbmWmqTypeSystem.ChannelStatusTable(Arrays.asList(statistics));

        } catch (IOException | MQException e) {
            return null;
        }
    }

    @AttributeInfo(typeProvider = "createQmgrStatusTableType")
    final public IbmWmqTypeSystem.QMgrStatusTable getQmgrStatus() {
        try {
            final PCFMessage inquireQmgrStatus = new PCFMessage(CMQCFC.MQCMD_INQUIRE_Q_MGR_STATUS);

            //inquireQmgrStatus.addParameter(CMQCFC.MQIACF_Q_MGR_STATUS_ATTRS, new int[] { CMQCFC.MQIACF_ALL  }); // this is the default
            final PCFMessage[] statistics = mMonitor.send(inquireQmgrStatus);
            return new IbmWmqTypeSystem.QMgrStatusTable(Arrays.asList(statistics));

        } catch (IOException | MQException e) {
            return null;
        }
    }

    @AttributeInfo(typeProvider = "createServiceStatusTableType")
    final public IbmWmqTypeSystem.ServiceStatusTable getServicesStatus() {
        try {
            final PCFMessage inquireServiceStatus = new PCFMessage(CMQCFC.MQCMD_INQUIRE_SERVICE);
            if(mObjectFilter.containsKey("serviceFilter"))
                inquireServiceStatus.addParameter(CMQC.MQCA_SERVICE_NAME, mObjectFilter.get("serviceFilter"));
            else
                inquireServiceStatus.addParameter(CMQC.MQCA_SERVICE_NAME, "*");
            //inquireServiceStatus.addParameter(CMQCFC.MQIACF_SERVICE_ATTRS, new int[] { CMQCFC.MQIACF_ALL  }); // this is the default
            final PCFMessage[] statistics = mMonitor.send(inquireServiceStatus);
            return new IbmWmqTypeSystem.ServiceStatusTable(Arrays.asList(statistics));

        } catch (IOException | MQException e) {
            return null;
        }
    }

    @Override
    public void close() throws Exception {
        super.close();
        mMonitor.disconnect();
        mQmgrInstance.disconnect();
    }
}
