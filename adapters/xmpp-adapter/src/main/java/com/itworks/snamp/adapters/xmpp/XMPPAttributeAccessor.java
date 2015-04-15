package com.itworks.snamp.adapters.xmpp;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.itworks.snamp.StringAppender;
import com.itworks.snamp.adapters.AttributeAccessor;
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
    private static Gson FORMATTER = Formatters.enableAll(new GsonBuilder()).create();

    XMPPAttributeAccessor(final MBeanAttributeInfo metadata) {
        super(metadata);
    }

    String getValue(final AttributeValueFormat format) throws JMException{
        switch (format){
            case JSON: return getValueAsJson();
            default: return getValueAsText();
        }
    }

    protected abstract String getValueAsText() throws JMException;

    private String getValueAsJson() throws JMException{
        return FORMATTER.toJson(getValue());
    }

    String getOptions(){
        final Descriptor descr = getMetadata().getDescriptor();
        final StringAppender result = new StringAppender();
        for(final String fieldName: descr.getFieldNames())
            result.appendln("%s = %s", fieldName, descr.getFieldValue(fieldName));
        return result.toString();
    }
}
