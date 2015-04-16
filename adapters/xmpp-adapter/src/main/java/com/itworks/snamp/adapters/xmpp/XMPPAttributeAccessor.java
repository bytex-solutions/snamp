package com.itworks.snamp.adapters.xmpp;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.itworks.snamp.StringAppender;
import com.itworks.snamp.adapters.AttributeAccessor;
import com.itworks.snamp.connectors.attributes.AttributeDescriptor;
import com.itworks.snamp.jmx.json.Formatters;

import javax.management.Descriptor;
import javax.management.JMException;
import javax.management.MBeanAttributeInfo;

/**
 * @author Roman Sakno
 * @version 1.0
 * @since 1.0
 */
abstract class XMPPAttributeAccessor extends AttributeAccessor {
    protected static Gson FORMATTER = Formatters.enableAll(new GsonBuilder()).create();

    XMPPAttributeAccessor(final MBeanAttributeInfo metadata) {
        super(metadata);
    }

    final String getValue(final AttributeValueFormat format) throws JMException{
        switch (format){
            case JSON: return getValueAsJson();
            default: return getValueAsText();
        }
    }

    final void setValue(final String input) throws JMException {
        if (getType() != null && canWrite())
            setValue(FORMATTER.fromJson(input, getType().getJavaType()));
        else throw new UnsupportedOperationException(String.format("Attribute %s is read-only", getName()));
    }

    protected abstract String getValueAsText() throws JMException;

    private String getValueAsJson() throws JMException{
        return FORMATTER.toJson(getValue());
    }

    final void printOptions(final StringAppender output) {
        final Descriptor descr = getMetadata().getDescriptor();
        for (final String fieldName : descr.getFieldNames())
            output.appendln("%s = %s", fieldName, descr.getFieldValue(fieldName));
    }

    final String getOriginalName() {
        return AttributeDescriptor.getAttributeName(getMetadata());
    }
}
