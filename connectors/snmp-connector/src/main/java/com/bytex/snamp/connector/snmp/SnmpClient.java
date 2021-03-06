package com.bytex.snamp.connector.snmp;

import com.bytex.snamp.Aggregator;
import com.google.common.base.Stopwatch;
import com.google.common.collect.Maps;
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

import javax.annotation.Nonnull;
import java.io.Closeable;
import java.io.IOException;
import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import static com.bytex.snamp.ArrayUtils.emptyArray;

/**
 * Represents SNMP client.
 * @author Roman Sakno
 * @version 2.0
 * @since 1.0
 */
abstract class SnmpClient extends Snmp implements Closeable, Aggregator {
    private static final AtomicInteger engineBoots = new AtomicInteger(0);

    private static final class SnmpResponseListener extends CompletableFuture<ResponseEvent> implements ResponseListener {

        @Override
        public void onResponse(final ResponseEvent event) {
            complete(event);
        }
    }

    private static final class SnmpTreeListener extends CompletableFuture<Collection<VariableBinding>> implements TreeListener {
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
                completeExceptionally(e);
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
                completeExceptionally(e);
                return;
            }
            complete(bindings);
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

    static SnmpClient create(final Address connectionAddress,
                             final OctetString engineID,
                             final OctetString userName,
                             final OID authenticationProtocol,
                             final OctetString password,
                             final OID encryptionProtocol,
                             final OctetString encryptionKey,
                             final OctetString contextName,
                             final Address localAddress,
                             final int socketTimeout,
                             final ExecutorService threadPool) throws IOException {
        if(userName == null ||
                userName.length() == 0 ||
                password == null ||
                password.length() == 0)
            return create(connectionAddress, new OctetString("public"), localAddress, socketTimeout, threadPool);
        final SecurityLevel secLevel = encryptionProtocol != null && encryptionProtocol.size() > 0 &&
                encryptionKey != null && encryptionKey.length() > 0 ?
            SecurityLevel.authPriv : SecurityLevel.authNoPriv;
        final MessageDispatcher dispatcher = new ConcurrentMessageDispatcher(threadPool);
        final USM userModel = new USM(DefaultSecurityProtocols.getInstance(), new OctetString(MPv3.createLocalEngineID()), engineBoots.getAndIncrement());
        userModel.addUser(userName, engineID,
                new UsmUser(userName, authenticationProtocol, password, encryptionProtocol, encryptionKey));
        dispatcher.addMessageProcessingModel(new MPv3(userModel));
        final DefaultUdpTransportMapping transport = localAddress instanceof UdpAddress ? new DefaultUdpTransportMapping((UdpAddress)localAddress) : new DefaultUdpTransportMapping();
        transport.setSocketTimeout(socketTimeout);
        return new SnmpClient(dispatcher, transport) {
            @Override
            protected Target createTarget(Duration timeout) {
                final UserTarget target = new UserTarget(connectionAddress,
                        userName,
                        engineID.getValue(),
                        secLevel.getSnmpValue());
                target.setSecurityModel(SecurityModel.SECURITY_MODEL_USM);
                target.setRetries(1);
                final long MAX_TIMEOUT = Long.MAX_VALUE / 100;
                if(timeout == null || timeout.toMillis() > MAX_TIMEOUT)
                    timeout = Duration.ofMillis(MAX_TIMEOUT);
                target.setTimeout(timeout.toMillis());
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
            public <T> Optional<T> queryObject(@Nonnull final Class<T> objectType) {
                final Optional<?> result;
                if (objectType.isInstance(this))
                    result = Optional.of(this);
                else if (objectType.isInstance(threadPool))
                    result = Optional.of(threadPool);
                else
                    result = Optional.empty();
                return result.map(objectType::cast);
            }
        };
    }

    static SnmpClient create(final Address connectionAddress,
                             final OctetString community,
                             final Address localAddress,
                             final int socketTimeout,
                             final ExecutorService threadPool) throws IOException{
        final MessageDispatcher dispatcher = new ConcurrentMessageDispatcher(threadPool);
        dispatcher.addMessageProcessingModel(new MPv2c());
        final DefaultUdpTransportMapping transport = localAddress instanceof UdpAddress ? new DefaultUdpTransportMapping((UdpAddress)localAddress) : new DefaultUdpTransportMapping();
        transport.setSocketTimeout(socketTimeout);
        return new SnmpClient(dispatcher, transport){
            @Override
            protected Target createTarget(Duration timeout) {
                final CommunityTarget target = new CommunityTarget(connectionAddress, community);
                target.setVersion(SnmpConstants.version2c);
                target.setRetries(1);
                final long MAX_TIMEOUT = Long.MAX_VALUE / 100;
                if(timeout == null || timeout.toMillis() > MAX_TIMEOUT)
                    timeout = Duration.ofMillis(MAX_TIMEOUT);
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
            public <T> Optional<T> queryObject(@Nonnull final Class<T> objectType) {
                final Optional<?> result;
                if(objectType.isInstance(this))
                    result = Optional.of(this);
                else if(objectType.isInstance(threadPool))
                    result = Optional.of(threadPool);
                else
                    result = Optional.empty();
                return result.map(objectType::cast);
            }
        };
    }

    protected abstract PDU createPDU(final int pduType);

    final Address[] getClientAddresses(){
        return getMessageDispatcher().getTransportMappings().stream()
                .map(TransportMapping::getListenAddress)
                .toArray(Address[]::new);
    }

    protected abstract Target createTarget(final Duration timeout);

    private static ResponseEvent waitForResponseEvent(final Future<ResponseEvent> awaitor, final Duration timeout) throws TimeoutException, IOException, InterruptedException, ExecutionException {
        final ResponseEvent response = timeout == null ? awaitor.get() : awaitor.get(timeout.toNanos(), TimeUnit.NANOSECONDS);
        if(response == null || response.getResponse() == null) throw new TimeoutException("PDU sending timeout.");
        else if(response.getError() != null)
            if(response.getError() instanceof IOException)
                throw (IOException)response.getError();
            else throw new IOException(response.getError());
        else return response;
    }

    private ResponseEvent send(final PDU data, final Duration timeout) throws IOException, TimeoutException, InterruptedException, ExecutionException {
        final SnmpResponseListener listener = new SnmpResponseListener();
        send(data, createTarget(timeout), null, listener);
        return waitForResponseEvent(listener, timeout);
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

    private Map<OID, Variable> get(final int pduType, final OID[] variables, final Duration timeout) throws IOException, TimeoutException, InterruptedException, ExecutionException {
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

    final Variable get(final OID variable, final Duration timeout) throws IOException, TimeoutException, InterruptedException, ExecutionException {
        return get(new OID[]{variable}, timeout).get(variable);
    }

    private Map<OID, Variable> get(final OID[] variables, final Duration timeout) throws IOException, TimeoutException, InterruptedException, ExecutionException {
        return get(PDU.GET, variables, timeout);
    }

    public final Map<OID, Variable> getBulk(final OID[] variables, final Duration timeout) throws IOException, TimeoutException, InterruptedException, ExecutionException {
        return get(PDU.GETBULK, variables, timeout);
    }

    private void set(final VariableBinding[] variables, final Duration timeout) throws IOException, TimeoutException, InterruptedException, ExecutionException {
        final PDU request = createPDU(PDU.SET);
        for (final VariableBinding v : variables)
            request.add(v);
        final ResponseEvent response = send(request, timeout);
        if (response.getResponse().getErrorStatus() != SnmpConstants.SNMP_ERROR_SUCCESS)
            throw new IOException(String.format("Unable to set %s variables. Status is %s(%s).", Arrays.toString(variables), response.getResponse().getErrorStatusText(), response.getResponse().getErrorStatus()));
    }

    private void walk(final OID root, final Duration timeout, final Collection<VariableBinding> output) throws TimeoutException, InterruptedException, ExecutionException {
        final TreeUtils tree = new TreeUtils(this, new DefaultPDUFactory());
        final SnmpTreeListener listener = new SnmpTreeListener(100);
        tree.walk(createTarget(timeout), new OID[]{root}, null, listener);
        output.addAll(listener.get(timeout.toNanos(), TimeUnit.NANOSECONDS));
    }

    final Collection<VariableBinding> walk(Duration timeout) throws TimeoutException, InterruptedException, ExecutionException {
        final Collection<VariableBinding> result = new Vector<>(20);
        final Stopwatch timer = Stopwatch.createStarted();
        for(int i = 1; i <= 10; i++) {
             timeout = timeout == null ?
                    null :
                    timeout.minus(timer.elapsed(TimeUnit.NANOSECONDS), ChronoUnit.NANOS);
            walk(new OID(new int[]{i}), timeout, result);
            if(Duration.ZERO.equals(timeout))
                throw new TimeoutException(String.format("Not enough time to collect all variables. Loop stopped at %s iteration.", i));
        }
        return result;
    }

    final void set(final Map<OID, Variable> variables, final Duration timeout) throws IOException, TimeoutException, InterruptedException, ExecutionException {
        final Collection<VariableBinding> bindings = variables.entrySet().stream()
                .map(entry -> new VariableBinding(entry.getKey(), entry.getValue()))
                .collect(Collectors.toList());
        set(bindings.toArray(emptyArray(VariableBinding[].class)), timeout);
    }
}
