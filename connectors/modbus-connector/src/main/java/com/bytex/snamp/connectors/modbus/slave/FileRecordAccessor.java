package com.bytex.snamp.connectors.modbus.slave;

import java.nio.ShortBuffer;

/**
 * @author Roman Sakno
 * @version 1.2
 * @since 1.0
 */
public interface FileRecordAccessor {
    ShortBuffer getRecord(final int recordNumber);
    int getRecords();
}
