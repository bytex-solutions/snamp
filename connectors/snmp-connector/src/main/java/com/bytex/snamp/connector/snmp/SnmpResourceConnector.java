package com.bytex.snamp.connector.snmp;

import com.bytex.snamp.Aggregator;
import com.bytex.snamp.ArrayUtils;
import com.bytex.snamp.concurrent.AbstractConcurrentResourceAccessor;
import com.bytex.snamp.concurrent.ConcurrentResourceAccessor;
import com.bytex.snamp.concurrent.LazyReference;
import com.bytex.snamp.configuration.ManagedResourceInfo;
import com.bytex.snamp.connector.AbstractManagedResourceConnector;
import com.bytex.snamp.connector.ResourceEventListener;
import com.bytex.snamp.connector.attributes.AbstractAttributeRepository;
import com.bytex.snamp.connector.attributes.AbstractOpenAttributeInfo;
import com.bytex.snamp.connector.attributes.AttributeDescriptor;
import com.bytex.snamp.connector.attributes.AttributeSpecifier;
import com.bytex.snamp.connector.metrics.MetricsSupport;
import com.bytex.snamp.connector.notifications.AbstractNotificationInfo;
import com.bytex.snamp.connector.notifications.AccurateNotificationRepository;
import com.bytex.snamp.connector.notifications.NotificationDescriptor;
import com.bytex.snamp.core.ClusterMember;
import com.bytex.snamp.core.LoggerProvider;
import com.bytex.snamp.core.SharedCounter;
import com.bytex.snamp.internal.Utils;
import com.bytex.snamp.io.Buffers;
import com.bytex.snamp.jmx.CompositeDataBuilder;
import com.bytex.snamp.jmx.JMExceptionUtils;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import org.osgi.framework.BundleContext;
import org.snmp4j.CommandResponder;
import org.snmp4j.CommandResponderEvent;
import org.snmp4j.PDU;
import org.snmp4j.jmx.SnmpNotification;
import org.snmp4j.smi.*;

import javax.annotation.Nonnull;
import javax.management.*;
import javax.management.openmbean.*;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.text.ParseException;
import java.time.Duration;
import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeoutException;
import java.util.function.Function;
import java.util.logging.Level;
import java.util.logging.Logger;

import static com.bytex.snamp.connector.snmp.SnmpConnectorDescriptionProvider.MESSAGE_OID_PARAM;
import static com.bytex.snamp.connector.snmp.SnmpConnectorDescriptionProvider.SNMP_CONVERSION_FORMAT_PARAM;
import static com.bytex.snamp.core.SharedObjectType.COUNTER;
import static com.bytex.snamp.internal.Utils.callUnchecked;


/**
 * Represents SNMP-compliant managed resource.
 * This class cannot be inherited.
 * @author Roman Sakno
 * @version 2.0
 * @since 1.0
 */
final class SnmpResourceConnector extends AbstractManagedResourceConnector {
    private static final class SnmpNotificationInfo extends AbstractNotificationInfo {
        private static final long serialVersionUID = -4792879013459588079L;
        private final LazyReference<OID> notificationID;

        private SnmpNotificationInfo(final String listID,
                                     final NotificationDescriptor descriptor){
            super(listID, getDescription(descriptor), descriptor);
            notificationID = LazyReference.strong();
        }

        private static String getDescription(final NotificationDescriptor descriptor){
            final String result = descriptor.getDescription();
            return result.isEmpty() ? "SNMP Trap" : result;
        }

        OID getNotificationID(){
            return notificationID.lazyGet(this, metadata -> new OID(NotificationDescriptor.getName(metadata)));
        }
    }

    private static final class SnmpNotificationRepository extends AccurateNotificationRepository<SnmpNotificationInfo> implements CommandResponder{
        private final AbstractConcurrentResourceAccessor<SnmpClient> client;
        private final SharedCounter sequenceNumberGenerator;
        private final ExecutorService listenerInvoker;

        private SnmpNotificationRepository(final String resourceName,
                                           final AbstractConcurrentResourceAccessor<SnmpClient> client,
                                           final BundleContext context){
            super(resourceName,
                    SnmpNotificationInfo.class);
            this.client = client;
            listenerInvoker = client.read(cl -> cl.queryObject(ExecutorService.class)).orElseThrow(AssertionError::new);
            sequenceNumberGenerator = ClusterMember.get(context).getService("notifications-".concat(resourceName), COUNTER).orElseThrow(AssertionError::new);
        }

        /**
         * Gets an executor used to execute event listeners.
         *
         * @return Executor service.
         */
        @Nonnull
        @Override
        protected ExecutorService getListenerExecutor() {
            return listenerInvoker;
        }

        private Logger getLogger(){
            return LoggerProvider.getLoggerForObject(this);
        }

        @Override
        protected SnmpNotificationInfo connectNotifications(final String category,
                                                           final NotificationDescriptor metadata) throws ParseException {
            final SnmpNotificationInfo result = new SnmpNotificationInfo(category, metadata);
            //enable for a first time only
            if (hasNoNotifications())
                client.write(client -> {
                    client.addCommandResponder(this);
                    return null;
                });
            return result;
        }

        @Override
        protected void disconnectNotifications(final SnmpNotificationInfo metadata) {
            if (hasNoNotifications())
                try {
                    client.write(client -> {
                        client.removeCommandResponder(this);
                        return null;
                    });
                } catch (final Exception e) {
                    getLogger().log(Level.WARNING, String.format("Subscription to SNMP event %s failed",
                            metadata.getNotificationID()), e);
                }
        }

        private void processPdu(final PDU event) {
            if (event.getVariableBindings().isEmpty())
                return;
            final Map<OID, Variable> bindings = Maps.newHashMapWithExpectedSize(event.getVariableBindings().size());
            //tries to detect event category and replace prefixes
            SnmpNotificationInfo notificationInfo = null;
            for (final SnmpNotificationInfo metadata : getNotificationInfo())
                if (notificationInfo == null)
                    for (final VariableBinding binding : event.getVariableBindings()) {
                        if (binding.getOid().startsWith(metadata.getNotificationID())) {
                            final OID postfix = SnmpConnectorHelpers.getPostfix(metadata.getNotificationID(), binding.getOid());
                            if (postfix.size() == 0)
                                continue;
                            notificationInfo = metadata;
                            bindings.put(postfix, binding.getVariable());
                        }
                    }
                else
                    break;
            //unknown notification
            if (bindings.isEmpty() || notificationInfo == null)
                return;
            String message;
            if (notificationInfo.getDescriptor().hasField(MESSAGE_OID_PARAM)) {      //extract message, add attachments
                final OID messageOID = new OID(Objects.toString(notificationInfo.getDescriptor().getFieldValue(MESSAGE_OID_PARAM)));
                final Variable messageContent = bindings.get(messageOID);
                message = messageContent == null ? "" : messageContent.toString();
            } else {//concatenate bindings
                message = String.join(System.lineSeparator(), bindings.values().stream().map(Variable::toString).toArray(String[]::new));
            }
            fire(NotificationDescriptor.getName(notificationInfo), message, bindings);
        }

        private static CompositeData createAttachment(final Map<OID, Variable> bindings) {
            if (bindings.isEmpty()) return null;
            final CompositeDataBuilder result = new CompositeDataBuilder("SNMPTrap", "SNMP Trap");
            bindings.forEach((oid, var) -> {
                if (var instanceof AssignableFromInteger)
                    result.put(oid.toDottedString(), "Integer value", var.toInt());
                else if (var instanceof AssignableFromLong)
                    result.put(oid.toDottedString(), "Long value", var.toLong());
                else if (var instanceof OID)
                    result.put(oid.toDottedString(), "Object Identifier", ((OID) var).toDottedString());
                else if (var instanceof OctetString) {
                    final OctetString str = (OctetString) var;
                    if (str.isPrintable())
                        result.put(oid.toDottedString(), "Octet String", new String(str.toByteArray(), SnmpObjectConverter.SNMP_ENCODING));
                    else
                        result.put(oid.toDottedString(), "Octet String", str.toByteArray());
                } else if (var instanceof Address)
                    result.put(oid.toDottedString(), "Address", var.toString());
                else if (var instanceof AssignableFromIntArray)
                    result.put(oid.toDottedString(), "Array of integers", ((AssignableFromIntArray) var).toIntArray());
                else if (var instanceof AssignableFromByteArray)
                    result.put(oid.toDottedString(), "Array of bytes", ((AssignableFromByteArray) var).toByteArray());
                else
                    result.put(oid.toDottedString(), "Raw value", var.toString());
            });
            return callUnchecked(result::build);
        }

        private static Function<MBeanNotificationInfo, SnmpNotification> notificationFactory(final String message,
                                                                                             final Map<OID, Variable> bindings,
                                                                                             final SharedCounter sequenceNumberGenerator) {
            return holder -> ArrayUtils.getFirst(holder.getNotifTypes()).map(notifType -> {
                final SnmpNotification notification = new SnmpNotification(notifType,
                        notifType,   //will be rewritten in repository
                        sequenceNumberGenerator.getAsLong(),
                        message,
                        bindings);
                notification.setUserData(createAttachment(bindings));
                return notification;
            }).orElse(null);
        }

        private void fire(final String category,
                          final String message,
                          final Map<OID, Variable> bindings) {
            fire(category, notificationFactory(message, bindings, sequenceNumberGenerator));
        }

        /**
         * Process an incoming request, report or notification PDU.
         *
         * @param event a <code>CommandResponderEvent</code> instance containing the PDU to
         *              process and some additional information returned by the message
         *              processing model that decoded the SNMP message.
         */
        @Override
        public void processPdu(final CommandResponderEvent event) {
            processPdu(event.getPDU());
        }
    }

    private static abstract class SnmpAttributeInfo<V extends Variable> extends AbstractOpenAttributeInfo implements SnmpObjectConverter<V> {
        private static final long serialVersionUID = 4948510436343027716L;
        private final LazyReference<OID> attributeID;

        private SnmpAttributeInfo(final String attributeID,
                                  final OpenType<?> openType,
                                  final AttributeDescriptor descriptor) {
            this(attributeID,
                    openType,
                    AttributeSpecifier.READ_WRITE,
                    descriptor);
        }

        private SnmpAttributeInfo(final String attributeID,
                                  final OpenType<?> openType,
                                  final AttributeSpecifier specifier,
                                  final AttributeDescriptor descriptor) {
            super(attributeID,
                    openType,
                    getDescription(descriptor),
                    specifier,
                    descriptor);
            this.attributeID = LazyReference.strong();
        }

        private static String getDescription(final AttributeDescriptor descriptor){
            return descriptor.getAlternativeName().orElse("SNMP Object");
        }

        final InvalidAttributeValueException invalidAttribute(final Object value,
                                                              final Class<V> snmpType){
            return new InvalidAttributeValueException(String.format("Unable convert %s to SNMP %s", value, snmpType));
        }

        OID getAttributeID() {
            return attributeID.lazyGet(this, metadata -> new OID(AttributeDescriptor.getName(metadata)));
        }
    }

    private static final class ReadOnlyAttributeInfo extends SnmpAttributeInfo<Variable>{
        private static final long serialVersionUID = 7929288330407108778L;

        private ReadOnlyAttributeInfo(final String attributeID,
                                      final AttributeDescriptor descriptor){
            super(attributeID, SimpleType.STRING, AttributeSpecifier.READ_ONLY, descriptor);
        }

        @Override
        public Variable convert(final Object value) throws InvalidAttributeValueException {
            throw new InvalidAttributeValueException();
        }

        @Override
        public Object convert(final Variable value) {
            return Objects.toString(value, "");
        }
    }

    private static final class Integer32AttributeInfo extends SnmpAttributeInfo<Integer32>{
        private static final long serialVersionUID = -6050715922710622727L;

        private Integer32AttributeInfo(final String attributeID,
                                       final AttributeDescriptor descriptor) {
            super(attributeID, SimpleType.INTEGER, descriptor);
        }

        @Override
        public Integer32 convert(final Object value) throws InvalidAttributeValueException {
            if(value instanceof Byte)
                return new Integer32((Byte)value);
            else if(value instanceof Short)
                return new Integer32((Short)value);
            else if(value instanceof Integer)
                return new Integer32((Integer)value);
            else throw invalidAttribute(value, Integer32.class);
        }

        @Override
        public Integer convert(final Integer32 value) {
            return value.toInt();
        }
    }

    private static final class Counter32AttributeInfo extends SnmpAttributeInfo<Counter32>{
        private static final long serialVersionUID = 7284093419509518876L;

        private Counter32AttributeInfo(final String attributeID,
                                               final AttributeDescriptor descriptor) {
            super(attributeID, SimpleType.LONG, descriptor);
        }

        @Override
        public Counter32 convert(final Object value) throws InvalidAttributeValueException {
            if(value instanceof Byte)
                return new Counter32((Byte)value);
            else if(value instanceof Short)
                return new Counter32((Short)value);
            else if(value instanceof Integer)
                return new Counter32((Integer)value);
            else if(value instanceof Long)
                return new Counter32((Long)value);
            else throw invalidAttribute(value, Counter32.class);
        }

        @Override
        public Long convert(final Counter32 value) {
            return value.toLong();
        }
    }

    private static final class TimeTicksAttributeInfo extends AbstractFormatterAttributeInfo<TimeTicks> {
        private static final long serialVersionUID = 4620458170097932634L;

        private TimeTicksAttributeInfo(final String attributeID,
                                       final AttributeDescriptor descriptor) {
            super(attributeID, TimeTicksConversionFormat.getFormat(descriptor), descriptor);
        }
    }

    private static final class Counter64AttributeInfo extends SnmpAttributeInfo<Counter64>{
        private static final long serialVersionUID = -8824691251974743887L;

        private Counter64AttributeInfo(final String attributeID,
                                       final AttributeDescriptor descriptor) {
            super(attributeID, SimpleType.LONG, descriptor);
        }

        @Override
        public Counter64 convert(final Object value) throws InvalidAttributeValueException {
            if(value instanceof Byte)
                return new Counter64((Byte)value);
            else if(value instanceof Short)
                return new Counter64((Short)value);
            else if(value instanceof Integer)
                return new Counter64((Integer)value);
            else if(value instanceof Long)
                return new Counter64((Long)value);
            else throw invalidAttribute(value, Counter64.class);
        }

        @Override
        public Long convert(final Counter64 value) {
            return value.toLong();
        }
    }

    private static final class Gauge32AttributeInfo extends SnmpAttributeInfo<Gauge32>{
        private static final long serialVersionUID = 5787884395470856436L;

        private Gauge32AttributeInfo(final String attributeID,
                                       final AttributeDescriptor descriptor) {
            super(attributeID, SimpleType.LONG, descriptor);
        }

        @Override
        public Gauge32 convert(final Object value) throws InvalidAttributeValueException {
            if(value instanceof Byte)
                return new Gauge32((Byte)value);
            else if(value instanceof Short)
                return new Gauge32((Short)value);
            else if(value instanceof Integer)
                return new Gauge32((Integer)value);
            else if(value instanceof Long)
                return new Gauge32((Long)value);
            else throw invalidAttribute(value, Gauge32.class);
        }

        @Override
        public Long convert(final Gauge32 value) {
            return value.toLong();
        }
    }

    private static abstract class AbstractFormatterAttributeInfo<V extends Variable> extends SnmpAttributeInfo<V> implements SnmpObjectConverter<V>{
        private static final long serialVersionUID = 4378706124755779647L;
        private final SnmpObjectConverter<V> formatter;

        private AbstractFormatterAttributeInfo(final String attributeID,
                                               final SnmpObjectConverter<V> converter,
                                               final AttributeDescriptor descriptor) {
            super(attributeID, converter.getOpenType(), descriptor);
            this.formatter = converter;
        }

        @Override
        public final V convert(final Object value) throws InvalidAttributeValueException {
            return formatter.convert(value);
        }

        @Override
        public Object convert(final V value) {
            return formatter.convert(value);
        }
    }

    private static final class IpAddressAttributeInfo extends AbstractFormatterAttributeInfo<IpAddress> {
        private static final long serialVersionUID = 7204017895936479653L;

        private IpAddressAttributeInfo(final String attributeID,
                                       final AttributeDescriptor descriptor) {
            super(attributeID, IpAddressConversionFormat.getFormat(descriptor), descriptor);
        }
    }

    private static final class NullAttributeInfo extends SnmpAttributeInfo<Null>{
        private static final long serialVersionUID = -1290255198516661270L;

        private NullAttributeInfo(final String attributeID,
                                  final AttributeDescriptor descriptor) {
            super(attributeID, SimpleType.BOOLEAN, descriptor);
        }

        @Override
        public Null convert(final Object value) throws InvalidAttributeValueException {
            return Null.instance;
        }

        @Override
        public Boolean convert(final Null value) {
            return Boolean.FALSE;
        }
    }

    private static final class OpaqueAttributeInfo extends SnmpAttributeInfo<Opaque>{
        private static final long serialVersionUID = 6889554647863614713L;

        private OpaqueAttributeInfo(final String attributeID, final AttributeDescriptor descriptor) throws OpenDataException {
            super(attributeID, new ArrayType<byte[]>(SimpleType.BYTE, true), descriptor);
        }

        @Override
        public Opaque convert(final Object value) throws InvalidAttributeValueException {
            if(value instanceof byte[])
                return new Opaque((byte[])value);
            else if(value instanceof Byte[])
                return new Opaque(ArrayUtils.unwrapArray((Byte[])value));
            else if(value instanceof ByteBuffer)
                return new Opaque(Buffers.readRemaining((ByteBuffer)value));
            else throw invalidAttribute(value, Opaque.class);
        }

        @Override
        public Object convert(final Opaque value) {
            return value.toByteArray();
        }
    }

    private static final class OctetStringAttributeInfo extends AbstractFormatterAttributeInfo<OctetString> {
        private static final long serialVersionUID = -4202389744046621844L;

        private OctetStringAttributeInfo(final String attributeID,
                                         final OctetString value,
                                         final AttributeDescriptor descriptor) {
            super(attributeID, OctetStringConversionFormat.getFormat(value, descriptor), descriptor);
        }
    }

    private static final class OidAttributeInfo extends AbstractFormatterAttributeInfo<OID> {
        private static final long serialVersionUID = 2175664004641918099L;

        private OidAttributeInfo(final String attributeID,
                                         final AttributeDescriptor descriptor) {
            super(attributeID, OidConversionFormat.getFormat(descriptor), descriptor);
        }
    }

    private static final class SnmpAttributeRepository extends AbstractAttributeRepository<SnmpAttributeInfo> implements Aggregator {
        private static final Duration BATCH_READ_WRITE_TIMEOUT = Duration.ofSeconds(30);
        private final AbstractConcurrentResourceAccessor<SnmpClient> client;
        private final ExecutorService executor;
        private final Duration discoveryTimeout;

        private SnmpAttributeRepository(final String resourceName,
                                        final AbstractConcurrentResourceAccessor<SnmpClient> client,
                                        final Duration discoveryTimeout){
            super(resourceName, SnmpAttributeInfo.class);
            this.client = client;
            this.discoveryTimeout = Objects.requireNonNull(discoveryTimeout);
            this.executor = client.read(cl -> cl.queryObject(ExecutorService.class)).orElseThrow(AssertionError::new);
        }

        private Address[] getClientAddresses(){
            return client.read(SnmpClient::getClientAddresses);
        }

        /**
         * Connects to the specified attribute.
         *
         * @param attributeName The id of the attribute.
         * @param descriptor  Attribute descriptor.
         * @return The description of the attribute.
         * @throws Exception Internal connector error.
         */
        @Override
        protected SnmpAttributeInfo connectAttribute(final String attributeName, final AttributeDescriptor descriptor) throws Exception {
            final Duration responseTimeout = SnmpConnectorDescriptionProvider.getResponseTimeout(descriptor);
            final OID attributeID = new OID(descriptor.getAlternativeName().orElse(attributeName));
            final Variable value = client.read(client -> client.get(attributeID, responseTimeout));
            if(value == null) throw JMExceptionUtils.attributeNotFound(attributeID.toDottedString());
            else switch (value.getSyntax()){
                case SMIConstants.SYNTAX_INTEGER32:
                    return new Integer32AttributeInfo(attributeName, descriptor);
                case SMIConstants.SYNTAX_NULL:
                    return new NullAttributeInfo(attributeName, descriptor);
                case SMIConstants.SYNTAX_OPAQUE:
                    return new OpaqueAttributeInfo(attributeName, descriptor);
                case SMIConstants.SYNTAX_OCTET_STRING:
                    return new OctetStringAttributeInfo(attributeName, (OctetString)value, descriptor);
                case SMIConstants.SYNTAX_OBJECT_IDENTIFIER:
                    return new OidAttributeInfo(attributeName, descriptor);
                case SMIConstants.SYNTAX_TIMETICKS:
                    return new TimeTicksAttributeInfo(attributeName, descriptor);
                case SMIConstants.SYNTAX_COUNTER32:
                    return new Counter32AttributeInfo(attributeName, descriptor);
                case SMIConstants.SYNTAX_COUNTER64:
                    return new Counter64AttributeInfo(attributeName, descriptor);
                case SMIConstants.SYNTAX_GAUGE32:
                    return new Gauge32AttributeInfo(attributeName, descriptor);
                case SMIConstants.SYNTAX_IPADDRESS:
                    return new IpAddressAttributeInfo(attributeName, descriptor);
                default:
                    return new ReadOnlyAttributeInfo(attributeName, descriptor);
            }
        }

        /**
         * Obtains the value of a specific attribute of the managed resource.
         *
         * @param metadata The metadata of the attribute.
         * @return The value of the attribute retrieved.
         * @throws Exception Internal connector error.
         */
        @SuppressWarnings("unchecked")
        @Override
        protected Object getAttribute(final SnmpAttributeInfo metadata) throws Exception {
            return client.read(client -> {
                final Variable attribute = client.get(metadata.getAttributeID(), metadata.getDescriptor().getReadWriteTimeout());
                switch (attribute.getSyntax()){
                    case SMIConstants.EXCEPTION_END_OF_MIB_VIEW:
                    case SMIConstants.EXCEPTION_NO_SUCH_INSTANCE:
                    case SMIConstants.EXCEPTION_NO_SUCH_OBJECT:
                        throw new AttributeNotFoundException(String.format("SNMP Object %s doesn't exist. Error info: %s",
                                metadata.getAttributeID(),
                                attribute.getSyntax()));
                    default: return metadata.convert(attribute);
                }
            });
        }

        /**
         * Set the value of a specific attribute of the managed resource.
         *
         * @param attribute The attribute of to set.
         * @param value     The value of the attribute.
         * @throws Exception                                       Internal connector error.
         * @throws javax.management.InvalidAttributeValueException Incompatible attribute type.
         */
        @Override
        protected void setAttribute(final SnmpAttributeInfo attribute, final Object value) throws Exception {
            client.read(client -> {
                client.set(ImmutableMap.of(attribute.getAttributeID(), attribute.convert(value)),
                        attribute.getDescriptor().getReadWriteTimeout());
                return null;
            });
        }

        private Logger getLogger(){
            return LoggerProvider.getLoggerForObject(this);
        }

        /**
         * Get the values of several attributes of the managed resource.
         *
         * @param attributes A list of the attributes to be retrieved.
         * @return The list of attributes retrieved.
         * @see #getAttributesParallel(java.util.concurrent.ExecutorService, String[], Duration)
         */
        @Override
        public AttributeList getAttributes(final String[] attributes) {
            try {
                return getAttributesParallel(executor, attributes, BATCH_READ_WRITE_TIMEOUT);
            } catch (final MBeanException e) {
                getLogger().log(Level.SEVERE, "Unable to read attributes", e.getCause());
                return new AttributeList();
            }
        }

        /**
         * Sets the values of several attributes of the managed resource.
         *
         * @param attributes A list of attributes: The identification of the
         *                   attributes to be set and  the values they are to be set to.
         * @return The list of attributes that were set, with their new values.
         * @see #setAttributesParallel(java.util.concurrent.ExecutorService, javax.management.AttributeList, Duration)
         */
        @Override
        public AttributeList setAttributes(final AttributeList attributes) {
            try {
                return setAttributesParallel(executor, attributes, BATCH_READ_WRITE_TIMEOUT);
            } catch (final MBeanException e) {
                getLogger().log(Level.SEVERE, "Unable to write attributes", e.getCause());
                return new AttributeList();
            }
        }

        @Override
        public AttributeList getAttributes() throws MBeanException {
            return getAttributesParallel(executor, BATCH_READ_WRITE_TIMEOUT);
        }

        private Map<String, AttributeDescriptor> expandImpl(final SnmpClient client) throws InterruptedException, ExecutionException, TimeoutException {
            final Map<String, AttributeDescriptor> result = new HashMap<>();
            for (final VariableBinding binding : client.walk(discoveryTimeout)) {
                final AttributeDescriptor descriptor = createDescriptor(config -> {
                    if (binding.getVariable() instanceof OctetString)
                        config.put(SNMP_CONVERSION_FORMAT_PARAM, OctetStringConversionFormat.adviceFormat((OctetString) binding.getVariable()));
                });
                result.put(binding.getOid().toDottedString(), descriptor);
            }
            return result;
        }

        @Override
        public Map<String, AttributeDescriptor> discoverAttributes() {
            try {
                return client.read(this::expandImpl);
            } catch (final Exception e) {
                failedToExpand(Level.WARNING, e);
                return Collections.emptyMap();
            }
        }

        @Override
        public <T> Optional<T> queryObject(@Nonnull final Class<T> objectType) {
            final Optional<?> result;
            if (objectType.isInstance(this))
                result = Optional.of(this);
            else if (objectType.isAssignableFrom(Address[].class))
                result = Optional.of(getClientAddresses());
            else
                result = Optional.empty();
            return result.map(objectType::cast);
        }
    }

    @Aggregation(cached = true)
    private final SnmpAttributeRepository attributes;
    @Aggregation(cached = true)
    private final SnmpNotificationRepository notifications;
    private final AbstractConcurrentResourceAccessor<SnmpClient> client;


    SnmpResourceConnector(final String resourceName,
                          final ManagedResourceInfo configuration) throws IOException {
        super(configuration);
        final SnmpConnectorDescriptionProvider parser = SnmpConnectorDescriptionProvider.getInstance();
        final Duration discoveryTimeout = parser.parseDiscoveryTimeout(configuration);
        client = new ConcurrentResourceAccessor<>(parser.createSnmpClient(GenericAddress.parse(configuration.getConnectionString()), configuration));
        attributes = new SnmpAttributeRepository(resourceName, client, discoveryTimeout);
        notifications = new SnmpNotificationRepository(resourceName,
                client,
                Utils.getBundleContextOfObject(this));
        notifications.setSource(this);
    }

    @Override
    protected MetricsSupport createMetricsReader() {
        return assembleMetricsReader(attributes, notifications);
    }

    void listen() throws IOException {
        client.write(client -> {
            client.listen();
            return null;
        });
    }

    /**
     * Adds a new listener for the connector-related events.
     * <p/>
     * The managed resource connector should holds a weak reference to all added event listeners.
     *
     * @param listener An event listener to add.
     */
    @Override
    public void addResourceEventListener(final ResourceEventListener listener) {
        addResourceEventListener(listener, attributes, notifications);
    }

    /**
     * Removes connector event listener.
     *
     * @param listener The listener to remove.
     */
    @Override
    public void removeResourceEventListener(final ResourceEventListener listener) {
        removeResourceEventListener(listener, attributes, notifications);
    }

    private void closeClient() throws IOException {
        client.write(client -> {
            client.close();
            return null;
        });
    }

    /**
     * Releases all resources associated with this connector.
     *
     * @throws IOException Unable to close SNMP client.
     */
    @Override
    public void close() throws Exception {
        Utils.closeAll(super::close, attributes, notifications, this::closeClient);
    }

    /**
     * Retrieves the aggregated object.
     *
     * @param objectType Type of the aggregated object.
     * @return An instance of the requested object; or {@literal null} if object is not available.
     */
    @Override
    public <T> Optional<T> queryObject(@Nonnull final Class<T> objectType) {
        return queryObject(objectType, attributes);
    }

}
