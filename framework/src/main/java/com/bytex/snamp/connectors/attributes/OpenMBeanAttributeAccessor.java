package com.bytex.snamp.connectors.attributes;

import com.google.common.collect.ImmutableSet;

import javax.management.openmbean.OpenMBeanAttributeInfo;
import javax.management.openmbean.OpenType;
import java.util.Set;

/**
 * Represents attribute of JMX open type that provides read/write methods.
 * @author Roman Sakno
 * @version 1.2
 * @since 1.0
 */
public abstract class OpenMBeanAttributeAccessor<T> extends OpenMBeanAttributeInfoImpl implements OpenMBeanAttributeInfo, AttributeDescriptorRead {
    private static final long serialVersionUID = 9200767724267121006L;

    protected OpenMBeanAttributeAccessor(final String attributeID,
                                         final String description,
                                         final OpenType<T> attributeType,
                                         final AttributeSpecifier specifier,
                                         final AttributeDescriptor descriptor){
        super(attributeID,
                attributeType,
                description,
                specifier,
                descriptor);
    }

    /**
     * Gets value of this attribute.
     * @return The value of this attribute.
     * @throws Exception Unable to read attribute value.
     */
    protected abstract T getValue() throws Exception;

    /**
     * Sets value of this attribute.
     * @param value The value of this attribute.
     * @throws Exception Unable to write attribute value.
     */
    protected abstract void setValue(final T value) throws Exception;

    /**
     * Returns the default value for this parameter, if it has one, or
     * <tt>null</tt> otherwise.
     *
     * @return the default value.
     */
    @Override
    public T getDefaultValue() {
        return null;
    }

    /**
     * Returns the set of legal values for this parameter, if it has
     * one, or <tt>null</tt> otherwise.
     *
     * @return the set of legal values.
     */
    @Override
    public Set<T> getLegalValues() {
        return ImmutableSet.of();
    }

    /**
     * Returns the minimal value for this parameter, if it has one, or
     * <tt>null</tt> otherwise.
     *
     * @return the minimum value.
     */
    @Override
    public Comparable<T> getMinValue() {
        return null;
    }

    /**
     * Returns the maximal value for this parameter, if it has one, or
     * <tt>null</tt> otherwise.
     *
     * @return the maximum value.
     */
    @Override
    public Comparable<T> getMaxValue() {
        return null;
    }

    /**
     * Returns <tt>true</tt> if this parameter has a specified default
     * value, or <tt>false</tt> otherwise.
     *
     * @return true if there is a default value.
     */
    @Override
    public boolean hasDefaultValue() {
        return false;
    }

    /**
     * Returns <tt>true</tt> if this parameter has a specified set of
     * legal values, or <tt>false</tt> otherwise.
     *
     * @return true if there is a set of legal values.
     */
    @Override
    public boolean hasLegalValues() {
        return false;
    }

    /**
     * Returns <tt>true</tt> if this parameter has a specified minimal
     * value, or <tt>false</tt> otherwise.
     *
     * @return true if there is a minimum value.
     */
    @Override
    public boolean hasMinValue() {
        return false;
    }

    /**
     * Returns <tt>true</tt> if this parameter has a specified maximal
     * value, or <tt>false</tt> otherwise.
     *
     * @return true if there is a maximum value.
     */
    @Override
    public boolean hasMaxValue() {
        return false;
    }
}
