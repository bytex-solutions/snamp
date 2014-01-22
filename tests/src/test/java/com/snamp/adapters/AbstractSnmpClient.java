package com.snamp.adapters;

import com.snamp.SynchronizationEvent;
import org.snmp4j.*;
import org.snmp4j.event.*;
import org.snmp4j.smi.*;
import org.snmp4j.util.*;

import java.io.IOException;
import java.util.*;
import java.util.logging.Logger;

/**
 * Represents abstract class for any SNMP client side helper
 * @author Evgeniy Kirichenko
 */
public abstract class AbstractSnmpClient implements SnmpClient {

    protected Snmp snmp = null;
    protected String address;
    protected Target target = null;
    protected TransportMapping transport = null;

    protected abstract Target getTarget();

    static Logger log = Logger.getLogger("snamp.snmp.log");

    /**
     * Helper method for table column id
     * @param baseID
     * @param columnCount
     * @return
     */
    private static OID[] makeColumnIDs(final OID baseID, final int columnCount){
        final OID[] result = new OID[columnCount];
        for(int i = 0; i <columnCount; i++)
            result[i] = new OID(baseID + "." + (i + 2));
        return result;
    }

    /**
     * Returns SNMP table using TableUtils
     * @param method
     * @param oidTable
     * @param columnCount
     * @return
     * @throws Exception
     */
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
     * This method is capable of handling multiple OIDs
     * @param oids
     * @return
     * @throws IOException
     */
    public ResponseEvent get(final ReadMethod method, final OID[] oids) throws IOException {
        method.prepareOIDs(oids);
        final PDU pdu = DefaultPDUFactory.createPDU(getTarget().getVersion());
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

    /**
     * PDU set method, Sync method
     * @param pdu
     * @return
     * @throws IOException
     */
    public ResponseEvent set(PDU pdu) throws IOException {

        return snmp.set(pdu, getTarget());
    }

    /**
     * PDU set method, async method
     * @param pdu
     * @param listenerResp
     * @throws IOException
     */
    public void set(PDU pdu, ResponseListener listenerResp) throws IOException {
        snmp.send(pdu, getTarget(), null, listenerResp);
        return;
    }

    /**
     * Append notification Listener for specified OID
     * @param notificationID
     * @return
     */
    public final SynchronizationEvent.Awaitor<SnmpWrappedNotification> addNotificationListener(final OID notificationID){
        final SynchronizationEvent<SnmpWrappedNotification> signaller = new SynchronizationEvent<>();
        snmp.addCommandResponder(new CommandResponder() {
            @Override
            public final void processPdu(final CommandResponderEvent event) {
                final PDU p = event.getPDU();
                if(p.getVariableBindings().size() == 0) return;
                else {
                    final Collection<? extends VariableBinding> bindings = p.getVariableBindings();
                    SnmpWrappedNotification notif = null;
                    for(final VariableBinding binding: bindings)
                        if(binding.getOid().startsWith(notificationID)){
                            if(notif == null) notif = new SnmpWrappedNotification(notificationID);
                            notif.put(binding);
                        }
                    if(notif != null && notif.size() > 0)
                        signaller.fire(notif);
                    else notif = null;
                }
            }
        });
        return signaller.getAwaitor();
    }

    /**
     * Default getClientPort implementation
     * @return
     */
    public int getClientPort(){
        final UdpAddress address = (UdpAddress)transport.getListenAddress();
        return address.getPort();
    }
}
