package com.itworks.snamp.adapters;

import com.google.common.reflect.TypeToken;
import com.itworks.snamp.Consumer;
import com.itworks.snamp.ExceptionPlaceholder;
import com.itworks.snamp.TypeTokens;
import com.itworks.snamp.concurrent.SimpleCache;
import com.itworks.snamp.connectors.attributes.AttributeDescriptor;
import com.itworks.snamp.connectors.attributes.AttributeSupport;
import com.itworks.snamp.connectors.attributes.CustomAttributeInfo;
import com.itworks.snamp.jmx.DescriptorUtils;
import com.itworks.snamp.jmx.SimpleTypeParseException;
import com.itworks.snamp.jmx.SimpleTypeParser;
import com.itworks.snamp.jmx.WellKnownType;

import javax.management.*;
import javax.management.openmbean.OpenType;
import java.io.Serializable;
import java.util.Objects;

/**
 * Exposes access to individual management attribute.
 * <p>
 *     This accessor can be used for retrieving and changing value of the attribute.
 * @author Roman Sakno
 * @since 1.0
 * @version 1.0
 */
public class AttributeAccessor extends FeatureAccessor<MBeanAttributeInfo, AttributeSupport> implements AttributeValueReader, Consumer<Object, JMException> {
    /**
     * Represents a value of the attribute specified by the SNAMP administrator
     * in the attribute configuration parameter.
     * @author Roman Sakno
     * @since 1.0
     * @version 1.0
     */
    protected static class ValueLevel implements Serializable{
        private static final long serialVersionUID = -7199946659701345760L;
        private final String fieldName;
        private final int defaultComparisonResult;

        /**
         * Initializes a new definition of the special attribute value.
         * @param parameterName The name of the configuration parameter.
         * @param defaultComparisonResult Default comparison result used
         *                                when configuration parameter was not spcified.
         */
        public ValueLevel(final String parameterName,
                             final int defaultComparisonResult){
            this.fieldName = Objects.requireNonNull(parameterName);
            this.defaultComparisonResult = defaultComparisonResult;
        }
    }

    private static final class WellKnownTypeCache extends SimpleCache<FeatureAccessor<? extends MBeanAttributeInfo, ?>, WellKnownType, ExceptionPlaceholder> {

        @Override
        protected WellKnownType init(final FeatureAccessor<? extends MBeanAttributeInfo, ?> accessor) {
            return CustomAttributeInfo.getType(accessor.getMetadata());
        }

        private void clear(){
            invalidate();
        }
    }

    private static final class OpenTypeCache extends SimpleCache<FeatureAccessor<? extends MBeanAttributeInfo, ?>, OpenType<?>, ExceptionPlaceholder> {

        @Override
        protected OpenType<?> init(final FeatureAccessor<? extends MBeanAttributeInfo, ?> input) {
            return AttributeDescriptor.getOpenType(input.getMetadata());
        }

        private void clear(){
            invalidate();
        }
    }

    /**
     * Represents an exception that can be produced by attribute interceptor.
     * @author Roman Sakno
     * @since 1.0
     * @version 1.0
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

    protected static final ValueLevel MAX_VALUE_LEVEL = new ValueLevel(DescriptorUtils.MAX_VALUE_FIELD, -1);
    protected static final ValueLevel MIN_VALUE_LEVEL = new ValueLevel(DescriptorUtils.MIN_VALUE_FIELD, 1);
    private AttributeSupport attributeSupport;
    private final WellKnownTypeCache wellKnownType;
    private final OpenTypeCache openType;

    /**
     * Initializes a new attribute accessor.
     * @param metadata The metadata of the attribute. Cannot be {@literal null}.
     */
    public AttributeAccessor(final MBeanAttributeInfo metadata) {
        super(metadata);
        attributeSupport = null;
        wellKnownType = new WellKnownTypeCache();
        openType = new OpenTypeCache();
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

    private AttributeSupport verifyOnDisconnected() throws AttributeNotFoundException {
        final AttributeSupport as = attributeSupport;
        if(as == null)
            throw new AttributeNotFoundException(String.format("Attribute %s is disconnected", getName()));
        else return as;
    }

    @Override
    final void connect(final AttributeSupport value) {
        attributeSupport = value;
    }

    /**
     * Disconnects attribute accessor from the managed resource.
     */
    @Override
    public final void disconnect() {
        attributeSupport = null;
        wellKnownType.clear();
        openType.clear();
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
    public final WellKnownType getType(){
        return wellKnownType.get(this);
    }

    /**
     * Gets JMX Open Type of this attribute.
     * @return The type of this attribute.
     */
    public final OpenType<?> getOpenType(){
        return openType.get(this);
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
        verifyOnDisconnected().setAttribute(new Attribute(getName(), interceptSet(value)));
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
        return interceptGet(verifyOnDisconnected().getAttribute(getName()));
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
            return TypeTokens.cast(result, valueType);
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
        return getValue(valueType.getJavaType());
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

    public final Class<?> getRawType() throws ReflectionException{
        try {
            return Class.forName(getMetadata().getType());
        } catch (ClassNotFoundException e) {
            throw new ReflectionException(e);
        }
    }

    /**
     * Gets attribute value.
     *
     * @return The attribute value.
     * @throws javax.management.JMException Internal connector error.
     * @throws javax.management.AttributeNotFoundException This attribute is disconnected.
     */
    @Override
    public final Object call() throws JMException {
        return getValue();
    }

    private Comparable<?> parseSimpleField(final String fieldName,
                                                   final SimpleTypeParser parser) throws SimpleTypeParseException {
        final Object fieldValue = getMetadata().getDescriptor().getFieldValue(fieldName);
        final WellKnownType attributeType = getType();
        if(attributeType == null || fieldValue == null) return null;
        else if(attributeType.isSimpleType()){
            if(attributeType.isInstance(fieldValue)) return (Comparable<?>)fieldValue;
            else return parser.parse(attributeType, fieldValue.toString());
        }
        else return null;
    }

    @SuppressWarnings("unchecked")
    protected final int checkValue(final Object actualValue,
                                   final ValueLevel level,
                                   final SimpleTypeParser parser) {
        if (!(actualValue instanceof Comparable)) return level.defaultComparisonResult;
        final Comparable<?> fieldValue;
        try {
            fieldValue = parseSimpleField(level.fieldName, parser);
        } catch (final SimpleTypeParseException e) {
            return level.defaultComparisonResult;
        }
        return fieldValue == null ? level.defaultComparisonResult :
                ((Comparable) actualValue).compareTo(fieldValue);
    }
}