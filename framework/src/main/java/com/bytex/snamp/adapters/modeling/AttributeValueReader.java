package com.bytex.snamp.adapters.modeling;

import com.google.common.reflect.TypeToken;
import com.bytex.snamp.jmx.WellKnownType;

import javax.management.*;
import javax.management.openmbean.OpenType;
import java.util.concurrent.Callable;

/**
 * Represents attribute value reader.
 * @author Roman Sakno
 * @version 1.2
 * @since 1.0
 */
interface AttributeValueReader extends Callable<Object> {
    /**
     * Alias for {@link #getValue()}
     * @return The attribute value.
     * @throws MBeanException Internal connector error.
     * @throws AttributeNotFoundException This attribute is disconnected.
     * @throws ReflectionException Internal connector error.
     */
    @Override
    default Object call() throws MBeanException, AttributeNotFoundException, ReflectionException{
        return getValue();
    }

    /**
     * Gets attribute value.
     * @return The attribute value.
     * @throws MBeanException Internal connector error.
     * @throws AttributeNotFoundException This attribute is disconnected.
     * @throws ReflectionException Internal connector error.
     */
    Object getValue() throws MBeanException, AttributeNotFoundException, ReflectionException;

        /**
         * Gets type of the attribute.
         * @return The type of the attribute.
         */
    WellKnownType getType();

    /**
     * Gets attribute value in typed manner.
     * @param valueType The expected type of the attribute.
     * @param <T> The expected type of the attribute.
     * @return The typed attribute value.
     * @throws MBeanException Internal connector error.
     * @throws AttributeNotFoundException This attribute is disconnected.
     * @throws ReflectionException Internal connector error.
     * @throws javax.management.InvalidAttributeValueException Attribute type mismatch.
     */
    <T> T getValue(final TypeToken<T> valueType) throws MBeanException, AttributeNotFoundException, ReflectionException, InvalidAttributeValueException;

    /**
     * Gets attribute value in typed manner.
     * @param valueType The expected type of the attribute.
     * @param <T> The expected type of the attribute.
     * @return The typed attribute value.
     * @throws MBeanException Internal connector error.
     * @throws AttributeNotFoundException This attribute is disconnected.
     * @throws ReflectionException Internal connector error.
     * @throws InvalidAttributeValueException Attribute type mismatch.
     */
    <T> T getValue(final Class<T> valueType) throws MBeanException, AttributeNotFoundException, ReflectionException, InvalidAttributeValueException;

    /**
     * Gets attribute value in typed manner.
     * @param valueType The expected type of the attribute.
     * @return The typed attribute value.
     * @throws MBeanException Internal connector error.
     * @throws AttributeNotFoundException This attribute is disconnected.
     * @throws ReflectionException Internal connector error.
     * @throws InvalidAttributeValueException Attribute type mismatch.
     */
    Object getValue(final WellKnownType valueType) throws MBeanException, AttributeNotFoundException, ReflectionException, InvalidAttributeValueException;

    /**
     * Gets attribute value in typed manner.
     * @param valueType The expected type of the attribute.
     * @param <T> The expected type of the attribute.
     * @return The typed attribute value.
     * @throws MBeanException Internal connector error.
     * @throws AttributeNotFoundException This attribute is disconnected.
     * @throws ReflectionException Internal connector error.
     * @throws InvalidAttributeValueException Attribute type mismatch.
     */
    <T> T getValue(final OpenType<T> valueType) throws MBeanException, AttributeNotFoundException, ReflectionException, InvalidAttributeValueException;
}
