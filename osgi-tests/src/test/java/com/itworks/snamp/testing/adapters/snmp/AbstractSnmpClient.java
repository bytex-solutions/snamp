package com.itworks.snamp.testing.adapters.snmp;

import com.google.common.collect.ImmutableList;
import com.itworks.snamp.concurrent.SynchronizationEvent;
import com.itworks.snamp.testing.SnmpTable;
import org.snmp4j.*;
import org.snmp4j.event.ResponseEvent;
import org.snmp4j.event.ResponseListener;
import org.snmp4j.smi.OID;
import org.snmp4j.smi.UdpAddress;
import org.snmp4j.smi.Variable;
import org.snmp4j.smi.VariableBinding;
import org.snmp4j.util.DefaultPDUFactory;
import org.snmp4j.util.TableEvent;
import org.snmp4j.util.TableUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.logging.Logger;

/**
 * Represents abstract class for any SNMP client side helper
 * @author Evgeniy Kirichenko
 */
abstract class AbstractSnmpClient implements SnmpClient {
    private static final class SnmpTableImpl extends ArrayList<Variable[]> implements SnmpTable{
        private static final long serialVersionUID = -3238910325989531874L;
        private final ImmutableList<Class<?>> columns;

        private SnmpTableImpl(final List<Class<?>> columns, final int rowCapacity){
            super(rowCapacity);
            this.columns = ImmutableList.copyOf(columns);
        }

        @Override
        public int getRowCount() {
            return size();
        }

        @Override
        public int getColumnCount() {
            return columns.size();
        }

        @Override
        public Variable getRawCell(final int columnIndex, final int rowIndex) {
            return get(rowIndex)[columnIndex];
        }

        @Override
        public Object getCell(final int columndIndex, final int rowIndex) {
            return AbstractSnmpTable.deserialize(getRawCell(columndIndex, rowIndex), columns.get(columndIndex));
        }
    }

    protected Snmp snmp = null;
    protected String address;
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

    private SnmpTable getTable(final ReadMethod method, final OID oidTable, final List<Class<?>> cols) throws Exception{
        final TableUtils utils = new TableUtils(snmp, method.createPduFactory());
        final List<TableEvent> events = utils.getTable(getTarget(), makeColumnIDs(oidTable, cols.size()), null, null);
        final SnmpTableImpl result = new SnmpTableImpl(cols, events.size());
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
    private ResponseEvent get(final ReadMethod method, final OID[] oids) throws IOException {
        method.prepareOIDs(oids);
        final PDU pdu = DefaultPDUFactory.createPDU(getTarget(), method.getPduType());
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
    private ResponseEvent set(PDU pdu) throws IOException {

        return snmp.set(pdu, getTarget());
    }

    /**
     * PDU set method, async method
     * @param pdu
     * @param listenerResp
     * @throws IOException
     */
    private void set(PDU pdu, ResponseListener listenerResp) throws IOException {
        snmp.send(pdu, getTarget(), null, listenerResp);
    }

    /**
     * Append notification Listener for specified OID
     * @param notificationID
     * @return
     */
    public final SynchronizationEvent.EventAwaitor<SnmpNotification> addNotificationListener(final OID notificationID){
        final SynchronizationEvent<SnmpNotification> signaller = new SynchronizationEvent<>();
        snmp.addCommandResponder(new CommandResponder() {
            @Override
            public final void processPdu(final CommandResponderEvent event) {
                final PDU p = event.getPDU();
                if(p.getVariableBindings().size() == 0) return;
                else {
                    final Collection<? extends VariableBinding> bindings = p.getVariableBindings();
                    SnmpNotification notif = null;
                    for(final VariableBinding binding: bindings)
                        if(binding.getOid().startsWith(notificationID)){
                            if(notif == null) notif = new SnmpNotification(notificationID);
                            notif.put(binding);
                        }
                    if(notif != null && notif.size() > 0)
                        signaller.fire(notif);
                }
            }
        });
        return signaller.getAwaitor();
    }

    /**
     * Default getClientPort implementation
     * @return
     */
    public final int getClientPort(){
        final UdpAddress address = (UdpAddress)transport.getListenAddress();
        return address.getPort();
    }

    /**
     * Write attribute to OID
     * @param oid
     * @param value
     * @param valueType
     * @param <T>
     * @throws IOException
     */
    public final  <T> void writeAttribute(final OID oid, final T value, final Class<T> valueType) throws IOException{
        final PDU pdu = DefaultPDUFactory.createPDU(this.getTarget(), PDU.SET);
        final VariableBinding varBind = new VariableBinding(oid, AbstractSnmpTable.serialize(value, valueType));
        pdu.add(varBind);
        final ResponseListener listener = new ResponseListener() {
            public void onResponse(ResponseEvent event) {
                final PDU strResponse;
                final String result;
                ((Snmp)event.getSource()).cancel(event.getRequest(), this);
                strResponse = event.getResponse();
                if (strResponse!= null) {
                    result = strResponse.getErrorStatusText();
                    System.out.println("Set Status is: "+result);
                }
                else
                    System.out.println("SNMP error occured while sending SET request on OID: " + oid.toDottedString());
            }};

        this.set(pdu, listener);
    }


    /**
     * Read attibute and returns it as some Java class representation
     * @param method
     * @param oid
     * @param className
     * @param <T>
     * @return
     * @throws IOException
     */
    public final  <T>T readAttribute(final ReadMethod method, final OID oid, final Class<T> className) throws IOException {
        final ResponseEvent value = this.get(method, new OID[]{oid});
        //assertNotNull(value);
        return AbstractSnmpTable.deserialize(value.getResponse().getVariable(oid), className);

    }

    /**
     * Read table and returns it as Table<Integer> object instance
     * @param method
     * @param oid
     * @param columns
     * @return
     * @throws Exception
     */
    @Override
    public final SnmpTable readTable(final ReadMethod method, final OID oid, final Class<?>... columns) throws Exception {
        return getTable(method, oid, Arrays.asList(columns));
    }

    /**
     * Attempts to write table, return PDU response as a result of writing
     * @param tablePrefix
     * @param table
     * @return
     * @throws IOException
     */
    @Override
    public final PDU writeTable(final String tablePrefix, final SnmpTable table) throws IOException {
        final PDU pdu = DefaultPDUFactory.createPDU(this.getTarget(), PDU.SET);
        for(int rowIndex = 0; rowIndex < table.getRowCount(); rowIndex++)
            for(int columnIndex = 0; columnIndex < table.getColumnCount(); columnIndex++){
                //row index always starts from 1 in SnmpAdapter
                //column index always starts from 2 in SnmpAdapter
                final OID rowId = new OID(tablePrefix + '.' + (columnIndex + 2) + '.' + (rowIndex + 1));
                pdu.add(new VariableBinding(rowId, table.getRawCell(columnIndex, rowIndex)));
            }
        return this.set(pdu).getResponse();
        //assertEquals(response.getErrorStatusText(), SnmpConstants.SNMP_ERROR_SUCCESS, response.getErrorStatus());
    }


}
