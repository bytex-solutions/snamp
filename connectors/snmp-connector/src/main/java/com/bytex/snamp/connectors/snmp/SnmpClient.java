package com.bytex.snamp.connectors.snmp;

import com.google.common.base.Stopwatch;
import com.google.common.base.Supplier;
import com.google.common.collect.Maps;
import com.google.common.util.concurrent.AbstractFuture;
import com.bytex.snamp.Aggregator;
import com.bytex.snamp.ArrayUtils;
import com.bytex.snamp.TimeSpan;
import com.bytex.snamp.concurrent.SynchronizationEvent;
import org.snmp4j.*;
import org.snmp4j.event.ResponseEvent;
import org.snmp4j.event.ResponseListener;
import org.snmp4j.mp.MPv2c;
import org.snmp4j.mp.MPv3;
import org.snmp4j.mp.SnmpConstants;
import org.snmp4j.security.*;
import org.snmp4j.smi.*;
import org.snmp4j.transport.DefaultUdpTransportMapping;
import org.snmp4j.util.*;

import java.io.Closeable;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Represents SNMP client.
 * @author Roman Sakno
 * @version 1.0
 * @since 1.0
 */
abstract class SnmpClient extends Snmp implements Closeable, Aggregator {
    private static final AtomicInteger engineBoots = new AtomicInteger(0);

    private static final class SnmpResponseListener extends SynchronizationEvent<ResponseEvent> implements ResponseListener {
        public SnmpResponseListener() {
            super(false);
        }

        @Override
        public void onResponse(final ResponseEvent event) {
            fire(event);
        }
    }

    private static final class SnmpTreeListener extends AbstractFuture<Collection<VariableBinding>> implements TreeListener {
        private final Collection<VariableBinding> bindings;

        private SnmpTreeListener(final int capacity) {
            bindings = new Vector<>(capacity);
        }

        /**
         * Consumes the next table event, which is typically the next row in a
         * table retrieval operation.
         *
         * @param event a <code>TableEvent</code> instance.
         * @return <code>true</code> if this listener wants to receive more events,
         * otherwise return <code>false</code>. For example, a
         * <code>TreeListener</code> can return <code>false</code> to stop
         * tree retrieval.
         */
        @Override
        @SuppressWarnings("ThrowableResultOfMethodCallIgnored")
        public boolean next(final TreeEvent event) {
            final VariableBinding[] attachments = event.getVariableBindings();
            if (attachments != null)
                Collections.addAll(bindings, event.getVariableBindings());
            else if (event.isError()) {
                Exception e = event.getException();
                if(e == null) e = new IOException(event.getErrorMessage());
                setException(e);
                return false;
            }
            return true;
        }

        /**
         * Indicates in a series of tree events that no more events will follow.
         *
         * @param event a <code>TreeEvent</code> instance that will either indicate an error
         *              ({@link org.snmp4j.util.TreeEvent#isError()} returns <code>true</code>) or success
         *              of the tree retrieval operation.
         */
        @Override
        @SuppressWarnings("ThrowableResultOfMethodCallIgnored")
        public void finished(final TreeEvent event) {
            final VariableBinding[] attachments = event.getVariableBindings();
            if (attachments != null)
                Collections.addAll(bindings, event.getVariableBindings());
            else if (event.isError()) {
                Exception e = event.getException();
                if(e == null) e = new IOException(event.getErrorMessage());
                setException(e);
                return;
            }
            set(bindings);
        }

        /**
         * Indicates whether the tree walk is complete or not.
         *
         * @return <code>true</code> if it is complete, <code>false</code> otherwise.
         * @since 1.10
         */
        @Override
        public boolean isFinished() {
            return isDone();
        }
    }

    private SnmpClient(final MessageDispatcher dispatcher,
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
                                    final Address localAddress,
                                    final int socketTimeout,
                                    final Supplier<ExecutorService> threadPoolFactory) throws IOException {
        if(userName == null ||
                userName.length() == 0 ||
                password == null ||
                password.length() == 0)
            return create(connectionAddress, new OctetString("public"), localAddress, socketTimeout, threadPoolFactory);
        final SecurityLevel secLevel = encryptionProtocol != null && encryptionProtocol.size() > 0 &&
                encryptionKey != null && encryptionKey.length() > 0 ?
            SecurityLevel.authPriv : SecurityLevel.authNoPriv;
        final ExecutorService threadPool = threadPoolFactory.get();
        final MessageDispatcher dispatcher = new ConcurrentMessageDispatcher(threadPool);
        final USM userModel = new USM(DefaultSecurityProtocols.getInstance(), new OctetString(MPv3.createLocalEngineID()), engineBoots.getAndIncrement());
        userModel.addUser(userName, engineID,
                new UsmUser(userName, authenticationProtocol, password, encryptionProtocol, encryptionKey));
        dispatcher.addMessageProcessingModel(new MPv3(userModel));
        final DefaultUdpTransportMapping transport = localAddress instanceof UdpAddress ? new DefaultUdpTransportMapping((UdpAddress)localAddress) : new DefaultUdpTransportMapping();
        transport.setSocketTimeout(socketTimeout);
        return new SnmpClient(dispatcher, transport) {
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
                    timeout = TimeSpan.ofMillis(MAX_TIMEOUT);
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

            @Override
            public <T> T queryObject(final Class<T> objectType) {
                return objectType.isInstance(threadPool) ? objectType.cast(threadPool) : null;
            }

            @Override
            public void close() throws IOException {
                try {
                    super.close();
                }
                finally {
                    threadPool.shutdown();
                }
            }
        };
    }

    public static SnmpClient create(final Address connectionAddress,
                                    final OctetString community,
                                    final Address localAddress,
                                    final int socketTimeout,
                                    final Supplier<ExecutorService> threadPoolFactory) throws IOException{
        final ExecutorService threadPool = threadPoolFactory.get();
        final MessageDispatcher dispatcher = new ConcurrentMessageDispatcher(threadPool);
        dispatcher.addMessageProcessingModel(new MPv2c());
        final DefaultUdpTransportMapping transport = localAddress instanceof UdpAddress ? new DefaultUdpTransportMapping((UdpAddress)localAddress) : new DefaultUdpTransportMapping();
        transport.setSocketTimeout(socketTimeout);
        return new SnmpClient(dispatcher, transport){
            @Override
            protected Target createTarget(TimeSpan timeout) {
                final CommunityTarget target = new CommunityTarget(connectionAddress, community);
                target.setVersion(SnmpConstants.version2c);
                target.setRetries(1);
                final long MAX_TIMEOUT = Long.MAX_VALUE / 100;
                if(timeout == TimeSpan.INFINITE || timeout.toMillis() > MAX_TIMEOUT)
                    timeout = TimeSpan.ofMillis(MAX_TIMEOUT);
                target.setTimeout(timeout.toMillis());
                return target;
            }

            @Override
            protected PDU createPDU(final int pduType) {
                final PDU result = new PDU();
                result.setType(pduType);
                return result;
            }

            @Override
            public <T> T queryObject(final Class<T> objectType) {
                return objectType.isInstance(threadPool) ? objectType.cast(threadPool) : null;
            }

            @Override
            public void close() throws IOException {
                try {
                    super.close();
                }
                finally {
                    threadPool.shutdown();
                }
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
    private static ResponseEvent waitForResponseEvent(final SynchronizationEvent.EventAwaitor<ResponseEvent> awaitor, final TimeSpan timeout) throws TimeoutException, IOException, InterruptedException {
        final ResponseEvent response = awaitor.await(timeout);
        if(response == null || response.getResponse() == null) throw new TimeoutException("PDU sending timeout.");
        else if(response.getError() != null)
            if(response.getError() instanceof IOException)
                throw (IOException)response.getError();
            else throw new IOException(response.getError());
        else return response;
    }

    @SuppressWarnings("ThrowableResultOfMethodCallIgnored")
    public final ResponseEvent send(final PDU data, final TimeSpan timeout) throws IOException, TimeoutException, InterruptedException {
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
            final int num = Integer.parseInt(result.substring(lastDot + 1)) + 1;
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
        final Map<OID, Variable> result = Maps.newHashMapWithExpectedSize(variables.length);
        for (final VariableBinding binding : response.getResponse().getVariableBindings())
            result.put(binding.getOid(), binding.getVariable());
        return result;
    }

    public final Variable get(final OID variable, final TimeSpan timeout) throws IOException, TimeoutException, InterruptedException {
        return get(new OID[]{variable}, timeout).get(variable);
    }

    public final Map<OID, Variable> get(final OID[] variables, final TimeSpan timeout) throws IOException, TimeoutException, InterruptedException {
        return get(PDU.GET, variables, timeout);
    }

    public final Map<OID, Variable> getBulk(final OID[] variables, final TimeSpan timeout) throws IOException, TimeoutException, InterruptedException {
        return get(PDU.GETBULK, variables, timeout);
    }

    public final void set(final VariableBinding[] variables, final TimeSpan timeout) throws IOException, TimeoutException, InterruptedException {
        final PDU request = createPDU(PDU.SET);
        for (final VariableBinding v : variables)
            request.add(v);
        final ResponseEvent response = send(request, timeout);
        if (response.getResponse().getErrorStatus() != SnmpConstants.SNMP_ERROR_SUCCESS)
            throw new IOException(String.format("Unable to set %s variables. Status is %s(%s).", Arrays.toString(variables), response.getResponse().getErrorStatusText(), response.getResponse().getErrorStatus()));
    }

    public final void walk(final OID root, final TimeSpan timeout, final Collection<VariableBinding> output) throws TimeoutException, InterruptedException, ExecutionException {
        final TreeUtils tree = new TreeUtils(this, new DefaultPDUFactory());
        final SnmpTreeListener listener = new SnmpTreeListener(100);
        tree.walk(createTarget(timeout), new OID[]{root}, null, listener);
        output.addAll(listener.get(timeout.toMillis(), TimeUnit.MILLISECONDS));
    }

    public final Collection<VariableBinding> walk(TimeSpan timeout) throws TimeoutException, InterruptedException, ExecutionException {
        final Collection<VariableBinding> result = new Vector<>(20);
        final Stopwatch timer = Stopwatch.createStarted();
        for(int i = 1; i <= 10; i++) {
             timeout = timeout == TimeSpan.INFINITE ?
                    TimeSpan.INFINITE:
                    timeout.subtract(timer.elapsed(TimeUnit.MILLISECONDS), TimeUnit.MILLISECONDS);
            walk(new OID(new int[]{i}), timeout, result);
            if(TimeSpan.ZERO.equals(timeout))
                throw new TimeoutException(String.format("Not enough time to collect all variables. Loop stopped at %s iteration.", i));
        }
        return result;
    }

    public final void set(final Map<OID, Variable> variables, final TimeSpan timeout) throws IOException, TimeoutException, InterruptedException{
        final Collection<VariableBinding> bindings = new ArrayList<>(variables.size());
        for(final Map.Entry<OID, Variable> entry: variables.entrySet())
            bindings.add(new VariableBinding(entry.getKey(), entry.getValue()));
        set(ArrayUtils.toArray(bindings, VariableBinding.class), timeout);
    }
}
