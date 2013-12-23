package com.snamp.connectors;

import com.ibm.mq.headers.pcf.PCFException;
import com.ibm.mq.headers.pcf.PCFMessage;
import com.ibm.mq.constants.CMQC;
import com.ibm.mq.constants.CMQCFC;
import com.snamp.SimpleTable;
import com.snamp.Table;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class IbmWmqTypeSystem extends WellKnownTypeSystem {

    /**
     * Class for queue status table representation
     * Created in need to distinguish multiple getters that return same types
     *
     */
    final public static class QueueStatusTable extends ArrayList<PCFMessage> {
        public QueueStatusTable(Collection<PCFMessage> c) {
            super(c);
        }
    }

    @Converter
    public static Table<String> convertToTable(QueueStatusTable queueRows) {
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
        for(final PCFMessage queueStatus : queueRows)
            try {
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
            } catch (PCFException ignored) {
                // just skip invalid message
            }

        return resTable;
    }

    public final ManagementEntityType createQueueStatusTableType() {
        return createEntityTabularType(new HashMap<String, ManagementEntityType>() {{
            put("QueueName", createStringType());
            put("CurrentDepth", createIntegerType());
            put("LastDequeueDate", createStringType());
            put("LastDequeueTime", createStringType());
            put("LastEnqueueDate", createStringType());
            put("LastEnqueueTime", createStringType());
            put("OldestMessageAge", createStringType());
            put("OpenInputCount", createIntegerType());
            put("OpenOutputCount", createIntegerType());
            put("UncommittedMessagesCount", createIntegerType());
        }});
    }

    final public static class ChannelStatusTable extends ArrayList<PCFMessage> {
        public ChannelStatusTable(Collection<PCFMessage> c) {
            super(c);
        }
    }

    @Converter
    public static Table<String> convertToTable(ChannelStatusTable queueRows) {
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
        for(final PCFMessage queueStatus : queueRows)
            try {
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
            } catch (PCFException ignored) {
                // just skip invalid message
            }

        return resTable;
    }
}
