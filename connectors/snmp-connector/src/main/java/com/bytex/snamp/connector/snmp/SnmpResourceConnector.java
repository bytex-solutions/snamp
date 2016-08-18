package com.bytex.snamp.connector.snmp;

import com.bytex.snamp.Aggregator;
import com.bytex.snamp.ArrayUtils;
import com.bytex.snamp.concurrent.AbstractConcurrentResourceAccessor;
import com.bytex.snamp.concurrent.ConcurrentResourceAccessor;
import com.bytex.snamp.connector.AbstractManagedResourceConnector;
import com.bytex.snamp.connector.ResourceEventListener;
import com.bytex.snamp.connector.attributes.*;
import com.bytex.snamp.connector.metrics.MetricsReader;
import com.bytex.snamp.connector.notifications.*;
import com.bytex.snamp.core.DistributedServices;
import com.bytex.snamp.internal.Utils;
import com.bytex.snamp.io.Buffers;
import com.bytex.snamp.jmx.CompositeDataUtils;
import com.bytex.snamp.jmx.JMExceptionUtils;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import org.osgi.framework.BundleContext;
import org.snmp4j.CommandResponder;
import org.snmp4j.CommandResponderEvent;
import org.snmp4j.PDU;
import org.snmp4j.smi.*;

import javax.management.*;
import javax.management.openmbean.*;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.text.ParseException;
import java.time.Duration;
import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeoutException;
import java.util.logging.Level;
import java.util.logging.Logger;

import static com.bytex.snamp.configuration.AttributeConfiguration.*;
import static com.bytex.snamp.connector.snmp.SnmpConnectorDescriptionProvider.*;

/**
 * Represents SNMP-compliant managed resource.
 * This class cannot be inherited.
 * @author Roman Sakno
 * @version 2.0
 * @since 1.0
 */
final class SnmpResourceConnector extends AbstractManagedResourceConnector {
    private static final class SnmpNotificationInfo extends CustomNotificationInfo{
        private static final long serialVersionUID = -4792879013459588079L;

        private SnmpNotificationInfo(final String listID,
                                     final NotificationDescriptor descriptor){
            super(listID, getDescription(descriptor), descriptor);
        }

        private static String getDescription(final NotificationDescriptor descriptor){
            final String result = descriptor.getDescription();
            return result == null || result.isEmpty() ? "SNMP Trap" : result;
        }

        private OID getNotificationID(){
            return new OID(getDescriptor().getName(ArrayUtils.getFirst(getNotifTypes())));
        }

    }

    private static final class SnmpNotificationRepository extends AbstractNotificationRepository<SnmpNotificationInfo> implements CommandResponder{
        private final AbstractConcurrentResourceAccessor<SnmpClient> client;
        private final NotificationListenerInvoker listenerInvoker;
        private final Logger logger;

        private SnmpNotificationRepository(final String resourceName,
                                           final AbstractConcurrentResourceAccessor<SnmpClient> client,
                                           final BundleContext context,
                                           final Logger logger){
            super(resourceName,
                    SnmpNotificationInfo.class,
                    DistributedServices.getDistributedCounter(context, "notifications-".concat(resourceName)),
                    false);
            this.logger = Objects.requireNonNull(logger);
            this.client = client;
            final Executor executor = client.read(cl -> cl.queryObject(Executor.class));
            listenerInvoker = createListenerInvoker(executor, logger);
        }

        private static NotificationListenerInvoker createListenerInvoker(final Executor executor, final Logger logger) {
            return NotificationListenerInvokerFactory.createParallelExceptionResistantInvoker(executor, (e, source) -> logger.log(Level.SEVERE, "Unable to process SNMP notification", e));
        }

        /**
         * Determines whether raising of registered events is suspended.
         *
         * @return {@literal true}, if events are suspended; otherwise {@literal false}.
         */
        @Override
        public boolean isSuspended() {
            return super.isSuspended() && !DistributedServices.isActiveNode(Utils.getBundleContextOfObject(this));
        }

        /**
         * Reports an error when enabling notifications.
         *
         * @param category An event category.
         * @param e        Internal connector error.
         * @see #failedToEnableNotifications(java.util.logging.Logger, java.util.logging.Level, String, Exception)
         */
        @Override
        protected void failedToEnableNotifications(final String category, final Exception e) {
            failedToEnableNotifications(logger, Level.WARNING, category, e);
        }

        @Override
        protected SnmpNotificationInfo enableNotifications(final String category,
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
        protected void disableNotifications(final SnmpNotificationInfo metadata) {
            if (hasNoNotifications())
                try {
                    client.write(client -> {
                        client.removeCommandResponder(this);
                        return null;
                    });
                } catch (final Exception e) {
                    logger.log(Level.WARNING, String.format("Subscription to SNMP event %s failed",
                            metadata.getNotificationID()), e);
                }
        }

        /**
         * Gets the invoker used to executed notification listeners.
         *
         * @return The notification listener invoker.
         */
        @Override
        protected NotificationListenerInvoker getListenerInvoker() {
            return listenerInvoker;
        }

        private void processPdu(final PDU event){
            final List<VariableBinding> bindings = new ArrayList<>(event.getVariableBindings());
            if(bindings.size() == 0) return;
            //tries to detect event category
            final SnmpNotificationInfo notificationInfo = ArrayUtils.find(getNotificationInfo(), input -> {
                for (final VariableBinding bnd : bindings)
                    if (bnd.getOid().startsWith(input.getNotificationID()))
                        return true;
                return false;
            });
            //unknown notification
            if(notificationInfo == null) return;
            String message;
            if(notificationInfo.getDescriptor().hasField(MESSAGE_TEMPLATE_PARAM)){  //format message, no attachments
                message = notificationInfo.getDescriptor().getField(MESSAGE_TEMPLATE_PARAM, String.class);
                for(final VariableBinding binding: bindings) {
                    final OID postfix = SnmpConnectorHelpers.getPostfix(notificationInfo.getNotificationID(),
                            binding.getOid());
                    if (postfix.size() == 0) continue;
                    message = message.replaceAll(String.format("\\{%s\\}", postfix),
                            binding.getVariable().toString());
                }
                bindings.clear();
            }
            else if(notificationInfo.getDescriptor().hasField(MESSAGE_OID_PARAM)){      //extract message, add attachments
                final OID messageOID = new OID(message = notificationInfo.getDescriptor().getField(MESSAGE_OID_PARAM, String.class));
                final Iterator<VariableBinding> iterator = bindings.iterator();
                while (iterator.hasNext()){
                    final VariableBinding b = iterator.next();
                    if(Objects.equals(messageOID, SnmpConnectorHelpers.getPostfix(notificationInfo.getNotificationID(), b.getOid()))){
                        message = b.getVariable().toString();
                        iterator.remove();
                        break;
                    }
                }
            }
            else {//concatenate bindings, no attachments
                message = String.join(System.lineSeparator(), (CharSequence[]) bindings.stream().map(VariableBinding::toString).toArray(String[]::new));
                bindings.clear();
            }
            fire(notificationInfo.getDescriptor().getName(ArrayUtils.getFirst(notificationInfo.getNotifTypes())),
                    message,
                    bindings);
        }

        private static HashMap<String, ?> toArray(final List<VariableBinding> bindings){
            if(bindings.isEmpty()) return null;
            final HashMap<String, Object> result = Maps.newHashMapWithExpectedSize(bindings.size());
            for(final VariableBinding bnd: bindings)
                if(bnd.getVariable() instanceof Null)
                    result.put(bnd.getOid().toDottedString(), null);
                else if(bnd.getVariable() instanceof AssignableFromInteger)
                    result.put(bnd.getOid().toDottedString(), bnd.getVariable().toInt());
                else if(bnd.getVariable() instanceof AssignableFromLong)
                    result.put(bnd.getOid().toDottedString(), bnd.getVariable().toLong());
                else if(bnd.getVariable() instanceof OID)
                    result.put(bnd.getOid().toDottedString(), ((OID)bnd.getVariable()).toDottedString());
                else if(bnd.getVariable() instanceof OctetString){
                    final OctetString str = (OctetString)bnd.getVariable();
                    result.put(bnd.getOid().toDottedString(), str.isPrintable() ? new String(str.toByteArray(), SnmpObjectConverter.SNMP_ENCODING) : str.toByteArray());
                }
                else if(bnd.getVariable() instanceof Address)
                    result.put(bnd.getOid().toDottedString(), bnd.getVariable().toString());
                else if(bnd.getVariable() instanceof AssignableFromIntArray)
                    result.put(bnd.getOid().toDottedString(), ((AssignableFromIntArray)bnd.getVariable()).toIntArray());
                else if(bnd.getVariable() instanceof AssignableFromByteArray)
                    result.put(bnd.getOid().toDottedString(), ((AssignableFromByteArray)bnd.getVariable()).toByteArray());
                else result.put(bnd.getOid().toDottedString(), bnd.getVariable().toString());
            return result;
        }

        private void fire(final String category,
                          final String message,
                          final List<VariableBinding> attachment){
            super.fire(category,
                    message,
                    toArray(attachment));
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

    private static abstract class SnmpAttributeInfo<V extends Variable> extends OpenMBeanAttributeInfoImpl implements SnmpObjectConverter<V> {
        private static final long serialVersionUID = 4948510436343027716L;

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
        }

        private static String getDescription(final AttributeDescriptor descriptor){
            final String result = descriptor.getDescription();
            return result == null || result.isEmpty() ? "SNMP Object" : result;
        }

        final InvalidAttributeValueException invalidAttribute(final Object value,
                                                              final Class<V> snmpType){
            return new InvalidAttributeValueException(String.format("Unable convert %s to SNMP %s", value, snmpType));
        }

        private OID getAttributeID(){
            return new OID(getDescriptor().getName(getName()));
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

    private static final class TimeTicksAttributeInfo extends CustomFormatterAttributeInfo<TimeTicks>{
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

    private static abstract class CustomFormatterAttributeInfo<V extends Variable> extends SnmpAttributeInfo<V> implements SnmpObjectConverter<V>{
        private static final long serialVersionUID = 4378706124755779647L;
        private final SnmpObjectConverter<V> formatter;

        private CustomFormatterAttributeInfo(final String attributeID,
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

    private static final class IpAddressAttributeInfo extends CustomFormatterAttributeInfo<IpAddress>{
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

    private static final class OctetStringAttributeInfo extends CustomFormatterAttributeInfo<OctetString>{
        private static final long serialVersionUID = -4202389744046621844L;

        private OctetStringAttributeInfo(final String attributeID,
                                         final OctetString value,
                                         final AttributeDescriptor descriptor) {
            super(attributeID, OctetStringConversionFormat.getFormat(value, descriptor), descriptor);
        }
    }

    private static final class OidAttributeInfo extends CustomFormatterAttributeInfo<OID>{
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
        private final Logger logger;
        private final Duration discoveryTimeout;

        private SnmpAttributeRepository(final String resourceName,
                                        final AbstractConcurrentResourceAccessor<SnmpClient> client,
                                        final Logger logger,
                                        final boolean expandable,
                                        final Duration discoveryTimeout){
            super(resourceName, SnmpAttributeInfo.class, expandable);
            this.client = client;
            this.logger = Objects.requireNonNull(logger);
            this.discoveryTimeout = Objects.requireNonNull(discoveryTimeout);
            this.executor = client.read(cl -> cl.queryObject(ExecutorService.class));
        }

        /**
         * Reports an error when connecting attribute.
         *
         * @param attributeName The name of the attribute.
         * @param e             Internal connector error.
         * @see #failedToConnectAttribute(java.util.logging.Logger, java.util.logging.Level, String, Exception)
         */
        @Override
        protected void failedToConnectAttribute(final String attributeName, final Exception e) {
            failedToConnectAttribute(logger, Level.WARNING, attributeName, e);
        }

        /**
         * Reports an error when getting attribute.
         *
         * @param attributeID The attribute identifier.
         * @param e           Internal connector error.
         * @see #failedToGetAttribute(java.util.logging.Logger, java.util.logging.Level, String, Exception)
         */
        @Override
        protected void failedToGetAttribute(final String attributeID, final Exception e) {
            failedToGetAttribute(logger, Level.WARNING, attributeID, e);
        }

        /**
         * Reports an error when updating attribute.
         *
         * @param attributeID The attribute identifier.
         * @param value       The value of the attribute.
         * @param e           Internal connector error.
         * @see #failedToSetAttribute(java.util.logging.Logger, java.util.logging.Level, String, Object, Exception)
         */
        @Override
        protected void failedToSetAttribute(final String attributeID, final Object value, final Exception e) {
            failedToSetAttribute(logger, Level.WARNING, attributeID, value, e);
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
            final Variable value = client.read(client -> client.get(new OID(descriptor.getName(attributeName)), responseTimeout));
            if(value == null) throw JMExceptionUtils.attributeNotFound(descriptor.getName(attributeName));
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

        /**
         * Get the values of several attributes of the managed resource.
         *
         * @param attributes A list of the attributes to be retrieved.
         * @return The list of attributes retrieved.
         * @see #getAttributesSequential(String[])
         * @see #getAttributesParallel(java.util.concurrent.ExecutorService, String[], Duration)
         */
        @Override
        public AttributeList getAttributes(final String[] attributes) {
            try {
                return getAttributesParallel(executor, attributes, BATCH_READ_WRITE_TIMEOUT);
            } catch (final InterruptedException | TimeoutException e) {
                logger.log(Level.SEVERE, "Unable to read attributes", e);
                return new AttributeList();
            }
        }

        /**
         * Sets the values of several attributes of the managed resource.
         *
         * @param attributes A list of attributes: The identification of the
         *                   attributes to be set and  the values they are to be set to.
         * @return The list of attributes that were set, with their new values.
         * @see #setAttributesSequential(javax.management.AttributeList)
         * @see #setAttributesParallel(java.util.concurrent.ExecutorService, javax.management.AttributeList, Duration)
         */
        @Override
        public AttributeList setAttributes(final AttributeList attributes) {
            try {
                return setAttributesParallel(executor, attributes, BATCH_READ_WRITE_TIMEOUT);
            } catch (final TimeoutException | InterruptedException e) {
                logger.log(Level.SEVERE, "Unable to write attributes", e);
                return new AttributeList();
            }
        }

        private List<SnmpAttributeInfo> expandImpl(final SnmpClient client) throws InterruptedException, ExecutionException, TimeoutException, OpenDataException {
            final LinkedList<SnmpAttributeInfo> result = new LinkedList<>();
            for (final VariableBinding binding : client.walk(discoveryTimeout)) {
                final Map<String, String> parameters = new HashMap<>(5);
                parameters.put(AUTOMATICALLY_ADDED_KEY, Boolean.TRUE.toString());
                if (binding.getVariable() instanceof OctetString)
                    parameters.put(SNMP_CONVERSION_FORMAT_PARAM, OctetStringConversionFormat.adviceFormat((OctetString) binding.getVariable()));
                final SnmpAttributeInfo attr = addAttribute(binding.getOid().toDottedString(),
                        TIMEOUT_FOR_SMART_MODE,
                        CompositeDataUtils.create(parameters, SimpleType.STRING));
                if (attr != null) result.add(attr);
            }
            return result;
        }

        @Override
        public List<SnmpAttributeInfo> expandAttributes() {
            try {
                return client.read(this::expandImpl);
            } catch (final Exception e) {
                failedToExpand(logger, Level.WARNING, e);
                return Collections.emptyList();
            }
        }

        @Override
        public <T> T queryObject(final Class<T> objectType) {
            if(objectType == null)
                return null;
            else if(objectType.isAssignableFrom(Address[].class))
                return objectType.cast(getClientAddresses());
            else return null;
        }
    }

    @Aggregation(cached = true)
    private final SnmpAttributeRepository attributes;
    @Aggregation(cached = true)
    private final SnmpNotificationRepository notifications;
    private final AbstractConcurrentResourceAccessor<SnmpClient> client;


    SnmpResourceConnector(final String resourceName,
                          final String connectionString,
                          final Map<String, String> parameters,
                          final Duration discoveryTimeout) throws IOException {
        final boolean smartMode = SnmpConnectorDescriptionProvider.getInstance().isSmartModeEnabled(parameters);
        client = new ConcurrentResourceAccessor<>(SnmpConnectorDescriptionProvider.getInstance().createSnmpClient(GenericAddress.parse(connectionString), parameters));
        attributes = new SnmpAttributeRepository(resourceName, client, getLogger(), smartMode, discoveryTimeout);
        notifications = new SnmpNotificationRepository(resourceName,
                client,
                Utils.getBundleContextOfObject(this),
                getLogger());

    }

    @Override
    protected MetricsReader createMetricsReader() {
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

    /**
     * Releases all resources associated with this connector.
     *
     * @throws IOException Unable to close SNMP client.
     */
    @Override
    public void close() throws Exception {
        super.close();
        attributes.close();
        notifications.close();
        client.write(client -> {
                client.close();
                return null;
        });
    }

    /**
     * Retrieves the aggregated object.
     *
     * @param objectType Type of the aggregated object.
     * @return An instance of the requested object; or {@literal null} if object is not available.
     */
    @Override
    public <T> T queryObject(final Class<T> objectType) {
        return queryObject(objectType, attributes);
    }

}
