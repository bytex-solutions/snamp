package com.bytex.snamp.connector.composite;

import com.bytex.snamp.SpecialUse;
import com.bytex.snamp.connector.attributes.AttributeManager;
import com.bytex.snamp.scripting.groovy.Scriptlet;
import com.bytex.snamp.scripting.groovy.TypeDeclarationDSL;

import javax.management.Attribute;
import javax.management.AttributeNotFoundException;
import javax.management.JMException;
import javax.management.openmbean.OpenType;
import javax.management.openmbean.SimpleType;
import java.lang.reflect.Method;
import java.util.Objects;

/**
 * @author Roman Sakno
 * @version 2.1
 * @since 2.0
 */
public abstract class AggregationAttributeScriptlet extends Scriptlet implements TypeDeclarationDSL {
    private static final String GET_VALUE_METHOD = "getValue";
    private static final String SET_VALUE_METHOD = "setValue";
    private volatile AttributeManager attributes;
    private OpenType<?> openType = SimpleType.STRING;

    final Object getValue(final AttributeManager attributes) throws Exception{
        this.attributes = attributes;
        return getValue();
    }

    final Object setValue(final AttributeManager attributes, final Object value) throws Exception{
        this.attributes = attributes;
        return setValue(value);
    }

    /**
     * Sets type of this attribute.
     *
     * @param value The type of this attribute
     */
    @SpecialUse(SpecialUse.Case.SCRIPTING)
    public final void type(final OpenType<?> value) {
        this.openType = Objects.requireNonNull(value);
    }

    /**
     * Gets value of this attribute.
     *
     * @return The value of this attribute.
     * @throws Exception Unable to get attribute value.
     */
    @SpecialUse(SpecialUse.Case.SCRIPTING)
    public Object getValue() throws Exception {
        throw new UnsupportedOperationException();
    }

    /**
     * Determines whether this attribute is readable.
     *
     * @return {@literal true}, if this method is readable.
     */
    @SpecialUse(SpecialUse.Case.SCRIPTING)
    public final boolean isReadable() {
        try {
            final Method getter = getClass().getMethod(GET_VALUE_METHOD);
            return Objects.equals(getter.getDeclaringClass(), getClass());
        } catch (final NoSuchMethodException e) {
            return false;
        }
    }

    /**
     * Determines whether this attribute is writable.
     *
     * @return {@literal true}, if this method is writable.
     */
    @SpecialUse(SpecialUse.Case.SCRIPTING)
    public final boolean isWritable() {
        try {
            final Method getter = getClass().getMethod(SET_VALUE_METHOD, Object.class);
            return Objects.equals(getter.getDeclaringClass(), getClass());
        } catch (final ReflectiveOperationException e) {
            return false;
        }
    }

    /**
     * Gets type of this attribute.
     *
     * @return The type of this attribute.
     */
    public final OpenType<?> type() {
        return openType;
    }

    /**
     * Sets value of this attribute.
     *
     * @param value The value of this attribute.
     * @return A new attribute value.
     * @throws Exception Unable to set attribute value.
     */
    @SpecialUse(SpecialUse.Case.SCRIPTING)
    public Object setValue(final Object value) throws Exception {
        throw new UnsupportedOperationException();
    }

    @Override
    public final void setProperty(final String property, final Object newValue) {
        final AttributeManager support = attributes;
        if (support != null)
            try {
                support.setAttribute(new Attribute(property, newValue));
            } catch (final AttributeNotFoundException e) {
                super.setProperty(property, newValue);
            } catch (final JMException e) {
                throw new IllegalStateException(e);
            }
        else
            super.setProperty(property, newValue);
    }

    @Override
    public final Object getProperty(final String property) {
        final AttributeManager support = attributes;
        if (support != null)
            try{
                return support.getAttribute(property);
            } catch (final AttributeNotFoundException e){
                return super.getProperty(property);
            } catch (final JMException e){
                throw new IllegalStateException(e);
            }
        else
            return super.getProperty(property);
    }
}
