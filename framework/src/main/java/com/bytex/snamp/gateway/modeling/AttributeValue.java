package com.bytex.snamp.gateway.modeling;

import com.bytex.snamp.Convert;
import com.google.common.reflect.TypeToken;
import com.bytex.snamp.jmx.WellKnownType;

import javax.management.*;
import javax.management.openmbean.CompositeData;
import javax.management.openmbean.OpenType;
import javax.management.openmbean.TabularData;
import java.nio.Buffer;
import java.util.Objects;

/**
 * Represents value of the attribute.
 * @author Roman Sakno
 * @version 2.0
 * @since 1.0
 */
public class AttributeValue extends Attribute implements AttributeValueReader {
    private static final long serialVersionUID = -6273677063905416787L;
    private final WellKnownType type;

    AttributeValue(final String name,
                           final Object value,
                           final WellKnownType type){
        super(name, value);
        this.type = Objects.requireNonNull(type, "type cannot be detected");
    }

    /**
     * Initializes a new attribute value.
     * @param name The name of the attribute.
     * @param value The value of the attribute.
     * @param type The type of the attribute. Cannot be {@literal null}.
     * @param <T> The type of the attribute.
     */
    public <T> AttributeValue(final String name,
                              final T value,
                              final OpenType<T> type){
        this(name, value, WellKnownType.getType(type));
    }

    /**
     * Initializes a new attribute value.
     * @param name The name of the attribute.
     * @param value The value of the attribute.
     */
    public AttributeValue(final String name,
                          final Buffer value){
        this(name, value, WellKnownType.getType(value.getClass()));
    }

    public AttributeValue(final String name,
                          final CompositeData[] value){
        this(name, value, WellKnownType.DICTIONARY_ARRAY);
    }

    public AttributeValue(final String name,
                          final TabularData[] value){
        this(name, value, WellKnownType.TABLE_ARRAY);
    }

    /**
     * Gets type of the attribute.
     *
     * @return The type of the attribute.
     */
    @Override
    public final WellKnownType getType() {
        return type;
    }

    /**
     * Gets attribute value in typed manner.
     *
     * @param valueType The expected type of the attribute.
     * @return The typed attribute value.
     * @throws javax.management.InvalidAttributeValueException Attribute type mismatch.
     */
    @Override
    public final <T> T getValue(final TypeToken<T> valueType) throws InvalidAttributeValueException {
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
     *
     * @param valueType The expected type of the attribute.
     * @return The typed attribute value.
     * @throws javax.management.InvalidAttributeValueException Attribute type mismatch.
     */
    @Override
    public final <T> T getValue(final Class<T> valueType) throws InvalidAttributeValueException {
        return getValue(TypeToken.of(valueType));
    }

    /**
     * Gets attribute value in typed manner.
     *
     * @param valueType The expected type of the attribute.
     * @return The typed attribute value.
     * @throws javax.management.InvalidAttributeValueException Attribute type mismatch.
     */
    @Override
    public final Object getValue(final WellKnownType valueType) throws InvalidAttributeValueException {
        return getValue(valueType.getJavaType());
    }

    /**
     * Gets attribute value in typed manner.
     *
     * @param valueType The expected type of the attribute.
     * @return The typed attribute value.
     * @throws javax.management.MBeanException                 Internal connector error.
     * @throws javax.management.AttributeNotFoundException     This attribute is disconnected.
     * @throws javax.management.ReflectionException            Internal connector error.
     * @throws javax.management.InvalidAttributeValueException Attribute type mismatch.
     */
    @SuppressWarnings("unchecked")
    @Override
    public final  <T> T getValue(final OpenType<T> valueType) throws MBeanException, AttributeNotFoundException, ReflectionException, InvalidAttributeValueException {
        final Object result = getValue();
        if(valueType.isValue(result)) return (T)result;
        else throw new InvalidAttributeValueException(String.format("Value %s is not of type %s", result, valueType));
    }

    /**
     * Computes a result, or throws an exception if unable to do so.
     *
     * @return computed result
     */
    @Override
    public final Object call() {
        return getValue();
    }

    /**
     * Compares the current Attribute Object with another Attribute Object.
     *
     * @param other The Attribute that the current Attribute is to be compared with.
     * @return True if the two Attribute objects are equal, otherwise false.
     */
    public boolean equals(final AttributeValue other) {
        return other != null &&
                super.equals(other) &&
                Objects.equals(type, other.type);
    }

    /**
     * Compares the current Attribute Object with another Attribute Object.
     *
     * @param other The Attribute that the current Attribute is to be compared with.
     * @return True if the two Attribute objects are equal, otherwise false.
     */
    @Override
    public final boolean equals(final Object other) {
        return other instanceof AttributeValue ?
                equals((AttributeValue) other) :
                super.equals(other);
    }

    /**
     * Returns a hash code value for this attribute.
     *
     * @return a hash code value for this attribute.
     */
    @Override
    public int hashCode() {
        final Object value = getValue();
        return value != null ?
                Objects.hash(value, getName(), type) :
                Objects.hash(getName(), type);
    }
}
