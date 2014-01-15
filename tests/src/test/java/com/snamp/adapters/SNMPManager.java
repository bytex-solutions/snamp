package com.snamp.adapters;

import com.snamp.SynchronizationEvent;
import org.snmp4j.*;
import org.snmp4j.event.ResponseEvent;
import org.snmp4j.event.ResponseListener;
import org.snmp4j.mp.SnmpConstants;
import org.snmp4j.smi.*;
import org.snmp4j.transport.DefaultUdpTransportMapping;
import org.snmp4j.util.DefaultPDUFactory;
import org.snmp4j.util.PDUFactory;
import org.snmp4j.util.TableEvent;
import org.snmp4j.util.TableUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: temni
 * Date: 20.10.13
 * Time: 17:19
 * Simple SNMP client that uses snmp4j lib to access all neccessary methods/attrs
 */
public final class SNMPManager {

    private Snmp snmp = null;
    private final String address;
    private CommunityTarget target = null;
    private TransportMapping transport = null;

    public static enum ReadMethod{
        GET(PDU.GET),
        GETBULK(PDU.GETBULK);

        private final int method;

        private ReadMethod(final int m){
            method = m;
        }

        public final void setPduType(final PDU pdu){
            pdu.setType(method);
        }

        public final PDUFactory createPduFactory(){
            return new DefaultPDUFactory(method);
        }

        public final void prepareOIDs(final OID[] oids) {
            switch (method){
                case PDU.GETBULK:
                    for(int i = 0; i < oids.length; i++)
                        oids[i] = prepareOidForGetBulk(oids[i]);
            }
        }

        private static OID prepareOidForGetBulk(final OID oid) {
            //if ends with '0' then remove it
            final String result = oid.toString();
            if(result.endsWith(".0"))
                return new OID(result.substring(0, result.length() - 2));
            else {
                final int lastDot = result.lastIndexOf('.');
                final int num = Integer.valueOf(result.substring(lastDot + 1)) + 1;
                return new OID(result.substring(0, lastDot) + "." + num);
            }
        }
    }

    /**
     * Constructor
     * @param add
     */
    public SNMPManager(String add) {
        address = add;
        try {
            start();
        } catch (IOException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
    }

    public int getClientPort(){
        final UdpAddress address = (UdpAddress)transport.getListenAddress();
        return address.getPort();
    }


    /**
     * Start the Snmp session. If you forget the listen() method you will not
     * get any answers because the communication is asynchronous
     * and the listen() method listens for answers.
     * @throws java.io.IOException
     */
    public void start() throws IOException {
        transport = new DefaultUdpTransportMapping();
        transport.listen();
        snmp = new Snmp(transport);
    }

    private static OID[] makeColumnIDs(final OID baseID, final int columnCount){
        final OID[] result = new OID[columnCount];
        for(int i = 0; i <columnCount; i++)
            result[i] = new OID(baseID + "." + (i + 2));
        return result;
    }

    public final List<Variable[]> getTable(final ReadMethod method, final OID oidTable, final int columnCount) throws Exception{
        final TableUtils utils = new TableUtils(snmp, method.createPduFactory());
        final List<TableEvent> events = utils.getTable(getTarget(), makeColumnIDs(oidTable, columnCount), null, null);
        final List<Variable[]> result = new ArrayList<>(events.size());
        for(final TableEvent ev: events)
            if(ev.isError()) throw new Exception(ev.getErrorMessage());
            else {
                final VariableBinding[] columns = ev.getColumns();
                if(columns == null)
                    throw new NullPointerException("Columns is null.");
                final Variable[] cells = new Variable[columns.length];
                for(int i = 0; i < columns.length; i++)
                    cells[i] = columns[i].getVariable();
                result.add(cells);
            }
        return result;
    }

    /**
     * Method which takes a single OID and returns the response from the agent as a String.
     * @param oid
     * @return
     * @throws IOException
     */
    public String getAsString(final ReadMethod method, final OID oid) throws IOException {
        final ResponseEvent event = get(method, new OID[] { oid });
        return event.getResponse().get(0).getVariable().toString();
    }

    /**
     * This method is capable of handling multiple OIDs
     * @param oids
     * @return
     * @throws IOException
     */
    public ResponseEvent get(final ReadMethod method, final OID[] oids) throws IOException {
        method.prepareOIDs(oids);
        final PDU pdu = new PDU();
        method.setPduType(pdu);
        pdu.setMaxRepetitions(50);
        pdu.setNonRepeaters(1);
        for (OID oid : oids) {
            pdu.add(new VariableBinding(oid));
        }

        final ResponseEvent event = snmp.send(pdu, getTarget(), null);
        if(event != null) {
            return event;
        }
        throw new RuntimeException("GET timed out");
    }


    public ResponseEvent set(PDU pdu) throws IOException {

        return snmp.set(pdu, getTarget());
    }

    public void set(PDU pdu, ResponseListener listenerResp) throws IOException {

        snmp.send(pdu, getTarget(), null, listenerResp);
        return;
    }

    public final SynchronizationEvent.Awaitor<PDU> addNotificationListener(){
        final SynchronizationEvent<PDU> result = new SynchronizationEvent<>();
        snmp.addCommandResponder(new CommandResponder() {
            @Override
            public void processPdu(final CommandResponderEvent event) {
                result.fire(event.getPDU());
                snmp.removeCommandResponder(this);
            }
        });
        return result.getAwaitor();
    }

    /**
     * This method returns a Target, which contains information about
     * where the data should be fetched and how.
     * @return
     */
    private Target getTarget() {
        if (target == null)
        {
            final Address targetAddress = GenericAddress.parse(address);
            target = new CommunityTarget();
            target.setCommunity(new OctetString("public"));
            target.setAddress(targetAddress);
            target.setRetries(3);
            target.setTimeout(5000000);
            target.setVersion(SnmpConstants.version2c);
        }
        return target;
    }
}