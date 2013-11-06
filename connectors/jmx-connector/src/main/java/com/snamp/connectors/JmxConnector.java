package com.snamp.connectors;

import com.snamp.TimeSpan;
import com.snamp.licensing.JmxConnectorLimitations;

import javax.management.*;
import javax.management.openmbean.*;
import javax.management.remote.*;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicLong;
import java.util.logging.*;

import static com.snamp.connectors.JmxAttributeTypeInfoBuilder.createJmxType;
import static com.snamp.connectors.AttributeTypeInfoBuilder.AttributeConvertibleTypeInfo;

/**
 * Represents JMX connector.
 * @author roman
 */
final class JmxConnector extends AbstractManagementConnector {
    /**
     * Represents JMX connector name.
     */
    public static final String connectorName = "jmx";
    private static final Logger log = AbstractManagementConnectorFactory.getLogger(connectorName);

    /**
     * Represents count of instantiated connectors.
     */
    private static final AtomicLong instanceCounter = new AtomicLong(0);

    private final JMXServiceURL serviceURL;
    private final Map<String, Object> connectionProperties;

    /**
     * Represents Management Bean connection handler.
     * @param <T> Type of the connection handling result.
     */
    private static interface MBeanServerConnectionReader<T> {
        /**
         * Extracts object from the connection,
         * @param connection
         * @return
         * @throws IOException
         * @throws JMException
         */
        public T read(final MBeanServerConnection connection) throws IOException, JMException;
    }

    /**
     * Represents field navigator in the composite JMX data.
     */
    private static final class CompositeValueNavigator
    {
        /**
         * Represents path delimiter.
         */
        public static final char delimiter = '@';
        /**
         * Represents the name of the attribute that has a composite type.
         */
        public final String attributeName;
        private final String[] path;

        /**
         * Initializes a new field navigator.
         * @param attributeName The name
         */
        public CompositeValueNavigator(final String attributeName)
        {
            if(!isCompositeAttribute(attributeName)) throw new IllegalArgumentException("Неверный формат имени составного атрибута");
            final String[] parts = attributeName.split(new String(new char[]{delimiter}));
            this.attributeName = parts[0];
            this.path = Arrays.copyOfRange(parts, 1, parts.length);
        }

        /**
         * Returns the path depth.
         * @return
         */
        public int depth(){
            return path.length;
        }

        /**
         * Returns the subfield name by depth index.
         * @param index
         * @return
         */
        public String item(int index)
        {
            return path[index];
        }

        private Object getValue(final Object root, final int index)
        {
            if(root instanceof CompositeData && index < path.length){
                final CompositeData cdata = (CompositeData)root;
                final String subattr = path[index];
                return cdata.containsKey(subattr) ? getValue(cdata.get(subattr), index + 1) : root;
            }
            else return root;
        }

        /**
         * Получить значение вложенного атрибута.
         * @param root
         * @return
         */
        public Object getValue(final Object root)
        {
            return getValue(root, 0);
        }

        private Object getType(final Object root, final int index)
        {
            if(root instanceof CompositeType && index < path.length){
                final CompositeType cdata = (CompositeType)root;
                final String subattr = path[index];
                return cdata.containsKey(subattr) ? getType(cdata.getType(subattr), index + 1) : root;
            }
            else return root;
        }

        /**
         * Returns a type of the inner field.
         * @param root
         * @return String or OpenType.
         */
        public Object getType(final OpenType<?> root)
        {
            return getType(root, 0);
        }

        /**
         * Получить полный путь композитного атрибута.
         */

        public String toString()
        {
            return this.attributeName + Arrays.toString(path).replace(", ", new String(new char[]{delimiter}));
        }

        /**
         * Determines whether the attribute name contains subfield path.
         * @param attributeName
         * @return
         */
        public static boolean isCompositeAttribute(final String attributeName)
        {
            return attributeName.indexOf(delimiter) >= 0;
        }
    }

    /**
     * Represents JMX attribute metadata.
     */
    public static interface JmxAttributeMetadata extends AttributeMetadata {
        static final String ownerOption = "owner";
        /**
         * Returns the object name in which the current attribute is located.
         * @return
         */
        public ObjectName getOwner();

        /**
         * Returns the type of the attribute value.
         * @return The type of the attribute value.
         */
        @Override
        public AttributeConvertibleTypeInfo<?> getAttributeType();
    }

    /**
     * Represents an abstract class for building JMX attribute providers.
     */
    private abstract class JmxAttributeProvider extends GenericAttributeMetadata<AttributeConvertibleTypeInfo<?>> implements JmxAttributeMetadata {

        private final ObjectName namespace;
        private MBeanServerConnectionReader<Object> attributeValueReader;

        protected JmxAttributeProvider(final String attributeName,
                                       final ObjectName namespace){
            super(attributeName, namespace.toString());
            this.namespace = namespace;
        }

        @Override
        public final int size() {
            return 1;
        }

        @Override
        public final boolean isEmpty() {
            return false;
        }

        @Override
        public final boolean containsKey(final Object option) {
            return ownerOption.equals(option);
        }

        @Override
        public final boolean containsValue(Object o) {
            return namespace.equals(o);
        }

        @Override
        public final String get(Object o) {
            return null;  //To change body of implemented methods use File | Settings | File Templates.
        }

        @Override
        public final Set<String> keySet() {
            return new HashSet<String>(1){{
                add(ownerOption);
            }};
        }

        @Override
        public final Collection<String> values() {
            return new ArrayList(1){{
                add(namespace.toString());
            }};
        }

        @Override
        public final Set<Entry<String, String>> entrySet() {
            return new HashSet<Entry<String, String>>(1){{
                add(new Entry<String, String>(){

                    @Override
                    public final String getKey() {
                        return ownerOption;
                    }

                    @Override
                    public final String getValue() {
                        return namespace.toString();
                    }

                    @Override
                    public final String setValue(String s) {
                        throw new UnsupportedOperationException();
                    }
                });
            }};
        }

        /**
         * Creates a new instance of the attribute value reader.
         * @return
         */
        protected abstract MBeanServerConnectionReader<Object> createAttributeValueReader();

        /**
         * Creates a new instance of the attribute value writer.
         * @return
         */
        protected abstract MBeanServerConnectionReader<Boolean> createAttributeValueWriter(final Object value);

        /**
         * Returns the attribute owner.
         * @return
         */
        public final ObjectName getOwner(){
            return namespace;
        }

        /**
         * Returns the value of the attribute.
         * @param defval
         * @return
         */
        public final Object getValue(final Object defval){
            if(canRead()){
                if(attributeValueReader == null) attributeValueReader = createAttributeValueReader();
                return handleConnection(attributeValueReader, defval);
            }
            else return defval;
        }

        /**
         * Writes the value to the attribute.
         * @param value
         * @return
         */
        public final boolean setValue(final Object value){
            final AttributeConvertibleTypeInfo<?> typeInfo = getAttributeType();
            if(canWrite() && value != null && typeInfo.canConvertFrom(value.getClass())){
                return handleConnection(createAttributeValueWriter(typeInfo.convertFrom(value)), false);
            }
            else return false;
        }
    }

    private JmxAttributeProvider createPlainAttribute(final ObjectName namespace, final String attributeName){
        //extracts JMX attribute metadata
        final MBeanAttributeInfo targetAttr = handleConnection(new MBeanServerConnectionReader<MBeanAttributeInfo>(){
            @Override
            public MBeanAttributeInfo read(final MBeanServerConnection connection) throws IOException, JMException {
                for(final MBeanAttributeInfo attr: connection.getMBeanInfo(namespace).getAttributes())
                    if(attributeName.equals(attr.getName())) return attr;
                return null;
            }
        }, null);
        return targetAttr != null ? new JmxAttributeProvider(targetAttr.getName(), namespace){
            @Override
            protected final AttributeConvertibleTypeInfo<?> detectAttributeType() {
                if(targetAttr instanceof OpenMBeanAttributeInfoSupport)
                    return createJmxType(((OpenMBeanAttributeInfoSupport) targetAttr).getOpenType());
                else return createJmxType(targetAttr.getType());
            }

            /**
             * Determines whether the value of this attribute can be changed, returns {@literal true} by default.
             *
             * @return {@literal true}, if the attribute value can be changed; otherwise, {@literal false}.
             */
            @Override
            public boolean canWrite() {
                return targetAttr.isWritable();
            }

            /**
             * By default, returns {@literal true}.
             *
             * @return
             */
            @Override
            public boolean canRead() {
                return targetAttr.isReadable();
            }

            /**
             * Creates a new instance of the attribute value reader.
             *
             * @return
             */
            @Override
            protected final MBeanServerConnectionReader<Object> createAttributeValueReader() {
                return new MBeanServerConnectionReader<Object>(){

                    public Object read(final MBeanServerConnection connection) throws IOException, JMException {
                        return connection.getAttribute(namespace, getAttributeName());
                    }

                };
            }
            /**
             * Creates a new instance of the attribute value writer.
             *
             * @return
             */
            @Override
            protected final MBeanServerConnectionReader<Boolean> createAttributeValueWriter(final Object value) {
                return new MBeanServerConnectionReader<Boolean>(){

                    public Boolean read(final MBeanServerConnection connection) throws IOException, JMException {
                        connection.setAttribute(namespace, new Attribute(getAttributeName(), value));
                        return true;
                    }
                };
            }
        } : null;
    }

    private JmxAttributeProvider createCompositeAttribute(final ObjectName namespace, final String attributeName){
        final CompositeValueNavigator navigator = new CompositeValueNavigator(attributeName);
        //получить описатель поля, этот описатель может содержать знак @ для вложенного атрибута
        final MBeanAttributeInfo targetAttr = handleConnection(new MBeanServerConnectionReader<MBeanAttributeInfo>() {
            @Override
            public MBeanAttributeInfo read(final MBeanServerConnection connection) throws IOException, JMException {
                for(final MBeanAttributeInfo attr: connection.getMBeanInfo(namespace).getAttributes())
                    if(navigator.attributeName.equals(attr.getName())) return attr;
                return null;
            }
        }, null);
        return targetAttr != null ? new JmxAttributeProvider(targetAttr.getName(), namespace){
            private final OpenType<?> compositeType = targetAttr instanceof OpenMBeanAttributeInfoSupport ? ((OpenMBeanAttributeInfoSupport)targetAttr).getOpenType() : SimpleType.STRING;

            @Override
            public final boolean canRead() {
                return targetAttr.isReadable();
            }

            @Override
            public final boolean canWrite(){
                return false;
            }

            /**
             * Creates a new instance of the attribute value reader.
             *
             * @return
             */
            @Override
            protected final MBeanServerConnectionReader<Object> createAttributeValueReader() {
                return new MBeanServerConnectionReader<Object>(){
                    public Object read(final MBeanServerConnection connection) throws IOException, JMException {
                        return navigator.getValue(connection.getAttribute(namespace, navigator.attributeName));
                    }
                };
            }

            /**
             * The writer for the composite data structure is not supported.
             *
             * @return
             */
            @Override
            protected final MBeanServerConnectionReader<Boolean> createAttributeValueWriter(Object value) {
                return new MBeanServerConnectionReader<Boolean>() {
                    @Override
                    public Boolean read(MBeanServerConnection connection) throws IOException, JMException {
                        return false;
                    }
                };
            }

            @Override
            protected final AttributeConvertibleTypeInfo<?> detectAttributeType() {
                final Object attributeType = navigator.getType(compositeType);
                if(attributeType instanceof OpenType)
                    return createJmxType((OpenType<?>)attributeType);
                else if(attributeType instanceof Class<?>)
                    return createJmxType((Class<?>)attributeType);
                else return createJmxType(Objects.toString(attributeType, ""));
            }
        } : null;
    }

    private ObjectName findObjectName(final ObjectName namespace){
        return handleConnection(new MBeanServerConnectionReader<ObjectName>() {
            public ObjectName read(final MBeanServerConnection connection) throws IOException, JMException {

                final Set<ObjectInstance> beans = connection.queryMBeans(namespace, null);

                for(final ObjectInstance instance : beans ) return instance.getObjectName();
                return null;
            }
        }, null);
    }

    /**
     * Initializes a new connector and established connection to the JMX provider.
     * @param serviceURL JMX connection string.
     * @param connectionProperties JMX connection properties(such as credentials).
     * @exception IllegalArgumentException Could not establish connection to JMX provider.
     */
    public JmxConnector(final JMXServiceURL serviceURL, final Map<String, Object> connectionProperties){
        if(serviceURL == null) throw new IllegalArgumentException("serviceURL is null.");
        this.serviceURL = serviceURL;
        this.connectionProperties = connectionProperties != null ? Collections.unmodifiableMap(connectionProperties) : new HashMap<String, Object>();
        JmxConnectorLimitations.current().verifyMaxInstanceCount(instanceCounter.incrementAndGet());
    }

    private JmxAttributeProvider connectAttribute(final ObjectName namespace, final String attributeName, final boolean useRegexp){
        //creates JMX attribute provider based on its metadata and connection options.
        if(namespace == null) return null;
        if(CompositeValueNavigator.isCompositeAttribute(attributeName))
            return createCompositeAttribute(namespace, attributeName);
        else if(useRegexp) return connectAttribute(findObjectName(namespace), attributeName, false);
        else return createPlainAttribute(namespace, attributeName);
    }

    /**
     * Connects to the specified attribute.
     * @param attributeName The name of the attribute.
     * @param options The attribute discovery options.
     * @return The description of the attribute.
     */
    @Override
    protected JmxAttributeMetadata connectAttribute(final String attributeName, final Map<String, String> options){
        final String namespace = Objects.toString(options.get("objectName"), "");
        try {
            return connectAttribute(new ObjectName(namespace), attributeName, options.containsKey("useRegexp") && Boolean.TRUE.equals(options.get("useRegexp")));
        } catch (final MalformedObjectNameException e) {
            log.log(Level.SEVERE, String.format("Unsupported JMX object name: %s", namespace), e);
            return null;
        }
        finally {
            JmxConnectorLimitations.current().verifyMaxAttributeCount(attributesCount());
        }
    }

    /**
     * Handles connection.
     * @param reader The MBean reader.
     * @param defval The default value returned by the connection handler if it handling failed.
     * @param <TOutput> Type of the connection handling result.
     * @return The result of the connection handling result.
     */
    private <TOutput> TOutput handleConnection(final MBeanServerConnectionReader<TOutput> reader, final TOutput defval)
    {
        try{
            final javax.management.remote.JMXConnector connector = JMXConnectorFactory.connect(serviceURL, connectionProperties);
            final TOutput result = reader.read(connector.getMBeanServerConnection());
            connector.close();
            return result;
        }
        catch(final Exception e){
            log.log(Level.WARNING, e.getMessage(), e);
            return defval;
        }
    }

    /**
     * Throws an exception if the connector is not initialized.
     */
    @Override
    protected void verifyInitialization() {
        //do nothing, because this connector doesn't store connection session.
    }

    /**
     * Returns the value of the attribute.
     *
     * @param attribute    The metadata of the attribute to get.
     * @param readTimeout
     * @param defaultValue The default value of the attribute if reading fails.
     * @return The value of the attribute.
     * @throws java.util.concurrent.TimeoutException
     *
     */
    @Override
    protected Object getAttributeValue(final AttributeMetadata attribute, final TimeSpan readTimeout, final Object defaultValue) throws TimeoutException {
        return attribute instanceof JmxAttributeProvider ? ((JmxAttributeProvider)attribute).getValue(defaultValue) : defaultValue;
    }

    /**
     * Sends the attribute value to the remote agent.
     *
     * @param attribute    The metadata of the attribute to set.
     * @param writeTimeout
     * @param value
     * @return
     */
    @Override
    protected final boolean setAttributeValue(final AttributeMetadata attribute, final TimeSpan writeTimeout, final Object value) {
        return attribute instanceof JmxAttributeProvider && ((JmxAttributeProvider)attribute).setValue(value);
    }

    /**
     * Executes remote action.
     *
     * @param actionName The name of the action,
     * @param args       The invocation arguments.
     * @param timeout    The Invocation timeout.
     * @return The invocation result.
     */
    @Override
    public Object doAction(String actionName, Arguments args, TimeSpan timeout) throws UnsupportedOperationException, TimeoutException {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    /**
     * Releases all resources associated with this connector.
     */
    @Override
    public final void close(){
        instanceCounter.decrementAndGet();
    }

    @Override
    protected final void finalize() {
        close();
    }
}
