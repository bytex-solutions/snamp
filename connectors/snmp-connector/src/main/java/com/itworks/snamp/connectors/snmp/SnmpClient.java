package com.itworks.snamp.connectors.snmp;

import com.itworks.snamp.SynchronizationEvent;
import com.itworks.snamp.TimeSpan;
import org.snmp4j.*;
import org.snmp4j.event.ResponseEvent;
import org.snmp4j.event.ResponseListener;
import org.snmp4j.mp.MPv2c;
import org.snmp4j.mp.SnmpConstants;
import org.snmp4j.smi.*;
import org.snmp4j.transport.DefaultUdpTransportMapping;
import org.snmp4j.util.DefaultPDUFactory;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * Represents SNMP client.
 * @author Roman Sakno
 * @version 1.0
 * @since 1.0
 */
abstract class SnmpClient extends Snmp {
    private static final class SnmpResponseListener extends SynchronizationEvent<ResponseEvent> implements ResponseListener{
        public SnmpResponseListener(){
            super(false);
        }

        @Override
        public void onResponse(final ResponseEvent event) {
            fire(event);
        }
    }

    private SnmpClient(final MessageDispatcher dispatcher,
                       final TransportMapping<?> transport){
        super(dispatcher, transport);
    }

    public static SnmpClient createClient(final Address connectionAddress,
                                          final OctetString community,
                                          final Address localAddress) throws IOException{
        final MessageDispatcher dispatcher = new MessageDispatcherImpl();
        dispatcher.addMessageProcessingModel(new MPv2c(new DefaultPDUFactory()));
        return new SnmpClient(dispatcher, localAddress instanceof UdpAddress ? new DefaultUdpTransportMapping((UdpAddress)localAddress) : new DefaultUdpTransportMapping()){
            @Override
            protected Target createTarget(TimeSpan timeout) {
                final CommunityTarget target = new CommunityTarget();
                target.setCommunity(community);
                target.setAddress(connectionAddress);
                target.setVersion(SnmpConstants.version2c);
                target.setRetries(1);
                final long MAX_TIMEOUT = Long.MAX_VALUE / 100;
                if(timeout == TimeSpan.INFINITE || timeout.convert(TimeUnit.MILLISECONDS).duration > MAX_TIMEOUT)
                    timeout = new TimeSpan(MAX_TIMEOUT);
                target.setTimeout(timeout.convert(TimeUnit.MILLISECONDS).duration);
                return target;
            }
        };
    }

    public final Address[] getClientAddresses(){
        final Collection<TransportMapping> mappings = getMessageDispatcher().getTransportMappings();
        final Address[] result = new Address[mappings.size()];
        int i = 0;
        for(final TransportMapping m: mappings)
            result[i++] = m.getListenAddress();
        return result;
    }

    protected abstract Target createTarget(final TimeSpan timeout);

    private static ResponseEvent waitForResponseEvent(final SynchronizationEvent.Awaitor<ResponseEvent> awaitor, final TimeSpan timeout) throws TimeoutException, IOException, InterruptedException {
        final ResponseEvent response = awaitor.await(timeout);
        if(response == null || response.getResponse() == null) throw new TimeoutException(String.format("PDU sending timeout."));
        else if(response.getError() != null)
            if(response.getError() instanceof IOException)
                throw (IOException)response.getError();
            else throw new IOException(response.getError());
        else return response;
    }

    @SuppressWarnings("ThrowableResultOfMethodCallIgnored")
    public ResponseEvent send(final PDU data, final TimeSpan timeout) throws IOException, TimeoutException, InterruptedException {
        final SnmpResponseListener listener = new SnmpResponseListener();
        send(data, createTarget(timeout), null, listener);
        return waitForResponseEvent(listener.getAwaitor(), timeout);
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

    private static PDU createPDU(final Target target, final int pduType){
        final PDU request = DefaultPDUFactory.createPDU(target, pduType);
        request.setMaxRepetitions(10);
        request.setNonRepeaters(1);
        return request;
    }

    private Map<OID, Variable> get(final int pduType, final OID[] variables, final TimeSpan timeout) throws IOException, TimeoutException, InterruptedException {
        final PDU request = createPDU(createTarget(timeout), pduType);
        if (pduType == PDU.GETBULK)
            for (int i = 0; i < variables.length; i++)
                variables[i] = prepareOidForGetBulk(variables[i]);
        for (final OID oid : variables)
            request.add(new VariableBinding(oid));
        final ResponseEvent response = send(request, timeout);
        final Map<OID, Variable> result = new HashMap<>(variables.length);
        for (final VariableBinding binding : response.getResponse().getVariableBindings())
            result.put(binding.getOid(), binding.getVariable());
        return result;
    }

    public Variable get(final OID variable, final TimeSpan timeout) throws IOException, TimeoutException, InterruptedException {
        return get(new OID[]{variable}, timeout).get(variable);
    }

    public Map<OID, Variable> get(final OID[] variables, final TimeSpan timeout) throws IOException, TimeoutException, InterruptedException {
        return get(PDU.GET, variables, timeout);
    }

    public Map<OID, Variable> getBulk(final OID[] variables, final TimeSpan timeout) throws IOException, TimeoutException, InterruptedException {
        return get(PDU.GETBULK, variables, timeout);
    }

    public void set(final VariableBinding[] variables, final TimeSpan timeout) throws IOException, TimeoutException, InterruptedException {
        final PDU request = createPDU(createTarget(timeout), PDU.SET);
        for(final VariableBinding v: variables)
            request.add(v);
        final ResponseEvent response = send(request, timeout);
        if(response == null) throw new TimeoutException(String.format("Unable to set variables %s. Timeout reached.", Arrays.toString(variables)));
        else if(response.getResponse().getErrorStatus() != SnmpConstants.SNMP_ERROR_SUCCESS)
            throw new IOException(String.format("Unable to set %s variables. Status is %s(%s).", Arrays.toString(variables), response.getResponse().getErrorStatusText(), response.getResponse().getErrorStatus()));
    }

    public void set(final Map<OID, Variable> variables, final TimeSpan timeout) throws IOException, TimeoutException, InterruptedException{
        final Collection<VariableBinding> bindings = new ArrayList<>(variables.size());
        for(final OID key: variables.keySet())
            bindings.add(new VariableBinding(key, variables.get(key)));
        set(bindings.toArray(new VariableBinding[bindings.size()]), timeout);
    }
}
