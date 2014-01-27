package com.snamp.adapters;

import com.snamp.SimpleTable;
import com.snamp.SynchronizationEvent;
import com.snamp.Table;
import org.snmp4j.*;
import org.snmp4j.event.*;
import org.snmp4j.mp.SnmpConstants;
import org.snmp4j.smi.*;
import org.snmp4j.util.*;

import java.io.IOException;
import java.math.BigInteger;
import java.util.*;
import java.util.logging.Logger;

/**
 * Represents abstract class for any SNMP client side helper
 * @author Evgeniy Kirichenko
 */
public abstract class AbstractSnmpClient implements SnmpClient {

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

    /**
     * Returns SNMP table using TableUtils
     * @param method
     * @param oidTable
     * @param columnCount
     * @return
     * @throws Exception
     */
    private final List<Variable[]> getTable(final ReadMethod method, final OID oidTable, final int columnCount) throws Exception{
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
    private ResponseEvent get(final ReadMethod method, final OID[] oids) throws IOException {
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

    /**
     * Write attribute to OID
     * @param oid
     * @param value
     * @param valueType
     * @param <T>
     * @throws IOException
     */
    public <T> void writeAttribute(final OID oid, final T value, final Class<T> valueType) throws IOException{
        final PDU pdu = DefaultPDUFactory.createPDU(this.getTarget(), PDU.SET);
        final Variable var;

        if (valueType == int.class || valueType == Integer.class || valueType == short.class)
        {
            var = new Integer32(Integer.class.cast(value));
        }
        else if (valueType == long.class || valueType == Long.class)
        {
            var = new Counter64(Long.class.cast(value));
        }
        else if (valueType == Boolean.class || valueType == boolean.class)
        {
            var = new Integer32((Boolean.class.cast(value) == Boolean.TRUE)?1:0);
        }
        else if (valueType == byte[].class)
        {
            var = new OctetString((byte[])value);
        }
        else
        {
            var = new OctetString(value.toString());
        }

        final VariableBinding varBind = new VariableBinding(oid,var);

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
     * Helper class that transform SMI variable to an appropriate Java type class representation
     * @param var
     * @param className
     * @param <T>
     * @return
     */
    private static <T> T deserialize(final Variable var, final Class<T> className){
        final Object result;
        if (var instanceof UnsignedInteger32 || var instanceof Integer32)
            result = (className == Boolean.class)?(var.toInt() == 1):var.toInt();
        else if (var instanceof OctetString)
        {
            if (className == BigInteger.class)
                result = new BigInteger(var.toString());
            else if (className == Float.class)
                result = Float.valueOf(var.toString());
            else if (className == byte[].class)
                result = ((OctetString) var).toByteArray();
            else
                result = var.toString();
        }
        else if (var instanceof IpAddress)
            result = var.toString();
        else if (var instanceof Counter64)
            result = var.toLong();
        else result = null;
        return className.cast(result);
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
    public <T>T readAttribute(final ReadMethod method, final OID oid, final Class<T> className) throws IOException {
        final ResponseEvent value = this.get(method, new OID[]{oid});
        //assertNotNull(value);
        return deserialize(value.getResponse().getVariable(oid), className);

    }

    /**
     * Read table and returns it as Table<Integer> object instance
     * @param method
     * @param oid
     * @param columns
     * @return
     * @throws Exception
     */
    public Table<Integer> readTable(final ReadMethod method, final OID oid, final Map<Integer, Class<?>> columns) throws Exception {
        final Table<Integer> table = new SimpleTable<>(columns);
        final Collection<Variable[]> rows = this.getTable(method, oid, columns.size());
        for(final Variable[] row: rows)
            table.addRow(new HashMap<Integer, Object>(){{
                for(int i = 0; i < row.length; i++){
                    final Integer column = new Integer(i + 2);
                    put(column, deserialize(row[i], columns.get(column)));
                }
            }});
        return table;
    }

    /**
     * Attemps to write table, return PDU response as a result of writing
     * @param tablePrefix
     * @param table
     * @return
     * @throws IOException
     */
    public PDU writeTable(final String tablePrefix, final Table<Integer> table) throws IOException {
        final PDU pdu = DefaultPDUFactory.createPDU(this.getTarget().getVersion());
        pdu.setType(PDU.SET);
        //add rows
        for(int i = 0; i < table.getRowCount(); i++){
            //iterate through each column
            final Integer rowIndex = i;
            for(final Integer column: table.getColumns()){
                final OID rowId = new OID(tablePrefix + "." + column + "." + (rowIndex + 1));
                pdu.add(new VariableBinding(rowId, (Variable)table.getCell(column, rowIndex)));
            }
        }
        final PDU response = this.set(pdu).getResponse();
        return response;
        //assertEquals(response.getErrorStatusText(), SnmpConstants.SNMP_ERROR_SUCCESS, response.getErrorStatus());
    }


}
