package com.itworks.snamp.testing.adapters.snmp;

import com.itworks.snamp.SynchronizationEvent;
import com.itworks.snamp.Table;
import org.snmp4j.PDU;
import org.snmp4j.smi.OID;

import java.io.IOException;
import java.util.Map;

/**
 * Represents SNMP client for flexible testing of snmp adapter
 * @author Evgeniy Kirichenko
 */
public interface SnmpClient {

    /**
     * Returns specific client port for initialised udp connection
     * @return client port
     */
    int getClientPort();

    /**
     * Add Notification Listener with specified OID
     * @param notificationID
     * @return
     */
    SynchronizationEvent.Awaitor<SnmpNotification> addNotificationListener(final OID notificationID);

    /**
     * Writes table object to certain OID prefix table
     * @return
     * @throws IOException
     */
    PDU writeTable(final String tablePrefix, final Table<Integer> table) throws IOException;


    /**
     * Read table and return it to the Table object
     * @return
     * @throws IOException
     */
    Table<Integer> readTable(final ReadMethod method, final OID oid, final Map<Integer, Class<?>> columns) throws Exception;


    /**
     * Read attribute connected to the certain OID
     * @param method
     * @param oid
     * @param className
     * @param <T>
     * @return
     * @throws IOException
     */
    <T> T readAttribute(final ReadMethod method, final OID oid, final Class<T> className) throws IOException;

    /**
     * Write attribute connected to the certain OID
     * @param oid
     * @param value
     * @param valueType
     * @param <T>
     * @throws IOException
     */
    <T> void writeAttribute(final OID oid, final T value, final Class<T> valueType) throws IOException;

}
