package com.bytex.snamp.connector.composite;

import com.bytex.snamp.connector.attributes.AttributeSupport;
import com.bytex.snamp.scripting.groovy.AttributeScript;
import com.bytex.snamp.scripting.groovy.ScriptingAPISupport;

import javax.management.Attribute;
import javax.management.AttributeNotFoundException;
import javax.management.JMException;
import javax.management.openmbean.*;
import java.util.Collection;
import java.util.Map;

/**
 * @author Roman Sakno
 * @version 2.0
 * @since 2.0
 */
public abstract class AttributeScriptImpl extends ScriptingAPISupport implements AttributeScript {
    private volatile AttributeSupport attributes;
    private OpenType<?> openType = SimpleType.STRING;

    public abstract Object getValue() throws Exception;

    final Object getValue(final AttributeSupport attributes) throws Exception{
        this.attributes = attributes;
        return getValue();
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

    /**
     * Declares attribute type.
     *
     * @param value Attribute type.
     */
    @Override
    public final void type(final OpenType<?> value) {
        openType = value;
    }

    /**
     * Gets attribute type.
     *
     * @return Type of attribute.
     */
    @Override
    public final OpenType<?> type() {
        return openType;
    }

    /**
     * Converts map into composite data.
     *
     * @param items Items of the composite data.
     * @return Composite data.
     * @throws OpenDataException Unable to create composite data.
     */
    @Override
    public final CompositeData asDictionary(final Map<String, ?> items) throws OpenDataException {
        if(openType instanceof CompositeType)
            return AttributeScript.asDictionary((CompositeType) openType, items);
        else
            throw new OpenDataException();
    }

    @Override
    public final TabularData asTable(final Collection<Map<String, ?>> rows) throws OpenDataException {
        if(openType instanceof TabularType)
            return AttributeScript.asTable((TabularType) openType, rows);
        else
            throw new OpenDataException();
    }
}
