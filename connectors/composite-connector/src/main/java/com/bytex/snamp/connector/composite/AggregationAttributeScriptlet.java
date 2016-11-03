package com.bytex.snamp.connector.composite;

import com.bytex.snamp.connector.attributes.AttributeSupport;
import com.bytex.snamp.scripting.groovy.AbstractAttributeScriptlet;
import com.bytex.snamp.scripting.groovy.AttributeScriptlet;

import javax.management.Attribute;
import javax.management.AttributeNotFoundException;
import javax.management.JMException;

/**
 * @author Roman Sakno
 * @version 2.0
 * @since 2.0
 */
public abstract class AggregationAttributeScriptlet extends AbstractAttributeScriptlet implements AttributeScriptlet {
    private volatile AttributeSupport attributes;

    final Object getValue(final AttributeSupport attributes) throws Exception{
        this.attributes = attributes;
        return getValue();
    }

    final Object setValue(final AttributeSupport attributes, final Object value) throws Exception{
        this.attributes = attributes;
        return setValue(value);
    }

    @Override
    public final void setProperty(final String property, final Object newValue) {
        final AttributeSupport support = attributes;
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
        final AttributeSupport support = attributes;
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
