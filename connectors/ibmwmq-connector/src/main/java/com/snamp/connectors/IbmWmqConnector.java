package com.snamp.connectors;

import com.ibm.mq.MQEnvironment;
import com.ibm.mq.MQQueueManager;
import com.ibm.mq.constants.CMQC;
import com.ibm.mq.constants.CMQCFC;
import com.ibm.mq.headers.MQDataException;
import com.ibm.mq.headers.pcf.PCFMessage;
import com.ibm.mq.headers.pcf.PCFMessageAgent;
import com.ibm.mq.headers.pcf.PCFParameter;
import com.snamp.SimpleTable;

import java.beans.IntrospectionException;
import java.io.IOException;
import java.net.URI;
import java.util.Enumeration;
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
class IbmWmqConnector extends ManagementConnectorBean {
    public static final String NAME = "ibm-wmq";
    private final Map<String, String> mObjectFilter;
    private final PCFMessageAgent mMonitor;

    /**
     * Initializes a new management connector.
     *
     * @param typeBuilder Type information provider that provides property type converter.
     * @throws IllegalArgumentException
     *          typeBuilder is {@literal null}.
     */
    public IbmWmqConnector(String connectionString, Map<String, String> connectionProperties) throws IntrospectionException {
        super(new WellKnownTypeSystem());
        try {
            final URI address = URI.create(connectionString);
            if(address.getScheme().equals("wmq")) {
                MQEnvironment.hostname = address.getHost();
                MQEnvironment.channel = address.getUserInfo();
                MQEnvironment.port = address.getPort();

                //blocking calls, capped by 5 secs, exception on timeout
                final MQQueueManager mQmgrInstance = new MQQueueManager(address.getPath().substring(1));
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
     * This function represents the table that holds all the attributes for queue status
     * Each row is filled with some queue statistics data
     *
     *
     * @return Table of queue attributes
     */
    final public SimpleTable<String> getQueuesStatus() {
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

    /**
     * This function represents the table that holds all the attri butes for channel status
     * Each row is filled with some channel statistics data
     * This table is static due to
     *
     *
     * @return Table of queue attributes
     */
    final public SimpleTable<String> getChannelsStatus() {
        try {
            final PCFMessage inquireChannelStatus = new PCFMessage(CMQCFC.MQCMD_INQUIRE_CHANNEL_STATUS);
            if(mObjectFilter.containsKey("channelFilter"))
                inquireChannelStatus.addParameter(CMQCFC.MQCACH_CHANNEL_NAME, mObjectFilter.get("channelFilter"));
            else
                inquireChannelStatus.addParameter(CMQCFC.MQCACH_CHANNEL_NAME, "*");
            //inquireChannelStatus.addParameter(CMQCFC.MQIACH_CHANNEL_INSTANCE_ATTRS, new int[] { CMQCFC.MQIACF_ALL  }); // this is the default
            final PCFMessage[] statistics = mMonitor.send(inquireChannelStatus);
            // TODO: Переделать на новую систему типов
            final Map<String, Class<?>> columnsAndTypes = new HashMap<String, Class<?>>() {{
                for(final PCFMessage curr : statistics) { // we need to add every parameter that appears at least once to table
                    final Enumeration paramEnum = curr.getParameters();
                    while (paramEnum.hasMoreElements()) { // fill the columns
                        final PCFParameter parameter = (PCFParameter) paramEnum.nextElement();

                        if(!containsKey(parameter.getParameterName())) {
                            final Class parameterClazz = parameter.getValue().getClass();
                            if(!parameterClazz.isArray()) // arrays are converted to their String representation since adapters may not handle nested table types
                                put(parameter.getParameterName(), parameterClazz);
                            else
                                put(parameter.getParameterName(), String.class);
                        }
                    }
                }
            }};

            // then iteratively fill table with data
            final SimpleTable<String> resTable = new SimpleTable<>(columnsAndTypes);
            fillTableData(resTable, statistics);
            return resTable;
        } catch (IOException | MQDataException e) {
            return null;
        }
    }

    final public SimpleTable<String> getQmgrStatus() {
        try {
            final PCFMessage inquireQmgrStatus = new PCFMessage(CMQCFC.MQCMD_INQUIRE_Q_MGR_STATUS);

            //inquireQmgrStatus.addParameter(CMQCFC.MQIACF_Q_MGR_STATUS_ATTRS, new int[] { CMQCFC.MQIACF_ALL  }); // this is the default
            final PCFMessage[] statistics = mMonitor.send(inquireQmgrStatus);
            // TODO: Переделать на новую систему типов
            final Map<String, Class<?>> columnsAndTypes = new HashMap<String, Class<?>>() {{
                for(final PCFMessage curr : statistics) { // we need to add every parameter that appears at least once to table
                    final Enumeration paramEnum = curr.getParameters();
                    while (paramEnum.hasMoreElements()) { // fill the columns
                        final PCFParameter parameter = (PCFParameter) paramEnum.nextElement();

                        if(!containsKey(parameter.getParameterName())) {
                            final Class parameterClazz = parameter.getValue().getClass();
                            if(!parameterClazz.isArray()) // arrays are converted to their String representation since adapters may not handle nested table types
                                put(parameter.getParameterName(), parameterClazz);
                            else
                                put(parameter.getParameterName(), String.class);
                        }
                    }
                }

            }};

            // then iteratively fill table with data
            final SimpleTable<String> resTable = new SimpleTable<>(columnsAndTypes);
            fillTableData(resTable, statistics);

            return resTable;

        } catch (IOException | MQDataException e) {
            return null;
        }
    }

    final public SimpleTable<String> getServiceStatus() {
        try {
            final PCFMessage inquireServiceStatus = new PCFMessage(CMQCFC.MQCMD_INQUIRE_SERVICE_STATUS);
            if(mObjectFilter.containsKey("channelFilter"))
                inquireServiceStatus.addParameter(CMQC.MQCA_SERVICE_NAME, mObjectFilter.get("channelFilter"));
            else
                inquireServiceStatus.addParameter(CMQC.MQCA_SERVICE_NAME, "*");
            //inquireServiceStatus.addParameter(CMQCFC.MQIACF_SERVICE_ATTRS, new int[] { CMQCFC.MQIACF_ALL  }); // this is the default
            final PCFMessage[] statistics = mMonitor.send(inquireServiceStatus);
            // TODO: Переделать на новую систему типов
            final Map<String, Class<?>> columnsAndTypes = new HashMap<String, Class<?>>() {{
                for(final PCFMessage curr : statistics) { // we need to add every parameter that appears at least once to table
                    final Enumeration paramEnum = curr.getParameters();
                    while (paramEnum.hasMoreElements()) { // fill the columns
                        final PCFParameter parameter = (PCFParameter) paramEnum.nextElement();

                        if(!containsKey(parameter.getParameterName())) {
                            final Class parameterClazz = parameter.getValue().getClass();
                            if(!parameterClazz.isArray()) // arrays are converted to their String representation since adapters may not handle nested table types
                                put(parameter.getParameterName(), parameterClazz);
                            else
                                put(parameter.getParameterName(), String.class);
                        }
                    }
                }

            }};

            // then iteratively fill table with data
            final SimpleTable<String> resTable = new SimpleTable<>(columnsAndTypes);
            fillTableData(resTable, statistics);

            return resTable;

        } catch (IOException | MQDataException e) {
            return null;
        }
    }

    private void fillTableData(final SimpleTable<String> table, final PCFMessage[] stats) {
        for(final PCFMessage curr : stats) // each row
            table.addRow(new HashMap<String, Object>() {{
                final Enumeration paramEnum = curr.getParameters();
                while (paramEnum.hasMoreElements()) { // each column
                    PCFParameter parameter = (PCFParameter) paramEnum.nextElement();
                    Class parameterClazz = parameter.getValue().getClass();
                    if(!parameterClazz.isArray())
                        put(parameter.getParameterName(), parameter.getValue());
                    else
                        put(parameter.getParameterName(), parameter.getStringValue());
                }
            }});
    }
}
