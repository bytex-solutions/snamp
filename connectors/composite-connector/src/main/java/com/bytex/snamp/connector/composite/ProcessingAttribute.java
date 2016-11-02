package com.bytex.snamp.connector.composite;

import com.bytex.snamp.connector.attributes.AttributeSupport;
import com.bytex.snamp.jmx.DescriptorUtils;
import com.google.common.collect.ImmutableSet;

import javax.management.Descriptor;
import javax.management.openmbean.OpenMBeanAttributeInfo;
import java.util.Set;

/**
 * @author Roman Sakno
 * @version 2.0
 * @since 2.0
 */
abstract class ProcessingAttribute extends AbstractCompositeAttribute implements OpenMBeanAttributeInfo {
    private static final long serialVersionUID = -2637191597713326303L;

    ProcessingAttribute(final String name, final String type, final String description, final boolean isReadable, final boolean isWritable, final boolean isIs, final Descriptor descriptor) {
        super(name, type, description, isReadable, isWritable, isIs, descriptor);
    }

    abstract Object getValue(final AttributeSupport support) throws Exception;

    /**
     * Returns the default value for this parameter, if it has one, or
     * <tt>null</tt> otherwise.
     *
     * @return the default value.
     */
    @Override
    public Object getDefaultValue() {
        return DescriptorUtils.getDefaultValue(getDescriptor(), Object.class);
    }

    /**
     * Returns the set of legal values for this parameter, if it has
     * one, or <tt>null</tt> otherwise.
     *
     * @return the set of legal values.
     */
    @Override
    public Set<?> getLegalValues() {
        return ImmutableSet.of();
    }

    /**
     * Returns the minimal value for this parameter, if it has one, or
     * <tt>null</tt> otherwise.
     *
     * @return the minimum value.
     */
    @Override
    public Comparable<?> getMinValue() {
        return (Comparable<?>) DescriptorUtils.getRawMinValue(getDescriptor());
    }

    /**
     * Returns the maximal value for this parameter, if it has one, or
     * <tt>null</tt> otherwise.
     *
     * @return the maximum value.
     */
    @Override
    public Comparable<?> getMaxValue() {
        return (Comparable<?>) DescriptorUtils.getRawMaxValue(getDescriptor());
    }

    /**
     * Returns <tt>true</tt> if this parameter has a specified default
     * value, or <tt>false</tt> otherwise.
     *
     * @return true if there is a default value.
     */
    @Override
    public boolean hasDefaultValue() {
        return DescriptorUtils.hasField(getDescriptor(), DescriptorUtils.DEFAULT_VALUE_FIELD);
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
        return DescriptorUtils.hasField(getDescriptor(), DescriptorUtils.MIN_VALUE_FIELD);
    }

    /**
     * Returns <tt>true</tt> if this parameter has a specified maximal
     * value, or <tt>false</tt> otherwise.
     *
     * @return true if there is a maximum value.
     */
    @Override
    public boolean hasMaxValue() {
        return DescriptorUtils.hasField(getDescriptor(), DescriptorUtils.MAX_VALUE_FIELD);
    }

    /**
     * Tests whether <var>obj</var> is a valid value for the parameter
     * described by this <code>OpenMBeanParameterInfo</code> instance.
     *
     * @param obj the object to be tested.
     * @return <code>true</code> if <var>obj</var> is a valid value
     * for the parameter described by this
     * <code>OpenMBeanParameterInfo</code> instance,
     * <code>false</code> otherwise.
     */
    @Override
    public boolean isValue(final Object obj) {
        return getOpenType().isValue(obj);
    }
}
