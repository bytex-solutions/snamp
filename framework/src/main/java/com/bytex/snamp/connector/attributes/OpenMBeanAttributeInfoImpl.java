package com.bytex.snamp.connector.attributes;

import javax.management.openmbean.OpenMBeanAttributeInfo;
import javax.management.openmbean.OpenType;
import java.util.Set;

/**
 * @author Roman Sakno
 * @version 2.0
 * @since 1.0
 */
public class OpenMBeanAttributeInfoImpl extends CustomAttributeInfo implements OpenMBeanAttributeInfo {
    private static final long serialVersionUID = -7592242456297020895L;
    private final OpenType<?> openType;

    /**
     * Constructs an <CODE>MBeanAttributeInfo</CODE> object.
     *
     * @param name        The name of the attribute.
     * @param type        The type or class name of the attribute.
     * @param description A human readable description of the attribute.
     * @param specifier   Attribute access specifier. Cannot be {@literal null}.
     * @param descriptor  The descriptor for the attribute.  This may be null
     */
    public OpenMBeanAttributeInfoImpl(final String name,
                                      final OpenType<?> type,
                                      final String description,
                                      final AttributeSpecifier specifier,
                                      final AttributeDescriptor descriptor) {
        super(name, type.getClassName(), descriptor.getDescription(description), specifier, descriptor);
        this.openType = type;
    }

    /**
     * Returns the <i>open type</i> of the values of the parameter
     * described by this <tt>OpenMBeanParameterInfo</tt> instance.
     *
     * @return the open type.
     */
    @Override
    public final OpenType<?> getOpenType() {
        return openType;
    }

    /**
     * Returns the default value for this parameter, if it has one, or
     * <tt>null</tt> otherwise.
     *
     * @return the default value.
     */
    @Override
    public Object getDefaultValue() {
        return null;
    }

    /**
     * Returns the set of legal values for this parameter, if it has
     * one, or <tt>null</tt> otherwise.
     *
     * @return the set of legal values.
     */
    @Override
    public Set<?> getLegalValues() {
        return null;
    }

    /**
     * Returns the minimal value for this parameter, if it has one, or
     * <tt>null</tt> otherwise.
     *
     * @return the minimum value.
     */
    @Override
    public Comparable<?> getMinValue() {
        return null;
    }

    /**
     * Returns the maximal value for this parameter, if it has one, or
     * <tt>null</tt> otherwise.
     *
     * @return the maximum value.
     */
    @Override
    public Comparable<?> getMaxValue() {
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
    public final boolean isValue(final Object obj) {
        return openType.isValue(obj);
    }
}
