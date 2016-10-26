package com.bytex.snamp.connector.modbus.slave;

import java.nio.ShortBuffer;

/**
 * @author Roman Sakno
 * @version 2.0
 * @since 1.0
 */
public interface FileRecordAccessor {
    ShortBuffer getRecord(final int recordNumber);
    int getRecords();
}
