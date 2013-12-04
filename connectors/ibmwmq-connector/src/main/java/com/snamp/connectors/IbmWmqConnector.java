package com.snamp.connectors;

import com.ibm.mq.MQEnvironment;
import com.ibm.mq.MQQueueManager;
import com.ibm.mq.constants.CMQC;
import com.ibm.mq.constants.CMQCFC;
import com.ibm.mq.headers.MQDataException;
import com.ibm.mq.headers.pcf.PCFMessage;
import com.ibm.mq.headers.pcf.PCFMessageAgent;
import com.snamp.SimpleTable;

import java.beans.IntrospectionException;
import java.io.IOException;
import java.net.URI;
import java.util.HashMap;
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
public class IbmWmqConnector extends ManagementConnectorBean {
    public static final String NAME = "ibm-wmq";
    public final MQQueueManager mQmgrInstance;
    public final Map<String, String> mObjectFilter;
    public final PCFMessageAgent mMonitor;

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
                mMonitor = new PCFMessageAgent(mQmgrInstance);
                mObjectFilter = connectionProperties;
            }
            else
                throw new IllegalArgumentException("Cannot create IBM Connector: insufficient parameters!");
        } catch (Exception e) {
            throw new IntrospectionException(e.toString());
        }
    }

    public SimpleTable<String> getQueuesStatus() {
        try {
            final PCFMessage inquireQueueStatus = new PCFMessage(CMQCFC.MQCMD_INQUIRE_Q_STATUS);
            if(mObjectFilter.containsKey("queueFilter"))
                inquireQueueStatus.addParameter(CMQC.MQCA_Q_NAME, mObjectFilter.get("queueFilter"));
            else
                inquireQueueStatus.addParameter(CMQC.MQCA_Q_NAME, "*");
            inquireQueueStatus.addParameter(CMQCFC.MQIACF_Q_STATUS_TYPE, CMQCFC.MQIACF_Q_STATUS);
            inquireQueueStatus.addParameter(CMQCFC.MQIACF_Q_STATUS_ATTRS, new int[] {
                CMQC.MQCA_Q_NAME, CMQC.MQIA_CURRENT_Q_DEPTH,
                CMQCFC.MQCACF_LAST_GET_DATE, CMQCFC.MQCACF_LAST_GET_TIME,
                CMQCFC.MQCACF_LAST_PUT_DATE, CMQCFC.MQCACF_LAST_PUT_TIME,
                CMQCFC.MQIACF_OLDEST_MSG_AGE, CMQC.MQIA_OPEN_INPUT_COUNT,
                CMQC.MQIA_OPEN_OUTPUT_COUNT, CMQCFC.MQIACF_UNCOMMITTED_MSGS });

            final PCFMessage[] statistics = mMonitor.send(inquireQueueStatus);
            final Map<String, Class<?>> columnsAndTypes = new HashMap<String, Class<?>>() {{
                put("QueueName", String.class);
                put("CurrentDepth", Integer.class);
                put("LastDequeueDate", String.class);
                put("LastDequeueTime", String.class);
                put("LastEnqueueDate", String.class);
                put("LastEnqueueTime", String.class);
                put("OldestMessageAge", String.class);
                put("OpenInputCount", Integer.class);
                put("OpenOutputCount", Integer.class);
                put("UncommittedMessagesCount", Integer.class);
            }};
            final SimpleTable<String> resTable = new SimpleTable<>(columnsAndTypes);
            for(final PCFMessage queueStatus : statistics)
                resTable.addRow(new HashMap<String, Object>() {{
                    put("QueueName", queueStatus.getStringParameterValue(CMQC.MQCA_Q_NAME));
                    put("CurrentDepth", queueStatus.getIntParameterValue(CMQC.MQIA_CURRENT_Q_DEPTH));
                    put("LastDequeueDate", queueStatus.getStringParameterValue(CMQCFC.MQCACF_LAST_GET_DATE));
                    put("LastDequeueTime", queueStatus.getStringParameterValue(CMQCFC.MQCACF_LAST_GET_TIME));
                    put("LastEnqueueDate", queueStatus.getStringParameterValue(CMQCFC.MQCACF_LAST_PUT_DATE));
                    put("LastEnqueueTime", queueStatus.getStringParameterValue(CMQCFC.MQCACF_LAST_PUT_DATE));
                    put("OldestMessageAge", queueStatus.getIntParameterValue(CMQCFC.MQIACF_OLDEST_MSG_AGE));
                    put("OpenInputCount", queueStatus.getIntParameterValue(CMQC.MQIA_OPEN_INPUT_COUNT));
                    put("OpenOutputCount", queueStatus.getIntParameterValue(CMQC.MQIA_OPEN_OUTPUT_COUNT));
                    put("UncommittedMessagesCount", queueStatus.getIntParameterValue(CMQCFC.MQIACF_UNCOMMITTED_MSGS));
                }});

            return resTable;

        } catch (IOException | MQDataException e) {
            return null;
        }
    }

    public SimpleTable<String> getChannelsStatus() {
        try {
            final PCFMessage inquireChannelStatus = new PCFMessage(CMQCFC.MQCMD_INQUIRE_CHANNEL_STATUS);
            if(mObjectFilter.containsKey("channelFilter"))
                inquireChannelStatus.addParameter(CMQCFC.MQCACH_CHANNEL_NAME, mObjectFilter.get("channelFilter"));
            else
                inquireChannelStatus.addParameter(CMQCFC.MQCACH_CHANNEL_NAME, "*");
            //inquireChannelStatus.addParameter(CMQCFC.MQIACH_CHANNEL_INSTANCE_ATTRS, new int[] { CMQCFC.MQIACF_ALL  }); // this is the default

            final PCFMessage[] statistics = mMonitor.send(inquireChannelStatus);
            final Map<String, Class<?>> columnsAndTypes = new HashMap<String, Class<?>>() {{
                put("ChannelName", String.class);
                put("ConnectionName", String.class);
                put("TransmissionQueueName", String.class);
                put("ChannelInstanceType", Integer.class);
                put("LastUncommittedMessageSequenceNumber", Integer.class);
                put("CurrentIndoubtStatus", Integer.class);
                put("LastCommittedMessageSequenceNumber", Integer.class);

                put("ChannelStartDate", String.class);
                put("ChannelStartTime", String.class);
                put("LastMessageDate", String.class);
                put("LastMessageTime", String.class);

                put("ChannelLocalAddress", String.class);
                put("ChannelMCAJobName", String.class);
                put("ChannelMCAUserName", String.class);
                put("ChannelRemoteAppName", String.class);

                // TODO: test MQIACF_MONITORING

                put("ChannelBatchesCompleted", Integer.class);
                put("ChannelBuffersReceived", Integer.class);
                put("ChannelBuffersSent", Integer.class);
                put("ChannelBytesReceived", Integer.class);
                put("ChannelBytesSent", Integer.class);

                put("ChannelMessagesTotalNumber", Integer.class);
                put("ChannelMessagesAvailable", Integer.class);
                put("ChannelBatchSize", Integer.class);
                put("ChannelHeartBeatInterval", Integer.class);
                put("ChannelMessageSpeed", Integer.class);
            }};


            final SimpleTable<String> resTable = new SimpleTable<>(columnsAndTypes);
            for(final PCFMessage queueStatus : statistics)
                resTable.addRow(new HashMap<String, Object>() {{
                    put("ChannelName", queueStatus.getStringParameterValue(CMQCFC.MQCACH_CHANNEL_NAME));
                    put("ConnectionName", queueStatus.getStringParameterValue(CMQCFC.MQCACH_CONNECTION_NAME));
                    put("TransmissionQueueName", queueStatus.getStringParameterValue(CMQCFC.MQCACH_XMIT_Q_NAME));
                    put("ChannelInstanceType", queueStatus.getIntParameterValue(CMQCFC.MQIACH_CHANNEL_INSTANCE_TYPE));
                    put("LastUncommittedMessageSequenceNumber", queueStatus.getIntParameterValue(CMQCFC.MQIACH_CURRENT_SEQ_NUMBER));
                    put("CurrentIndoubtStatus", queueStatus.getIntParameterValue(CMQCFC.MQIACH_IN_DOUBT));
                    put("LastCommittedMessageSequenceNumber", queueStatus.getIntParameterValue(CMQCFC.MQIACH_LAST_SEQ_NUMBER));

                    put("ChannelStartDate", queueStatus.getStringParameterValue(CMQCFC.MQCACH_CHANNEL_START_DATE));
                    put("ChannelStartTime", queueStatus.getStringParameterValue(CMQCFC.MQCACH_CHANNEL_START_TIME));
                    put("LastMessageDate", queueStatus.getStringParameterValue(CMQCFC.MQCACH_LAST_MSG_DATE));
                    put("LastMessageTime", queueStatus.getStringParameterValue(CMQCFC.MQCACH_LAST_MSG_TIME));

                    put("ChannelLocalAddress", queueStatus.getStringParameterValue(CMQCFC.MQCACH_LOCAL_ADDRESS));
                    put("ChannelMCAJobName", queueStatus.getStringParameterValue(CMQCFC.MQCACH_MCA_JOB_NAME));
                    put("ChannelMCAUserName", queueStatus.getStringParameterValue(CMQCFC.MQCACH_MCA_USER_ID));
                    put("ChannelRemoteAppName", queueStatus.getStringParameterValue(CMQCFC.MQCACH_REMOTE_APPL_TAG));

                    // TODO: test MQIACF_MONITORING

                    put("ChannelBatchesCompleted", queueStatus.getIntParameterValue(CMQCFC.MQIACH_BATCHES));
                    put("ChannelBuffersReceived", queueStatus.getIntParameterValue(CMQCFC.MQIACH_BUFFERS_RCVD));
                    put("ChannelBuffersSent", queueStatus.getIntParameterValue(CMQCFC.MQIACH_BUFFERS_SENT));
                    put("ChannelBytesReceived", queueStatus.getIntParameterValue(CMQCFC.MQIACH_BYTES_RCVD));
                    put("ChannelBytesSent", queueStatus.getIntParameterValue(CMQCFC.MQIACH_BYTES_SENT));

                    put("ChannelMessagesTotalNumber", queueStatus.getIntParameterValue(CMQCFC.MQIACH_MSGS));
                    put("ChannelMessagesAvailable", queueStatus.getIntParameterValue(CMQCFC.MQIACH_XMITQ_MSGS_AVAILABLE));
                    put("ChannelBatchSize", queueStatus.getIntParameterValue(CMQCFC.MQIACH_BATCH_SIZE));
                    put("ChannelHeartBeatInterval", queueStatus.getIntParameterValue(CMQCFC.MQIACH_HB_INTERVAL));
                    put("ChannelMessageSpeed", queueStatus.getIntParameterValue(CMQCFC.MQIACH_NPM_SPEED));
                }});

            return resTable;

        } catch (IOException | MQDataException e) {
            return null;
        }
    }
}
