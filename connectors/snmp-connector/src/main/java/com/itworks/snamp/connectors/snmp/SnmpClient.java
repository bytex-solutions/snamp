package com.itworks.snamp.connectors.snmp;

import com.itworks.snamp.SynchronizationEvent;
import com.itworks.snamp.TimeSpan;
import org.snmp4j.*;
import org.snmp4j.event.ResponseEvent;
import org.snmp4j.event.ResponseListener;
import org.snmp4j.mp.MPv2c;
import org.snmp4j.mp.MPv3;
import org.snmp4j.mp.SnmpConstants;
import org.snmp4j.security.*;
import org.snmp4j.smi.*;
import org.snmp4j.transport.DefaultUdpTransportMapping;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Represents SNMP client.
 * @author Roman Sakno
 * @version 1.0
 * @since 1.0
 */
abstract class SnmpClient extends Snmp {
    private static final AtomicInteger engineBoots = new AtomicInteger(0);
    private static final class SnmpResponseListener extends SynchronizationEvent<ResponseEvent> implements ResponseListener{
        public SnmpResponseListener(){
            super(false);
        }

        @Override
        public void onResponse(final ResponseEvent event) {
            fire(event);
        }
    }

    SnmpClient(final MessageDispatcher dispatcher,
                       final TransportMapping<?> transport){
        super(dispatcher, transport);
    }

    public static SnmpClient create(final Address connectionAddress,
                                    final OctetString engineID,
                                    final OctetString userName,
                                    final OID authenticationProtocol,
                                    final OctetString password,
                                    final OID encryptionProtocol,
                                    final OctetString encryptionKey,
                                    final OctetString contextName,
                                    final Address localAddress) throws IOException {
        if(userName == null ||
                userName.length() == 0 ||
                password == null ||
                password.length() == 0)
            return create(connectionAddress, new OctetString("public"), localAddress);
        final SecurityLevel secLevel = encryptionProtocol != null && encryptionProtocol.size() > 0 &&
                encryptionKey != null && encryptionKey.length() > 0 ?
            SecurityLevel.authPriv : SecurityLevel.authNoPriv;
        final MessageDispatcher dispatcher = new MessageDispatcherImpl();
        final USM userModel = new USM(SecurityProtocols.getInstance(), new OctetString(MPv3.createLocalEngineID()), engineBoots.getAndIncrement());
        userModel.addUser(userName, engineID,
                new UsmUser(userName, authenticationProtocol, password, encryptionProtocol, encryptionKey));
        dispatcher.addMessageProcessingModel(new MPv3(userModel));
        return new SnmpClient(dispatcher, localAddress instanceof UdpAddress ? new DefaultUdpTransportMapping((UdpAddress)localAddress) : new DefaultUdpTransportMapping()) {
            @Override
            protected Target createTarget(TimeSpan timeout) {
                final UserTarget target = new UserTarget(connectionAddress,
                        userName,
                        engineID.getValue(),
                        secLevel.getSnmpValue());
                target.setSecurityModel(SecurityModel.SECURITY_MODEL_USM);
                target.setRetries(1);
                final long MAX_TIMEOUT = Long.MAX_VALUE / 100;
                if(timeout == TimeSpan.INFINITE || timeout.convert(TimeUnit.MILLISECONDS).duration > MAX_TIMEOUT)
                    timeout = new TimeSpan(MAX_TIMEOUT);
                target.setTimeout(timeout.convert(TimeUnit.MILLISECONDS).duration);
                target.setVersion(SnmpConstants.version3);
                return target;
            }

            @Override
            protected ScopedPDU createPDU(final int pduType) {
                final ScopedPDU result = new ScopedPDU();
                result.setType(pduType);
                if(contextName != null && contextName.length() > 0)
                    result.setContextName(contextName);
                return result;
            }
        };
    }

    public static SnmpClient create(final Address connectionAddress,
                                    final OctetString community,
                                    final Address localAddress) throws IOException{
        final MessageDispatcher dispatcher = new MessageDispatcherImpl();
        dispatcher.addMessageProcessingModel(new MPv2c());
        return new SnmpClient(dispatcher, localAddress instanceof UdpAddress ? new DefaultUdpTransportMapping((UdpAddress)localAddress) : new DefaultUdpTransportMapping()){
            @Override
            protected Target createTarget(TimeSpan timeout) {
                final CommunityTarget target = new CommunityTarget(connectionAddress, community);
                target.setVersion(SnmpConstants.version2c);
                target.setRetries(1);
                final long MAX_TIMEOUT = Long.MAX_VALUE / 100;
                if(timeout == TimeSpan.INFINITE || timeout.convert(TimeUnit.MILLISECONDS).duration > MAX_TIMEOUT)
                    timeout = new TimeSpan(MAX_TIMEOUT);
                target.setTimeout(timeout.convert(TimeUnit.MILLISECONDS).duration);
                return target;
            }

            @Override
            protected PDU createPDU(final int pduType) {
                final PDU result = new PDU();
                result.setType(pduType);
                return result;
            }
        };
    }

    protected abstract PDU createPDU(final int pduType);

    public final Address[] getClientAddresses(){
        final Collection<TransportMapping> mappings = getMessageDispatcher().getTransportMappings();
        final Address[] result = new Address[mappings.size()];
        int i = 0;
        for(final TransportMapping m: mappings)
            result[i++] = m.getListenAddress();
        return result;
    }

    protected abstract Target createTarget(final TimeSpan timeout);

    @SuppressWarnings("ThrowableResultOfMethodCallIgnored")
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

    private Map<OID, Variable> get(final int pduType, final OID[] variables, final TimeSpan timeout) throws IOException, TimeoutException, InterruptedException {
        final PDU request = createPDU(pduType);
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
        final PDU request = createPDU(PDU.SET);
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
