package com.bytex.snamp.jmx;

import com.bytex.snamp.MethodStub;
import com.bytex.snamp.LazyValueFactory;
import com.bytex.snamp.LazyValue;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Maps;

import javax.management.*;
import javax.management.openmbean.*;
import java.lang.reflect.Method;
import java.util.*;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Supplier;

import static com.bytex.snamp.ArrayUtils.emptyArray;
import static com.bytex.snamp.jmx.DescriptorUtils.DEFAULT_VALUE_FIELD;

/**
 * Represents an abstract class for building Open MBeans.
 * <p>
 *     You can derive from this to simplify writing of Open MBeans.
 *     Note that this class has the following limitations for MBean modeling:
 *     <ul>
 *         <li>You can use only Open Types for attributes and operations.</li>
 *         <li>You cannot derive from {@link javax.management.Notification}, therefore, you cannot use your own notification classes.</li>
 *         <li>MBean operation cannot be overloaded with the same name.</li>
 *         <li>No constructors will be exposed to the JMX client (array of {@link javax.management.openmbean.OpenMBeanConstructorInfo} always empty).</li>
 *     </ul>
 * </p>
 * @author Roman Sakno
 * @version 2.0
 * @since 1.0
 */
public abstract class OpenMBean extends NotificationBroadcasterSupport implements DynamicMBean {

    /**
     * Represents a superclass for all OpeMBean elements, such as attributes and operations.
     * @param <T> Type of the provided MBean feature.
     * @author Roman Sakno
     * @since 1.0
     * @version 2.0
     * @see OpenMBean.OpenNotification
     * @see OpenMBean.OpenOperation
     * @see OpenMBean.OpenAttribute
     */
    public static abstract class OpenMBeanElement<T extends MBeanFeatureInfo> implements Supplier<T> {
        /**
         * Represents name of this element.
         */
        public final String name;

        private OpenMBeanElement(final String elementName){
            this.name = elementName;
        }

        /**
         * Gets descriptor for this element.
         * <p>
         *     In the default implementation this method returns {@literal null}.
         * </p>
         * @return The descriptor of this element.
         * @see javax.management.ImmutableDescriptor
         */
        @MethodStub
        protected Descriptor getDescriptor(){
            return ImmutableDescriptor.EMPTY_DESCRIPTOR;
        }

        /**
         * Gets description of this element.
         * @return The description of this element.
         */
        protected abstract String getDescription();
    }

    /**
     * Represents superclass for all notification descriptors.
     * @param <N> Type of the native notification.
     * @author Roman Sakno
     * @since 1.0
     * @version 2.0
     */
    public static abstract class OpenNotification<N> extends OpenMBeanElement<MBeanNotificationInfo>{
        private final String[] types;
        private final Class<N> eventObjectType;
        private final AtomicLong sequenceNumber;

        /**
         * Initializes a new instance of the notification descriptor.
         * @param name The name of the notification.
         * @param eventObject Type of the native notification object.
         * @param types An array of notification types.
         */
        protected OpenNotification(final String name,
                                final Class<N> eventObject,
                                final String... types){
            super(name);
            this.types = types.clone();
            this.eventObjectType = eventObject;
            sequenceNumber = new AtomicLong(0L);
        }

        /**
         * Extracts message from the native notification.
         * @param eventObject The native notification to parse.
         * @return The human-readable message of the notification.
         */
        protected abstract String getMessage(final N eventObject);

        /**
         * Extracts type of the notification from the native notification.
         * @param eventObject The native notification to parse.
         * @return The type of the notification.
         */
        protected abstract String getType(final N eventObject);

        /**
         * Extracts additional payload from the native notification.
         * <p>
         *     In the default implementation this method returns {@literal null}.
         * </p>
         * @param eventObject The native notification to parse.
         * @return The additional notification payload.
         */
        @MethodStub
        protected Object getUserData(final N eventObject){
            return null;
        }

        Notification createNotification(final Object sender, final N eventObject){
            final Notification result = new Notification(getType(eventObject),
                    sender,
                    sequenceNumber.getAndIncrement(),
                    getMessage(eventObject));
            result.setUserData(getUserData(eventObject));
            return result;
        }

        private Notification createNotificationUnsafe(final Object sender, final Object rawEventObject){
            return createNotification(sender, eventObjectType.cast(rawEventObject));
        }

        /**
         * Gets human-readable description for this notification.
         * @return A human-readable description of this notification.
         */
        @Override
        protected String getDescription(){
            return String.format("%s notification.", name);
        }

        /**
         * Creates a new MBean feature.
         *
         * @return A new MBean feature.
         */
        @Override
        public final MBeanNotificationInfo get() {
            return new MBeanNotificationInfo(types, name, getDescription(), getDescriptor());
        }
    }

    public static abstract class OpenOperation<R, T extends OpenType<R>> extends OpenMBeanElement<MBeanOperationInfo>{
        private static final class OperationArgumentException extends IllegalArgumentException{
            private static final long serialVersionUID = 1217237225436599211L;

            private OperationArgumentException(final OpenMBeanParameterInfo nullParameter) {
                super(String.format("Parameter %s cannot be null", nullParameter.getName()));
            }

            private OperationArgumentException(final OpenType<?> expectedType, final OpenDataException e){
                super(String.format("Illegal type of the actual argument. Expected type: %s", expectedType), e);
            }
        }

        /**
         * Describes parameter of the operation.
         * @param <T> Type of the parameter
         */
        public static final class TypedParameterInfo<T> implements Supplier<OpenMBeanParameterInfoSupport>{
            private final OpenMBeanParameterInfoSupport parameter;
            private final boolean nullable;

            public TypedParameterInfo(final String name,
                                      final String description,
                                      final OpenType<T> openType,
                                      final boolean nullable,
                                      final T defValue) {
                final Map<String, Object> descriptor = new HashMap<>();
                if(defValue != null)
                    descriptor.put(DEFAULT_VALUE_FIELD, defValue);
                this.parameter = new OpenMBeanParameterInfoSupport(name, description, openType, new ImmutableDescriptor(descriptor));
                this.nullable = nullable;
            }

            /**
             * Gets MBean operation parameter.
             *
             * @return MBean operation parameter.
             */
            @Override
            public OpenMBeanParameterInfoSupport get() {
                return parameter;
            }

            public TypedParameterInfo(final String name,
                                      final String description,
                                      final OpenType<T> openType) {
                this(name, description, openType, true);
            }

            public TypedParameterInfo(final String name,
                                      final String description,
                                      final OpenType<T> openType,
                                      final boolean nullable) {
                this(name, description, openType, nullable, null);
            }

            /**
             * Determines whether value of this parameter can be {@literal null}.
             *
             * @return {@literal true}, if this value of this parameter can be {@literal null}; otherwise, {@literal false}.
             */
            public boolean isNullable() {
                return nullable;
            }

            @SuppressWarnings("unchecked")
            public T getDefaultValue() {
                return (T)parameter.getDefaultValue();
            }

            @SuppressWarnings("unchecked")
            public OpenType<T> getOpenType() {
                return (OpenType<T>) parameter.getOpenType();
            }

            /**
             * Extracts actual value of this parameter from the map of arguments.
             * @param arguments A map of arguments.
             * @return Actual value of this parameter.
             * @throws IllegalArgumentException Incorrect actual value of this parameter.
             */
            public T getArgument(final Map<String, ?> arguments) throws OperationArgumentException {
                if (arguments.containsKey(parameter.getName())) {
                    try {
                        return cast(getOpenType(), arguments.get(parameter.getName()));
                    } catch (final OpenDataException e) {
                        throw new OperationArgumentException(getOpenType(), e);
                    }
                } else if (isNullable() || getDefaultValue() != null)
                    return getDefaultValue();
                else
                    throw new OperationArgumentException(parameter);
            }
        }

        /**
         * Represents return type of this operation.
         */
        protected final T returnType;
        private final ImmutableList<OpenMBeanParameterInfo> parameters;

        protected OpenOperation(final String operationName, final T returnType, final OpenMBeanParameterInfo... parameters){
            super(operationName);
            this.returnType = returnType;
            this.parameters = ImmutableList.copyOf(parameters);
        }

        protected OpenOperation(final String operationName, final T returnType, final TypedParameterInfo<?>... parameters) {
            this(operationName, returnType, Arrays.stream(parameters).map(TypedParameterInfo::get).toArray(OpenMBeanParameterInfo[]::new));
        }

        protected OpenOperation(final String operationName, final T returnType){
            this(operationName, returnType, emptyArray(OpenMBeanParameterInfo[].class));
        }

        public abstract R invoke(final Map<String, ?> arguments) throws Exception;

        private R invoke(final Object[] arguments) throws ReflectionException, MBeanException {
            if (arguments.length < parameters.size())
                throw new ReflectionException(new IllegalArgumentException(String.format("Invalid arguments count. Expected %s but found %s.", parameters.size(), arguments.length)));
            final Map<String, Object> args = Maps.newHashMapWithExpectedSize(arguments.length);
            for (int i = 0; i < arguments.length; i++) {
                final OpenMBeanParameterInfo paramInfo = parameters.get(i);
                args.put(paramInfo.getName(), arguments[i]);
            }
            try {
                return invoke(args);
            } catch (final OperationArgumentException e) {
                throw new MBeanException(e);
            } catch (final MBeanException | ReflectionException e) {
                throw e;
            } catch (final Exception e) {
                throw new ReflectionException(e);
            }
        }

        @Override
        protected String getDescription(){
            return String.format("%s operation.", name);
        }

        /**
         * Creates a new MBean feature.
         *
         * @return A new MBean feature.
         */
        @Override
        public final OpenMBeanOperationInfoSupport get() {
            return new OpenMBeanOperationInfoSupport(name,
                    getDescription(),
                    parameters.toArray(new OpenMBeanParameterInfo[parameters.size()]),
                    returnType,
                    getImpact(),
                    getDescriptor());
        }

        protected int getImpact(){
            return MBeanOperationInfo.UNKNOWN;
        }
    }

    /**
     * Represents OpenMBean attribute.
     * @param <V> Type of the Open Type value.
     * @param <T> Open Type.
     * @author Roman Sakno
     * @since 1.0
     */
    public static abstract class OpenAttribute<V, T extends OpenType<V>> extends OpenMBeanElement<MBeanAttributeInfo> {
        private static final String GET_VALUE_METHOD = "getValue";
        private static final String SET_VALUE_METHOD = "setValue";

        /**
         * Represents type of the attribute.
         */
        protected final T openType;
        private final LazyValue<Class<V>> javaType;
        private final ThreadLocal<OpenMBean> owner;

        /**
         * Initializes a new attribute.
         * @param attributeName The name of the attribute.
         * @param openType The type of the attribute.
         */
        protected OpenAttribute(final String attributeName, final T openType){
            super(attributeName);
            this.openType = openType;
            javaType = LazyValueFactory.THREAD_SAFE.of(() -> null);
            owner = new ThreadLocal<>();
        }

        /**
         * Gets description of this attribute.
         * @return The description of this attribute.
         */
        @Override
        protected String getDescription(){
            return String.format("%s description.", name);
        }

        /**
         * Determines whether the attribute is flag.
         * <p>
         *     In the default implementation this method returns {@literal false}.
         * </p>
         * @return {@literal true}, if this attribute is flag; otherwise, {@literal false}.
         */
        public boolean isIs(){
            return false;
        }

        /**
         * Gets value of the attribute.
         * @return The value of the attribute.
         * @throws java.lang.UnsupportedOperationException The attribute is write-only.
         * @throws java.lang.Exception Some error occurred during get operation.
         */
        @MethodStub
        public V getValue() throws Exception{
            throw new UnsupportedOperationException(String.format("Attribute %s is not readable.", name));
        }

        private V getValueInternal(final OpenMBean owner) throws MBeanException, ReflectionException{
            this.owner.set(owner);
            try{
                return getValue();
            }
            catch (final MBeanException | ReflectionException e){
                throw e;
            }
            catch (final UnsupportedOperationException e){
                throw new MBeanException(e);
            }
            catch (final Exception e){
                throw new ReflectionException(e);
            }
            finally {
                this.owner.remove();
            }
        }

        /**
         * Sets value of the attribute.
         * @param value A new value of the attribute.
         * @throws java.lang.UnsupportedOperationException The attribute is read-only.
         * @throws java.lang.Exception Some error occurred during set operation.
         */
        @SuppressWarnings("UnusedParameters")
        @MethodStub
        public void setValue(final V value) throws Exception{
            throw new UnsupportedOperationException(String.format("Attribute %s is not writable.", name));
        }

        private void setValueInternal(final Object value, final OpenMBean owner) throws MBeanException, ReflectionException, InvalidAttributeValueException{
            this.owner.set(owner);
            try{
                setValue(getJavaType().cast(value));
            }
            catch (final MBeanException | ReflectionException | InvalidAttributeValueException e){
                throw e;
            }
            catch (final UnsupportedOperationException e){
                throw new MBeanException(e);
            }
            catch (final Exception e){
                throw new ReflectionException(e);
            }
            finally {
                this.owner.remove();
            }
        }

        /**
         * Gets owner of this attribute.
         * <p>
         *      This contextual property is accessible inside of
         *      {@link #getValue()} or {@link #setValue(Object)} method only.
         * @return The owner of this attribute.
         */
        protected final OpenMBean getOwner(){
            return owner.get();
        }

        @SuppressWarnings("unchecked")
        private Class<V> getJavaType() throws ClassNotFoundException {
            try {
                return javaType.get(() -> (Class<V>) Class.forName(openType.getClassName()));
            } catch (final ClassNotFoundException e) {
                throw e;
            } catch (final Exception e) {
                throw new ClassNotFoundException(String.format("Unable to load class for type '%s'", openType), e);
            }
        }

        /**
         * Determines whether this attribute is readable.
         * @return {@literal true}, if this method is readable.
         */
        public final boolean isReadable(){
            try {
                final Method getter = getClass().getMethod(GET_VALUE_METHOD);
                return getClass().equals(getter.getDeclaringClass());
            }
            catch (final NoSuchMethodException e) {
                return false;
            }
        }

        /**
         * Determines whether this attribute is writable.
         * @return {@literal true}, if this method is writable.
         */
        public final boolean isWritable(){
            try {
                final Method getter = getClass().getMethod(SET_VALUE_METHOD, getJavaType());
                return getClass().equals(getter.getDeclaringClass());
            }
            catch (final ReflectiveOperationException e) {
                return false;
            }
        }

        /**
         * Creates a new MBean feature.
         *
         * @return A new MBean feature.
         */
        @Override
        public final OpenMBeanAttributeInfoSupport get() {
            return new OpenMBeanAttributeInfoSupport(name,
                    getDescription(),
                    openType,
                    isReadable(),
                    isWritable(),
                    isIs(),
                    getDescriptor());
        }
    }

    private final ImmutableList<OpenAttribute<?, ?>> attributes;
    private final ImmutableList<OpenNotification<?>> notifications;
    private final ImmutableList<OpenOperation<?, ?>> operations;

    /**
     * Initializes a new OpenMBean.
     * @param elements A collection of OpenMBean elements.
     */
    protected OpenMBean(final OpenMBeanElement... elements){
        final ImmutableList.Builder<OpenAttribute<?, ?>> attributes = ImmutableList.builder();
        final ImmutableList.Builder<OpenNotification<?>> notifications = ImmutableList.builder();
        final ImmutableList.Builder<OpenOperation<?, ?>> operations = ImmutableList.builder();
        //parse bean elements
        for(final OpenMBeanElement element: elements)
            if(element instanceof OpenAttribute<?, ?>)
                attributes.add((OpenAttribute<?, ?>)element);
            else if(element instanceof OpenNotification<?>)
                notifications.add((OpenNotification<?>)element);
            else if(element instanceof OpenOperation<?, ?>)
                operations.add((OpenOperation<?, ?>)element);
        this.attributes = attributes.build();
        this.notifications = notifications.build();
        this.operations = operations.build();
    }

    /**
     * Reads attribute value.
     * @param attributeType The type of the attribute definition. Cannot be {@literal null}.
     * @param <V> The type of the attribute value.
     * @return The attribute value.
     * @throws ReflectionException Wrapped exception occurred in {@link OpenAttribute#getValue()}
     * @throws MBeanException Failed to read attribute.
     * @throws AttributeNotFoundException The attribute of the specified type doesn't exist.
     */
    protected final <V> V getAttribute(final Class<? extends OpenAttribute<V, ?>> attributeType) throws ReflectionException, MBeanException, AttributeNotFoundException {
        final OpenAttribute<V, ?> attributeDef = getAttributeInfo(attributeType);
        if(attributeDef != null)
            return attributeDef.getValueInternal(this);
        else throw new AttributeNotFoundException(String.format("Attribute %s doesn't exist", attributeType));
    }

    protected final <A extends OpenAttribute<?, ?>> A getAttributeInfo(final Class<A> attributeType){
        for(final OpenAttribute<?, ?> attribute: attributes)
            if(attributeType.isInstance(attribute))
                return attributeType.cast(attribute);
        return null;
    }

    /**
     * Writes attribute value.
     * @param attributeType The type of the attribute definition.
     * @param value The attribute value.
     * @param <V> Type of the attribute value.
     * @throws ReflectionException Wrapped exception occurred in {@link com.bytex.snamp.jmx.OpenMBean.OpenAttribute#setValue(Object)}.
     * @throws InvalidAttributeValueException Invalid attribute value.
     * @throws MBeanException Failed to write attribute value.
     * @throws AttributeNotFoundException The attribute of the specified type doesn't exist.
     */
    protected final <V> void setAttribute(final Class<? extends OpenAttribute<V, ?>> attributeType, final V value) throws MBeanException, InvalidAttributeValueException, ReflectionException, AttributeNotFoundException {
        final OpenAttribute<V, ?> attributeDef = getAttributeInfo(attributeType);
        if(attributeDef != null)
            attributeDef.setValueInternal(value, this);
        else throw new AttributeNotFoundException(String.format("Attribute %s doesn't exist", attributeType));
    }

    /**
     * Obtain the value of a specific attribute of the Dynamic MBean.
     *
     * @param name The name of the attribute to be retrieved
     * @return The value of the attribute retrieved.
     * @throws javax.management.AttributeNotFoundException
     * @throws javax.management.MBeanException             Wraps a <CODE>java.lang.Exception</CODE> thrown by the MBean's getter.
     * @throws javax.management.ReflectionException        Wraps a <CODE>java.lang.Exception</CODE> thrown while trying to invoke the getter.
     * @see #setAttribute
     */
    @Override
    public final Object getAttribute(final String name) throws AttributeNotFoundException, MBeanException, ReflectionException {
        for(final OpenAttribute<?, ?> attribute: attributes)
            if(Objects.equals(name, attribute.name))
                return attribute.getValueInternal(this);
        throw new AttributeNotFoundException(String.format("Attribute %s doesn't exist.", name));
    }

    /**
     * Set the value of a specific attribute of the Dynamic MBean.
     *
     * @param attribute The identification of the attribute to
     *                  be set and  the value it is to be set to.
     * @throws javax.management.AttributeNotFoundException
     * @throws javax.management.InvalidAttributeValueException
     * @throws javax.management.MBeanException                 Wraps a <CODE>java.lang.Exception</CODE> thrown by the MBean's setter.
     * @throws javax.management.ReflectionException            Wraps a <CODE>java.lang.Exception</CODE> thrown while trying to invoke the MBean's setter.
     * @see #getAttribute
     */
    @Override
    public final void setAttribute(final Attribute attribute) throws AttributeNotFoundException, InvalidAttributeValueException, MBeanException, ReflectionException {
        for(final OpenAttribute<?, ?> setter: attributes)
            if(Objects.equals(attribute.getName(), setter.name)) {
                setter.setValueInternal(attribute.getValue(), this);
                return;
            }
        throw new AttributeNotFoundException(String.format("Attribute %s doesn't exist.", attribute.getName()));
    }

    /**
     * Get the values of several attributes of the Dynamic MBean.
     *
     * @param names A list of the attributes to be retrieved.
     * @return The list of attributes retrieved.
     * @see #setAttributes
     */
    @Override
    public final AttributeList getAttributes(final String[] names) {
        final AttributeList result = new AttributeList();
        for(final String name: names)
            try{
                result.add(new Attribute(name, getAttribute(name)));
            }
            catch(final Exception ignored){

            }
        return result;
    }

    /**
     * Sets the values of several attributes of the Dynamic MBean.
     *
     * @param attributes A list of attributes: The identification of the
     *                   attributes to be set and  the values they are to be set to.
     * @return The list of attributes that were set, with their new values.
     * @see #getAttributes
     */
    @Override
    public final AttributeList setAttributes(final AttributeList attributes) {
        final AttributeList result = new AttributeList();
        for(final Object attr: attributes)
            try{
                setAttribute((Attribute)attr);
                result.add(attr);
            }
            catch (final Exception ignored){

            }
        return result;
    }

    /**
     * Allows an action to be invoked on the Dynamic MBean.
     *
     * @param actionName The name of the action to be invoked.
     * @param params     An array containing the parameters to be set when the action is
     *                   invoked.
     * @param signature  An array containing the signature of the action. The class objects will
     *                   be loaded through the same class loader as the one used for loading the
     *                   MBean on which the action is invoked.
     * @return The object returned by the action, which represents the result of
     * invoking the action on the MBean specified.
     * @throws javax.management.MBeanException      Wraps a <CODE>java.lang.Exception</CODE> thrown by the MBean's invoked method.
     * @throws javax.management.ReflectionException Wraps a <CODE>java.lang.Exception</CODE> thrown while trying to invoke the method
     */
    @Override
    public final Object invoke(final String actionName, final Object[] params, final String[] signature) throws MBeanException, ReflectionException {
        for(final OpenOperation<?, ?> operation: operations)
            if(Objects.equals(actionName, operation.name))
                return operation.invoke(params);
        throw new MBeanException(new UnsupportedOperationException(String.format("Action %s doesn't exist.", actionName)));
    }

    /**
     * Gets description of this MBean.
     * @return Description of this MBean.
     */
    protected String getDescription(){
        return "OpenMBean implementation.";
    }

    /**
     * Gets descriptor for this MBean.
     * @return The descriptor for this MBean.
     */
    protected Descriptor getDescriptor(){
        return null;
    }

    private OpenMBeanAttributeInfo[] getAttributes(){
        final OpenMBeanAttributeInfo[] result = new OpenMBeanAttributeInfo[attributes.size()];
        for(int i = 0; i < attributes.size(); i++)
            result[i] = attributes.get(i).get();
        return result;
    }

    private MBeanNotificationInfo[] getNotifications(){
        final MBeanNotificationInfo[] result = new MBeanNotificationInfo[notifications.size()];
        for(int i = 0; i < notifications.size(); i++)
            result[i] = notifications.get(i).get();
        return result;
    }

    private OpenMBeanOperationInfo[] getOperations(){
        final OpenMBeanOperationInfoSupport[] result = new OpenMBeanOperationInfoSupport[operations.size()];
        for(int i = 0; i < operations.size(); i++)
            result[i] = operations.get(i).get();
        return result;
    }

    protected final <N> void sendNotification(final Class<? extends OpenNotification<N>> notifType, final N eventObject){
        notifications.stream()
                .filter(notifType::isInstance)
                .forEach(n -> sendNotification(notifType.cast(n).createNotification(OpenMBean.this, eventObject)));
    }

    public final void sendNotification(final String name, final Object eventObject){
        notifications.stream()
                .filter(n -> n.name.equals(name))
                .forEach(n -> sendNotification(n.createNotificationUnsafe(OpenMBean.this, eventObject)));
    }

    /**
     * Provides the exposed attributes and actions of the Dynamic MBean using an MBeanInfo object.
     *
     * @return An instance of <CODE>MBeanInfo</CODE> allowing all attributes and actions
     * exposed by this Dynamic MBean to be retrieved.
     */
    @Override
    public final OpenMBeanInfoSupport getMBeanInfo() {
        return new OpenMBeanInfoSupport(getClass().getName(),
                getDescription(),
                getAttributes(),
                emptyArray(OpenMBeanConstructorInfo[].class),
                getOperations(),
                getNotifications(),
                getDescriptor());
    }

    public static abstract class OpenOneWayOperation extends OpenMBeanElement<MBeanOperationInfo>{

        protected OpenOneWayOperation(final String operationName){
            super(operationName);
        }

        public abstract void invoke() throws Exception;

        @Override
        protected String getDescription(){
            return String.format("%s operation.", name);
        }

        /**
         * Creates a new MBean feature.
         *
         * @return A new MBean feature.
         */
        @Override
        public final OpenMBeanOperationInfoSupport get() {
            return new OpenMBeanOperationInfoSupport(name,
                    getDescription(),
                    emptyArray(OpenMBeanParameterInfo[].class),
                    SimpleType.VOID,
                    getImpact(),
                    getDescriptor());
        }

        protected int getImpact(){
            return MBeanOperationInfo.UNKNOWN;
        }
    }

    /**
     * Casts value to the specified JMX Open Type.
     * @param type JMX Open Type. Cannot be {@literal null}.
     * @param value Value to cast.
     * @param <T> Type of the conversion result.
     * @return Converter value.
     * @throws OpenDataException Unable to cast value to the specified JMX Open Type.
     */
    @SuppressWarnings("unchecked")
    public static <T> T cast(final OpenType<T> type, final Object value) throws OpenDataException{
        if(type.isValue(value))
            return (T)value;
        else throw new OpenDataException(String.format("Unable cast %s to %s", value, type));
    }
}
