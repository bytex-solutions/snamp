package com.bytex.snamp.adapters.xmpp;

import com.bytex.snamp.adapters.modeling.AttributeAccessor;
import com.bytex.snamp.connectors.attributes.AttributeDescriptor;
import com.bytex.snamp.io.IOUtils;
import com.bytex.snamp.jmx.WellKnownType;
import com.bytex.snamp.jmx.json.JsonUtils;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.jivesoftware.smack.packet.ExtensionElement;
import org.jivesoftware.smackx.jiveproperties.packet.JivePropertiesExtension;

import javax.management.Descriptor;
import javax.management.JMException;
import javax.management.MBeanAttributeInfo;
import java.util.Collection;

/**
 * @author Roman Sakno
 * @version 1.2
 * @since 1.0
 */
abstract class XMPPAttributeAccessor extends AttributeAccessor {
    static final String GET_COMMAND_PATTERN = "get -n %s -r %s";
    static final String SET_COMMAND_PATTERN = "set -n %s -r %s -v %s";
    protected static final Gson FORMATTER = JsonUtils.registerTypeAdapters(new GsonBuilder())
                        .serializeSpecialFloatingPointValues()
                        .serializeNulls()
            .create();

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

    final void printOptions(final StringBuilder output) {
        final Descriptor descr = getMetadata().getDescriptor();
        for (final String fieldName : descr.getFieldNames())
            IOUtils.appendln(output, "%s = %s", fieldName, descr.getFieldValue(fieldName));
    }

    final String getOriginalName() {
        return AttributeDescriptor.getName(getMetadata());
    }

    final void createExtensions(final Collection<ExtensionElement> output) {
        if (XMPPAdapterConfigurationProvider.isM2MEnabled(getMetadata().getDescriptor())) {
            final JivePropertiesExtension result = new JivePropertiesExtension();
            result.setProperty("writable", getMetadata().isWritable());
            result.setProperty("readable", getMetadata().isReadable());
            final WellKnownType attributeType = getType();
            if (attributeType != null)
                result.setProperty("type", attributeType.getDisplayName());
            XMPPUtils.copyDescriptorFields(getMetadata().getDescriptor(), result);
        }
    }

    final String getReadCommand(final String resourceName){
        return String.format(GET_COMMAND_PATTERN, getName(), resourceName);
    }

    final String getWriteCommand(final String resourceName){
        return String.format(SET_COMMAND_PATTERN, getName(), resourceName, "<json-value>");
    }
}
