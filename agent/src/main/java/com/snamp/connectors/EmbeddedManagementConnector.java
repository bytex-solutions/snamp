package com.snamp.connectors;

import com.snamp.TimeSpan;

import java.beans.*;
import java.lang.ref.*;
import java.util.*;
import java.util.concurrent.TimeoutException;
import java.lang.reflect.*;
import static com.snamp.connectors.AttributeTypeInfoBuilder.AttributeConvertibleTypeInfo;

/**
 * Represents SNAMP in-process management connector that exposes Java Bean properties through connector attributes.
 * @author roman
 */
public class EmbeddedManagementConnector extends ManagementConnectorBase {

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
    protected EmbeddedManagementConnector(final AttributeTypeInfoBuilder typeBuilder) throws IntrospectionException {
        if(typeBuilder == null) throw new IllegalArgumentException("typeBuilder is null.");
        this.typeInfoBuilder = typeBuilder;
        this.beanMetadata = Introspector.getBeanInfo(getClass(), EmbeddedManagementConnector.class);
        this.beanInstance = null;
    }

    private EmbeddedManagementConnector(final Object beanInstance, final AttributeTypeInfoBuilder typeBuilder) throws IntrospectionException {
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
    public static <T> EmbeddedManagementConnector wrap(final T beanInstance, final AttributeTypeInfoBuilder typeBuilder) throws IntrospectionException {
        return new EmbeddedManagementConnector(beanInstance, typeBuilder);
    }

    /**
     * Throws an exception if the connector is not initialized.
     */
    @Override
    protected void verifyInitialization() {
    }

    /**
     * Returns an array of all discovered attributes available for registration.
     * @return
     */
    public final String[] availableAttributes(){
        final PropertyDescriptor[] properties = beanMetadata.getPropertyDescriptors();
        final String[] result = new String[properties.length];
        for(int i = 0; i < properties.length; i++)
            result[i] = properties[i].getName();
        return result;
    }

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
     * @return
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

    @Override
    public void close() throws Exception {
        //To change body of implemented methods use File | Settings | File Templates.
    }
}
