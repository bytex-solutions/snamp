package com.snamp.connectors;

import com.snamp.*;

import java.beans.*;
import java.lang.ref.*;
import java.util.*;
import java.util.concurrent.TimeoutException;
import java.lang.reflect.*;
import static com.snamp.connectors.AttributeTypeInfoBuilder.AttributeConvertibleTypeInfo;

/**
 * Represents SNAMP in-process management connector that exposes Java Bean properties through connector attributes.
 * <p>
 *     Use this class as base class for your custom management connector, if schema of the management information base
 *     is well known at the compile time and stable through connector instantiations.
 *     The following example demonstrates management connector bean:
 *     <pre>{@code
 *     public final class CustomConnector extends ManagementConnectorBean{
 *       private String prop1;
 *
 *       public CustomConnector(){
 *           super(new AttributePrimitiveTypeBuilder());
 *           prop1 = "Hello, world!";
 *       }
 *
 *       public String getProperty1(){
 *         return prop1;
 *       }
 *
 *       public String setProperty1(final String value){
 *         prop1 = value;
 *       }
 *     }
 *
 *     final CustomConnector c = new CustomConnector();
 *     c.connectProperty("001", "property1", new HashMap<>());
 *     System.out.println(c.getAttribute("001", TimeSpan.INFINITE, ""));//output is: Hello, world!
 *     }</pre>
 * </p>
 * @author Roman Sakno
 * @since 1.0
 * @version 1.0
 */
@Lifecycle(InstanceLifecycle.NORMAL)
public class ManagementConnectorBean extends AbstractManagementConnector {

    private  final static class JavaBeanPropertyMetadata extends GenericAttributeMetadata<AttributeConvertibleTypeInfo<?>>{
        private final Map<String, String> properties;
        private final Class<?> propertyType;
        private final Method getter;
        private final Method setter;
        private final Reference<AttributeTypeInfoBuilder> typeBuilder;

        public JavaBeanPropertyMetadata(final PropertyDescriptor descriptor, final AttributeTypeInfoBuilder typeBuilder, final Map<String, String> props){
            super(descriptor.getName(), "");
            properties = new HashMap<>(props);
            properties.put("displayName", descriptor.getDisplayName());
            properties.put("shortDescription", descriptor.getShortDescription());
            propertyType = descriptor.getPropertyType();
            getter = descriptor.getReadMethod();
            if(getter != null && !getter.isAccessible())
                getter.setAccessible(true);
            setter = descriptor.getWriteMethod();
            if(setter != null && !setter.isAccessible())
                setter.setAccessible(true);
            this.typeBuilder = new WeakReference<>(typeBuilder);
        }

        public final Object getValue(final Object beanInstance) throws ReflectiveOperationException {
            if(getter == null) return null;
            return getter.invoke(beanInstance);
        }

        public final void setValue(final Object beanInstance, final Object value) throws ReflectiveOperationException {
            setter.invoke(beanInstance, getAttributeType().convertFrom(value));
        }

        /**
         * By default, returns {@literal true}.
         *
         * @return
         */
        @Override
        public final boolean canRead() {
            return getter != null;
        }

        /**
         * Determines whether the value of this attribute can be changed, returns {@literal true} by default.
         *
         * @return {@literal true}, if the attribute value can be changed; otherwise, {@literal false}.
         */
        @Override
        public boolean canWrite() {
            return setter != null;
        }

        /**
         * Detects the attribute type (this method will be called by infrastructure once).
         *
         * @return Detected attribute type.
         */
        @Override
        protected final AttributeConvertibleTypeInfo<?> detectAttributeType() {
            final AttributeConvertibleTypeInfo<?> typeInfo = typeBuilder.get().createTypeInfo(propertyType);
            typeBuilder.clear();
            return typeInfo;
        }

        @Override
        public final int size() {
            return properties.size();
        }

        @Override
        public final boolean isEmpty() {
            return properties.isEmpty();
        }

        @Override
        public final boolean containsKey(final Object key) {
            return properties.containsKey(key);
        }

        @Override
        public final boolean containsValue(final Object value) {
            return properties.containsValue(value);
        }

        @Override
        public final String get(final Object key) {
            return properties.get(key);
        }

        @Override
        public final Set<String> keySet() {
            return properties.keySet();
        }

        @Override
        public final Collection<String> values() {
            return properties.values();
        }

        @Override
        public final Set<Entry<String, String>> entrySet() {
            return properties.entrySet();
        }
    }

    private final BeanInfo beanMetadata;
    private final AttributeTypeInfoBuilder typeInfoBuilder;
    private final Object beanInstance;

    /**
     * Initializes a new management connector.
     * @param typeBuilder Type information provider that provides property type converter.
     * @throws IllegalArgumentException typeBuilder is {@literal null}.
     */
    protected ManagementConnectorBean(final AttributeTypeInfoBuilder typeBuilder) throws IntrospectionException {
        if(typeBuilder == null) throw new IllegalArgumentException("typeBuilder is null.");
        this.typeInfoBuilder = typeBuilder;
        this.beanMetadata = Introspector.getBeanInfo(getClass(), ManagementConnectorBean.class);
        this.beanInstance = null;
    }

    private ManagementConnectorBean(final Object beanInstance, final AttributeTypeInfoBuilder typeBuilder) throws IntrospectionException {
        if(beanInstance == null) throw new IllegalArgumentException("beanInstance is null.");
        else if(typeBuilder == null) throw new IllegalArgumentException("typeBuilder is null.");
        this.beanInstance = beanInstance;
        this.beanMetadata = Introspector.getBeanInfo(beanInstance.getClass());
        this.typeInfoBuilder = typeBuilder;
    }

    /**
     * Creates SNAMP management connector from the specified Java Bean.
     * @param beanInstance An instance of the Java Bean to wrap.
     * @param typeBuilder Bean property type converter.
     * @param <T> Type of the Java Bean to wrap.
     * @return A new instance of the management connector that wraps the Java Bean.
     * @throws IntrospectionException
     */
    public static <T> ManagementConnectorBean wrap(final T beanInstance, final AttributeTypeInfoBuilder typeBuilder) throws IntrospectionException {
        return new ManagementConnectorBean(beanInstance, typeBuilder);
    }

    /**
     * Throws an exception if the connector is not initialized.
     */
    @Override
    protected void verifyInitialization() {
    }

    /**
     * Returns an array of all discovered attributes available for registration.
     * @return An array of all discovered attributes available for registration.
     */
    public final String[] availableAttributes(){
        final PropertyDescriptor[] properties = beanMetadata.getPropertyDescriptors();
        final String[] result = new String[properties.length];
        for(int i = 0; i < properties.length; i++)
            result[i] = properties[i].getName();
        return result;
    }

    /**
     * Connects the specified Java Bean property.
     * @param property Java Bean property to connect.
     * @param options Additional connection options.
     * @return An information about registered attribute.
     */
    protected final AttributeMetadata connectAttribute(final PropertyDescriptor property, final Map<String, String> options){
        return new JavaBeanPropertyMetadata(property, typeInfoBuilder, options);
    }

    /**
     * Connects to the specified attribute.
     *
     * @param attributeName The name of the attribute.
     * @param options       Attribute discovery options.
     * @return The description of the attribute.
     */
    @Override
    protected final AttributeMetadata connectAttribute(final String attributeName, final Map<String, String> options) {
        for(final PropertyDescriptor pd: beanMetadata.getPropertyDescriptors())
            if(Objects.equals(pd.getName(), attributeName))
                return connectAttribute(pd, options);
        return null;
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
    protected final Object getAttributeValue(final AttributeMetadata attribute, final TimeSpan readTimeout, final Object defaultValue) throws TimeoutException {
        if(attribute instanceof JavaBeanPropertyMetadata)
            try {
                return ((JavaBeanPropertyMetadata)attribute).getValue(beanInstance != null ? beanInstance : this);
            }
            catch (final ReflectiveOperationException e) {
                return null;
            }
        else return null;
    }

    /**
     * Sends the attribute value to the remote agent.
     *
     * @param attribute    The metadata of the attribute to set.
     * @param writeTimeout
     * @param value
     * @return {@literal true} if attribute is overridden successfully; otherwise, {@literal false}.
     */
    @Override
    protected final boolean setAttributeValue(final AttributeMetadata attribute, final TimeSpan writeTimeout, final Object value) {
        if(attribute.canWrite() && attribute instanceof JavaBeanPropertyMetadata){
            try {
                ((JavaBeanPropertyMetadata)attribute).setValue(beanInstance != null ? beanInstance : this, value);
                return true;
            }
            catch (final ReflectiveOperationException e) {
                return false;
            }
        }
        else return false;
    }

    /**
     * Invokes the Java Bean method.
     * @param action An action to execute.
     * @param args Action invocation arguments.
     * @return The invocation result.
     */
    protected final Object doAction(final MethodDescriptor action, final Arguments args){
        try {
            return action.getMethod().invoke(beanInstance != null ? beanInstance : this, args.values().toArray());
        } catch (final ReflectiveOperationException e) {
            return null;
        }
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
    public final Object doAction(final String actionName, final Arguments args, final TimeSpan timeout) throws UnsupportedOperationException, TimeoutException {
        for(final MethodDescriptor md: beanMetadata.getMethodDescriptors())
            if(Objects.equals(md.getName(), actionName))
                return doAction(md, args);
        return null;
    }

    /**
     * Releases all resources associated with this management connector.
     * @throws Exception
     */
    @Override
    public void close() throws Exception {
        //To change body of implemented methods use File | Settings | File Templates.
    }
}
