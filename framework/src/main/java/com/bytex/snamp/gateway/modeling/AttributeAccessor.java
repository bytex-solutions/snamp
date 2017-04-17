package com.bytex.snamp.gateway.modeling;

import com.bytex.snamp.Acceptor;
import com.bytex.snamp.Convert;
import com.bytex.snamp.concurrent.LazyStrongReference;
import com.bytex.snamp.connector.FeatureModifiedEvent;
import com.bytex.snamp.connector.attributes.AttributeDescriptor;
import com.bytex.snamp.connector.attributes.AttributeSupport;
import com.bytex.snamp.jmx.DescriptorUtils;
import com.bytex.snamp.jmx.JMExceptionUtils;
import com.bytex.snamp.jmx.WellKnownType;
import com.google.common.reflect.TypeToken;

import javax.management.*;
import javax.management.openmbean.OpenType;
import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.util.Objects;
import java.util.Optional;

import static com.bytex.snamp.internal.Utils.callAndWrapException;

/**
 * Exposes access to individual management attribute.
 * <p>
 *     This accessor can be used for retrieving and changing value of the attribute.
 * @author Roman Sakno
 * @since 1.0
 * @version 2.0
 */
public class AttributeAccessor extends FeatureAccessor<MBeanAttributeInfo> implements AttributeValueReader, Acceptor<Object, JMException> {
    /**
     * Represents an exception that can be produced by attribute interceptor.
     * @author Roman Sakno
     * @since 1.0
     * @version 2.0
     */
    public static class InterceptionException extends ReflectionException{
        private static final long serialVersionUID = 8373399508228810347L;

        /**
         * Initializes a new exception and wraps actual exception.
         * @param e The wrapped exception.
         */
        public InterceptionException(final Exception e) {
            super(e);
        }
    }

    private AttributeSupport attributeSupport;
    private final LazyStrongReference<WellKnownType> wellKnownType;
    private final LazyStrongReference<OpenType<?>> openType;

    /**
     * Initializes a new attribute accessor.
     * @param metadata The metadata of the attribute. Cannot be {@literal null}.
     */
    public AttributeAccessor(final MBeanAttributeInfo metadata) {
        super(metadata);
        attributeSupport = null;
        wellKnownType = new LazyStrongReference<>();
        openType = new LazyStrongReference<>();
    }

    public AttributeAccessor(final String attributeName, final AttributeSupport support) throws AttributeNotFoundException{
        this(getMetadata(attributeName, support));
        attributeSupport = Objects.requireNonNull(support);
    }

    private static MBeanAttributeInfo getMetadata(final String attributeName, final AttributeSupport attributeSupport) throws AttributeNotFoundException {
        final MBeanAttributeInfo attributeInfo = attributeSupport.getAttributeInfo(attributeName);
        if(attributeInfo == null)
            throw JMExceptionUtils.attributeNotFound(attributeName);
        else
            return attributeInfo;
    }

    /**
     * Determines whether the feature of the managed resource is accessible
     * through this object.
     *
     * @return {@literal true}, if this feature is accessible; otherwise, {@literal false}.
     */
    @Override
    public final boolean isConnected() {
        return attributeSupport != null;
    }

    private AttributeSupport ensureConnected() throws AttributeNotFoundException {
        final AttributeSupport as = attributeSupport;
        if(as == null)
            throw new AttributeNotFoundException(String.format("Attribute %s is disconnected", getName()));
        else return as;
    }

    @Override
    public final boolean processEvent(final FeatureModifiedEvent<MBeanAttributeInfo> event) {
        assert event.getSource() instanceof AttributeSupport;
        switch (event.getModifier()) {
            case ADDED:
                connect((AttributeSupport) event.getSource());
                return true;
            case REMOVING:
                close();
                return true;
            default:
                return false;
        }
    }

    private void connect(final AttributeSupport value){
        this.attributeSupport = value;
    }

    /**
     * Disconnects attribute accessor from the managed resource.
     */
    @Override
    public final void close() {
        attributeSupport = null;
        wellKnownType.reset();
        openType.reset();
        super.close();
    }

    /**
     * Gets name of the attribute.
     * @return The name of the attribute.
     */
    public final String getName(){
        return getMetadata().getName();
    }

    /**
     * Gets type of this attribute.
     * @return The type of this attribute.
     */
    public final WellKnownType getType() {
        return wellKnownType.<FeatureAccessor<MBeanAttributeInfo>>lazyGet(this, accessor -> AttributeDescriptor.getType(accessor.getMetadata()));
    }

    /**
     * Gets JMX Open Type of this attribute.
     * @return The type of this attribute.
     */
    public final OpenType<?> getOpenType() {
        return openType.<FeatureAccessor<MBeanAttributeInfo>>lazyGet(this, accessor -> AttributeDescriptor.getOpenType(accessor.getMetadata()));
    }

    /**
     * Changes the value of the attribute.
     * @param value A new attribute value.
     * @throws javax.management.AttributeNotFoundException This attribute is disconnected.
     * @throws javax.management.MBeanException Internal connector error.
     * @throws javax.management.ReflectionException Internal connector error.
     * @throws javax.management.InvalidAttributeValueException Value type mismatch.
     */
    public final void setValue(final Object value) throws AttributeNotFoundException, MBeanException, ReflectionException, InvalidAttributeValueException {
        ensureConnected().setAttribute(new Attribute(getName(), interceptSet(value)));
    }

    /**
     * Changes the value of the attribute.
     * @param value A new attribute value.
     * @throws javax.management.JMException Internal connector error.
     * @throws javax.management.InvalidAttributeValueException Value type mismatch.
     * @throws javax.management.AttributeNotFoundException This attribute is disconnected.
     */
    @Override
    public final void accept(final Object value) throws JMException {
        setValue(value);
    }

    /**
     * Gets attribute value.
     * @return The attribute value.
     * @throws javax.management.MBeanException Internal connector error.
     * @throws javax.management.AttributeNotFoundException This attribute is disconnected.
     * @throws javax.management.ReflectionException Internal connector error.
     */
    public final Object getValue() throws MBeanException, AttributeNotFoundException, ReflectionException {
        return interceptGet(ensureConnected().getAttribute(getName()));
    }

    /**
     * Gets attribute value in typed manner.
     * @param valueType The expected type of the attribute.
     * @param <T> The expected type of the attribute.
     * @return The typed attribute value.
     * @throws javax.management.MBeanException Internal connector error.
     * @throws javax.management.AttributeNotFoundException This attribute is disconnected.
     * @throws javax.management.ReflectionException Internal connector error.
     * @throws javax.management.InvalidAttributeValueException Attribute type mismatch.
     */
    public final  <T> T getValue(final TypeToken<T> valueType) throws MBeanException, AttributeNotFoundException, ReflectionException, InvalidAttributeValueException{
        final Object result = getValue();
        try {
            return Convert.toTypeToken(result, valueType);
        }
        catch (final ClassCastException e){
            throw new InvalidAttributeValueException(e.getMessage());
        }
    }

    /**
     * Gets attribute value in typed manner.
     * @param valueType The expected type of the attribute.
     * @param <T> The expected type of the attribute.
     * @return The typed attribute value.
     * @throws javax.management.MBeanException Internal connector error.
     * @throws javax.management.AttributeNotFoundException This attribute is disconnected.
     * @throws javax.management.ReflectionException Internal connector error.
     * @throws javax.management.InvalidAttributeValueException Attribute type mismatch.
     */
    public final  <T> T getValue(final Class<T> valueType) throws MBeanException, AttributeNotFoundException, ReflectionException, InvalidAttributeValueException{
        return getValue(TypeToken.of(valueType));
    }

    /**
     * Gets attribute value in typed manner.
     * @param valueType The expected type of the attribute.
     * @return The typed attribute value.
     * @throws javax.management.MBeanException Internal connector error.
     * @throws javax.management.AttributeNotFoundException This attribute is disconnected.
     * @throws javax.management.ReflectionException Internal connector error.
     * @throws javax.management.InvalidAttributeValueException Attribute type mismatch.
     */
    public final Object getValue(final WellKnownType valueType) throws MBeanException, AttributeNotFoundException, ReflectionException, InvalidAttributeValueException{
        try {
            return valueType.convert(getValue());
        } catch (final ClassCastException e){
            final InvalidAttributeValueException invalidValue = new InvalidAttributeValueException(e.getMessage());
            invalidValue.initCause(e);
            throw invalidValue;
        }
    }

    /**
     * Gets attribute value in typed manner.
     * @param valueType The expected type of the attribute.
     * @param <T> The expected type of the attribute.
     * @return The typed attribute value.
     * @throws javax.management.MBeanException Internal connector error.
     * @throws javax.management.AttributeNotFoundException This attribute is disconnected.
     * @throws javax.management.ReflectionException Internal connector error.
     * @throws javax.management.InvalidAttributeValueException Attribute type mismatch.
     */
    @SuppressWarnings("unchecked")
    public final  <T> T getValue(final OpenType<T> valueType) throws MBeanException, AttributeNotFoundException, ReflectionException, InvalidAttributeValueException{
        final Object result = getValue();
        if(valueType.isValue(result)) return (T)result;
        else throw new InvalidAttributeValueException(String.format("Value %s is not of type %s", result, valueType));
    }

    /**
     * Gets attribute value and its type.
     * @return The attribute value and type.
     * @throws javax.management.MBeanException Internal connector error.
     * @throws javax.management.AttributeNotFoundException This attribute is disconnected.
     * @throws javax.management.ReflectionException Internal connector error.
     */
    public final AttributeValue getRawValue() throws MBeanException, AttributeNotFoundException, ReflectionException{
        return new AttributeValue(getName(), getValue(), getType());
    }

    /**
     * Determines whether this accessor supports attribute value reading.
     * @return {@literal true}, if this attribute is readable; otherwise, {@literal false}.
     */
    public boolean canRead(){
        return getMetadata().isReadable();
    }

    /**
     * Determines whether this accessor supports attribute value writing.
     * @return {@literal true}, if this attribute is writable; otherwise, {@literal false}.
     */
    public boolean canWrite(){
        return getMetadata().isWritable();
    }

    /**
     * Intercepts {@link #setValue(Object)} invocation.
     * @param value The value of the attribute passed as an argument to {@link #setValue(Object)} method.
     * @return The value of the attribute that will be passed to the managed resource connector.
     * @throws javax.management.InvalidAttributeValueException Invalid attribute type.
     * @throws InterceptionException Internal interceptor error.
     */
    protected Object interceptSet(final Object value) throws InvalidAttributeValueException, InterceptionException{
        return value;
    }

    /**
     * Intercepts {@link #getValue()} invocation.
     * @param value The value of the attribute obtained from the managed resource.
     * @return The modified attribute value.
     * @throws InterceptionException Internal interceptor error.
     */
    protected Object interceptGet(final Object value) throws InterceptionException{
        return value;
    }

    public final Class<?> getRawType() throws ReflectionException {
        final String type = getMetadata().getType();
        final ClassLoader loader = getClass().getClassLoader();
        return callAndWrapException(() -> Class.forName(type, true, loader), ReflectionException::new);
    }

    /**
     * Gets attribute value.
     * @return The attribute value.
     * @throws javax.management.MBeanException Internal connector error.
     * @throws javax.management.AttributeNotFoundException This attribute is disconnected.
     * @throws javax.management.ReflectionException Internal connector error.
     */
    @Override
    public final Object call() throws MBeanException, AttributeNotFoundException, ReflectionException {
        return getValue();
    }

    private static Optional<BigDecimal> toBigDecimal(Object value,
                                         final DecimalFormat format) throws ParseException {
        if(value == null)
            return Optional.empty();
        else if(value instanceof String)
            value = format.parse((String)value);
        return Convert.toBigDecimal(value);
    }

    protected final boolean isInRange(final Number value,
                                      final DecimalFormat format) throws ParseException {
        final Optional<BigDecimal> minValue = toBigDecimal(DescriptorUtils.getRawMinValue(getMetadata().getDescriptor()),
                format);
        final Optional<BigDecimal> maxValue = toBigDecimal(DescriptorUtils.getRawMaxValue(getMetadata().getDescriptor()),
                format);
        final BigDecimal actualValue = toBigDecimal(value, format).orElseThrow(() -> new ParseException(value.toString(), 0));
        return !(minValue.isPresent() && actualValue.compareTo(minValue.get()) <= 0) && !(maxValue.isPresent() && actualValue.compareTo(maxValue.get()) >= 0);
    }

    public static int removeAll(final Iterable<? extends AttributeAccessor> attributes,
                             final MBeanAttributeInfo metadata){
        return FeatureAccessor.removeAll(attributes, metadata);
    }

    public static <A extends AttributeAccessor> A remove(final Iterable<A> attributes,
                                                         final MBeanAttributeInfo metadata){
        return FeatureAccessor.remove(attributes, metadata);
    }
}