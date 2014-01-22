package com.snamp.adapters;

import com.snamp.SynchronizationEvent;
import org.snmp4j.PDU;
import org.snmp4j.event.ResponseEvent;
import org.snmp4j.event.ResponseListener;
import org.snmp4j.smi.OID;
import org.snmp4j.smi.Variable;

import java.io.IOException;
import java.util.List;

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
    SynchronizationEvent.Awaitor<SnmpWrappedNotification> addNotificationListener(final OID notificationID);

    /**
     * Send PDU to the server and returns ResponseEvent variable contained the response
     * @param method
     * @param oids
     * @return
     * @throws IOException
     */
    ResponseEvent get(final ReadMethod method, final OID[] oids) throws IOException;


    /**
     * Send SET PDU request and synchronously returns response event
     * @param pdu
     * @return
     * @throws IOException
     */
    ResponseEvent set(PDU pdu) throws IOException;


    /**
     * Send SET PDU request and set listenerResp param to actual response Listener
     * @param pdu
     * @param listenerResp
     * @throws IOException
     */
    void set(PDU pdu, ResponseListener listenerResp) throws IOException;


    /**
     * Returns table for associated OID
     * @param method
     * @param oidTable
     * @param columnCount
     * @return
     * @throws Exception
     */
    List<Variable[]> getTable(final ReadMethod method, final OID oidTable, final int columnCount) throws Exception;



}
